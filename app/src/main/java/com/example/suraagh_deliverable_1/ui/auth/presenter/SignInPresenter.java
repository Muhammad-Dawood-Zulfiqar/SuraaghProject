package com.example.suraagh.ui.auth.presenter;

import com.example.suraagh.ModelClasses.User;
import com.example.suraagh.ui.auth.contract.AuthContract;
import com.example.suraagh.ui.auth.contract.SignInContract;
import com.example.suraagh.Utilities.Validator;

public class SignInPresenter implements SignInContract.Presenter {

    private SignInContract.View view;
    private SignInContract.Service authService;

    public SignInPresenter(SignInContract.View view, SignInContract.Service authService) {
        this.view = view;
        this.authService = authService;
        setupAuthListener();
    }

    private void setupAuthListener() {
        authService.setAuthListener(new AuthContract.AuthListener() {
            @Override
            public void onSuccess(String message) {
                view.hideLoading();

                // Check if this is a password reset success or sign-in success
                if ("PASSWORD_RESET_SENT".equals(message)) {
                    // Handle password reset success
                    view.onPasswordResetSent();
                } else if (message.contains("Sign in successful")) {
                    // Handle sign-in success
                    view.showSuccess(message);
                    User dummyUser = new User();
                    view.onSignInSuccess(dummyUser);
                } else {
                    // For any other success messages
                    view.showSuccess(message);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                view.hideLoading();
                view.showError(errorMessage);

                // Check if it's a sign-in failure or password reset failure
                if (errorMessage.contains("password") || errorMessage.contains("reset")) {
                    // Password reset related error - we don't call onSignInFailure for this
                    // Just showError is enough
                } else {
                    // Sign-in related error
                    view.onSignInFailure(errorMessage);
                }
            }
        });
    }

    @Override
    public void signIn(String email, String password) {
        if (validateCredentialsForSignIn(email, password)) {
            view.showLoading();
            authService.performSignIn(email, password);
        }
    }

    @Override
    public void resetPassword(String email) {
        if (Validator.isValidEmail(email)) {
            view.showLoading();
            authService.sendPasswordResetEmail(email);
        } else {
            view.showError("Please enter a valid email address");
        }
    }

    @Override
    public void validateCredentials(String email, String password) {
        // Real-time validation - just check without showing errors
        if (!Validator.isValidEmail(email) && !email.isEmpty()) {
            return;
        }

        if (!Validator.isValidPassword(password) && !password.isEmpty()) {
            return;
        }
    }

    private boolean validateCredentialsForSignIn(String email, String password) {
        if (!Validator.isValidEmail(email)) {
            view.showError("Please enter a valid email address");
            return false;
        }

        if (!Validator.isValidPassword(password)) {
            view.showError("Password must be at least 6 characters long");
            return false;
        }

        return true;
    }
}