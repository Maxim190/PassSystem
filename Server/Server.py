import socket
import sys
from ClientManager import ClientManager


class Server:

    def __init__(self, host="localhost", port=8000, instances=None):
        self.HOST = host
        self.PORT = port
        self.clients = []

        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

        try:
            s.bind((self.HOST, self.PORT))
            print("Socket created host" + str(s.getsockname()))
        except socket.error as error:
            print("Bind failed, error code: " + str(error[0]) + " Msg: " + error[1])
            sys.exit()

        s.listen()

        while True:
            print("Waiting for new client")
            conn, address = s.accept()
            print("Connected new client: " + str(conn) + "\n" + str(address))
            self.clients.append(ClientManager(conn, address, instances))


