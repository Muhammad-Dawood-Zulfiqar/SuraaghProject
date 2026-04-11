package com.example.suraagh_deliverable_1.ui.auth.contract;

import com.example.suraagh_deliverable_1.ModelClasses.User;

public interface SignInContract {
    interface View extends AuthContract.BaseView {
        void onSignInSuccess(User user);
        void onSignInFailure(String error);
        void onPasswordResetSent();
    }

    interface Presenter {
        void signIn(String email, String password);
        void resetPassword(String email);
        void validateCredentials(String email, String password);
    }

    interface Service extends AuthContract.BaseAuthService {
        void performSignIn(String email, String password);
        void sendPasswordResetEmail(String email);
    }
}