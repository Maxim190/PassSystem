import json
from json import JSONDecodeError
from threading import *
from MsgHandler import MsgHandler
from MsgHandler import DataType
from MsgHandler import error_msg
import time
from threading import Thread


class ClientManager(Thread):
    def __init__(self, sock, addr, db_conn):
        print("ClientManager starting..")
        Thread.__init__(self)
        self.msg_handler = MsgHandler(db_conn)
        self.sock = sock
        self.addr = addr
        self.client_alive = True
        self.start()

    def run(self):
        while True:
            if not self.client_alive:
                print("Connection with " + str(self.addr) + " has lost")
                self.sock.close()
                break
            try:
                print("Waiting msg")
                received_msg = self.getMsg()
                print("Received request '" + str(list(received_msg.keys())[0]) + "' from " + str(self.addr))

                msg_for_client = self.msg_handler.handle(received_msg)
                # print("Msg for client " + str(msg_for_client))

                header, body = self.build_response(msg_for_client)
                self.sendMsg(header, body)
            except Exception as e:
                msg = self.build_response(
                    error_msg("Error", str(e)))
                self.sendMsg(*msg)
                pass

    def sendMsg(self, header, body):
        try:
            print("SEND HEADER " + str(header))
            print("SEND BODY " + str(body))
            self.sock.sendall(header)
            time.sleep(0.1)
            self.sock.sendall(body)
        except ConnectionAbortedError as e:
            print("Failed to send msg to " + str(self.sock))
            self.client_alive = False

    def build_response(self, msg):
        print("Build Response from: " + str(msg))
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

        return header_bytes, body

    def getMsg(self):
        raw_data = self.sock.recv(32)
        if raw_data is None or len(raw_data) == 0:
            raise Exception("Get empty data packet")
        try:
            print(raw_data)
            header = json.loads(raw_data)
        except JSONDecodeError as e:
            print("Get msg error " + e.msg)
            raise Exception('Invalid json: {}'.format(e)) from None

        total_size = 0
        for key in header:
            total_size += int(header[key])

        sock_file = self.sock.makefile('rb')
        data = sock_file.read(total_size)

        result = {}
        iterator = 0
        for key in header:
            next_block_size = int(header[key])
            result[key] = data[iterator: iterator + next_block_size]
            iterator += next_block_size

        return result
