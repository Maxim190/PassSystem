package com.example.facedetector.ui.activities.authorization;

import android.content.Intent;

public interface AuthorizationViewContract {
    interface View {
        String getLogin();
        String getPassword();

        void openMainActivity();
        void displayMsg(String msg);
    }
    interface Presenter {
        void signIn();
    }
}
