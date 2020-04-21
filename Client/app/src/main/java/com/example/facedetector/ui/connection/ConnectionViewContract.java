package com.example.facedetector.ui.connection;

public interface ConnectionViewContract {
    interface View {
        String getIp();
        void displayLog(String msg);
        void closeActivity();
    }
    interface Presenter {
        void connect();
    }
}
