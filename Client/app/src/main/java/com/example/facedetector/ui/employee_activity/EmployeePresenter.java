package com.example.facedetector.ui.employee_activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.example.facedetector.model.MsgListener;
import com.example.facedetector.model.NetworkService;
import com.example.facedetector.model.employee.IndexedEmployee;
import com.example.facedetector.model.employee.NotIndexedEmployee;
import com.example.facedetector.ui.authorization.AuthorizationHandler;
import com.example.facedetector.ui.authorization.AuthorizationPresenter;
import com.example.facedetector.utils.Bundlebuilder;
import com.example.facedetector.utils.Consts;
import com.example.facedetector.utils.JSONManager;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EmployeePresenter implements EmployeeViewContract.Presenter, MsgListener {

    private NotIndexedEmployee notIndexedEmployee;
    private IndexedEmployee indexedEmployee;

    private int activity_access_mode;
    private EmployeeActivity currentView;
    private NetworkService model = NetworkService.getIntent();

    public EmployeePresenter(EmployeeActivity currentView, Bundle bundle) {
        this.currentView = currentView;
        if (bundle != null) {
            setActivityMode(bundle.getInt(Consts.DATA_TYPE_BUNDLE));

            if (activity_access_mode == EmployeeActivity.ACTIVITY_VIEW_MODE) {
                fillViewFields(bundle);
            }
            else {
                model.getAllDepartments(this);
            }
        }
    }

    private void setActivityMode(int mode) {
        activity_access_mode = mode;
        currentView.setActivityMode(activity_access_mode);
    }

    @Override
    public void departmentSelected(String department) {
        if (activity_access_mode != EmployeeActivity.ACTIVITY_VIEW_MODE) {
            model.getDepartmentPositions(department, this);
        }
    }

    private void fillViewFields(Bundle bundle) {
        currentView.setName(bundle.getString(Consts.DATA_TYPE_NAME));
        currentView.setLastName(bundle.getString(Consts.DATA_TYPE_LAST_NAME));
        currentView.setDepartments(Collections.singletonList(bundle.getString(Consts.DATA_TYPE_DEPARTMENT)));
        currentView.setPositions(Collections.singletonList(bundle.getString(Consts.DATA_TYPE_POSITION)));
        indexedEmployee = new IndexedEmployee(
                getEmployeeDataFromView(), bundle.getString(Consts.DATA_TYPE_ID));

        byte[] photo = bundle.getByteArray(Consts.DATA_TYPE_PHOTO);
        if (photo != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(photo, 0, photo.length);
            currentView.setPhoto(bitmap);
        }
    }

    private NotIndexedEmployee getEmployeeDataFromView() {
        return new NotIndexedEmployee(
                currentView.getName(),
                currentView.getLastName(),
                currentView.getDepartment(),
                currentView.getPosition(),
                convertBitmapToArray(currentView.getPhoto())
        );
    }

    private byte[] convertBitmapToArray(Bitmap bitmap) {
        if (bitmap == null) {
            return new byte[0];
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    private boolean isViewer() {
        if (Consts.ACCESS_VIEWER.equals(AuthorizationHandler.getCurrentRightMode())) {
            currentView.displayMsg("You don't have permission");
            return true;
        }
        return false;
    }

    @Override
    public void addEmployee() {
        if (!isViewer()) {
            notIndexedEmployee = getEmployeeDataFromView();
            if (notIndexedEmployee.hasEmptyField()) {
                currentView.displayMsg("Fill all the fields");
                return;
            }
            model.addEmployee(notIndexedEmployee, this);
            currentView.setActivityEnabled(false);
        }
    }

    @Override
    public void editEmployee() {
        if (!isViewer()) {
            notIndexedEmployee = getEmployeeDataFromView();
            if (notIndexedEmployee.hasEmptyField()) {
                currentView.displayMsg("Fill all the fields");
                return;
            }
            model.editEmployee(new IndexedEmployee(notIndexedEmployee, indexedEmployee.getId()), this);
            currentView.setActivityEnabled(false);
        }
    }

    @Override
    public void deleteEmployee() {
        if (!isViewer()) {
            String id = indexedEmployee.getId();
            if (id == null) {
                currentView.displayMsg("There's no employee data from server");
                return;
            }
            model.deleteEmployee(id, this);
            currentView.setActivityEnabled(false);
        }
    }

    @Override
    public void openEditMode() {
        if (!isViewer()) {
            model.getAllDepartments(this);
            setActivityMode(EmployeeActivity.ACTIVITY_EDIT_MODE);
        }
    }

    private List<String> swapFirstElementWith(List<String> list, String swapItem) {
        if (list.contains(swapItem)) {
            int swapItemIndex = list.indexOf(swapItem);
            String buffer = list.get(0);
            list.set(0, swapItem);
            list.set(swapItemIndex, buffer);
        }
        return list;
    }

    private void getCallbackHandler(Map<String, byte[]> data) {
        String type = JSONManager.parseToStr(data.get(Consts.MSG_TYPE_GET));
        List<String> array = new ArrayList<>(
                JSONManager.parseToMap(data.get(Consts.DATA_TYPE_DATA)).values());

        if (Consts.DATA_TYPE_DEPARTMENT.equals(type)) {
            String selectedDepartment = currentView.getDepartment();
            swapFirstElementWith(array, selectedDepartment);
            currentView.setDepartments(array);
        }
        else if (Consts.DATA_TYPE_POSITION.equals(type)){
            String selectedPosition = currentView.getPosition();
            swapFirstElementWith(array, selectedPosition);
            currentView.setPositions(array);
        }
    }

    @Override
    public void callback(Map<String, byte[]> data) {
        currentView.setActivityEnabled(true);

        if (data.isEmpty() || !data.containsKey(Consts.DATA_TYPE_CODE)) {
            currentView.displayMsg("Failed receiving msg from server");
            return;
        }
        String code = JSONManager.parseToStr(data.get(Consts.DATA_TYPE_CODE));
        if (Consts.CODE_ERROR.equals(code)) {
            String errorMsg = new String(data.values().iterator().next());
            if (Consts.MSG_TYPE_AUTHORIZE.equals(data.keySet().iterator().next())) {
                if(AuthorizationPresenter.reSignIn()) {
                    errorMsg = "You have been re-authorized, please try again";
                }
            }
            currentView.displayMsg(errorMsg);
            return;
        }

        String msgType = data.keySet().iterator().next();
        switch (msgType) {
            case Consts.MSG_TYPE_RECOGNIZE: {
                currentView.displayMsg("Employee has already existed");
                fillViewFields(Bundlebuilder.build(data));
                break;
            }
            case Consts.MSG_TYPE_ADD: {
                String id = JSONManager.parseToStr(data.get(Consts.MSG_TYPE_ADD));
                indexedEmployee = new IndexedEmployee(getEmployeeDataFromView(), id);
                currentView.displayMsg("Added new employee successfully");
                break;
            }
            case  Consts.MSG_TYPE_EDIT: {
                currentView.displayMsg(JSONManager.parseToStr(data.get(Consts.MSG_TYPE_EDIT)));
                break;
            }
            case  Consts.MSG_TYPE_DELETE: {
                currentView.displayMsg(JSONManager.parseToStr(data.get(Consts.MSG_TYPE_DELETE)));
                currentView.closeActivity();
                return;
            }
            case Consts.MSG_TYPE_GET: {
                getCallbackHandler(data);
                return;
            }
        }
        setActivityMode(EmployeeActivity.ACTIVITY_VIEW_MODE);
    }
}
