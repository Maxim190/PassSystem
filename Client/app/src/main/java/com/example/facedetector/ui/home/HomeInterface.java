package com.example.facedetector.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

public interface HomeInterface {
    interface View {
        Context getContext();
        void displayText(String text);
        void setViewEnabled(boolean value);
        void setConnectionStatus(boolean value);
        void startNewActivityForResult(Intent intent, int requestCode);
    }

    interface Presenter {
        void onActivityResult(int requestCode, int resultCode, Intent data);
        void addEmployee();
        void recognizeFace();
        void openConnectionActivity();
        void signOut();
    }
}
