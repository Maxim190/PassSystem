package com.example.facedetector.model.employee;

import com.example.facedetector.utils.Consts;

import java.util.HashMap;

public class IndexedEmployee extends Employee {

    private String id;
    private String department;
    private String position;
    private String name;
    private String lastName;
    private byte[] photo;

    public IndexedEmployee(NotIndexedEmployee employee, String id) {
        this(id, employee.getName(),
                employee.getLastName(), employee.getDepartment(), employee.getPosition(), employee.getPhoto());
    }

    public IndexedEmployee(String id, String name, String lastName, String department, String position, byte[] photo) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.department = department;
        this.position = position;
        this.photo = photo;
    }

    public String getId() {
        return id;
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

    @Override
    public HashMap<String, String> toHashMapExcludingPhoto() {
        return new HashMap<String, String>(){{
            put(Consts.DATA_TYPE_ID, id);
            put(Consts.DATA_TYPE_NAME, name);
            put(Consts.DATA_TYPE_LAST_NAME, lastName);
            put(Consts.DATA_TYPE_DEPARTMENT, department);
            put(Consts.DATA_TYPE_POSITION, position);
        }};
    }
}
