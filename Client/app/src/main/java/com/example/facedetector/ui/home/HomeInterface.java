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
        void startNewActivity(Intent intent, Integer resultCode);
        void setName(String name);
        void setPosition(String position);
    }

    interface Presenter {
        void onActivityResult(int requestCode, int resultCode, Intent data);
        void addEmployee();
        void recognizeFace();
        void openConnectionActivity();
        void signOut();
    }
}
