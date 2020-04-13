package com.example.facedetector.ui.fragments.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public interface HomeInterface {
    interface View {
        Context getContext();
        void displayText(String text);
        void openEmployeeActivity(Bundle bundle);
    }

    interface Presenter {
        void addEmployee();
        void recognizeFace(Intent data);
    }
}
