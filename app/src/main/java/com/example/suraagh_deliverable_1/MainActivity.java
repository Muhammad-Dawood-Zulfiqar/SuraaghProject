package com.example.suraagh_deliverable_1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.suraagh_deliverable_1.Adapters.ViewPagerAdapter;
import com.example.suraagh_deliverable_1.Utilities.CloudinaryConfig;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigation;
    private ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Cloudinary
        try {
            CloudinaryConfig.initCloudinary(this);
        } catch (Exception e) {
            // Already initialized, ignore.
        }
        fetchAndSaveFCMToken();
        askNotificationPermission();
        viewPager = findViewById(R.id.view_pager);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        setupViewPager();
        setupBottomNavigation();

        // Disable user swiping if you want Strict Tab behavior (Optional)
        // viewPager.setUserInputEnabled(false);
    }
    private void askNotificationPermission() {
        // This is only necessary for Android 13 (API level 33) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // Permission is already granted, we are good to go!
                Log.d("FCM_PERMISSION", "Notification permission already granted.");
            } else {
                // Ask the user for permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void fetchAndSaveFCMToken() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w("FCM_TOKEN", "Fetching FCM registration token failed", task.getException());
                return;
            }

            // Get new FCM registration token
            String token = task.getResult();
            Log.d("FCM_TOKEN", "Token: " + token);

            // Save it to the user's document in Firestore
            FirebaseFirestore.getInstance().collection("users").document(currentUserId)
                    .update("fcmToken", token)
                    .addOnSuccessListener(aVoid -> Log.d("FCM_TOKEN", "Token saved to Firestore"))
                    .addOnFailureListener(e -> {
                        // If update fails, it might be because the document doesn't exist yet,
                        // so we use 'set' with merge option
                        java.util.Map<String, Object> data = new java.util.HashMap<>();
                        data.put("fcmToken", token);
                        FirebaseFirestore.getInstance().collection("users").document(currentUserId)
                                .set(data, com.google.firebase.firestore.SetOptions.merge());
                    });
        });
    }

    private void setupViewPager() {
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        // Sync ViewPager swipes with Bottom Navigation
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateBottomNavigationSelection(position);
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_view_posts) {
                    viewPager.setCurrentItem(0, true);
                    return true;
                }
                else if (itemId == R.id.navigation_chat) {
                    viewPager.setCurrentItem(1, true);
                    return true;
                }
                else if (itemId == R.id.navigation_add) {
                    // 1. Reset the ViewPager to the first fragment (Manage Posts)
                    viewPager.setCurrentItem(0, false);

                    // 2. Visually select the first item in the bottom nav
                    // We need to temporarily remove the listener to avoid a loop,
                    // or just let the ViewPager callback handle it.
                    // However, doing it manually ensures the state is correct before launch.
                    bottomNavigation.getMenu().findItem(R.id.navigation_view_posts).setChecked(true);

                    // 3. Launch the Activity
                    startActivity(new Intent(MainActivity.this, AddPost.class));

                    // 4. Return false so the "Add" icon doesn't stay selected visually
                    return false;
                }
                else if (itemId == R.id.navigation_matches) {
                    // Mapped to Index 2 now
                    viewPager.setCurrentItem(2, true);
                    return true;
                }
                else if (itemId == R.id.navigation_account) {
                    // Mapped to Index 3 now
                    viewPager.setCurrentItem(3, true);
                    return true;
                }
                return false;
            }
        });
    }

    // This handles the reverse direction: When user SWIPES the ViewPager, update the button
    private void updateBottomNavigationSelection(int position) {
        int itemId = -1;
        switch (position) {
            case 0:
                itemId = R.id.navigation_view_posts;
                break;
            case 1:
                itemId = R.id.navigation_chat;
                break;
            case 2:
                // ViewPager Index 2 is now Matches
                itemId = R.id.navigation_matches;
                break;
            case 3:
                // ViewPager Index 3 is now Account
                itemId = R.id.navigation_account;
                break;
        }

        if (itemId != -1 && bottomNavigation.getSelectedItemId() != itemId) {
            // Remove listener temporarily to avoid circular loop if needed,
            // but usually setting checked is safe here.
            bottomNavigation.getMenu().findItem(itemId).setChecked(true);
        }
    }
}