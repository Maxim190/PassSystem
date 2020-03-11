package com.example.facedetector;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class AddEmployee extends AppCompatActivity {

    ImageView imageView;
    EditText nameEditText;
    EditText lastNameEditText;
    EditText birthEditText;
    EditText departmentEditText;

    boolean isDefaultImg = false;//true;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_employee);

        resizeLayout();

        imageView = findViewById(R.id.imageView);
        nameEditText = findViewById(R.id.nameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        birthEditText = findViewById(R.id.birthEditText);
        departmentEditText = findViewById(R.id.departmentEditText);

        Button takePhoto = findViewById(R.id.takePhotoBtn);
        takePhoto.setOnClickListener((v)->{
            PhotoManager.requestPhoto(this);
        });

        Button addEmployee = findViewById(R.id.addEmployeeBtn);
        addEmployee.setOnClickListener((v)->{
            addEmployeeBtnClicked();
        });

        fillFields();
    }

    private void fillFields() {
        nameEditText.setText("Maxim");
        lastNameEditText.setText("Sachkov");
        birthEditText.setText("1998-01-01");
        departmentEditText.setText("1");
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


    private byte[] getEmployeePhoto() {
        if (isDefaultImg) {
            return new byte[0];
        }
        Bitmap bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    private Map<String, String> getEmployeeData() {
        return new LinkedHashMap<String, String>(){{
            put(Consts.DATA_TYPE_NAME, nameEditText.getText().toString());
            put(Consts.DATA_TYPE_LASTNAME, lastNameEditText.getText().toString());
            put(Consts.DATA_TYPE_BIRTH, birthEditText.getText().toString());
            put(Consts.DATA_TYPE_DEPARTMENT, departmentEditText.getText().toString());
        }};
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void addEmployeeBtnClicked() {
        Map<String, String> employeeData = getEmployeeData();
        byte[] employeePhoto = getEmployeePhoto();
        boolean existsEmptyField = employeeData.entrySet().stream().anyMatch(entry -> entry.getValue().isEmpty());
        if(existsEmptyField && employeePhoto.length == 0) {
            Toast.makeText(getApplicationContext(),
                    "Not all data is present", Toast.LENGTH_SHORT).show();
            return;
        }
        TreeMap<MsgType, byte[]> msg = new TreeMap<>();
        msg.put(MsgType.ADD, employeeData.toString().getBytes());
        msg.put(MsgType.ADDITIONAL_PHOTO, getEmployeePhoto());
        MainActivity.msgSender.exchangeMessages(msg);
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
}
