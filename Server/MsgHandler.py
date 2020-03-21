import re


class MsgHandler:
    PHOTO = "ADDITIONAL_PHOTO"
    NAME = "NAME"
    LAST_NAME = "LASTNAME"
    BIRTH = "BIRTH"
    DEPARTMENT = "DEPARTMENT"
    ID = "ID"

    def __init__(self, instances):
        self.instances = instances

    # MgStruct: [['MsgType', msg (byte[])], ['MsgType', msg (byte[])], ...]
    def handle(self, msg):
        if msg is None or len(msg) == 0:
            return "empty msg received"
        parsed_msg = self.parse_msg(msg)
        if msg[0][0] == "RECOGNIZE":
            return self.recognize_face(parsed_msg["RECOGNIZE"])
        elif msg[0][0] == "ADD":
            print("ADD: " + str(parsed_msg))
            return self.add_employee(parsed_msg)
        elif msg[0][0] == "EDIT":
            print("EDIT: " + str(parsed_msg))
            return "edited"
        elif msg[0][0] == "CHECK":
            return str(parsed_msg)

    def parse_msg(self, raw_msg):
        result = {}
        for i in range(0, len(raw_msg)):
            if raw_msg[i][0] == "ADDITIONAL_PHOTO" or raw_msg[i][0] == "RECOGNIZE":
                result[raw_msg[i][0]] = raw_msg[i][1]
            else:
                processed_str = re.sub("[{}b'\"\s]", "", str(raw_msg[i][1]))
                parameters_array = processed_str.split(',')

                for item in parameters_array:
                    parsed_item = item.split('=')
                    if len(parsed_item) == 1:
                        result[parsed_item[0]] = parsed_item[0]
                    else:
                        result[parsed_item[0]] = parsed_item[1]

        return result

    def recognize_face(self, photo):
        data = self.instances.DATA_BASE.get_all_img_data()
        photo_descriptor = self.instances.FACE_MANAGER.get_descriptor(photo)
        if photo_descriptor is None:
            return "Face not found"
        for item in data:
            item_descriptor = [float(i) for i in item[2].split(':')]
            if self.instances.FACE_MANAGER.is_one_person(item_descriptor, photo_descriptor):
                return str(self.instances.DATA_BASE.get_employee_by_id(item[0]))
        return "There is no face like that in base"

    def add_employee(self, data):
        try:
            descriptor = self.instances.FACE_MANAGER.get_descriptor(data[self.PHOTO])
            if descriptor is None:
                return "Face is not detected"
            employee_id = self.instances.DATA_BASE.add_employee(
                data[self.NAME], data[self.LAST_NAME], data[self.BIRTH], data[self.DEPARTMENT])
            photo_file_path = self.instances.FACE_MANAGER.photo_storage_path + str(employee_id) + ".jpg"
            self.instances.DATA_BASE.add_image_data(
                employee_id, photo_file_path,
                self.instances.FACE_MANAGER.descriptor_to_string(descriptor))
            self.instances.FACE_MANAGER.write_img(data[self.PHOTO], photo_file_path)
            return "Added new employee successfully"
        except IndexError:
            return "Not enough data for adding new employee"

    def edit_employee(self, data):
        try:
            self.instances.DATA_BASE.edit_employee(
                data[self.ID], data[self.NAME], data[self.LAST_NAME], data[self.BIRTH], data[self.DEPARTMENT])
            return "Edited employee successfully"
        except IndexError:
            return "Not enough data for editing"
