package com.example.facedetector.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.facedetector.R;
import com.example.facedetector.presenter.AddEmployeePresenter;
import com.example.facedetector.presenter.PhotoManager;
import com.example.facedetector.view.interfaces.AddEmployeeView;

public class AddEmployee extends AppCompatActivity implements AddEmployeeView {

    ImageView imageView;
    EditText nameEditText;
    EditText lastNameEditText;
    EditText birthEditText;
    EditText departmentEditText;

    boolean isDefaultImg = false;//true;

    private AddEmployeePresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_employee);

        resizeLayout();
        presenter = new AddEmployeePresenter(this);

        imageView = findViewById(R.id.imageView);
        nameEditText = findViewById(R.id.nameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        birthEditText = findViewById(R.id.birthEditText);
        departmentEditText = findViewById(R.id.departmentEditText);

        fillFields();
    }

    private void fillFields() {
        nameEditText.setText("Maxim");
        lastNameEditText.setText("Sachkov");
        birthEditText.setText("1998-01-01");
        departmentEditText.setText("1");
    }

    public void onTakePhotoBtnClicked(View view) {
        PhotoManager.requestPhoto(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PhotoManager.CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
        {
            Bitmap bmp = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bmp);
            isDefaultImg = false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void addEmployeeBtnClicked(View view) {
        presenter.addEmployee();
    }

    private void resizeLayout() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width * .8), (int)(height * .7));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;
        getWindow().setAttributes(params);
    }

    @Override
    public String getName() {
        return nameEditText.getText().toString();
    }

    @Override
    public String getLastName() {
        return lastNameEditText.getText().toString();
    }

    @Override
    public String getBirthDate() {
        return birthEditText.getText().toString();
    }

    @Override
    public String getDepartmentId() {
        return departmentEditText.getText().toString();
    }

    @Override
    public Bitmap getPhoto() {
        return ((BitmapDrawable) imageView.getDrawable()).getBitmap();
    }

    @Override
    public void displayMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
