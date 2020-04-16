import re
import json
from enum import Enum


class RequestType:
    AUTHORIZE = "AUTHORIZE"
    RECOGNIZE = "RECOGNIZE"
    ADD = "ADD"
    EDIT = "EDIT"
    DELETE = "DELETE"
    CHECK = "CHECK"


class DataType:
    LOGIN = "login"
    PASSWORD = "password"
    ACCESS_ADMIN = "admin"
    ACCESS_VIEWER = "viewer"
    PHOTO = "PHOTO"
    NAME = "name"
    LAST_NAME = "lastName"
    BIRTH = "birth"
    DEPARTMENT_ID = "departmentId"
    ID = "id"
    CODE = "code"


class Code:
    SUCCESS = "1"
    ERROR = "2"


def error_msg(msg_type, msg):
    return {msg_type: msg, DataType.CODE: Code.ERROR}


def success_msg(msg_type, msg):
    return {msg_type: msg, DataType.CODE: Code.SUCCESS}


class MsgHandler:

    def __init__(self, instances):
        self.instances = instances

    def handle(self, request):
        if request is None or len(request) == 0:
            return "empty msg received"
        request_header = list(request.keys())[0]
        print("REQUEST: " + str(request))

        if request_header == RequestType.CHECK:
            return self.check_request()
        elif request_header == RequestType.RECOGNIZE:
            return self.recognize_face(request)
        elif request_header == RequestType.ADD:
            return self.add_employee(request)
        elif request_header == RequestType.EDIT:
            return self.edit_employee(request)
        elif request_header == RequestType.DELETE:
            return self.delete_employee(request)

        return error_msg("UNKNOWN", "unknown request type")

    def check_request(self):
        return {RequestType.CHECK: "RESPONSE", DataType.CODE: Code.SUCCESS}

    def recognize_face(self, data):
        photo_bytes = data[RequestType.RECOGNIZE]
        if photo_bytes is None or len(photo_bytes) == 0:
            return error_msg(RequestType.RECOGNIZE, "Photo did not come completely")

        photo_descriptor = self.instances \
            .FACE_MANAGER \
            .get_descriptor(photo_bytes)

        if photo_descriptor is None:
            return error_msg(RequestType.RECOGNIZE, "Face not found")

        all_images_data = self.instances \
            .DATA_BASE \
            .get_all_img_data()

        for item in all_images_data:
            item_descriptor = [float(i) for i in item["descriptor"].split(':')]

            if self.instances.FACE_MANAGER.is_one_person(item_descriptor, photo_descriptor):
                return self.build_response_with_employee_data(item["id"])

        return error_msg(RequestType.RECOGNIZE, "There is no face like that in base")

    def build_response_with_employee_data(self, employee_id):
        return {
            RequestType.RECOGNIZE:
                self.instances.DATA_BASE.get_employee_by_id(employee_id),
            DataType.PHOTO:
                self.instances.FACE_MANAGER.read_img(
                    self.instances.DATA_BASE.get_image_data(employee_id)["photo"]),
            DataType.CODE: Code.SUCCESS
        }

    def find_employee(self, descriptor_on_check):
        all_images_data = self.instances \
            .DATA_BASE \
            .get_all_img_data()

        for item in all_images_data:
            base_descriptor = [float(i) for i in item["descriptor"].split(':')]
            if self.instances.FACE_MANAGER.is_one_person(base_descriptor, descriptor_on_check):
                return item[DataType.ID]

    def add_employee(self, request):
        try:
            descriptor = self.instances \
                .FACE_MANAGER \
                .get_descriptor(request[DataType.PHOTO])

            if descriptor is None:
                return error_msg(RequestType.ADD, "Face is not detected on the photo")

            existing_employee_id = self.find_employee(descriptor)
            if existing_employee_id is not None:
                print("Impossible to add employee: already exists")
                return self.build_response_with_employee_data(existing_employee_id)

            data = json.loads(request[RequestType.ADD])
            employee_id = self.instances \
                .DATA_BASE \
                .add_employee(
                data[DataType.NAME],
                data[DataType.LAST_NAME],
                data[DataType.BIRTH],
                data[DataType.DEPARTMENT_ID])

            photo_file_path = self.instances.FACE_MANAGER \
                .build_photo_path(employee_id)

            self.instances \
                .DATA_BASE \
                .add_image_data(
                employee_id,
                photo_file_path,
                self.instances.FACE_MANAGER
                    .descriptor_to_string(descriptor))

            self.instances.FACE_MANAGER \
                .write_img(request[DataType.PHOTO], photo_file_path)

            return {
                RequestType.ADD: employee_id,
                DataType.CODE: Code.SUCCESS
            }
        except IndexError:
            return error_msg(RequestType.ADD, "Not enough data for adding new employee")

    def edit_employee(self, request):
        try:
            data = json.loads(request[RequestType.EDIT])
            self.instances.DATA_BASE.edit_employee(
                data[DataType.ID],
                data[DataType.NAME],
                data[DataType.LAST_NAME],
                data[DataType.BIRTH],
                data[DataType.DEPARTMENT_ID])

            descriptor = self.instances \
                .FACE_MANAGER \
                .get_descriptor(request[DataType.PHOTO])

            response_msg = "Edited employee successfully."

            if descriptor is not None:
                photo_file_path = self.instances.FACE_MANAGER \
                    .build_photo_path(data[DataType.ID])

                self.instances \
                    .DATA_BASE \
                    .edit_img_data(
                    data[DataType.ID],
                    photo_file_path,
                    self.instances.FACE_MANAGER.descriptor_to_string(descriptor))

                self.instances.FACE_MANAGER \
                    .write_img(request[DataType.PHOTO], photo_file_path)
            else:
                response_msg += "Failed to edit photo: no face detected"

            return {RequestType.EDIT: response_msg,
                    DataType.CODE: Code.SUCCESS}
        except IndexError:
            return error_msg(RequestType.EDIT, "Not enough data for editing")

    def delete_employee(self, request):
        id = json.loads(request[RequestType.DELETE])

        photo_path = self.instances.DATA_BASE.get_image_data(id)["photo"]

        self.instances.DATA_BASE.del_image_data(id)
        self.instances.DATA_BASE.del_employee(id)
        self.instances.FACE_MANAGER.delete_photo(photo_path)

        return {RequestType.DELETE: "Employee deleted successfully",
                DataType.CODE: Code.SUCCESS}
