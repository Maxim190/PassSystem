import json
from io import StringIO

from FaceManager import FaceManager
from DBManager import DB
from Server import Server


class Instances:
    FACE_MANAGER = FaceManager()
    DATA_BASE = DB()


if __name__ == "__main__":
    instances = Instances()
    server = Server("192.168.0.102", 8000, instances)

    # "RECOGNIZE{PHOTO:photo_bytes}"
    # "ADD{NAME:name, LAST_NAME:last_name, BIRTH:birth, PHOTO:photo_bytes}"
    # "EDIT{ID:id,NAME:new_name, LAST_NAME:new_last_name, BIRTH:new_birth, PHOTO:new_photo_bytes}"
