package com.example.facedetector.ui.employee_activity;

import android.graphics.Bitmap;

import java.util.List;

public interface EmployeeViewContract {
    interface View {
        String getName();
        String getLastName();
        String getDepartment();
        String getPosition();
        Bitmap getPhoto();

        void setActivityEnabled(boolean value);
        void displayMsg(String msg);
        void closeActivity();
        void setActivityMode(int mode);
        void setName(String name);
        void setLastName(String lastName);
        void setPhoto(Bitmap bitmap);
        void setDepartments(List<String> data);
        void setPositions(List<String> data);
    }
    interface Presenter {
        void addEmployee();
        void editEmployee();
        void deleteEmployee();
        void openEditMode();
        void departmentSelected(String department);
    }
}
