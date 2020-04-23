package com.example.facedetector.model.employee;

import com.example.facedetector.utils.Consts;

import java.util.HashMap;

public class NotIndexedEmployee extends Employee {

    private String name;
    private String lastName;
    private String department;
    private String position;
    private byte[] photo;

    public NotIndexedEmployee(String name, String lastName,
                              String department, String position, byte[] photo) {
        this.name = name;
        this.lastName = lastName;
        this.department = department;
        this.position = position;
        this.photo = photo;
    }

    public String getDepartment() {
        return department;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPosition() {
        return position;
    }

    @Override
    public byte[] getPhoto() {
        return photo;
    }

    public boolean hasEmptyField() {
        return name.isEmpty() || lastName.isEmpty() ||
                department.isEmpty() || position.isEmpty() || photo.length == 0;
    }

    @Override
    public HashMap<String, String> toHashMapExcludingPhoto() {
        return new HashMap<String, String>(){{
            put(Consts.DATA_TYPE_NAME, name);
            put(Consts.DATA_TYPE_LAST_NAME, lastName);
            put(Consts.DATA_TYPE_DEPARTMENT, department);
            put(Consts.DATA_TYPE_POSITION, position);
        }};
    }
}
