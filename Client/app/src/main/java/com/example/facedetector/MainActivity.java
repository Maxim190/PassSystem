package com.example.facedetector;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.facedetector.ui.fragments.account.AccountFragment;
import com.example.facedetector.ui.fragments.connection.ConnectionFragment;
import com.example.facedetector.ui.fragments.home.HomeFragment;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;


public class MainActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private BlurView blurView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        fragmentManager = getSupportFragmentManager();

        blurView = findViewById(R.id.blurView);
        blurSetUp();

        openFragment(new HomeFragment());
    }

    private void blurSetUp() {
        float radius = 5f;

        View decorView = getWindow().getDecorView();
        ViewGroup rootView = decorView.findViewById(android.R.id.content);
        Drawable windowBackground = decorView.getBackground();

        blurView.setupWith(rootView)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(new RenderScriptBlur(this))
                .setBlurRadius(radius)
                .setHasFixedTransformationMatrix(true);
        setBlur(false);
    }

    public void setBlur(boolean value) {
        blurView.setBlurEnabled(value);
    }

    private void openFragment(Fragment newFragment) {
        fragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, newFragment)
                .addToBackStack(null)
                .commit();
    }

    public void menuBtnClicked(View view) {
        int btnId = view.getId();
        switch (btnId){
            case R.id.home_btn: openFragment(new HomeFragment()); break;
            case R.id.account_btn: openFragment(new AccountFragment()); break;
            case R.id.connection_btn: openFragment(new ConnectionFragment()); break;
        }
    }

}
