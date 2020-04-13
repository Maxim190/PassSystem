package com.example.facedetector.model.employee;

import java.util.HashMap;

public class NotIndexedEmployee extends Employee {

    private String departmentId;
    private String name;
    private String lastName;
    private String birthDate;
    private byte[] photo;

    public NotIndexedEmployee(String departmentId, String name,
                              String lastName, String birthDate, byte[] photo) {
        this.departmentId = departmentId;
        this.name = name;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.photo = photo;
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

    public String getBirthDate() {
        return birthDate;
    }

    @Override
    public byte[] getPhoto() {
        return photo;
    }

    public boolean hasEmptyField() {
        return name.isEmpty() || lastName.isEmpty() ||
                birthDate.isEmpty() || photo.length == 0;
    }

    @Override
    public HashMap<String, String> toHashMapExcludingPhoto() {
        return new HashMap<String, String>(){{
            put("departmentId", String.valueOf(departmentId));
            put("name", name);
            put("lastName", lastName);
            put("birth", birthDate);
        }};
    }
}
