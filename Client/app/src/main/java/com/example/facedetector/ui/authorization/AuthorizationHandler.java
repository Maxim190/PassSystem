package com.example.facedetector.ui.authorization;

import android.os.Bundle;

import com.example.facedetector.utils.Consts;
import com.example.facedetector.utils.JSONManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AuthorizationHandler {

    private static String currentRightMode;
    private static String login;
    private static String password;
    private static Bundle employeeBundle;

    public interface AccessRightsListener {
        void accessRightsChanged(String rightMode);
    }

    private static List<AccessRightsListener> listeners = new ArrayList<>();

    public static void setAccessRightsChangeListener(AccessRightsListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    private static void setRightMode(String newMode) {
        if (!newMode.equals(currentRightMode)) {
            currentRightMode = newMode;
            listeners.forEach(v -> {
                if (v != null) {
                    v.accessRightsChanged(newMode);
                }
            });
        }
    }

    private AuthorizationHandler(){}

    public static Bundle getEmployeeBundle() {
        return employeeBundle;
    }

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

    public static void clearData() {
        login = null;
        password = null;
        currentRightMode = null;
    }

    public static void extractAccessRightData(Map<String, byte[]> data) {
        if (data == null || data.isEmpty()) {
            currentRightMode = null;
        }
        else if (data.containsKey(Consts.DATA_TYPE_CODE)) {
            String code = JSONManager.parseToStr(data.get(Consts.DATA_TYPE_CODE));
            if (Consts.CODE_SUCCESS.equals(code)) {
                Map<String, String> array = JSONManager.parse(
                        new String(data.get(Consts.MSG_TYPE_AUTHORIZE)));
                employeeBundle = new Bundle();
                array.forEach(employeeBundle::putString);
                setRightMode(JSONManager.parseToStr(data.get(Consts.DATA_TYPE_ACCESS_RIGHTS)));
                return;
            }
        }
        setRightMode(null);
    }
}
