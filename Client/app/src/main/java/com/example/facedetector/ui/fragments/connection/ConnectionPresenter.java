package com.example.facedetector.ui.fragments.connection;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.facedetector.model.NetworkService;

public class ConnectionPresenter {

    private static Thread checkConnectionThread;
    private ConnectionInterface currentView;
    private NetworkService model;

    public ConnectionPresenter(ConnectionInterface currentView) {
        this.currentView = currentView;
        model = NetworkService.getIntent();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void connectToServerToggle() {
        if (model.isConnected()) {
            model.disconnect();
            currentView.displayConnectionStatus(false);
            checkConnectionStatus(false);
        } else {
            String host = currentView.getIp();
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
}
