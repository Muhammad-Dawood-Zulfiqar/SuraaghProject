package com.example.suraagh_deliverable_1.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.suraagh_deliverable_1.ModelClasses.User;
import com.example.suraagh_deliverable_1.R;
import com.example.suraagh_deliverable_1.Utilities.FirebaseManager;
import com.example.suraagh_deliverable_1.Utilities.LocalStorage;
import com.example.suraagh_deliverable_1.ui.auth.view.AboutUsActivity;
import com.example.suraagh_deliverable_1.ui.auth.view.EditProfileActivity;
import com.example.suraagh_deliverable_1.ui.auth.view.SignInActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountFragment extends Fragment {

    private TextView tvUserName, tvUserEmail;
    private MaterialButton btnLogout;
    private ProgressBar progressBar;
    private View btnEditProfile, btnAboutUs, btnHelpSupport;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private User currentUser;
    private LocalStorage localStorage;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        localStorage = new LocalStorage(requireContext());

        initializeViews(view);
        setupFirebase();
        setupClickListeners(view);
        loadUserData();

        return view;
    }

    private void initializeViews(View view) {
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        btnLogout = view.findViewById(R.id.btn_logout);
        progressBar = view.findViewById(R.id.progress_bar);

        // Initialize the clickable views
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnAboutUs = view.findViewById(R.id.btn_settings); // We'll use settings button for About Us
        btnHelpSupport = view.findViewById(R.id.btn_help);

        // Set initial loading state
        showLoading(true);
    }

    private void setupFirebase() {
        FirebaseManager firebaseManager = FirebaseManager.getInstance();
        auth = firebaseManager.auth;
        db = firebaseManager.db;
    }

    private void setupClickListeners(View view) {
        btnLogout.setOnClickListener(v -> logoutUser());

        // Edit Profile - Opens EditProfileActivity
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        // About Us - Opens AboutUsActivity
        btnAboutUs.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AboutUsActivity.class);
            startActivity(intent);
        });

        // Help & Support - Shows help dialog
        btnHelpSupport.setOnClickListener(v -> showHelpSupportDialog());
    }

    private void loadUserData() {
        if (auth.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        // 1. FIRST LOAD INSTANTLY FROM LOCAL STORAGE
        User cachedUser = localStorage.getUser();
        if (cachedUser != null) {
            currentUser = cachedUser;
            updateUIWithUserData();
            showLoading(false); // Stop loading animation since we already have data
        } else {
            showLoading(true); // Keep loading if no cache is found
        }

        // 2. FETCH FROM FIREBASE IN BACKGROUND TO SYNC ANY CHANGES
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);

                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            User fetchedUser = document.toObject(User.class);
                            if (fetchedUser != null) {
                                currentUser = fetchedUser;

                                // UPDATE LOCAL STORAGE WITH NEW DATA
                                localStorage.saveUser(currentUser);

                                // UPDATE UI
                                updateUIWithUserData();
                            }
                        }
                    }
                });
    }

    private void updateUIWithUserData() {
        if (currentUser != null && isAdded()) { // isAdded checks if fragment is still attached
            // Set user name
            if (currentUser.getUserName() != null && !currentUser.getUserName().isEmpty()) {
                tvUserName.setText(currentUser.getUserName());
            } else {
                tvUserName.setText("User");
            }

            // Set email
            if (currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()) {
                tvUserEmail.setText(currentUser.getEmail());
            } else {
                tvUserEmail.setText("No email provided");
            }
        }
    }

    private void logoutUser() {
        showLoading(true);

        // Sign out from Firebase
        auth.signOut();

        // Clear Local Storage
        if (localStorage != null) {
            localStorage.clearData();
        }

        // Redirect to SignIn Activity
        redirectToLogin();

        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private void showHelpSupportDialog() {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Help & Support")
                .setMessage("Need help with Suraagh?\n\n" +
                        "📱 How to use:\n" +
                        "• Post lost items with descriptions\n" +
                        "• Post found items you come across\n" +
                        "• Our AI will match lost and found posts\n" +
                        "• Chat with potential matches\n" +
                        "• Earn trust points by helping others\n\n" +
                        "🔧 Support:\n" +
                        "• Email: support@suraagh.com\n" +
                        "• Community Guidelines\n" +
                        "• FAQ Section")
                .setPositiveButton("Community Guidelines", (dialog, which) -> {
                    showCommunityGuidelines();
                })
                .setNeutralButton("FAQ", (dialog, which) -> {
                    showFAQ();
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void showCommunityGuidelines() {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Community Guidelines")
                .setMessage("To keep Suraagh safe and trustworthy:\n\n" +
                        "✅ DO:\n" +
                        "• Post accurate descriptions\n" +
                        "• Upload clear photos\n" +
                        "• Respond to messages promptly\n" +
                        "• Be honest about found items\n" +
                        "• Help others in the community\n\n" +
                        "❌ DON'T:\n" +
                        "• Post false information\n" +
                        "• Harass other users\n" +
                        "• Share personal information\n" +
                        "• Claim items that aren't yours\n" +
                        "• Spam the community")
                .setPositiveButton("Understood", null)
                .show();
    }

    private void showFAQ() {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Frequently Asked Questions")
                .setMessage("🤔 Common Questions:\n\n" +
                        "Q: How does AI matching work?\n" +
                        "A: Our AI analyzes descriptions and images to find similarities between lost and found posts.\n\n" +
                        "Q: What are Trust Points?\n" +
                        "A: Points earned by successfully returning found items to their owners.\n\n" +
                        "Q: What are Report Strikes?\n" +
                        "A: Strikes given for false reports or inappropriate behavior.\n\n" +
                        "Q: Is Suraagh free?\n" +
                        "A: Yes, completely free to help our community!")
                .setPositiveButton("Got it", null)
                .show();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(getActivity(), SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        if (btnLogout != null) {
            btnLogout.setEnabled(!show);
        }
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh user data when fragment becomes visible
        if (auth.getCurrentUser() != null) {
            loadUserData();
        }
    }
}