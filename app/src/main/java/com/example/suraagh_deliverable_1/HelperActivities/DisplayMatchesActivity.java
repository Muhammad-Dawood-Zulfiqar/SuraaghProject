package com.example.suraagh_deliverable_1.HelperActivities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.suraagh_deliverable_1.Adapters.MatchDetailsAdapter;
import com.example.suraagh_deliverable_1.ModelClasses.Match;
import com.example.suraagh_deliverable_1.ModelClasses.Post;
import com.example.suraagh_deliverable_1.R;
import com.example.suraagh_deliverable_1.Utilities.FirebaseManager;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DisplayMatchesActivity extends AppCompatActivity {

    private TextView tvLostItemTitle, tvNoMatches;
    private View btnBack;
    private RecyclerView rvMatches;
    private ProgressBar progressBar;
    private MatchDetailsAdapter adapter;
    private List<MatchDetailsAdapter.MatchItem> matchItemsList;
    private FirebaseFirestore db;
    private String lostPostId; // Add this field
    private int totalMatches = 0;
    private int processedMatches = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_matches);

        // Get Intent Data
        lostPostId = getIntent().getStringExtra("lostPostId"); // Store it as a field
        String lostPostTitle = getIntent().getStringExtra("lostPostTitle");

        initViews(lostPostId); // Pass lostPostId to initViews

        if (lostPostTitle != null) tvLostItemTitle.setText(lostPostTitle);

        if (lostPostId != null) {
            loadMatchesForPost(lostPostId);
        } else {
            Toast.makeText(this, "Error: No Post ID provided", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews(String lostPostId) { // Add parameter
        tvLostItemTitle = findViewById(R.id.tvLostItemTitle);
        tvNoMatches = findViewById(R.id.tvNoMatches);
        rvMatches = findViewById(R.id.rvMatches);
        progressBar = findViewById(R.id.progressBar);
        db = FirebaseManager.getInstance().db;

        matchItemsList = new ArrayList<>();

        // FIX: Add the lostPostId parameter to the constructor
        adapter = new MatchDetailsAdapter(this, matchItemsList, lostPostId);

        rvMatches.setLayoutManager(new LinearLayoutManager(this));
        rvMatches.setAdapter(adapter);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            finish(); // This closes the current activity and goes back to Fragment
        });
    }

    private void checkAllProcessed() {
        if (processedMatches >= totalMatches) {
            progressBar.setVisibility(View.GONE);
            if (matchItemsList.isEmpty()) {
                tvNoMatches.setVisibility(View.VISIBLE);
            } else {
                tvNoMatches.setVisibility(View.GONE);
            }
        }
    }

    private void loadMatchesForPost(String lostPostId) {
        progressBar.setVisibility(View.VISIBLE);
        tvNoMatches.setVisibility(View.GONE);

        db.collection("matches")
                .whereEqualTo("lostPostId", lostPostId)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    if (querySnapshots.isEmpty()) {
                        showEmptyState();
                        return;
                    }

                    // 1. Collect all "Fetch Tasks"
                    List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                    List<Match> matchObjects = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshots) {
                        Match match = doc.toObject(Match.class);
                        if (match != null) {
                            matchObjects.add(match);
                            // Create a task to fetch the Found Post
                            Task<DocumentSnapshot> task = db.collection("foundPosts")
                                    .document(match.getFinderPostId())
                                    .get();
                            tasks.add(task);
                        }
                    }

                    // 2. Wait for ALL tasks to finish
                    Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
                        matchItemsList.clear();

                        // Process results (results order matches tasks order)
                        for (int i = 0; i < results.size(); i++) {
                            DocumentSnapshot doc = (DocumentSnapshot) results.get(i);
                            if (doc.exists()) {
                                Post post = doc.toObject(Post.class);
                                if (post != null) {
                                    float percent = matchObjects.get(i).getPercentageMatch();
                                    matchItemsList.add(new MatchDetailsAdapter.MatchItem(post, percent));
                                }
                            }
                        }

                        // 3. Update UI ONCE
                        progressBar.setVisibility(View.GONE);
                        if (matchItemsList.isEmpty()) {
                            tvNoMatches.setVisibility(View.VISIBLE);
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                    });

                })
                .addOnFailureListener(e -> {
                    Log.e("DisplayMatches", "Error", e);
                    showEmptyState();
                });
    }
    private void fetchFoundPostDetails(String finderPostId, float matchPercent) {
        db.collection("foundPosts").document(finderPostId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Post post = documentSnapshot.toObject(Post.class);
                        if (post != null) {
                            matchItemsList.add(new MatchDetailsAdapter.MatchItem(post, matchPercent));
                            adapter.notifyDataSetChanged();
                        }
                    }
                    processedMatches++;
                    checkAllProcessed();
                })
                .addOnFailureListener(e -> {
                    Log.e("DisplayMatches", "Error loading found post: " + finderPostId, e);
                    processedMatches++;
                    checkAllProcessed();
                });

    }



    private void showEmptyState() {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            if (matchItemsList.isEmpty()) {
                tvNoMatches.setVisibility(View.VISIBLE);
            }
        });
    }
}