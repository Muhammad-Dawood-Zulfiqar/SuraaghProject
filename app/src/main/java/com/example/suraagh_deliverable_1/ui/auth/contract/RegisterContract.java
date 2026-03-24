package com.example.suraagh_deliverable_1.ui.auth.contract;

import com.example.suraagh_deliverable_1.ModelClasses.User;

public interface RegisterContract {
    interface View extends AuthContract.BaseView {
        void onRegistrationSuccess(User user);
        void onRegistrationFailure(String error);
        void onVerificationSent();
    }

    interface Presenter {
        void register(User user, String password);
        void validateRegistrationData(User user, String password, String confirmPassword);
        void sendVerificationEmail();
    }

    interface Service extends AuthContract.BaseAuthService {
        void performRegistration(User user, String password);
        void sendEmailVerification();
    }
}