package com.example.facedetector.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public interface HomeInterface {
    interface View {
        Context getContext();
        void displayText(String text);
        void openEmployeeActivity(Bundle bundle);
        void openAuthorizationActivity();
        void setViewEnabled(boolean value);
        void setConnectionStatus(boolean value);
    }

    interface Presenter {
        void addEmployee();
        void recognizeFace(Intent data);
        void openConnectionDialog();
        void signOut();
    }
}
