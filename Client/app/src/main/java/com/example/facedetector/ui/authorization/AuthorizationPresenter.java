package com.example.facedetector.ui.authorization;

import android.util.Log;

import com.example.facedetector.model.ConnectionStatusListener;
import com.example.facedetector.model.MsgListener;
import com.example.facedetector.model.NetworkService;
import com.example.facedetector.utils.Consts;
import com.example.facedetector.utils.JSONManager;

import java.util.Map;

public class AuthorizationPresenter implements AuthorizationViewContract.Presenter, MsgListener, ConnectionStatusListener {

    private AuthorizationViewContract.View currentView;
    private NetworkService model;

    AuthorizationPresenter(AuthorizationViewContract.View currentView) {
        this.currentView = currentView;
        model = NetworkService.getIntent();
        model.setConnectionStatusListener(this);
        model.connect(NetworkService.DEFAULT_HOST_IP);
    }

    @Override
    public void signIn() {
        currentView.displayMsg("");

        String login = currentView.getLogin();
        String password = currentView.getPassword();

        if (login.isEmpty() || password.isEmpty()) {
            currentView.displayMsg("Fill all the fields");
            return;
        }
        AuthorizationHandler.setLogin(login);
        AuthorizationHandler.setPassword(password);

        model.authorize(login, password, this);
    }

    @Override
    public void callback(Map<String, byte[]> data) {
        if (data.isEmpty()) {
            currentView.displayMsg("Failed receiving data from server");
        }
        else if (data.containsKey(Consts.DATA_TYPE_CODE)) {
            String code = JSONManager.parseToStr(data.get(Consts.DATA_TYPE_CODE));
            if (Consts.CODE_ERROR.equals(code)) {
                currentView.displayMsg(JSONManager.parseToStr(data.values().iterator().next()));
                AuthorizationHandler.clearData();
                return;
            }
            AuthorizationHandler.extractAccessRightData(data);
            model.deleteConnectionStatusListener(this);
            currentView.openMainActivity(AuthorizationHandler.getEmployeeBundle());
        }
    }

    @Override
    public void connectionStatusChanged(boolean isConnected) {
        currentView.displayConnectBtn(!isConnected);
    }
}
