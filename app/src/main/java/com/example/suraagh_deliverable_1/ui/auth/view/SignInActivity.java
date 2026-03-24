package com.example.suraagh.ui.auth.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.suraagh.Database.DataBaseGetFoundPosts;
import com.example.suraagh.Database.DatabaseGetLostPosts;
import com.example.suraagh.Firebase.CallBackForGetPost;
import com.example.suraagh.Firebase.FirebaseGetFoundPosts;
import com.example.suraagh.Firebase.FirebaseGetLostPosts;
import com.example.suraagh.MainActivity;
import com.example.suraagh.ModelClasses.FoundPost;
import com.example.suraagh.ModelClasses.LostPost;
import com.example.suraagh.ModelClasses.Post;
import com.example.suraagh.R;
import com.example.suraagh.ModelClasses.User;
import com.example.suraagh.Utilities.LocalStorage;
import com.example.suraagh.ui.auth.contract.SignInContract;
import com.example.suraagh.ui.auth.presenter.SignInPresenter;
import com.example.suraagh.ui.auth.service.FirebaseSignInService;
import com.example.suraagh.Utilities.FirebaseManager;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SignInActivity extends AppCompatActivity implements SignInContract.View {

    private static final String TAG = "SignInDebug"; // Filter Logcat by this tag

    private EditText etEmail, etPassword;
    private TextInputLayout tilEmail, tilPassword;
    private Button btnSignIn;
    private TextView tvForgotPassword, tvRegister;

    private SignInPresenter presenter;
    private boolean isFormValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        initializeViews();
        setupPresenter();
        setupClickListeners();
        setupTextWatchers();

        checkCurrentUser();
    }

    private void checkCurrentUser() {
        if (FirebaseManager.getInstance().auth.getCurrentUser() != null &&
                FirebaseManager.getInstance().auth.getCurrentUser().isEmailVerified()) {

            Log.d(TAG, "User already logged in. Starting Data Sync...");

            // FIX: Don't just navigate, SYNC DATA first!
            User currentUser = new User();
            currentUser.setUserId(FirebaseManager.getInstance().auth.getCurrentUser().getUid());
            onSignInSuccess(currentUser);
        }
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);
    }

    private void setupPresenter() {
        SignInContract.Service authService = new FirebaseSignInService();
        presenter = new SignInPresenter(this, authService);
    }

    private void setupClickListeners() {
        btnSignIn.setOnClickListener(v -> attemptSignIn());
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        tvRegister.setOnClickListener(v -> navigateToRegister());
    }

    private void setupTextWatchers() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                validateFormInRealTime();
            }
        };

        etEmail.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);
    }

    private void validateFormInRealTime() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        tilEmail.setError(null);
        tilPassword.setError(null);

        boolean isValid = true;

        if (TextUtils.isEmpty(email)) {
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            isValid = false;
        }

        isFormValid = isValid;
        btnSignIn.setEnabled(isFormValid);
    }

    private void attemptSignIn() {
        if (!isFormValid) {
            showError("Please fix all errors before signing in");
            return;
        }
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        Log.d(TAG, "Attempting Sign In for: " + email);
        presenter.signIn(email, password);
    }

    private void showForgotPasswordDialog() {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
        dialogBuilder.setTitle("Reset Password");
        dialogBuilder.setMessage("Enter your registered email to receive reset instructions");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);
        TextInputLayout textInputLayout = dialogView.findViewById(R.id.tilForgotPasswordEmail);
        EditText input = dialogView.findViewById(R.id.etForgotPasswordEmail);

        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton("Send Reset Link", null);
        dialogBuilder.setNegativeButton("Cancel", null);

        androidx.appcompat.app.AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        Button positiveButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
        positiveButton.setEnabled(false);

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String email = s.toString().trim();
                boolean isValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
                positiveButton.setEnabled(isValid);

                if (!isValid && !email.isEmpty()) {
                    textInputLayout.setError("Please enter a valid email");
                } else {
                    textInputLayout.setError(null);
                }
            }
        });

        positiveButton.setOnClickListener(v -> {
            String email = input.getText().toString().trim();
            if (!TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                presenter.resetPassword(email);
                dialog.dismiss();
            }
        });
    }

    private void navigateToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    private void navigateToMainActivity() {
        Log.d(TAG, "Navigating to MainActivity");
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    public void showLoading() {
        runOnUiThread(() -> {
            btnSignIn.setEnabled(false);
            btnSignIn.setText("Signing In...");
        });
    }

    @Override
    public void hideLoading() {
        runOnUiThread(() -> {
            btnSignIn.setEnabled(true);
            btnSignIn.setText("Sign In");
        });
    }

    @Override
    public void showError(String message) {
        Log.e(TAG, "Error Occurred: " + message);
        runOnUiThread(() -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Error")
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show();
        });
    }

    @Override
    public void showSuccess(String message) {
        Log.d(TAG, "Show Success: " + message);
        new MaterialAlertDialogBuilder(this)
                .setTitle("Success")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    if (message.contains("Sign in successful")) {
                        navigateToMainActivity();
                    }
                })
                .show();
    }

    // --- KEY FIXES HERE ---

    @Override
    public void onSignInSuccess(User user) {
        // 1. ROBUST ID CHECK
        // If the custom User object returns null, we grab the ID directly from Firebase.
        String currentUserId = user.getUserId();

        if (currentUserId == null) {
            if (FirebaseManager.getInstance().auth.getCurrentUser() != null) {
                currentUserId = FirebaseManager.getInstance().auth.getCurrentUser().getUid();
            } else {
                showError("Critical Error: Authentication succeeded but User ID is missing.");
                return;
            }
        }

        // Final variable for use inside lambda
        String finalUserId = currentUserId;

        Log.d(TAG, "Auth Successful. REAL User ID: " + finalUserId);

        runOnUiThread(() -> {
            // Show Loading Dialog
            MaterialAlertDialogBuilder loadingDialog = new MaterialAlertDialogBuilder(this)
                    .setTitle("Syncing Data")
                    .setMessage("Please wait while we load your posts...")
                    .setCancelable(false);

            androidx.appcompat.app.AlertDialog dialog = loadingDialog.show();

            // Pass the CORRECT ID to the fetch method
            fetchAndSaveUserData(finalUserId, dialog);
        });
    }
    private void fetchAndSaveUserData(String userId, androidx.appcompat.app.AlertDialog dialog) {
        Log.d(TAG, "Starting Data Fetch for User: " + userId);

        DatabaseGetLostPosts dbLost = new FirebaseGetLostPosts();
        DataBaseGetFoundPosts dbFound = new FirebaseGetFoundPosts();
        LocalStorage storage = new LocalStorage(this);
        List<Post> tempLost = new ArrayList<>();
        List<Post> tempFound = new ArrayList<>();

        // 0. Get and Save User Profile Data First!
        FirebaseManager.getInstance().db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            storage.saveUser(user);
                            Log.d(TAG, "User Profile Saved to LocalStorage.");
                        }
                    }
                });

        // 1. Get Lost Posts
        dbLost.getLostPosts(userId, new CallBackForGetPost() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    Log.d(TAG, "Lost Posts Fetched. Count: " + task.getResult().size());
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        LostPost p = doc.toObject(LostPost.class);
                        p.setPostId(doc.getId());
                        tempLost.add(p);
                    }
                    storage.saveLostPosts(tempLost);
                    Log.d(TAG, "Lost Posts Saved to LocalStorage.");
                } else {
                    Log.e(TAG, "Failed to fetch Lost posts: " + (task.getException() != null ? task.getException().getMessage() : "Unknown Error"));
                }

                // 2. Get Found Posts (Always run this, even if Lost fails)
                Log.d(TAG, "Fetching Found Posts...");
                dbFound.getFoundPosts(userId, new CallBackForGetPost() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Log.d(TAG, "Found Posts Fetched. Count: " + task.getResult().size());
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                FoundPost p = doc.toObject(FoundPost.class);
                                p.setPostId(doc.getId());
                                tempFound.add(p);
                            }
                            storage.saveFoundPosts(tempFound);
                            Log.d(TAG, "Found Posts Saved to LocalStorage.");
                        } else {
                            Log.e(TAG, "Failed to fetch Found posts: " + (task.getException() != null ? task.getException().getMessage() : "Unknown Error"));
                        }

                        // 3. DONE - Clean up and Exit
                        Log.d(TAG, "Data Sync Complete. Dismissing Dialog.");
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }

                        // Navigate
                        navigateToMainActivity();
                    }
                });
            }
        });
    }
    @Override
    public void onSignInFailure(String error) {
        Log.e(TAG, "Sign In Failure: " + error);
        showError(error);
    }

    @Override
    public void onPasswordResetSent() {
        runOnUiThread(() -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Success")
                    .setMessage("Password reset instructions have been sent to your email.")
                    .setPositiveButton("OK", null)
                    .show();
        });
    }
}