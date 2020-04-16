import json
import socket
import sys
import ClientManager
from threading import Thread
from MsgHandler import RequestType
from MsgHandler import DataType
from MsgHandler import error_msg
from MsgHandler import success_msg


class Server:

    def __init__(self, host="localhost", port=8000, instances=None):
        self.HOST = host
        self.PORT = port
        self.authorized_clients = []

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
            Thread(target=self.authorization, args=(conn, instances)).start()
            # self.authorized_clients.append(ClientManager(conn, address, instances))

    def authorization(self, client, instances):
        while True:
            print("client's authorization...")
            raw_data = ClientManager.get_msg(client)
            array = json.loads(raw_data[RequestType.AUTHORIZE])

            access_rights = None
            if array is None or RequestType.AUTHORIZE not in raw_data:
                ClientManager.send_msg(client, *ClientManager.build_response(
                    error_msg(RequestType.AUTHORIZE, "You are not authorized")))
                return

            admin = instances.DATA_BASE.get_admin_psw(array[DataType.LOGIN])

            if admin is not None and array[DataType.PASSWORD] == admin[DataType.PASSWORD]:
                access_rights = DataType.ACCESS_ADMIN
            else:
                viewer = instances.DATA_BASE.get_viewer_psw(array[DataType.LOGIN])
                if viewer is not None and viewer[DataType.PASSWORD] == array[DataType.PASSWORD]:
                    access_rights = DataType.ACCESS_VIEWER

            if access_rights is None:
                ClientManager.send_msg(client, *ClientManager.build_response(
                    error_msg(RequestType.AUTHORIZE, "Wrong login or password")))
            else:
                ClientManager.send_msg(client, *ClientManager.build_response(
                    success_msg(RequestType.AUTHORIZE, access_rights)))

                ClientManager.run(client, instances)
