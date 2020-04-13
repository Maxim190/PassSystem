package com.example.facedetector.ui.activities.employee_activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.facedetector.utils.Consts;
import com.example.facedetector.R;
import com.example.facedetector.utils.PhotoManager;

public class EmployeeActivity extends AppCompatActivity implements EmployeeViewContract.View {

    public static final String BUNDLE_MODE_KEY = "mode";
    public static final int ACTIVITY_ADD_MODE = 0;
    public static final int ACTIVITY_EDIT_MODE = 1;

    private ImageView imageView;
    private EditText fieldName;
    private EditText fieldLastName;
    private EditText fieldBirth;
    private EditText fieldDepartment;
    private Button buttonAddEmployee;
    private Button buttonEditEmployee;
    private Button buttonDeleteEmployee;

    private EmployeePresenter presenter;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_employee);

        imageView = findViewById(R.id.imageView);
        fieldName = findViewById(R.id.nameEditText);
        fieldLastName = findViewById(R.id.lastNameEditText);
        fieldBirth = findViewById(R.id.birthEditText);
        fieldDepartment = findViewById(R.id.departmentEditText);

        buttonAddEmployee = findViewById(R.id.button_add_employee);
        buttonAddEmployee.setOnClickListener(this::addBtnClicked);

        buttonEditEmployee = findViewById(R.id.button_edit_employee);
        buttonEditEmployee.setOnClickListener(this::editBtnClicked);

        buttonDeleteEmployee = findViewById(R.id.button_delete_employee);
        buttonDeleteEmployee.setOnClickListener(this::deleteBtnClicked);

        presenter = new EmployeePresenter(this, getIntent().getBundleExtra(BUNDLE_MODE_KEY));
    }

    public void setActivityMode(int mode) {
        if (mode == ACTIVITY_EDIT_MODE) {
            buttonAddEmployee.setVisibility(View.INVISIBLE);
            buttonEditEmployee.setVisibility(View.VISIBLE);
            buttonDeleteEmployee.setVisibility(View.VISIBLE);
        } else {
            buttonAddEmployee.setVisibility(View.VISIBLE);
            buttonEditEmployee.setVisibility(View.INVISIBLE);
            buttonDeleteEmployee.setVisibility(View.INVISIBLE);
        }
    }

    public void addBtnClicked(View view) {
        presenter.addEmployee();
    }

    public void editBtnClicked(View view) {
        presenter.editEmployee();
    }

    public void deleteBtnClicked(View view) {
        presenter.deleteEmployee();
    }

    public void takePhotoBtnClicked(View view) {
        PhotoManager.requestPhoto(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PhotoManager.CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap bmp = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bmp);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void addEmployeeBtnClicked(View view) {
        presenter.addEmployee();
    }

//    private void resizeLayout() {
//        DisplayMetrics dm = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(dm);
//
//        int width = dm.widthPixels;
//        int height = dm.heightPixels;
//
//        getWindow().setLayout((int)(width * .8), (int)(height * .7));
//
//        WindowManager.LayoutParams params = getWindow().getAttributes();
//        params.gravity = Gravity.CENTER;
//        params.x = 0;
//        params.y = -20;
//        getWindow().setAttributes(params);
//    }

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
    public String getBirthDate() {
        return fieldBirth.getText().toString();
    }

    @Override
    public String getDepartmentId() {
        return fieldDepartment.getText().toString();
    }

    @Override
    public Bitmap getPhoto() {
        return ((BitmapDrawable) imageView.getDrawable()).getBitmap();
    }

    @Override
    public void setName(String name) {
        fieldName.setText(name);
    }

    @Override
    public void setLastName(String lastName) {
        fieldLastName.setText(lastName);
    }

    @Override
    public void setBirthDate(String birthDate) {
        fieldBirth.setText(birthDate);
    }

    @Override
    public void setDepartmentId(String departmentId) {
        fieldDepartment.setText(departmentId);
    }

    @Override
    public void setPhoto(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

    @Override
    public void displayMsg(String msg) {
        runOnUiThread(()->
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        );
    }

    @Override
    public void closeActivity() {
        finish();
    }
}
