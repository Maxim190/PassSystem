package com.example.facedetector.ui.connection;

import com.example.facedetector.model.ConnectionStatusListener;
import com.example.facedetector.model.NetworkService;
import com.example.facedetector.model.ConnectionLogListener;

public class ConnectionPresenter implements ConnectionStatusListener, ConnectionLogListener, ConnectionViewContract.Presenter {

    private ConnectionViewContract.View currentView;
    private NetworkService model;

    public ConnectionPresenter(ConnectionViewContract.View currentView) {
        this.currentView = currentView;
        model = NetworkService.getIntent();
        model.setConnectionStatusListener(this);
        model.setLogListener(this);
        currentView.setIp(NetworkService.DEFAULT_HOST_IP);
    }

    @Override
    public void connectionStatusChanged(boolean isConnected) {
        if (isConnected) {
            currentView.closeActivity();
        }
    }

    @Override
    public void connect() {
        String ip = currentView.getIp();
        if (ip == null || ip.isEmpty()) {
            return;
        }
        model.connect(ip);
    }

    @Override
    public void logCallback(String msg) {
        currentView.displayLog(msg);
    }
}
