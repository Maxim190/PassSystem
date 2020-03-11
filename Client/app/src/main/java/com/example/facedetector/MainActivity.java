package com.example.facedetector;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;


public class MainActivity extends AppCompatActivity implements MsgListener {

    public static MsgSender msgSender;

    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    private TextView statusBarTextView;
    private EditText ipAddrTextView;
    private Button connectBtn;
    private Button takePhotoBtn;
    private TextView connectionStatus;

    private MyFaceDetector faceDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imgView);
        statusBarTextView = findViewById(R.id.statusBarTextView);
        ipAddrTextView = findViewById(R.id.ipAddrTextView);
        connectionStatus = findViewById(R.id.connectStatusTextView);

        takePhotoBtn = findViewById(R.id.recognizeBtn);
        takePhotoBtn.setOnClickListener(v -> {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
            statusBarTextView.setText("");
        });

        EditText msgToServ = findViewById(R.id.sendMsgTextView);

        Button sendMsgBtn = findViewById(R.id.sendMsgBtn);
        sendMsgBtn.setOnClickListener(v -> {
            sendMsgToServer(msgToServ.getText().toString());
        });

        connectBtn = findViewById(R.id.connectBtn);
        connectBtn.setOnClickListener((v) -> {
            connectToServer();
        });

        Button addEmployeeBtn = findViewById(R.id.addEmployeeBtn);
        addEmployeeBtn.setOnClickListener((v) -> {
            Intent intent = new Intent(this.getApplicationContext(), AddEmployee.class);
            startActivity(intent);
        });

        faceDetector = new MyFaceDetector(this);
        msgSender = new MsgSender(this);

        checkingConnectionStatus();
    }

    private void checkingConnectionStatus() {
        new Thread(() -> {
            while (true) {
                try {
                    checkServersConnection();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }

    private void sendMsgToServer(String msg) {
        if (imageView.getDrawable() != null) {
            sendPhoto(((BitmapDrawable) imageView.getDrawable()).getBitmap());
        } else {
            msgSender.exchangeMessages(
                    new TreeMap<>(Collections.singletonMap(MsgType.CHECK, msg.getBytes())));
        }
    }

    private void connectToServer() {
        Log.e("PassSystem", String.valueOf(msgSender.isConnected()));
        if (msgSender.isConnected()) {
            msgSender.disconnect();
        } else {
            String host = this.ipAddrTextView.getText().toString();
            Log.i("PassSystem", "Try connect");
            msgSender.connect(host.isEmpty() ? "localhost" : host, 8000);
        }
    }

    private void checkServersConnection() {
        runOnUiThread(() -> {
            String status = msgSender.isConnected() ? "Connected": "Disconnected";
            connectBtn.setText(msgSender.isConnected()? "Disconnect": "Connect");
            connectionStatus.setText(status);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
        {
            Bitmap bmp = (Bitmap) data.getExtras().get("data");
            sendPhoto(bmp);
            imageView.setImageBitmap(bmp);
        }
    }

    private void sendPhoto(Bitmap bmp) {
        if (faceDetector.containsFace(bmp)) {
            Log.e("PassSystem", "YES FACE");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            msgSender.exchangeMessages(
                    new TreeMap<>(Collections.singletonMap(MsgType.RECOGNIZE, byteArray)));
        }
    }

    @Override
    public void receiveMsg(String msg) {
        runOnUiThread(() -> statusBarTextView.setText(msg));
    }
}
