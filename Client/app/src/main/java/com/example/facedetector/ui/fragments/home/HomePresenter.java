package com.example.facedetector.ui.fragments.home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.facedetector.utils.Consts;
import com.example.facedetector.model.NetworkService;
import com.example.facedetector.model.MsgListener;
import com.example.facedetector.utils.JSONManager;
import com.example.facedetector.utils.MyFaceDetector;
import com.example.facedetector.ui.activities.employee_activity.EmployeeActivity;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class HomePresenter implements HomeInterface.Presenter, MsgListener {

    private HomeInterface.View currentView;
    private MyFaceDetector faceDetector;
    private NetworkService model;

    public HomePresenter(HomeInterface.View currentView) {
        this.currentView = currentView;
        this.faceDetector = new MyFaceDetector(currentView.getContext());
        this.model = NetworkService.getIntent();
    }

    @Override
    public void addEmployee() {
        Bundle bundle = new Bundle();
        bundle.putInt(EmployeeActivity.BUNDLE_MODE_KEY, EmployeeActivity.ACTIVITY_ADD_MODE);
        currentView.openEmployeeActivity(bundle);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void recognizeFace(Intent data) {
        if (data == null) {
            currentView.displayText("Photo is missing");
            return;
        }
        Bitmap bmp = (Bitmap) data.getExtras().get("data");
        recognizeFace(bmp);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void recognizeFace(Bitmap bmp) {
        if (bmp == null) {
            currentView.displayText("Failed getting photo");
            return;
        }
        if (!faceDetector.containsFace(bmp)) {
            currentView.displayText("There're no faces on the photo");
            return;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        byte[] byteArray = stream.toByteArray();

        model.recognizeEmployee(byteArray, this);
    }

    @Override
    public void callback(Map<String, byte[]> data) {
        Bundle bundle = new Bundle();
        for(String key: data.keySet()) {
            if(Consts.DATA_TYPE_PHOTO.equals(key)) {
                bundle.putByteArray(key, data.get(key));
            } else {
                byte[] bytes = data.get(key);
                if (bytes != null) {
                    Map<String, String> array = JSONManager.parse(new String(bytes));
                    for (String item: array.keySet()) {
                        bundle.putString(item, array.get(item));
                    }
                }
            }
        }
        bundle.putInt(EmployeeActivity.BUNDLE_MODE_KEY,
                EmployeeActivity.ACTIVITY_EDIT_MODE);
        currentView.openEmployeeActivity(bundle);
    }
}
