package com.example.facedetector.ui.employee_activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.facedetector.R;
import com.example.facedetector.utils.Consts;
import com.example.facedetector.utils.PhotoManager;

import java.util.List;

public class EmployeeActivity extends AppCompatActivity implements EmployeeViewContract.View {

    public static final int ACTIVITY_ADD_MODE = 0;
    public static final int ACTIVITY_EDIT_MODE = 1;
    public static final int ACTIVITY_VIEW_MODE = 2;

    private ImageView imageView;
    private EditText fieldName;
    private EditText fieldLastName;
//    private EditText fieldDepartment;
//    private EditText fieldPosition;
    private Button buttonAddEmployee;
    private Button buttonEditEmployee;
    private Button buttonDeleteEmployee;
    private Button buttonHamburger;
    private Spinner spinnerDepartments;
    private Spinner spinnerPositions;

    private EmployeePresenter presenter;
    private AlertDialog.Builder dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_employee);

        fieldName = findViewById(R.id.nameEditText);
        fieldLastName = findViewById(R.id.lastNameEditText);
//        fieldDepartment = findViewById(R.id.editText_department);
//        fieldPosition = findViewById(R.id.editText_position);

        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(v-> PhotoManager.requestPhoto(this));

        buttonAddEmployee = findViewById(R.id.button_add_employee);
        buttonAddEmployee.setOnClickListener(v-> presenter.addEmployee());

        buttonEditEmployee = findViewById(R.id.button_edit_employee);
        buttonEditEmployee.setOnClickListener(v-> presenter.editEmployee());

        buttonDeleteEmployee = findViewById(R.id.button_delete_employee);
        buttonDeleteEmployee.setOnClickListener(v-> presenter.deleteEmployee());

        buttonHamburger = findViewById(R.id.button_hamburger);
        buttonHamburger.setOnClickListener(v-> presenter.openEditMode());

        spinnerPositions = findViewById(R.id.spinner_positions);
        spinnerDepartments = findViewById(R.id.spinner_departments);
        spinnerDepartments.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView item = (TextView) view;
                presenter.departmentSelected(item.getText().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        presenter = new EmployeePresenter(this, getIntent().getBundleExtra(Consts.DATA_TYPE_BUNDLE));
    }

    public void setActivityMode(int mode) {
        runOnUiThread(()-> {
            boolean isEnabled = true;
            if (mode == ACTIVITY_EDIT_MODE) {
                buttonAddEmployee.setVisibility(View.INVISIBLE);
                buttonEditEmployee.setVisibility(View.VISIBLE);
                buttonDeleteEmployee.setVisibility(View.VISIBLE);
                buttonHamburger.setVisibility(View.GONE);
            }
            else if (mode == ACTIVITY_ADD_MODE){
                buttonAddEmployee.setVisibility(View.VISIBLE);
                buttonEditEmployee.setVisibility(View.INVISIBLE);
                buttonDeleteEmployee.setVisibility(View.INVISIBLE);
                buttonHamburger.setVisibility(View.GONE);
            }
            else {
                buttonAddEmployee.setVisibility(View.INVISIBLE);
                buttonEditEmployee.setVisibility(View.INVISIBLE);
                buttonDeleteEmployee.setVisibility(View.GONE);
                buttonHamburger.setVisibility(View.VISIBLE);
                isEnabled = false;
            }
            fieldName.setEnabled(isEnabled);
            fieldLastName.setEnabled(isEnabled);
            imageView.setClickable(isEnabled);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PhotoManager.CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap bmp = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bmp);
        }
    }

    public void closeActivity(View view) {
        finish();
    }

    @Override
    public String getName() {
        return fieldName.getText().toString();
    }

    @Override
    public String getLastName() {
        return fieldLastName.getText().toString();
    }

    @Override
    public String getDepartment() {
        return (String)spinnerDepartments.getSelectedItem();
    }

    @Override public String getPosition() { return (String)spinnerPositions.getSelectedItem(); }

    @Override
    public Bitmap getPhoto() {
        return ((BitmapDrawable) imageView.getDrawable()).getBitmap();
    }

    @Override
    public void setActivityEnabled(boolean value) {
        runOnUiThread(()-> {
            buttonAddEmployee.setEnabled(value);
            buttonEditEmployee.setEnabled(value);
            buttonDeleteEmployee.setEnabled(value);
        });
    }

    @Override
    public void setName(String name) {
        runOnUiThread(()->fieldName.setText(name));
    }

    @Override
    public void setLastName(String lastName) {
        fieldLastName.setText(lastName);
    }

    @Override
    public void setPhoto(Bitmap bitmap) {
        runOnUiThread(() -> imageView.setImageBitmap(bitmap));
    }

    private ArrayAdapter<String> getAdapter(List<String> data) {
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item_layout, data);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return dataAdapter;
    }

    @Override
    public void setDepartments(List<String> data) {
        runOnUiThread(()->{
            spinnerDepartments.setAdapter(getAdapter(data));
            spinnerDepartments.setClickable(data.size() > 1);
        });
    }

    @Override
    public void setPositions(List<String> data) {
        runOnUiThread(()->{
            spinnerPositions.setAdapter(getAdapter(data));
            spinnerPositions.setClickable(data.size() > 1);
        });
    }

    @Override
    public void displayMsg(String msg) {
        runOnUiThread(()-> {
            dialog = new AlertDialog.Builder(this);
            dialog.setMessage(msg);
            dialog.show();
        });
    }

    @Override
    public void closeActivity() {
        runOnUiThread(this::finish);
    }
}
