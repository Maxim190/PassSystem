package com.example.facedetector.ui.connection;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.facedetector.R;

import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectionActivity extends Activity implements ConnectionViewContract.View {

    private ConnectionViewContract.Presenter presenter;
    private EditText ipField;
    private TextView log;
    private TextView unlockBtnInfField;
    private Button connectBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection_fragment);

        unlockBtnInfField = findViewById(R.id.textView_unlock_btn_inf);
        log = findViewById(R.id.textView_log);
        ipField = findViewById(R.id.editText_ip);

        connectBtn = findViewById(R.id.button_connect);
        setEnableIfLongClick();

        presenter = new ConnectionPresenter(this);
    }

    private void setEnableIfLongClick() {
        AtomicBoolean isEnabled = new AtomicBoolean(false);
        connectBtn.setOnClickListener(v-> {
            if (isEnabled.get()) {
                connectBtnClicked(connectBtn);
            }
        });
        connectBtn.setOnLongClickListener(v-> {
            isEnabled.set(true);
            unlockBtnInfField.setVisibility(View.INVISIBLE);
            v.setBackground(getResources().getDrawable(R.drawable.button_pink_oval, null));
            return true;
        });
    }

    public void connectBtnClicked(View view) {
        presenter.connect();
    }

    @Override
    public String getIp() {
        return ipField.getText().toString();
    }

    @Override
    public void setIp(String ip) {
        ipField.setText(ip);
    }

    @Override
    public void displayLog(String msg) {
        runOnUiThread(()->log.append("\n" + msg));
    }

    @Override
    public void closeActivity() {
        runOnUiThread(this::finish);
    }

}
