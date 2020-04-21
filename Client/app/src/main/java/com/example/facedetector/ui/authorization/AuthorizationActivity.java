package com.example.facedetector.ui.authorization;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.facedetector.R;
import com.example.facedetector.ui.home.HomeActivity;
import com.example.facedetector.utils.Consts;

public class AuthorizationActivity extends AppCompatActivity implements AuthorizationViewContract.View {

    private AuthorizationViewContract.Presenter presenter;
    private EditText fieldLogin;
    private EditText fieldPassword;
    private TextView errorMsgField;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);

        fieldLogin = findViewById(R.id.editText_login);
        fieldPassword = findViewById(R.id.editText_password);
        errorMsgField = findViewById(R.id.textView_error_msg);

        Button signInBtn = findViewById(R.id.button_sign_in);
        signInBtn.setOnClickListener(v-> signInButtonClicked());

        presenter = new AuthorizationPresenter(this);
    }

    public void signInButtonClicked() {
        presenter.signIn();
    }

    public void openMainActivity(Bundle bundle) {
        runOnUiThread(()->{
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra(Consts.DATA_TYPE_BUNDLE, bundle);
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
        runOnUiThread(()-> errorMsgField.setText(msg));
    }

    @Override
    public void onBackPressed() {}
}
