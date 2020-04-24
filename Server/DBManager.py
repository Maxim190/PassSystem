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

    def get_admin(self, login):
        query = "SELECT * FROM admins WHERE login='" + str(login) + "'"
        self.cursor.execute(query)

        return self.cursor.fetchone()

    def get_viewer(self, login):
        query = "SELECT * FROM viewers WHERE login='" + str(login) + "'"
        self.cursor.execute(query)

        return self.cursor.fetchone()

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
        query = """
                SELECT id, name, last_name, department_name, position_name
                FROM (
                        SELECT id, name, last_name, department_name, position_id
                        FROM employees
                        LEFT JOIN departments
                        ON employees.department_id=departments.department_id
                    ) AS temp
                LEFT JOIN positions
                ON temp.position_id=positions.position_id
                WHERE id = {}
            """.format(str(id))
        self.cursor.execute(query)

        return self.cursor.fetchone()

    def get_all_employees(self):
        self.cursor.execute("SELECT id, name, last_name, "
                            "DATE_FORMAT(birth, \"%y-%m-%d\") AS birth, department_id "
                            "FROM employees")

        return self.cursor.fetchall()

    def add_employee(self, name, last_name, department_id, position_id):
        new_id = self.get_next_employee_id()
        query = 'INSERT INTO employees VALUES ( {}, "{}", "{}", {}, {} )' \
            .format(str(new_id), name, last_name, str(department_id), str(position_id))
        print(query)
        self.cursor.execute(query)
        self.conn.commit()

        return new_id

    def edit_employee(self, id, name, last_name, department_id, position_id):
        query = 'UPDATE employees SET name="{}", last_name="{}", department_id={}, position_id={} WHERE id={}'\
            .format(name, last_name, str(department_id), str(position_id), str(id))
        print("QUERY " + query)
        self.cursor.execute(query)
        self.conn.commit()

    def del_employee(self, id):
        query = 'DELETE FROM employees WHERE id=' + str(id)
        self.cursor.execute(query)
        self.conn.commit()

    def get_all_departments(self):
        query = "SELECT department_name FROM departments"
        self.cursor.execute(query)

        return self.cursor.fetchall()

    def get_department_id_by_name(self, department_name):
        query = "SELECT department_id FROM departments WHERE department_name='" + str(department_name) + "'"
        self.cursor.execute(query)

        return self.cursor.fetchone()["department_id"]

    def get_position_id_by_name(self, position_name):
        query = "SELECT position_id FROM positions WHERE position_name='" + str(position_name) + "'"
        self.cursor.execute(query)

        return self.cursor.fetchone()["position_id"]

    def get_department_positions(self, department_id):
        query = "SELECT position_name FROM positions WHERE department_id=" + str(department_id)
        self.cursor.execute(query)

        return self.cursor.fetchall()

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
