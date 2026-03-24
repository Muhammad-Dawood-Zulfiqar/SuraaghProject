package com.example.suraagh_deliverable_1.ui.auth.service;

import androidx.annotation.NonNull;

import com.example.suraagh_deliverable_1.ModelClasses.User;
import com.example.suraagh_deliverable_1.Utilities.FirebaseManager;
import com.example.suraagh_deliverable_1.ui.auth.contract.AuthContract;
import com.example.suraagh_deliverable_1.ui.auth.contract.RegisterContract;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;

public class FirebaseRegisterService implements RegisterContract.Service {

    private AuthContract.AuthListener authListener;
    private FirebaseManager firebaseManager;

    public FirebaseRegisterService() {
        firebaseManager = FirebaseManager.getInstance();
    }

    @Override
    public void setAuthListener(AuthContract.AuthListener listener) {
        this.authListener = listener;
    }

    @Override
    public void performRegistration(User user, String password) {
        firebaseManager.auth.createUserWithEmailAndPassword(user.getEmail(), password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = firebaseManager.auth.getCurrentUser();
                            if (firebaseUser != null) {
                                user.setUserId(firebaseUser.getUid());
                                saveUserToFirestore(user); // Firestore mein save karo
                            }
                        } else {
                            if (authListener != null) {
                                String error = task.getException() != null ?
                                        task.getException().getMessage() : "Registration failed";
                                authListener.onFailure(error);
                            }
                        }
                    }
                });
    }

    @Override
    public void sendEmailVerification() {
        FirebaseUser user = firebaseManager.auth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                if (authListener != null) {
                                    authListener.onSuccess("Verification email sent. Please verify your email.");
                                }
                            } else {
                                if (authListener != null) {
                                    String error = task.getException() != null ?
                                            task.getException().getMessage() : "Failed to send verification email";
                                    authListener.onFailure(error);
                                }
                            }
                        }
                    });
        } else {
            if (authListener != null) {
                authListener.onFailure("User not found");
            }
        }
    }

    // NAYA METHOD: Firestore mein user save karega
    private void saveUserToFirestore(User user) {
        // "users" collection mein document create karo with userID as document ID
        DocumentReference userDocRef = firebaseManager.db.collection("users").document(user.getUserId());

        userDocRef.set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Firestore mein successfully save hone ke baad verification email bhejo
                            sendEmailVerification();
                        } else {
                            if (authListener != null) {
                                String error = task.getException() != null ?
                                        task.getException().getMessage() : "Failed to save user data to Firestore";
                                authListener.onFailure(error);
                            }
                        }
                    }
                });
    }
}