package com.example.facedetector.ui.fragments.home;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.facedetector.MainActivity;
import com.example.facedetector.R;
import com.example.facedetector.ui.activities.employee_activity.EmployeeActivity;

import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment implements HomeInterface.View{

    private static final int CAMERA_REQUEST = 1888;
    private static final int ACTIVITY_CLOSED = 1000;

    private Button recognizeBtn;
    private Button addBtn;

    private HomeInterface.Presenter presenter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.home_fragment, container, false);
        presenter = new HomePresenter(this);

        addBtn = root.findViewById(R.id.add_employee_btn);
        addBtn.setOnClickListener(this::addBtnClicked);

        recognizeBtn = root.findViewById(R.id.recognize_btn);
        recognizeBtn.setOnClickListener(this::recognizeBtnClicked);

        return root;
    }

    private void addBtnClicked(View view) {
        presenter.addEmployee();
    }

    private void recognizeBtnClicked(View view) {
        //homePresenter.recognizeFace(BitmapFactory.decodeResource(getResources(), R.drawable.image1));
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    private void setBackgroundBlur(boolean value, int delay) {
        if (delay == 0) {
            ((MainActivity) getActivity()).setBlur(value);
        } else {
            new Thread(() -> {
                try {
                    Thread.sleep(delay);
                    ((MainActivity) getActivity()).setBlur(value);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
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
        } else if (requestCode == ACTIVITY_CLOSED) {
            setBackgroundBlur(false, 0);
        }
    }

    @Override
    public void openEmployeeActivity(Bundle bundle) {
        Objects.requireNonNull(getActivity()).runOnUiThread(()->{
            Intent intent = new Intent(getActivity(), EmployeeActivity.class);
            intent.putExtra(EmployeeActivity.BUNDLE_MODE_KEY, bundle);
            startActivityForResult(intent, ACTIVITY_CLOSED,
                    ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            setBackgroundBlur(true, 200);
        });
    }

    @Override
    public void setViewEnabled(boolean value) {
        Objects.requireNonNull(getActivity()).runOnUiThread(()-> {
            addBtn.setEnabled(value);
            recognizeBtn.setEnabled(value);
        });
    }

    public void displayText(String text) {
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            new AlertDialog.Builder(getActivity()).setMessage(text).show();
        });
    }
}
