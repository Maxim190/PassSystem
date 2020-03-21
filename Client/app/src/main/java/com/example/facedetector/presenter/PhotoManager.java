package com.example.facedetector.presenter;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

public class PhotoManager {

    public static final int CAMERA_REQUEST = 1888;

    public static void requestPhoto(AppCompatActivity activity) {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        activity.startActivityForResult(intent, CAMERA_REQUEST);
    }

    public static void compressImg(){};
}
