package com.example.facedetector.presenter;

import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.facedetector.Consts;
import com.example.facedetector.model.MsgSender;
import com.example.facedetector.model.MsgType;
import com.example.facedetector.view.interfaces.AddEmployeeView;

import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class AddEmployeePresenter {

    private AddEmployeeView currentView;
    private MsgSender model = MsgSender.getIntent();

    public AddEmployeePresenter(AddEmployeeView currentView) {
        this.currentView = currentView;
    }

    private Map<String, String> getEmployeeData() {
        return new LinkedHashMap<String, String>(){{
            put(Consts.DATA_TYPE_NAME, currentView.getName());
            put(Consts.DATA_TYPE_LASTNAME, currentView.getLastName());
            put(Consts.DATA_TYPE_BIRTH, currentView.getBirthDate());
            put(Consts.DATA_TYPE_DEPARTMENT, currentView.getDepartmentId());
        }};
    }

    private byte[] convertBitmapToArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void addEmployee() {
        Map<String, String> employeeData = getEmployeeData();
        byte[] employeePhoto = convertBitmapToArray(currentView.getPhoto());

        boolean fullData = employeeData.entrySet().stream().noneMatch(entry -> entry.getValue().isEmpty());
        if(!fullData && employeePhoto.length == 0) {
            currentView.displayMsg("Not all data provided");
            return;
        }
        TreeMap<MsgType, byte[]> msg = new TreeMap<>();
        msg.put(MsgType.ADD, employeeData.toString().getBytes());
        msg.put(MsgType.ADDITIONAL_PHOTO, employeePhoto);
        model.exchangeMessages(msg);
    }
}
