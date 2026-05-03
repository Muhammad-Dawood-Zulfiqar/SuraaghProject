package com.example.suraagh_deliverable_1.HelperActivities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.suraagh_deliverable_1.Adapters.ImageSliderAdapter;
import com.example.suraagh_deliverable_1.ModelClasses.LostPost;
import com.example.suraagh_deliverable_1.ModelClasses.Post;
import com.example.suraagh_deliverable_1.ModelClasses.User;
import com.example.suraagh_deliverable_1.R;
import com.example.suraagh_deliverable_1.Utilities.FirebaseManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {

    private static final String TAG = "PostDetail";

    // UI Components
    private View btnBack; // Using View to support ImageButton or Button
    private ViewPager2 viewPagerImages;
    private LinearLayout layoutIndicators;

    // Header Info
    private TextView tvPostType; // Status Badge (Lost/Found)
    private TextView tvThing;    // NEW: Main Title (e.g., "Black Wallet")

    // Spec Chips (Horizontal Row)
    private TextView tvType, tvMaterial, tvColor, tvSize;

    // Content
    private TextView tvLocation, tvAdditionalInfo;
    private TextView tvPostedBy, tvTrustPoints;
    private TextView tvDate;
    private View btnViewOnMap;

    // Adapters
    private ImageSliderAdapter imageSliderAdapter;

    // Data
    private Post post;
    private User postOwner;
    private List<String> imageUrls = new ArrayList<>();

    // Firebase
    private FirebaseFirestore db;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Initialize Firebase
        firebaseManager = FirebaseManager.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupListeners();
        loadPostFromIntent();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        viewPagerImages = findViewById(R.id.viewPagerImages);
        layoutIndicators = findViewById(R.id.layoutIndicators);

        // Status & Title
        tvPostType = findViewById(R.id.tvPostType);
        tvThing = findViewById(R.id.tvThing); // New Field ID from XML

        // Specs
        tvType = findViewById(R.id.tvType);
        tvMaterial = findViewById(R.id.tvMaterial);
        tvColor = findViewById(R.id.tvColor);
        tvSize = findViewById(R.id.tvSize);

        tvDate=findViewById(R.id.tvDate);
        // Details
        tvLocation = findViewById(R.id.tvLocation);
        tvAdditionalInfo = findViewById(R.id.tvAdditionalInfo);
        tvPostedBy = findViewById(R.id.tvPostedBy);
        btnViewOnMap = findViewById(R.id.btnViewOnMap);

        // Setup ViewPager
        imageSliderAdapter = new ImageSliderAdapter(imageUrls);
        viewPagerImages.setAdapter(imageSliderAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        if (btnViewOnMap != null) {
            btnViewOnMap.setOnClickListener(v -> openLocationInMap());
        }

        // ViewPager page change listener for indicators
        viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateIndicators(position);
            }
        });
    }

    private void loadPostFromIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("post")) {
            post = (Post) intent.getSerializableExtra("post");
            if (post != null) {
                displayPostDetails();
                loadUserInformation();
            } else {
                Toast.makeText(this, "Error loading post details", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "No post data found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayPostDetails() {
        // 1. Set Status Badge (Lost/Found)
        boolean isLostPost = post instanceof LostPost;
        tvPostType.setText(isLostPost ? "LOST" : "FOUND");

        // Set Colors using Palette Resources
        int textColorRes = isLostPost ? R.color.status_lost : R.color.status_found;
        tvPostType.setTextColor(ContextCompat.getColor(this, textColorRes));

        // 2. Set Main Title (The Thing)
        if (post.getThing() != null && !post.getThing().isEmpty()) {
            tvThing.setText(post.getThing());
        } else {
            tvThing.setText("Unknown Item");
        }

        // 3. Set Specs (Chips)
        tvType.setText(post.getType() != null ? post.getType() : "N/A");
        tvMaterial.setText(post.getMaterial() != null ? post.getMaterial() : "N/A");
        tvColor.setText(post.getColor() != null ? post.getColor() : "N/A");
        tvSize.setText(post.getSize() != null ? post.getSize() : "N/A");

        // 4. Set Location
        if (post.getAddress() != null && !post.getAddress().isEmpty()) {
            tvLocation.setText(post.getAddress());
            if (btnViewOnMap != null) btnViewOnMap.setVisibility(View.VISIBLE);
        } else {
            tvLocation.setText("Location not specified");
            if (btnViewOnMap != null) btnViewOnMap.setVisibility(View.GONE);
        }

        // 5. Set Description
        if (post.getAdditionalInfo() != null && !post.getAdditionalInfo().isEmpty()) {
            tvAdditionalInfo.setText(post.getAdditionalInfo());
        } else {
            tvAdditionalInfo.setText("No additional details provided.");
        }

        //set date
        if(post.getDate()!=null && post.getDate()!="")
        {
            String date=convertToStandardDate(post.getDate());
            tvDate.setText(date);

        }
        else{
            tvDate.setText("No date was specified");
        }
        // 6. Load Images
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            imageUrls.clear();
            imageUrls.addAll(post.getImageUrl());
            imageSliderAdapter.notifyDataSetChanged();
            setupIndicators();
        } else {
            // If no images, we hide the indicator logic or show a placeholder
            // For now, let's just ensure the list is empty
            imageUrls.clear();
            imageSliderAdapter.notifyDataSetChanged();
            layoutIndicators.setVisibility(View.GONE);
        }
    }

    public String convertToStandardDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) {
            return "N/A";
        }

        // 1. Define the format of the incoming string
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // 2. Define the format you want to output
        // "dd MMMM, yyyy" = 21 November, 2024 (Best for UI)
        // "yyyy-MM-dd"    = 2024-11-21 (Standard ISO format)
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault());

        try {
            Date date = inputFormat.parse(rawDate);
            if (date != null) {
                return outputFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Return the original string if parsing fails
        return rawDate;
    }
    private void loadUserInformation() {
        if (post.getUserId() == null || post.getUserId().isEmpty()) {
            setDefaultUserInfo();
            return;
        }

        db.collection("users").document(post.getUserId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            postOwner = document.toObject(User.class);
                            if (postOwner != null) {
                                displayUserInformation();
                            } else {
                                setDefaultUserInfo();
                            }
                        } else {
                            setDefaultUserInfo();
                        }
                    } else {
                        Log.e(TAG, "Error loading user information: " + task.getException());
                        setDefaultUserInfo();
                    }
                });
    }

    private void displayUserInformation() {
        if (postOwner != null) {
            String userName = postOwner.getUserName() != null ? postOwner.getUserName() : "Suraagh User";
            tvPostedBy.setText(userName);

//            if (postOwner.getTrustPoints() >= 0) {
//                tvTrustPoints.setText("Trust Points: " + postOwner.getTrustPoints());
//            } else {
//                tvTrustPoints.setText("Trust Points: 0");
//            }
        }
    }

    private void setDefaultUserInfo() {
        tvPostedBy.setText("Unknown User");
//        tvTrustPoints.setText("Trust Points: --");
    }

    // --- Indicator Logic (Dots) ---
    private void setupIndicators() {
        layoutIndicators.removeAllViews();

        if (imageUrls.size() <= 1) {
            layoutIndicators.setVisibility(View.GONE);
            return;
        }
        layoutIndicators.setVisibility(View.VISIBLE);

        for (int i = 0; i < imageUrls.size(); i++) {
            ImageView indicator = new ImageView(this);
            // Use specific size for dots
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            indicator.setLayoutParams(params);

            if (i == 0) {
                indicator.setImageResource(R.drawable.indicator_active);
            } else {
                indicator.setImageResource(R.drawable.indicator_inactive);
            }
            layoutIndicators.addView(indicator);
        }
    }

    private void updateIndicators(int position) {
        if (layoutIndicators.getChildCount() == 0) return;

        for (int i = 0; i < layoutIndicators.getChildCount(); i++) {
            ImageView indicator = (ImageView) layoutIndicators.getChildAt(i);
            if (i == position) {
                indicator.setImageResource(R.drawable.indicator_active);
            } else {
                indicator.setImageResource(R.drawable.indicator_inactive);
            }
        }
    }

    private void openLocationInMap() {
        if (post.getLatitude() != 0.0 && post.getLongitude() != 0.0) {
            // Standard Geo URI: geo:lat,lon?q=lat,lon(Label)
            String label = (post.getThing() != null) ? post.getThing() : "Item Location";
            String uriStr = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)",
                    post.getLatitude(), post.getLongitude(),
                    post.getLatitude(), post.getLongitude(),
                    label);

            Uri gmmIntentUri = Uri.parse(uriStr);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

            // Try to find ANY map app
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // Browser Fallback (Corrected URL)
                String browserUri = "https://www.google.com/maps/search/?api=1&query=" +
                        post.getLatitude() + "," + post.getLongitude();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(browserUri));
                startActivity(browserIntent);
            }
        } else {
            Toast.makeText(this, "Coordinates not available", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up
        if (viewPagerImages != null) {
            // No specific unregister needed for anonymous inner class usually,
            // but good practice if you had a named listener reference.
        }
    }
}