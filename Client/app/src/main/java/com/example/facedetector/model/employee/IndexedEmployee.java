package com.example.facedetector.model.employee;

import java.util.HashMap;

public class IndexedEmployee extends Employee {

    private String id;
    private String departmentId;
    private String name;
    private String lastName;
    private String birth;
    private byte[] photo;

    public IndexedEmployee(NotIndexedEmployee employee, String id) {
        this(id, employee.getDepartmentId(), employee.getName(),
                employee.getLastName(), employee.getBirthDate(), employee.getPhoto());
    }

    public IndexedEmployee(String id, String departmentId, String name, String lastName, String birth, byte[] photo) {
        this.id = id;
        this.departmentId = departmentId;
        this.name = name;
        this.lastName = lastName;
        this.birth = birth;
        this.photo = photo;
    }

    public String getId() {
        return id;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public String getBirth() {
        return birth;
    }

    @Override
    public byte[] getPhoto() {
        return photo;
    }

    @Override
    public HashMap<String, String> toHashMapExcludingPhoto() {
        return new HashMap<String, String>(){{
            put("id", String.valueOf(id));
            put("departmentId", String.valueOf(departmentId));
            put("name", name);
            put("lastName", lastName);
            put("birth", birth);
        }};
    }
}
