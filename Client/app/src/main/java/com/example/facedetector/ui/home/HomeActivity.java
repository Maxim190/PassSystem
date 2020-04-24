package com.example.facedetector.ui.home;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.facedetector.R;
import com.example.facedetector.utils.Consts;

public class HomeActivity extends AppCompatActivity implements HomeInterface.View{

    private ImageView imageView;
    private Button addBtn;
    private Button connectionBtn;
    private TextView nameField;
    private TextView positionField;

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

        nameField = findViewById(R.id.textView_name);
        positionField = findViewById(R.id.textView_position);

        Button signOutBtn = findViewById(R.id.button_sign_out);
        signOutBtn.setOnClickListener(v -> signOutBtnClicked());

        presenter = new HomePresenter(this, getIntent().getBundleExtra(Consts.DATA_TYPE_BUNDLE));
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

    public void startNewActivity(Intent intent, Integer resultCode) {
        runOnUiThread(()-> {
            if (resultCode != null) {
                startActivityForResult(intent, resultCode,
                        ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
            }
            else {
                startActivity(intent);
            }
        });
    }

    @Override
    public void setName(String name) {
        runOnUiThread(()->nameField.setText(name));
    }

    @Override
    public void setPosition(String position) {
        runOnUiThread(()->positionField.setText(position));
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
            Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP| Gravity.CENTER, 0, 50);
            toast.show();
        });
    }

    @Override
    public void onBackPressed() {}
}
