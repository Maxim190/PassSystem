import cv2
import dlib
import numpy as np
from scipy.spatial import distance
import os


class FaceManager:

    def __init__(self):
        print("FaceManager starting...")
        self.sp = dlib.shape_predictor("files/shape_predictor_68_face_landmarks.dat")
        self.face_rec = dlib.face_recognition_model_v1("files/dlib_face_recognition_resnet_model_v1.dat")
        self.detector = dlib.get_frontal_face_detector()
        self.desc_parser = ":"
        self.photo_storage_path = "photos/"
        print("FaceManager is ready")

    # def get_descriptor(self, image_path):
    #     img = io.imread(image_path)
    #     dets = self.detector(img, 1)
    #     for k, d in enumerate(dets):
    #         print("Detection {}: left: {} top: {} right: {} bottom: {} "
    #               .format(k, d.left(), d.top(), d.right(), d.bottom()))
    #         shape = self.sp(img, d)
    #
    #     face_descriptor = self.facerec.compute_face_descriptor(img, shape)
    #     return face_descriptor

    def get_descriptor(self, bytes):
        # with open("hehe.jpg", "w+b") as im:
        #     im.write(bytes)
        #     im.flush()
        nparr = np.fromstring(bytes, np.uint8)
        # decode image
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        dets = self.detector(img, 1)

        if len(dets) == 0:
            return None

        for k, d in enumerate(dets):
            shape = self.sp(img, d)

        face_descriptor = self.face_rec.compute_face_descriptor(img, shape)
        return face_descriptor

    def descriptor_to_string(self, descriptor):
        result = str(descriptor[0])
        for i in range(1, len(descriptor)):
            result += self.desc_parser + str(descriptor[i])
        return result

    def descriptor_from_string(self, str_descriptor):
        return str_descriptor.split(self.desc_parser)

    def is_one_person(self, descriptor1, descriptor2):
        return distance.euclidean(descriptor1, descriptor2) < 0.6

    def write_img(self, img, path):
        with open(path, "w+b") as file:
            file.write(img)
            file.flush()

    def read_img(self, file_path):
        with open(file_path, "rb") as file:
            return file.read()

    def build_photo_path(self, photo_name):
        return self.photo_storage_path + str(photo_name) + ".jpg"

    def delete_photo(self, path):
        if os.path.exists(path):
            os.remove(path)
