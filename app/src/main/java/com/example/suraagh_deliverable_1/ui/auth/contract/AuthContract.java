package com.example.suraagh.ui.auth.contract;

public interface AuthContract {
    interface BaseView {
        void showLoading();
        void hideLoading();
        void showError(String message);
        void showSuccess(String message);
    }

    interface BaseAuthService {
        void setAuthListener(AuthListener listener);
    }

    interface AuthListener {
        void onSuccess(String message);
        void onFailure(String errorMessage);
    }
}