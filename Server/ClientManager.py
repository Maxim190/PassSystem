import json
from json import JSONDecodeError

from MsgHandler import DataType
from MsgHandler import MsgHandler
from MsgHandler import error_msg


def run(socket, instances, access_rights):
    print("ClientManager starting..")
    msg_handler = MsgHandler(instances)
    while True:
        try:
            print("Waiting msg")
            received_msg = get_msg(socket)
            print("Received request '" + str(list(received_msg.keys())[0]) + "' from " + str(socket))

            msg_for_client = msg_handler.handle(received_msg, access_rights)
            # print("Msg for client " + str(msg_for_client))

            header, body = build_response(msg_for_client)
            send_msg(socket, header, body)
        except ConnectionAbortedError:
            print("Connection lost with " + str(socket))
            raise ConnectionAbortedError
        # except Exception as e:
        #     print("An error has occurred " + str(e))
        #     msg = build_response(
        #         error_msg("Error", str(e)))
        #     send_msg(socket, *msg)
        #     pass


def send_msg(socket, header, body):
    print("Sending msg: " + str(header) + "\n" + str(body))
    socket.sendall(header)
    socket.sendall(body)


def build_response(msg):
    header = {}
    body = bytes()
    for data_type in msg:
        if data_type != DataType.PHOTO:
            body_part = json.dumps(msg[data_type], ensure_ascii=False).encode('utf-8')
        else:
            body_part = msg[data_type]
        header[data_type] = str(len(body_part))
        body += body_part

    header_bytes = json.dumps(header, ensure_ascii=False).encode('utf-8')
    # constant header size - 64
    header_bytes += ("x" * (64 - len(header_bytes))).encode('utf-8')

    return header_bytes, body


def get_msg(socket):
    raw_data = socket.recv(64)
    if raw_data is None or len(raw_data) == 0:
        raise ConnectionAbortedError("Get empty data packet")
    try:
        print(raw_data)
        dec_raw_data = raw_data.decode('utf-8')
        header = json.loads(dec_raw_data[0: dec_raw_data.index('}') + 1])
    except JSONDecodeError as e:
        raise Exception('Invalid json: {}'.format(e)) from None

    total_size = 0
    for key in header:
        total_size += int(header[key])

    sock_file = socket.makefile('rb')
    data = sock_file.read(total_size)

    result = {}
    iterator = 0
    for key in header:
        next_block_size = int(header[key])
        result[key] = data[iterator: iterator + next_block_size]
        iterator += next_block_size

    return result
