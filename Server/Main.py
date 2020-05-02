from DBManager import DB
from FaceManager import FaceManager
from Server import Server


class Instances:
    FACE_MANAGER = FaceManager()
    DATA_BASE = DB()


if __name__ == "__main__":
    instances = Instances()
    server = Server("192.168.0.102", 8000, instances)
