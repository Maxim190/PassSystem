package com.example.facedetector.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.facedetector.R;
import com.example.facedetector.presenter.MainPresenter;
import com.example.facedetector.presenter.PhotoManager;
import com.example.facedetector.view.interfaces.MainView;


public class MainActivity extends AppCompatActivity implements MainView {

    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    private TextView statusBarTextView;
    private EditText ipAddrTextView;
    private TextView connectionStatus;
    private EditText msgToServ;
    private Button connectBtn;

    private MainPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        presenter = new MainPresenter(this);

        imageView = findViewById(R.id.imgView);
        statusBarTextView = findViewById(R.id.statusBarTextView);
        ipAddrTextView = findViewById(R.id.ipAddrTextView);
        connectionStatus = findViewById(R.id.connectStatusTextView);
        msgToServ = findViewById(R.id.sendMsgTextView);
        connectBtn = findViewById(R.id.connectBtn);
    }

    public void sendMsgToServerBtnClicked(View view) {
        if (imageView.getDrawable() != null) {
            presenter.recognizeFace(((BitmapDrawable) imageView.getDrawable()).getBitmap());
        } else {
            presenter.sendMsg(msgToServ.getText().toString().getBytes());
        }
    }

    public void connectBtnClicked(View view) {
        presenter.connectToServerToggle();
    }

    public void addEmployeeBtnClicked(View view) {
        Intent intent = new Intent(this.getApplicationContext(), AddEmployee.class);
        startActivity(intent);
    }

    public void recognizeBtnClicked(View view) {
        PhotoManager.requestPhoto(this);
        displayText("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            presenter.recognizeFace(data);
        }
        else {
            displayText("Failed taking photo");
        }
    }

    @Override
    public TextView getIp() {
        return ipAddrTextView;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void displayText(String text) {
        statusBarTextView.setText(text);
    }

    @Override
    public void displayConnectionStatus(Boolean value) {
        runOnUiThread(() -> {
            connectBtn.setText(value? "Disconnect": "Connect");
            connectionStatus.setText(value? "Connected": "Disconnected");
        });
    }


}
