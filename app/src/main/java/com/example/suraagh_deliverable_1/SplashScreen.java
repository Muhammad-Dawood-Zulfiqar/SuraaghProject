package com.example.suraagh;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import androidx.appcompat.app.AppCompatActivity;

import com.example.suraagh.Utilities.FirebaseManager;
import com.example.suraagh.Utilities.LocalStorage; // Import this
import com.example.suraagh.ui.auth.view.SignInActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private static final int SPLASH_DURATION = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fullscreen setup
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        mAuth = FirebaseManager.getInstance().auth;

        playAnimations();

        // --- OPTIMIZATION: Warm up the cache in a background thread ---
        // This forces the OS to read the JSON file from disk into RAM
        // so when MainActivity opens, the data is ready instantly.
        new Thread(() -> {
            LocalStorage storage = new LocalStorage(getApplicationContext());
            storage.loadLostPosts();
            storage.loadFoundPosts();
        }).start();

        // Delay with Safety Check
        new Handler(Looper.getMainLooper()).postDelayed(this::checkUserSession, SPLASH_DURATION);
    }

    private void playAnimations() {
        View logoContainer = findViewById(R.id.logoContainer);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(logoContainer, "alpha", 0f, 1f);
        fadeIn.setDuration(1200);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logoContainer, "scaleX", 0.95f, 1.05f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logoContainer, "scaleY", 0.95f, 1.05f);
        scaleX.setDuration(2500);
        scaleY.setDuration(2500);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(fadeIn, scaleX, scaleY);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.start();
    }

    private void checkUserSession() {
        // PRODUCTION FIX: Check if activity is still valid
        if (isFinishing() || isDestroyed()) return;

        FirebaseUser currentUser = mAuth.getCurrentUser();
        Intent intent;

        if (currentUser != null) {
            intent = new Intent(SplashScreen.this, MainActivity.class);
        } else {
            intent = new Intent(SplashScreen.this, SignInActivity.class);
        }

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}