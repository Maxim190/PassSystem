package com.example.facedetector.ui.employee_activity;

import android.graphics.Bitmap;

public interface EmployeeViewContract {
    interface View {
        String getName();
        String getLastName();
        String getBirthDate();
        String getDepartmentId();
        Bitmap getPhoto();

        void setActivityEnabled(boolean value);
        void displayMsg(String msg);
        void closeActivity();
        void setActivityMode(int mode);
        void setName(String name);
        void setLastName(String lastName);
        void setBirthDate(String birthDate);
        void setDepartmentId(String departmentId);
        void setPhoto(Bitmap bitmap);
    }
    interface Presenter {
        void addEmployee();
        void editEmployee();
        void deleteEmployee();
    }
}
