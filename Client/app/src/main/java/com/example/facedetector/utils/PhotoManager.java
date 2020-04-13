package com.example.facedetector.utils;

import android.content.Intent;

import androidx.fragment.app.FragmentActivity;

public class PhotoManager {

    public static final int CAMERA_REQUEST = 1888;

    public static void requestPhoto(FragmentActivity activity) {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        activity.startActivityForResult(intent, CAMERA_REQUEST);
    }

    public static void compressImg(){};
}
