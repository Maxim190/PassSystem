package com.example.facedetector.ui.authorization;

import android.os.Bundle;

public interface AuthorizationViewContract {
    interface View {
        String getLogin();
        String getPassword();

        void openMainActivity(Bundle bundle);
        void displayMsg(String msg);
    }
    interface Presenter {
        void signIn();
    }
}
