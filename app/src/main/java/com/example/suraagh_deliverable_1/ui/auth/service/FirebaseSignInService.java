package com.example.suraagh.ui.auth.service;

import com.example.suraagh.ModelClasses.User;
import com.example.suraagh.ui.auth.contract.AuthContract;
import com.example.suraagh.ui.auth.contract.SignInContract;
import com.example.suraagh.Utilities.FirebaseManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.DocumentSnapshot;

import androidx.annotation.NonNull;

public class FirebaseSignInService implements SignInContract.Service {

    private AuthContract.AuthListener authListener;
    private FirebaseManager firebaseManager;

    public FirebaseSignInService() {
        firebaseManager = FirebaseManager.getInstance();
    }

    @Override
    public void setAuthListener(AuthContract.AuthListener listener) {
        this.authListener = listener;
    }

    @Override
    public void performSignIn(String email, String password) {
        firebaseManager.auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (firebaseManager.auth.getCurrentUser() != null &&
                                    firebaseManager.auth.getCurrentUser().isEmailVerified()) {
                                fetchUserDataFromFirestore();
                            } else {
                                if (authListener != null) {
                                    authListener.onFailure("Please verify your email before signing in");
                                }
                            }
                        } else {
                            if (authListener != null) {
                                String error = task.getException() != null ?
                                        task.getException().getMessage() : "Sign in failed";
                                authListener.onFailure(error);
                            }
                        }
                    }
                });
    }

    @Override
    public void sendPasswordResetEmail(String email) {
        firebaseManager.auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if (authListener != null) {
                                // Send special message for password reset success
                                authListener.onSuccess("PASSWORD_RESET_SENT");
                            }
                        } else {
                            if (authListener != null) {
                                String error = task.getException() != null ?
                                        task.getException().getMessage() : "Failed to send password reset email";
                                authListener.onFailure("Password reset failed: " + error);
                            }
                        }
                    }
                });
    }

    private void fetchUserDataFromFirestore() {
        String userId = firebaseManager.auth.getCurrentUser().getUid();

        firebaseManager.db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                User user = document.toObject(User.class);
                                if (authListener != null) {
                                    authListener.onSuccess("Sign in successful");
                                }
                            } else {
                                if (authListener != null) {
                                    authListener.onFailure("User data not found in database");
                                }
                            }
                        } else {
                            if (authListener != null) {
                                authListener.onFailure("Failed to fetch user data: " +
                                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                            }
                        }
                    }
                });
    }
}