package com.example.facedetector.ui.activities.employee_activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;

import com.example.facedetector.model.MsgListener;
import com.example.facedetector.model.NetworkService;
import com.example.facedetector.model.employee.IndexedEmployee;
import com.example.facedetector.model.employee.NotIndexedEmployee;
import com.example.facedetector.utils.Consts;

import java.io.ByteArrayOutputStream;
import java.util.Map;

public class EmployeePresenter implements EmployeeViewContract.Presenter, MsgListener {

    private NotIndexedEmployee notIndexedEmployee;
    private IndexedEmployee indexedEmployee;

    private EmployeeActivity currentView;
    private NetworkService model = NetworkService.getIntent();

    public EmployeePresenter(EmployeeActivity currentView, Bundle bundle) {
        this.currentView = currentView;
        fillViewFields(bundle);
        indexedEmployee = new IndexedEmployee(
                getEmployeeDataFromView(), bundle.getString(Consts.DATA_TYPE_ID));
        currentView.setActivityMode(bundle.getInt(EmployeeActivity.BUNDLE_MODE_KEY));
    }

    private void fillViewFields(Bundle bundle) {
        currentView.setName(bundle.getString(Consts.DATA_TYPE_NAME));
        currentView.setLastName(bundle.getString(Consts.DATA_TYPE_LAST_NAME));
        currentView.setBirthDate(bundle.getString(Consts.DATA_TYPE_BIRTH));
        currentView.setDepartmentId(bundle.getString(Consts.DATA_TYPE_DEPARTMENT));

        byte[] photo = bundle.getByteArray(Consts.DATA_TYPE_PHOTO);
        if (photo != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(photo, 0, photo.length);
            currentView.setPhoto(bitmap);
        }
    }

    private NotIndexedEmployee getEmployeeDataFromView() {
        return new NotIndexedEmployee(
                currentView.getDepartmentId(),
                currentView.getName(),
                currentView.getLastName(),
                currentView.getBirthDate(),
                convertBitmapToArray(currentView.getPhoto())
        );
    }

    private byte[] convertBitmapToArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    @Override
    public void addEmployee() {
        notIndexedEmployee = getEmployeeDataFromView();
        if (notIndexedEmployee.hasEmptyField()) {
            currentView.displayMsg("Fill all the fields");
            return;
        }
        model.addEmployee(notIndexedEmployee, this);
    }

    @Override
    public void editEmployee() {
        notIndexedEmployee = getEmployeeDataFromView();
        if (notIndexedEmployee.hasEmptyField()) {
            currentView.displayMsg("Fill all the fields");
            return;
        }
        model.editEmployee(new IndexedEmployee(notIndexedEmployee, indexedEmployee.getId()), this);
    }

    @Override
    public void deleteEmployee() {
        String id = indexedEmployee.getId();
        if (id == null) {
            currentView.displayMsg("There's no employee data from server");
            return;
        }
        model.deleteEmployee(id, this);
    }

    @Override
    public void callback(Map<String, byte[]> data) {
        if (data.isEmpty() || !data.containsKey(Consts.DATA_TYPE_CODE)) {
            currentView.displayMsg("Failed receiving msg from server");
            return;
        }
        String code = new String(data.get(Consts.DATA_TYPE_CODE));
        if (code.equals(Consts.CODE_ERROR)) {
            currentView.displayMsg(new String(data.values().iterator().next()));
            return;
        }

        String msgType = data.keySet().iterator().next();
        switch (msgType) {
            case Consts.MSG_TYPE_ADD: {
                String id = new String(data.get(Consts.MSG_TYPE_ADD));
                indexedEmployee = new IndexedEmployee(getEmployeeDataFromView(), id);
                currentView.displayMsg("Added new employee successfully");
                currentView.setActivityMode(EmployeeActivity.ACTIVITY_EDIT_MODE);
                break;
            }
            case  Consts.MSG_TYPE_DELETE: {
                currentView.displayMsg(new String(data.get(Consts.MSG_TYPE_DELETE)));
                currentView.closeActivity();
            }
            case  Consts.MSG_TYPE_EDIT: {
                currentView.displayMsg(new String(data.get(Consts.MSG_TYPE_EDIT)));
            }
        }
    }
}
