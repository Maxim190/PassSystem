import base64
import json
import socket
import sys
import ClientManager

from threading import Thread

from hashlib import sha256

import Cryptography

from MsgHandler import RequestType
from MsgHandler import DataType
from MsgHandler import error_msg


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
            Thread(target=self.authorization, args=(conn, instances, Cryptography.AESCipher())).start()

    def execute(self, client, instances, coder):
        self.dh_key_exchange(client, coder)
        self.authorization(client, instances, coder)

    def dh_key_exchange(self, client, coder):
        while True:
            try:
                print("waiting dh params..")
                raw_data = ClientManager.get_msg(client)
                print("get msg " + str(raw_data))

                if "dh_params" in raw_data:
                    ClientManager.send_msg(client, *ClientManager.build_response(coder.get_dh_params()))
                    client_key_msg = ClientManager.get_msg(client)
                    if "public_key" not in client_key_msg:
                        raise Exception("Waiting for client public key for encryption")
                    coder.calc_shared_key(client_key_msg["public_key"])
                    return
                elif coder.shared_key is None:
                    raise Exception("Missing client public key for encryption")

            except Exception as e:
                print("Server exception " + str(e))
                ClientManager.send_msg(client, *ClientManager.build_response(
                    error_msg("dh_params", str(e))))

    def authorization(self, client, instances, coder):
        while True:
            try:
                print("waiting dh params..")
                raw_data = ClientManager.get_msg(client)
                print("get msg " + str(raw_data))

                if "dh_params" in raw_data:
                    ClientManager.send_msg(client, *ClientManager.build_response(coder.get_dh_params()))
                    client_key_msg = ClientManager.get_msg(client)
                    if "public_key" not in client_key_msg:
                        raise Exception("Waiting for client public key for encryption")
                    coder.calc_shared_key(client_key_msg["public_key"])
                    break
                elif coder.shared_key is None:
                    raise Exception("Missing client public key for encryption")

            except Exception as e:
                print("Server exception " + str(e))
                ClientManager.send_msg(client, *ClientManager.build_response(
                    error_msg("dh_params", str(e))))
        while True:
            print("client's authorization...")
            try:
                raw_data = ClientManager.get_msg(client, coder)
                print("get msg " + str(raw_data))

                if raw_data is None or RequestType.AUTHORIZE not in raw_data:
                    ClientManager.send_msg(client, *ClientManager.build_response(
                        error_msg(RequestType.AUTHORIZE, "You are not authorized")), coder)
                    continue

                array = json.loads(raw_data[RequestType.AUTHORIZE])
                access_rights = None
                admin = instances.DATA_BASE.get_admin(array[DataType.LOGIN])

                if admin is not None and self.psw_equals(admin[DataType.PASSWORD], array[DataType.PASSWORD]):
                    access_rights = DataType.ACCESS_ADMIN
                else:
                    viewer = instances.DATA_BASE.get_viewer(array[DataType.LOGIN])
                    if viewer is not None and self.psw_equals(viewer[DataType.PASSWORD], array[DataType.PASSWORD]):
                        access_rights = DataType.ACCESS_VIEWER

                if access_rights is None:
                    ClientManager.send_msg(client, *ClientManager.build_response(
                        error_msg(RequestType.AUTHORIZE, "Wrong login or password"))), coder
                else:
                    ClientManager.send_msg(client, *ClientManager.build_response(
                        {
                            RequestType.AUTHORIZE: admin if admin is not None else viewer,
                            DataType.ACCESS: access_rights,
                            DataType.CODE: DataType.CODE_SUCCESS
                        },
                        coder)
                                           )

                    ClientManager.run(client, instances, access_rights, coder)

            except ConnectionAbortedError as e:
                client.close()
                return
            except Exception as e:
                print("Server exception " + str(e))
                ClientManager.send_msg(client, *ClientManager.build_response(
                    error_msg(RequestType.AUTHORIZE, str(e)), coder))

    def psw_equals(self, from_db, verifiable):
        dec_db_psw = base64.b64decode(from_db)
        salt = dec_db_psw[-16:]
        sha256().update(verifiable.encode("utf-8") + salt)
        h_verifiable = sha256().digest() + salt
        return dec_db_psw == h_verifiable
