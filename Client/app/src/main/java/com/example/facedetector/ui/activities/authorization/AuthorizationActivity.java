package com.example.facedetector.ui.activities.authorization;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.facedetector.MainActivity;
import com.example.facedetector.R;
import com.example.facedetector.model.NetworkService;

public class AuthorizationActivity extends AppCompatActivity implements AuthorizationViewContract.View {

    private AuthorizationViewContract.Presenter presenter;
    private EditText fieldLogin;
    private EditText fieldPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);

        presenter = new AuthorizationPresenter(this);

        fieldLogin = findViewById(R.id.editText_login);
        fieldPassword = findViewById(R.id.editText_password);

        Button signInBtn = findViewById(R.id.button_sign_in);
        signInBtn.setOnClickListener(this::signInButtonClicked);
    }

    public void signInButtonClicked(View view) {
        presenter.signIn();
    }

    public void openMainActivity() {
        runOnUiThread(()->{
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public String getLogin() {
        return fieldLogin.getText().toString();
    }

    @Override
    public String getPassword() {
        return fieldPassword.getText().toString();
    }

    @Override
    public void displayMsg(String msg) {
        runOnUiThread(()-> new AlertDialog.Builder(this).setMessage(msg).show());
    }
}
