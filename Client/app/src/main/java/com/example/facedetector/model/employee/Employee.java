package com.example.facedetector.model.employee;

import java.util.HashMap;

public abstract class Employee {
    public abstract byte[] getPhoto();
    public abstract HashMap<String, String> toHashMapExcludingPhoto();
}
