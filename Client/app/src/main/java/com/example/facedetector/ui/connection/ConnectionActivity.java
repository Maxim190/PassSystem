package com.example.facedetector.ui.connection;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.facedetector.R;

public class ConnectionActivity extends Activity implements ConnectionViewContract.View {

    private ConnectionViewContract.Presenter presenter;
    private EditText ipTextView;
    private TextView log;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection_fragment);

        ipTextView = findViewById(R.id.editText_ip);
        log = findViewById(R.id.textView_log);

        Button connectBtn = findViewById(R.id.button_connect);
        connectBtn.setOnClickListener(i->presenter.connect());

        presenter = new ConnectionPresenter(this);
    }

    @Override
    public String getIp() {
        return ipTextView.getText().toString();
    }

    @Override
    public void displayLog(String msg) {
        runOnUiThread(()->log.setText(msg));
    }

    @Override
    public void closeActivity() {
        runOnUiThread(this::finish);
    }

}
