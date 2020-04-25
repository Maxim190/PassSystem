package com.example.facedetector.ui.connection;

public interface ConnectionViewContract {
    interface View {
        String getIp();
        void setIp(String ip);
        void displayLog(String msg);
        void closeActivity();
    }
    interface Presenter {
        void connect();
    }
}
