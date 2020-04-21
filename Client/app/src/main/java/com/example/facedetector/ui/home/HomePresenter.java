package com.example.facedetector.ui.home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.facedetector.model.ConnectionStatusListener;
import com.example.facedetector.ui.authorization.AuthorizationHandler;
import com.example.facedetector.utils.Bundlebuilder;
import com.example.facedetector.utils.Consts;
import com.example.facedetector.model.NetworkService;
import com.example.facedetector.model.MsgListener;
import com.example.facedetector.utils.JSONManager;
import com.example.facedetector.utils.MyFaceDetector;
import com.example.facedetector.ui.employee_activity.EmployeeActivity;

import java.io.ByteArrayOutputStream;
import java.util.Map;

public class HomePresenter implements HomeInterface.Presenter, MsgListener, ConnectionStatusListener {

    private HomeInterface.View currentView;
    private MyFaceDetector faceDetector;
    private NetworkService model;

    public HomePresenter(HomeInterface.View currentView) {
        this.currentView = currentView;
        this.faceDetector = new MyFaceDetector(currentView.getContext());
        this.model = NetworkService.getIntent();

        model.setConnectionStatusListener(this);
        currentView.setConnectionStatus(model.isConnected());
    }

    @Override
    public void addEmployee() {
        if (Consts.ACCESS_VIEWER.equals(AuthorizationHandler.getCurrentRightMode())) {
            currentView.displayText("You don't have permission");
        }
        else {
            Bundle bundle = new Bundle();
            bundle.putInt(EmployeeActivity.BUNDLE_MODE_KEY, EmployeeActivity.ACTIVITY_ADD_MODE);
            currentView.openEmployeeActivity(bundle);
        }
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

    @Override
    public void openConnectionDialog() {

    }

    @Override
    public void signOut() {
        model.disconnect();
        AuthorizationHandler.setLogin(null);
        AuthorizationHandler.setPassword(null);
        currentView.openAuthorizationActivity();
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
        currentView.setViewEnabled(false);
    }

    @Override
    public void callback(Map<String, byte[]> data) {
        currentView.setViewEnabled(true);

        if (data.containsKey(Consts.DATA_TYPE_CODE)
                && Consts.CODE_ERROR.equals(JSONManager.parseToStr(data.get(Consts.DATA_TYPE_CODE)))) {

            String errorRequest = data.keySet().iterator().next();
            String errorMsg = JSONManager.parseToStr(data.values().iterator().next());

            currentView.displayText(errorMsg);
            if (Consts.MSG_TYPE_AUTHORIZE.equals(errorRequest)) {
                signOut();
            }
            return;
        }

        Bundle bundle = Bundlebuilder.build(data);
        bundle.putInt(EmployeeActivity.BUNDLE_MODE_KEY,
                EmployeeActivity.ACTIVITY_EDIT_MODE);
        currentView.openEmployeeActivity(bundle);
    }

    @Override
    public void connectionStatusChanged(boolean isConnected) {
        currentView.setConnectionStatus(isConnected);
    }
}
