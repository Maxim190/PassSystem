package com.example.facedetector.presenter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.facedetector.model.interfaces.MsgListener;
import com.example.facedetector.model.MsgSender;
import com.example.facedetector.model.MsgType;
import com.example.facedetector.view.interfaces.MainView;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.TreeMap;

public class MainPresenter implements MsgListener {

    private MainView currentView;
    private MyFaceDetector faceDetector;
    private MsgSender model;

    private Thread checkConnectionThread;

    public MainPresenter(MainView currentView) {
        this.currentView = currentView;
        this.faceDetector = new MyFaceDetector(currentView.getContext());
        this.model = MsgSender.getIntent();
        model.setListener(this);
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
        model.exchangeMessages(
                new TreeMap<>(Collections.singletonMap(MsgType.RECOGNIZE, byteArray)));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void sendMsg(byte[] msg) {
        model.exchangeMessages(
                new TreeMap<>(Collections.singletonMap(MsgType.CHECK, msg)));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void connectToServerToggle() {
        if (model.isConnected()) {
            model.disconnect();
            currentView.displayConnectionStatus(false);
            checkConnectionStatus(false);
        } else {
            String host = currentView.getIp().getText().toString();
            //Log.i("PassSystem", "Trying connect");
            model.connect(host.isEmpty() ? "localhost" : host, 8000);
            checkConnectionStatus(true);
        }
    }

    private void checkConnectionStatus(boolean check) {
        if (checkConnectionThread != null) {
            checkConnectionThread.interrupt();
        }
        if (check) {
            checkConnectionThread = new Thread(() -> {
                while (!Thread.interrupted()) {
                    try {
                        currentView.displayConnectionStatus(model.isConnected());
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            });
            checkConnectionThread.start();
        }
    }

    @Override
    public void receiveMsg(String msg) {
        currentView.displayText(msg);
    }
}
