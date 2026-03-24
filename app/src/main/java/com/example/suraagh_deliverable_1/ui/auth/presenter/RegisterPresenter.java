package com.example.suraagh_deliverable_1.ui.auth.presenter;

import com.example.suraagh_deliverable_1.ModelClasses.User;
import com.example.suraagh_deliverable_1.Utilities.Validator;
import com.example.suraagh_deliverable_1.ui.auth.contract.AuthContract;
import com.example.suraagh_deliverable_1.ui.auth.contract.RegisterContract;

public class RegisterPresenter implements RegisterContract.Presenter {

    private RegisterContract.View view;
    private RegisterContract.Service authService;

    public RegisterPresenter(RegisterContract.View view, RegisterContract.Service authService) {
        this.view = view;
        this.authService = authService;
        setupAuthListener();
    }

    private void setupAuthListener() {
        authService.setAuthListener(new AuthContract.AuthListener() {
            @Override
            public void onSuccess(String message) {
                view.hideLoading();
                view.showSuccess(message);

                if (message.contains("Verification email sent")) {
                    view.onVerificationSent();
                    User dummyUser = new User();
                    view.onRegistrationSuccess(dummyUser);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                view.hideLoading();
                view.showError(errorMessage);
                view.onRegistrationFailure(errorMessage);
            }
        });
    }

    @Override
    public void register(User user, String password) {
        if (validateUserData(user, password, password)) {
            view.showLoading();
            authService.performRegistration(user, password);
        }
    }

    @Override
    public void validateRegistrationData(User user, String password, String confirmPassword) {
        if (!Validator.isValidEmail(user.getEmail())) {
            view.showError("Please enter a valid email address");
            return;
        }

        if (!Validator.isValidPassword(password)) {
            view.showError("Password must be at least 6 characters long");
            return;
        }

        if (!password.equals(confirmPassword)) {
            view.showError("Passwords do not match");
            return;
        }

        if (user.getUserName() == null || user.getUserName().trim().isEmpty()) {
            view.showError("Please enter your name");
            return;
        }
    }

    @Override
    public void sendVerificationEmail() {
        view.showLoading();
        authService.sendEmailVerification();
    }

    private boolean validateUserData(User user, String password, String confirmPassword) {
        if (!Validator.isValidEmail(user.getEmail())) {
            view.showError("Please enter a valid email address");
            return false;
        }

        if (!Validator.isValidPassword(password)) {
            view.showError("Password must be at least 6 characters long");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            view.showError("Passwords do not match");
            return false;
        }

        if (user.getUserName() == null || user.getUserName().trim().isEmpty()) {
            view.showError("Please enter your name");
            return false;
        }

        return true;
    }
}