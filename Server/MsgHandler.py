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
    GET = "GET"


class DataType:
    LOGIN = "login"
    PASSWORD = "password"
    ACCESS = "rights"
    ACCESS_ADMIN = "admin"
    ACCESS_VIEWER = "viewer"
    PHOTO = "PHOTO"
    NAME = "name"
    LAST_NAME = "last_name"
    BIRTH = "birth"
    DEPARTMENT_ID = "department_id"
    DEPARTMENT = "department_name"
    POSITION = "position_name"
    DATA = "data"
    ID = "id"
    CODE = "code"
    CODE_SUCCESS = "1"
    CODE_ERROR = "2"


def error_msg(msg_type, msg):
    return {msg_type: msg, DataType.CODE: DataType.CODE_ERROR}


def success_msg(msg_type, msg):
    return {msg_type: msg, DataType.CODE: DataType.CODE_SUCCESS}


class MsgHandler:

    def __init__(self, instances):
        self.instances = instances

    def handle(self, request, access_rights):
        if request is None or len(request) == 0:
            return "empty msg received"
        request_header = list(request.keys())[0]
        print("REQUEST: " + str(request))

        if request_header == RequestType.CHECK:
            return self.check_request()
        elif request_header == RequestType.RECOGNIZE:
            return self.recognize_face(request)

        if DataType.ACCESS_ADMIN == access_rights:
            if request_header == RequestType.GET:
                return self.get_request(request)
            if request_header == RequestType.ADD:
                return self.add_employee(request)
            elif request_header == RequestType.EDIT:
                return self.edit_employee(request)
            elif request_header == RequestType.DELETE:
                return self.delete_employee(request)
        else:
            return error_msg(request_header, "You do not have permission")

        return error_msg("UNKNOWN", "unknown request type")

    def check_request(self):
        return success_msg(RequestType.CHECK, "RESPONSE")

    def get_request(self, data):
        request = data[RequestType.GET].decode('utf-8')

        if request == DataType.DEPARTMENT:
            return {
                RequestType.GET: DataType.DEPARTMENT,
                DataType.DATA: self.instances.DATA_BASE.get_all_departments(),
                DataType.CODE: DataType.CODE_SUCCESS
            }
        else:
            department_id = self.instances.DATA_BASE.get_department_id_by_name(request)
            return {
                RequestType.GET: DataType.POSITION,
                DataType.DATA: self.instances.DATA_BASE.get_department_positions(department_id),
                DataType.CODE: DataType.CODE_SUCCESS
            }

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
            DataType.CODE: DataType.CODE_SUCCESS
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
                    self.instances.DATA_BASE.get_department_id_by_name(data[DataType.DEPARTMENT]),
                    self.instances.DATA_BASE.get_position_id_by_name(data[DataType.POSITION]))

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

            return success_msg(RequestType.ADD, employee_id)
        except IndexError:
            return error_msg(RequestType.ADD, "Not enough data for adding new employee")

    def edit_employee(self, request):
        try:
            data = json.loads(request[RequestType.EDIT])
            self.instances.DATA_BASE.edit_employee(
                data[DataType.ID],
                data[DataType.NAME],
                data[DataType.LAST_NAME],
                self.instances.DATA_BASE.get_department_id_by_name(data[DataType.DEPARTMENT]),
                self.instances.DATA_BASE.get_position_id_by_name(data[DataType.POSITION]))

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

            return success_msg(RequestType.EDIT, response_msg)
        except IndexError:
            return error_msg(RequestType.EDIT, "Not enough data for editing")

    def delete_employee(self, request):
        id = json.loads(request[RequestType.DELETE])

        photo_path = self.instances.DATA_BASE.get_image_data(id)["photo"]

        self.instances.DATA_BASE.del_image_data(id)
        self.instances.DATA_BASE.del_employee(id)
        self.instances.FACE_MANAGER.delete_photo(photo_path)

        return success_msg(RequestType.DELETE, "Employee deleted successfully")
