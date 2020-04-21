package com.example.facedetector.ui.authorization;

import com.example.facedetector.utils.Consts;
import com.example.facedetector.utils.JSONManager;

import java.util.Map;

public class AuthorizationHandler {

    private static String currentRightMode;
    private static String login;
    private static String password;

    private AuthorizationHandler(){}

    public static String getLogin() {
        return login;
    }

    public static String getPassword() {
        return password;
    }

    public static void setLogin(String value) {
        login = value;
    }

    public static void setPassword(String value) {
        password = value;
    }

    public static String getCurrentRightMode() {
        return currentRightMode;
    }

    public static void setCurrentRightMode(String value) {
        currentRightMode = value;
    }

    public static void clearData() {
        login = null;
        password = null;
        currentRightMode = null;
    }

    public static void extractAccessRightMode(Map<String, byte[]> data) {
        if (data == null || data.isEmpty()) {
            currentRightMode = null;
        }
        else if (data.containsKey(Consts.DATA_TYPE_CODE)) {
            String code = JSONManager.parseToStr(data.get(Consts.DATA_TYPE_CODE));
            if (Consts.CODE_SUCCESS.equals(code)) {
                currentRightMode = JSONManager.parseToStr(data.get(Consts.DATA_TYPE_ACCESS_RIGHTS));
                return;
            }
        }
        currentRightMode = null;
    }
}
