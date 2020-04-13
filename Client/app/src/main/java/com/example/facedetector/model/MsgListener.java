package com.example.facedetector.model;

import java.util.Map;

public interface MsgListener {
    void callback(Map<String, byte[]> data);
}
