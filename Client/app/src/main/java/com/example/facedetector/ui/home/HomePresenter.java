package com.example.facedetector.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.example.facedetector.model.ConnectionStatusListener;
import com.example.facedetector.model.MsgListener;
import com.example.facedetector.model.NetworkService;
import com.example.facedetector.ui.authorization.AuthorizationActivity;
import com.example.facedetector.ui.authorization.AuthorizationHandler;
import com.example.facedetector.ui.connection.ConnectionActivity;
import com.example.facedetector.ui.employee_activity.EmployeeActivity;
import com.example.facedetector.utils.Bundlebuilder;
import com.example.facedetector.utils.Consts;
import com.example.facedetector.utils.JSONManager;
import com.example.facedetector.utils.MyFaceDetector;

import java.io.ByteArrayOutputStream;
import java.util.Map;

public class HomePresenter implements HomeInterface.Presenter, MsgListener,
        ConnectionStatusListener, AuthorizationHandler.AccessRightsListener {

    private final int CAMERA_REQUEST = 1888;
    private final int ACTIVITY_CLOSED = 1000;

    private HomeInterface.View currentView;
    private MyFaceDetector faceDetector;
    private NetworkService model;

    HomePresenter(HomeInterface.View currentView, Bundle bundle) {
        this.currentView = currentView;
        this.faceDetector = new MyFaceDetector(currentView.getContext());
        this.model = NetworkService.getIntent();

        extractFromBundle(bundle);
        model.setConnectionStatusListener(this);
        AuthorizationHandler.setAccessRightsChangeListener(this);
        currentView.setConnectionStatus(model.isConnected());
    }

    private void extractFromBundle(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        String name = bundle.getString(Consts.DATA_TYPE_NAME);
        String lastName = bundle.getString(Consts.DATA_TYPE_LAST_NAME);
        String position = bundle.getString(Consts.DATA_TYPE_POSITION);

        currentView.setName(name + " " + lastName);
        currentView.setPosition(position);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                recognizeFace(data);
            } else {
                currentView.displayText("Failed taking photo");
            }
        }
        else if (requestCode == ACTIVITY_CLOSED) {
            currentView.setConnectionStatus(model.isConnected());
        }
    }

    private void openEmployeeActivity(Bundle bundle) {
        Intent intent = new Intent(currentView.getContext(), EmployeeActivity.class);
        intent.putExtra(Consts.DATA_TYPE_BUNDLE, bundle);
        currentView.startNewActivityForResult(intent, 0);
    }

    @Override
    public void addEmployee() {
        if (Consts.ACCESS_VIEWER.equals(AuthorizationHandler.getCurrentRightMode())) {
            currentView.displayText("You don't have permission");
        }
        else {
            Bundle bundle = new Bundle();
            bundle.putInt(Consts.DATA_TYPE_BUNDLE, EmployeeActivity.ACTIVITY_ADD_MODE);
            openEmployeeActivity(bundle);
        }
    }

    @Override
    public void recognizeFace() {
        currentView.startNewActivityForResult(
                new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_REQUEST);
    }

    private void recognizeFace(Intent data) {
        if (data == null) {
            currentView.displayText("Photo is missing");
            return;
        }
        Bitmap bmp = (Bitmap) data.getExtras().get("data");
        recognizeFace(bmp);
    }

    private void recognizeFace(Bitmap bmp) {
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
    public void openConnectionActivity() {
        currentView.startNewActivityForResult(
                new Intent(currentView.getContext(), ConnectionActivity.class), ACTIVITY_CLOSED);
    }

    @Override
    public void signOut() {
        AuthorizationHandler.clearData();
        model.disconnect();
        currentView.startNewActivityForResult(
                new Intent(currentView.getContext(), AuthorizationActivity.class), 0);
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
        bundle.putInt(Consts.DATA_TYPE_BUNDLE,
                EmployeeActivity.ACTIVITY_VIEW_MODE);
        openEmployeeActivity(bundle);
    }

    @Override
    public void connectionStatusChanged(boolean isConnected) {
        currentView.setConnectionStatus(isConnected);
    }

    @Override
    public void accessRightsChanged(String rightMode) {
        if (rightMode == null) {
            signOut();
        }
        else {
            extractFromBundle(AuthorizationHandler.getEmployeeBundle());
        }
    }
}
