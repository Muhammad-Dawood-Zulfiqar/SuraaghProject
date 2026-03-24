package com.example.suraagh_deliverable_1.ui.auth.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.suraagh_deliverable_1.ModelClasses.User;
import com.example.suraagh_deliverable_1.R;
import com.example.suraagh_deliverable_1.Utilities.FirebaseManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail;
    private TextInputLayout tilName;
    private MaterialButton btnSave, btnCancel;
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Set up action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeViews();
        setupFirebase();
        loadUserData();
        setupClickListeners();
    }

    private void initializeViews() {
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        tilName = findViewById(R.id.til_name);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupFirebase() {
        FirebaseManager firebaseManager = FirebaseManager.getInstance();
        auth = firebaseManager.auth;
        db = firebaseManager.db;
    }

    private void loadUserData() {
        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        currentUser = task.getResult().toObject(User.class);
                        if (currentUser != null) {
                            updateUIWithUserData();
                        }
                    }
                });
    }

    private void updateUIWithUserData() {
        if (currentUser != null) {
            // Set user name
            if (currentUser.getUserName() != null) {
                etName.setText(currentUser.getUserName());
            }

            // Set email (read-only)
            if (currentUser.getEmail() != null) {
                etEmail.setText(currentUser.getEmail());
            }
        }
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> saveProfileChanges());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveProfileChanges() {
        String name = etName.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(name)) {
            tilName.setError("Name is required");
            return;
        } else {
            tilName.setError(null);
        }

        showLoading(true);

        // Update user data
        String userId = auth.getCurrentUser().getUid();

        // Create updated user object
        User updatedUser = new User();
        updatedUser.setUserId(userId);
        updatedUser.setUserName(name);
        updatedUser.setEmail(currentUser.getEmail()); // Keep original email
        updatedUser.setTrustPoints(currentUser.getTrustPoints());
        updatedUser.setReportStrikes(currentUser.getReportStrikes());

        db.collection("users").document(userId)
                .set(updatedUser)
                .addOnCompleteListener(task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? ProgressBar.VISIBLE : ProgressBar.GONE);
        btnSave.setEnabled(!show);
        btnCancel.setEnabled(!show);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}