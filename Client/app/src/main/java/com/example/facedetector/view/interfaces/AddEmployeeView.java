package com.example.facedetector.view.interfaces;

import android.graphics.Bitmap;

public interface AddEmployeeView {
    String getName();
    String getLastName();
    String getBirthDate();
    String getDepartmentId();
    Bitmap getPhoto();
    void displayMsg(String msg);
}
