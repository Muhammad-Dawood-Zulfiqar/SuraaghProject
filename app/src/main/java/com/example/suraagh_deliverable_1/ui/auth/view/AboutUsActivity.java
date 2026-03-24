package com.example.suraagh_deliverable_1.ui.auth.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.suraagh_deliverable_1.R;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        // Setup toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("About Suraagh");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}