package com.example.facedetector.ui.connection;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.facedetector.R;

public class ConnectionFragment extends Fragment implements ConnectionInterface {

    private ConnectionPresenter presenter;
    private EditText ipTextView;
    private Button connectBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        presenter = new ConnectionPresenter(this);

        View root = inflater.inflate(R.layout.connection_fragment, container, false);

        ipTextView = root.findViewById(R.id.editText_ip);
        connectBtn = root.findViewById(R.id.btn_connection);
        connectBtn.setOnClickListener((view)-> presenter.connectToServerToggle());
        return root;
    }

    @Override
    public String getIp() {
        return ipTextView.getText().toString();
    }

    @Override
    public void displayConnectionStatus(Boolean value) {
        String btnText = value? "Disconnect": "Connect";
        connectBtn.setText(btnText);
    }
}
