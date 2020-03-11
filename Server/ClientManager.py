from threading import *
from MsgHandler import MsgHandler


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
            print("Waiting msg")
            received_msg = self.getMsg()
            print("Received msg: " + str(received_msg))
            try:
                msg_for_client = self.msg_handler.handle(received_msg)
                print("Send msg: " + msg_for_client)
                self.sendMsg(msg_for_client.encode())
            except Exception as e:
                self.sendMsg(str(e).encode())


    def sendMsg(self, msg):
        try:
            self.sock.sendall(msg)
        except ConnectionAbortedError as e:
            print("Failed to send msg to " + str(self.sock))
            self.client_alive = False

    def getMsg(self):
        result = []
        is_final_part = False
        while not is_final_part:
            raw_data = self.sock.recv(32)
            if not raw_data:
                break
            msg_header = raw_data.decode("utf-8")
            msg_header_array = msg_header.split(':')

            if msg_header_array[0].startswith("FINAL"):
                msg_header_array[0] = msg_header_array[0].replace("FINAL", "")
                is_final_part = True

            sock_file = self.sock.makefile('rb')
            data = sock_file.read(int(msg_header_array[1]))

            result.append([msg_header_array[0], data])

        return result
