import base64

from DBManager import DB
from FaceManager import FaceManager
from Server import Server
from hashlib import sha256
import uuid


class Instances:
    FACE_MANAGER = FaceManager()
    DATA_BASE = DB()


def hash_psw():
    def calc(items):
        for item in items:
            psw = item["password"]
            salt = uuid.uuid4().bytes
            sha256().update(psw.encode("utf-8") + salt)
            item["password"] = base64.b64encode(sha256().digest() + salt)
            print(salt)
            print(base64.b64decode(item["password"]))

    admins = instances.DATA_BASE.get_all_admins()
    viewers = instances.DATA_BASE.get_all_viewers()

    calc(admins)
    calc(viewers)

    for admin in admins:
        instances.DATA_BASE.set_admin_psw(admin["password"].decode("utf-8"), admin["login"])

    for viewer in viewers:
        instances.DATA_BASE.set_viewer_psw(viewer["password"].decode("utf-8"), viewer["login"])


if __name__ == "__main__":
    instances = Instances()
    # hash_psw()
    server = Server("192.168.0.102", 8000, instances)
