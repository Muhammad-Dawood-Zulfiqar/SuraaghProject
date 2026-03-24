package com.example.suraagh_deliverable_1.ui.auth.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.suraagh_deliverable_1.ModelClasses.User;
import com.example.suraagh_deliverable_1.R;
import com.example.suraagh_deliverable_1.Utilities.LocalStorage;
import com.example.suraagh_deliverable_1.ui.auth.contract.RegisterContract;
import com.example.suraagh_deliverable_1.ui.auth.presenter.RegisterPresenter;
import com.example.suraagh_deliverable_1.ui.auth.service.FirebaseRegisterService;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity implements RegisterContract.View {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private TextInputLayout tilName, tilEmail, tilPassword, tilConfirmPassword;
    private Button btnRegister;
    private TextView tvSignIn;

    private RegisterPresenter presenter;
    private boolean isFormValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeViews();
        setupPresenter();
        setupClickListeners();
        setupTextWatchers();
    }

    private void initializeViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tilName = findViewById(R.id.tilName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvSignIn = findViewById(R.id.tvSignIn);
    }

    private void setupPresenter() {
        RegisterContract.Service authService = new FirebaseRegisterService();
        presenter = new RegisterPresenter(this, authService);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> attemptRegistration());
        tvSignIn.setOnClickListener(v -> navigateToSignIn());
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

        etName.addTextChangedListener(textWatcher);
        etEmail.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);
        etConfirmPassword.addTextChangedListener(textWatcher);
    }

    private void validateFormInRealTime() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Clear previous errors
        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        boolean isValid = true;

        // Name validation
        if (TextUtils.isEmpty(name)) {
            // Don't show error when empty, just disable button
            isValid = false;
        } else if (name.length() < 2) {
            tilName.setError("Name must be at least 2 characters");
            isValid = false;
        }

        // Email validation
        if (TextUtils.isEmpty(email)) {
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email");
            isValid = false;
        }

        // Password validation
        if (TextUtils.isEmpty(password)) {
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            isValid = false;
        }

        // Confirm password validation
        if (TextUtils.isEmpty(confirmPassword)) {
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }

        isFormValid = isValid;
        btnRegister.setEnabled(isFormValid);
    }

    private void attemptRegistration() {
        if (!isFormValid) {
            showError("Please fix all errors before registering");
            return;
        }

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        User user = new User();
        user.setUserName(name);
        user.setEmail(email);
        user.setTrustPoints(100);
        user.setReportStrikes(0);

        presenter.register(user, password);
    }

    private void navigateToSignIn() {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    @Override
    public void showLoading() {
        runOnUiThread(() -> {
            btnRegister.setEnabled(false);
            btnRegister.setText("Creating Account...");
        });
    }

    @Override
    public void hideLoading() {
        runOnUiThread(() -> {
            btnRegister.setEnabled(true);
            btnRegister.setText("Create Account");
        });
    }

    @Override
    public void showError(String message) {
        runOnUiThread(() -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Registration Failed")
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show();
        });
    }

    @Override
    public void showSuccess(String message) {
        runOnUiThread(() -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Success")
                    .setMessage(message)
                    .setPositiveButton("OK", (dialog, which) -> {
                        if (message.contains("verification")) {
                            showVerificationDialog();
                        }
                    })
                    .show();
        });
    }

    private void showVerificationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Verify Your Email")
                .setMessage("We've sent a verification link to your email. Please verify your email before signing in.")
                .setPositiveButton("OK", (dialog, which) -> navigateToSignIn())
                .setCancelable(false)
                .show();
    }

    @Override
    public void onRegistrationSuccess(User user) {
        new LocalStorage(this).clearData();
        // This will be called from presenter
        showSuccess("Registration successful! Please check your email for verification.");
    }

    @Override
    public void onRegistrationFailure(String error) {
        showError(error);
    }

    @Override
    public void onVerificationSent() {
        // Handled in showSuccess method
    }
}