package com.example.facedetector.ui.home;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.facedetector.R;

public class HomeActivity extends AppCompatActivity implements HomeInterface.View{

    private ImageView imageView;
    private Button addBtn;
    private Button connectionBtn;

    private HomeInterface.Presenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        addBtn = findViewById(R.id.button_add_employee);
        addBtn.setOnClickListener(view -> addBtnClicked());

        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(v -> recognizeBtnClicked());

        connectionBtn = findViewById(R.id.button_connection);
        connectionBtn.setOnClickListener(v -> connectionBtnClicked());

        Button signOutBtn = findViewById(R.id.button_sign_out);
        signOutBtn.setOnClickListener(v -> signOutBtnClicked());

        presenter = new HomePresenter(this);
    }

    private void signOutBtnClicked() {
        presenter.signOut();
    }

    private void connectionBtnClicked() {
        presenter.openConnectionActivity();
    }

    private void addBtnClicked() {
        presenter.addEmployee();
    }

    private void recognizeBtnClicked() {
        presenter.recognizeFace();
    }

    public void startNewActivityForResult(Intent intent, int requestCode) {
        runOnUiThread(()->startActivityForResult(intent, requestCode,
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        presenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void setViewEnabled(boolean value) {
        runOnUiThread(()-> {
            addBtn.setEnabled(value);
            imageView.setEnabled(value);
        });
    }

    @Override
    public void setConnectionStatus(boolean isConnected) {
        runOnUiThread(()->{
            int drawableId = isConnected ? R.drawable.check_mark : R.drawable.x_mark;
            Drawable drawable = getContext().getResources().getDrawable(drawableId, null);
            connectionBtn.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
            connectionBtn.setClickable(!isConnected);
        });
    }

    @Override
    public Context getContext() {
        return this;
    }

    public void displayText(String text) {
        runOnUiThread(() -> {
            //new AlertDialog.Builder(this).setMessage(text).show();
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onBackPressed() {}
}
