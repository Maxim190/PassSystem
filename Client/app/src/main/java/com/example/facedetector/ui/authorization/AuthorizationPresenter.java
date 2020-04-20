package com.example.facedetector.ui.authorization;

import android.util.Log;

import com.example.facedetector.model.MsgListener;
import com.example.facedetector.model.NetworkService;
import com.example.facedetector.utils.Consts;
import com.example.facedetector.utils.JSONManager;

import java.util.Map;

public class AuthorizationPresenter implements AuthorizationViewContract.Presenter, MsgListener {

    public static String CURRENT_RIGHTS;

    private AuthorizationViewContract.View currentView;
    private NetworkService model;

    String host = "192.168.0.102";
    int port = 8000;

    AuthorizationPresenter(AuthorizationViewContract.View currentView) {
        this.currentView = currentView;
        model = NetworkService.getIntent();
        model.connect(host, port);
        CURRENT_RIGHTS = Consts.ACCESS_VIEWER;
    }

    @Override
    public void signIn() {
        if (!model.isConnected()) {
            model.connect(host, port);
        }
        String login = currentView.getLogin();
        String password = currentView.getPassword();

        if (login.isEmpty() || password.isEmpty()) {
            currentView.displayMsg("Fill all the fields");
            return;
        }
        model.authorize(login, password, this);
    }

    @Override
    public void callback(Map<String, byte[]> data) {
        Log.e("PassSystem", "CALLBACK " + data.toString());
        if (data.isEmpty()) {
            currentView.displayMsg("Failed receiving data from server");
        }
        else if (data.containsKey(Consts.DATA_TYPE_CODE)) {
            String code = JSONManager.parseToStr(data.get(Consts.DATA_TYPE_CODE));
            if (Consts.CODE_ERROR.equals(code)) {
                currentView.displayMsg(JSONManager.parseToStr(data.values().iterator().next()));
                return;
            }
            CURRENT_RIGHTS = JSONManager.parseToStr(data.get(Consts.MSG_TYPE_AUTHORIZE));
            currentView.openMainActivity();
        }
    }
}
