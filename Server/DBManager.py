import pymysql
from flask import Flask
from flaskext.mysql import MySQL
from pymysql.cursors import DictCursor


class DB:
    def __init__(self):
        print("Connecting to database")

        app = Flask(__name__)
        app.config['MYSQL_DATABASE_USER'] = 'root'
        app.config['MYSQL_DATABASE_PASSWORD'] = 'root'
        app.config['MYSQL_DATABASE_DB'] = 'pass_system'
        app.config['MYSQL_DATABASE_HOST'] = 'localhost'
        mysql = MySQL(cursorclass=DictCursor)
        mysql.init_app(app)

        self.conn = mysql.connect()
        self.cursor = self.conn.cursor()

        print("Connected to database")

        # print("Connecting to database")
        # self.conn = pymysql.connect('localhost', 'root', 'root', 'pass_system')
        # self.cursor = self.conn.cursor()
        # self.photo_storage_path = "\\photos\\"
        # print("Connected to database")

    def get_next_employee_id(self):
        query = 'SELECT MAX(id) as id FROM employees'
        self.cursor.execute(query)
        max_id = self.cursor.fetchone()["id"]

        if max_id is None:
            return 1
        else:
            return int(max_id) + 1

    def get_employee_id(self, name, last_name, birth):
        query = 'SELECT id FROM employees WHERE name="{}", last_name="{}", birth="{}"'\
            .format(name, last_name, birth)
        self.cursor.execute(query)
        id = self.cursor.fetchone()

        return int(id)

    def get_employee_by_id(self, id):
        query = "SELECT id, name, last_name, " \
                "DATE_FORMAT(birth, \"%y-%m-%d\") AS birth, department_id " \
                "FROM employees " \
                "WHERE id=" + str(id)
        self.cursor.execute(query)

        return self.cursor.fetchone()

    def get_all_employees(self):
        self.cursor.execute("SELECT id, name, last_name, "
                            "DATE_FORMAT(birth, \"%y-%m-%d\") AS birth, department_id "
                            "FROM employees")

        return self.cursor.fetchall()

    def add_employee(self, name, last_name, birth, department_id):
        new_id = self.get_next_employee_id()
        query = 'INSERT INTO employees VALUES ( {}, "{}", "{}", "{}", {} )' \
            .format(str(new_id), name, last_name, birth, str(department_id))
        self.cursor.execute(query)
        self.conn.commit()

        return new_id

    def edit_employee(self, id, name, last_name, birth, department_id):
        query = 'UPDATE employees SET name="{}", last_name="{}", birth="{}", department_id={} WHERE id={}'\
            .format(name, last_name, birth, str(department_id), str(id))
        self.cursor.execute(query)
        self.conn.commit()

    def del_employee(self, id):
        query = 'DELETE FROM employees WHERE id=' + str(id)
        self.cursor.execute(query)
        self.conn.commit()

    def get_image_data(self, id):
        query = 'SELECT * FROM images_data WHERE id=' + str(id)
        self.cursor.execute(query)

        return self.cursor.fetchone()

    def get_all_img_data(self):
        query = "SELECT * FROM images_data"
        self.cursor.execute(query)

        return self.cursor.fetchall()

    def add_image_data(self, id, photo, descriptor):
        query = 'INSERT INTO images_data VALUES (%s, %s, %s)'
        args = (id, photo, descriptor)
        self.cursor.execute(query, args)
        self.conn.commit()

    def edit_img_data(self, id, photo, descriptor):
        query = 'UPDATE images_data SET photo="{}", descriptor="{}" WHERE id={}'\
            .format(photo, descriptor, str(id))
        self.cursor.execute(query)
        self.conn.commit()

    def del_image_data(self, id):
        query = 'DELETE FROM images_data WHERE id=' + str(id)
        self.cursor.execute(query)
        self.conn.commit()

    def get_all_departments(self):
        query = 'SELECT * FROM departments'
        self.cursor.execute(query)

        return self.cursor.fetchall()

    def add_department(self, id, name):
        query = 'INSERT INTO department VALUES ({}, "{}")'\
            .format(str(id), name)
        self.cursor.execute(query)
        self.conn.commit()

    def edit_department(self, id, name):
        query = 'UPDATE departments SET name="{}" WHERE id={}'\
            .format(name, str(id))
        self.cursor.execute(query)
        self.conn.commit()

    def del_department(self, id):
        query = 'DELETE FROM departments WHERE id=' + str(id)
        self.cursor.execute(query)
        self.conn.commit()
