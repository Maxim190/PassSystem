package com.example.facedetector.ui.home;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.facedetector.R;
import com.example.facedetector.ui.authorization.AuthorizationActivity;
import com.example.facedetector.ui.employee_activity.EmployeeActivity;

public class HomeActivity extends AppCompatActivity implements HomeInterface.View{

    private static final int CAMERA_REQUEST = 1888;
    private static final int ACTIVITY_CLOSED = 1000;

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

    }

    private void addBtnClicked() {
        presenter.addEmployee();
    }

    private void recognizeBtnClicked() {
        //homePresenter.recognizeFace(BitmapFactory.decodeResource(getResources(), R.drawable.image1));
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {
                presenter.recognizeFace(data);
            } else {
                displayText("Failed taking photo");
            }
        }
    }

    @Override
    public void openEmployeeActivity(Bundle bundle) {
        runOnUiThread(()->{
            Intent intent = new Intent(this, EmployeeActivity.class);
            intent.putExtra(EmployeeActivity.BUNDLE_MODE_KEY, bundle);
            startActivityForResult(intent, ACTIVITY_CLOSED,
                    ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        });
    }

    @Override
    public void openAuthorizationActivity() {
        runOnUiThread(()->{
            Intent intent = new Intent(this, AuthorizationActivity.class);
            startActivity(intent);
            finish();
        });
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
            new AlertDialog.Builder(this).setMessage(text).show();
        });
    }
}
