package com.example.facedetector.ui.authorization;

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
