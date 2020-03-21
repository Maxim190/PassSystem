package com.example.facedetector.view.interfaces;

import android.content.Context;
import android.widget.TextView;

public interface MainView {
    TextView getIp();
    Context getContext();
    void displayText(String text);
    void displayConnectionStatus(Boolean value);
}
