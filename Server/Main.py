import json
from ast import literal_eval
from io import StringIO

from FaceManager import FaceManager
from DBManager import DB
from Server import Server


class Instances:
    FACE_MANAGER = FaceManager()
    DATA_BASE = DB()

# def test():
#     recognize = '{"RECOGNIZE": 23427787}'
#     add = '{"ADD": 234234, "PHOTO": 4324}'
#     edit = '{"EDIT": 23423, "PHOTO": 6545}'
#     delete = '{"DELETE": 23}'
#
#     with open('C:\\Users\\Maxim\\Desktop\\image.png', 'rb') as i:
#         image = i.read()
#         b = bytes(image)
#
#     b = "Hi".encode() + b
#     print(str(b[0: 2]))
#     b = b[2: len(b)]
#
#     with open('C:\\Users\\Maxim\\Desktop\\image1.png', 'wb') as i:
#         i.write(b)
#         i.flush()
#
#     print(str((recognize.encode('utf-8')[0:5])))
#     obj = json.loads(add)
#     for key in obj:
#         print(key + " " + str(obj[key]) + "\n")


if __name__ == "__main__":
    instances = Instances()
    server = Server("192.168.0.102", 8000, instances)
    # "RECOGNIZE{PHOTO:photo_bytes}"
    # "ADD{NAME:name, LAST_NAME:last_name, BIRTH:birth, PHOTO:photo_bytes}"
    # "EDIT{ID:id,NAME:new_name, LAST_NAME:new_last_name, BIRTH:new_birth, PHOTO:new_photo_bytes}"
