package com.example.suraagh_deliverable_1.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.suraagh_deliverable_1.Adapters.PostsAdapter;
import com.example.suraagh_deliverable_1.AddPost;
import com.example.suraagh_deliverable_1.Database.DataBaseGetFoundPosts;
import com.example.suraagh_deliverable_1.Database.DatabaseDeletePost;
import com.example.suraagh_deliverable_1.Database.DatabaseGetLostPosts;
import com.example.suraagh_deliverable_1.Firebase.CallBackDelete;
import com.example.suraagh_deliverable_1.Firebase.CallBackForGetPost;
import com.example.suraagh_deliverable_1.Firebase.FirebaseDeletePost;
import com.example.suraagh_deliverable_1.Firebase.FirebaseGetFoundPosts;
import com.example.suraagh_deliverable_1.Firebase.FirebaseGetLostPosts;
import com.example.suraagh_deliverable_1.ModelClasses.FoundPost;
import com.example.suraagh_deliverable_1.ModelClasses.LostPost;
import com.example.suraagh_deliverable_1.ModelClasses.Post;
import com.example.suraagh_deliverable_1.ModelClasses.SuraaghApplication;
import com.example.suraagh_deliverable_1.R;
import com.example.suraagh_deliverable_1.Utilities.FirebaseManager;
import com.example.suraagh_deliverable_1.Utilities.LocalStorage;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ManagePostsFragment extends Fragment implements PostsAdapter.OnPostActionListener {

    private View emptyStateLayout;
    private LocalStorage localStorage; // New helper
    private SuraaghApplication app;
    private android.widget.ProgressBar progressBar;
    private final String TAG = "GetPosts";
    private RecyclerView postsRecyclerView;
    private FloatingActionButton fabAddPost;
    private PostsAdapter postsAdapter;
    private List<Post> userPosts;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private CollectionReference lostPostsRef;
    private CollectionReference foundPostsRef;

    private DatabaseGetLostPosts dbLostPosts;
    private DataBaseGetFoundPosts dbFoundPosts;

    private DatabaseDeletePost dbDeletePost;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_posts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        app = (SuraaghApplication) requireActivity().getApplication();
        localStorage = new LocalStorage(requireContext()); // Init storage
        initViews(view);
        setupFirebase();

    }

    private void initViews(View view) {
        postsRecyclerView = view.findViewById(R.id.postsRecyclerView);
        fabAddPost = view.findViewById(R.id.fabAddPost);

        userPosts = new ArrayList<>();
        postsAdapter = new PostsAdapter(userPosts, this);

        // CRITICAL FIX: Use the custom wrapper LayoutManager to prevent scroll crashes
        postsRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(getContext()));

        postsRecyclerView.setAdapter(postsAdapter);
        progressBar = view.findViewById(R.id.progressBar);
        fabAddPost.setOnClickListener(v -> navigateToAddPost());
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
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
    private void checkEmptyState() {
        if (userPosts == null || userPosts.isEmpty()) {
            postsRecyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            postsRecyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    private void setupFirebase() {
        db = FirebaseManager.getInstance().db;
        auth = FirebaseManager.getInstance().auth;
        lostPostsRef = db.collection("lostPosts");
        foundPostsRef = db.collection("foundPosts");
        dbLostPosts = new FirebaseGetLostPosts();
        dbFoundPosts = new FirebaseGetFoundPosts();
        dbDeletePost = new FirebaseDeletePost();
    }

    // --- REPLACED LOAD LOGIC START ---
    private void loadUserPosts() {
        if (getContext() == null) return; // Safety Check

        progressBar.setVisibility(View.GONE);

        LocalStorage localStorage = new LocalStorage(requireContext());
        List<Post> lost = localStorage.loadLostPosts();
        List<Post> found = localStorage.loadFoundPosts();

        List<Post> allPosts = new ArrayList<>();
        allPosts.addAll(lost);
        allPosts.addAll(found);

        // --- OPTIMIZATION: Sort by Date (Newest First) ---
        // This ensures the post they just added appears at the VERY TOP
        Collections.sort(allPosts, (p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));

        userPosts.clear();
        userPosts.addAll(allPosts);
        postsAdapter.notifyDataSetChanged();

        checkEmptyState();
    }
    private void loadFoundPostsChained(String currentUserId) {
        // 2. Fetch Found Posts
        dbFoundPosts.getFoundPosts(currentUserId, new CallBackForGetPost() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                // Clear global found list first

                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        FoundPost post = document.toObject(FoundPost.class);
                        post.setPostId(document.getId());
                    }
                } else {
                    Log.e(TAG, "Failed to load found posts");
                }

                // 3. FINAL UI UPDATE
                progressBar.setVisibility(View.GONE);

                // Combine both global lists for THIS fragment's display
                userPosts.clear();

                // Update adapter safely
                postsAdapter.updatePosts(new ArrayList<>(userPosts));
                checkEmptyState();

                if (!userPosts.isEmpty()) {
                    showToast("Loaded " + userPosts.size() + " posts");
                }
            }
        });
    }
    private String getCurrentUserId() {
        if (auth.getCurrentUser() == null) {
            showToast("Please sign in to manage posts");
            return null;
        }
        return auth.getCurrentUser().getUid();
    }

    @Override
    public void onPostClick(Post post) {
//        Intent intent = new Intent(getContext(), PostDetailActivity.class);
//        intent.putExtra("post", post);
//        startActivity(intent);
    }

    @Override
    public void onPostLongClick(Post post) {
        showActionDialog(post);
    }

    private void showActionDialog(Post post) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Post Actions")
                .setMessage("What would you like to do with this post?")
                .setPositiveButton("Update", (dialog, which) -> {
                    updatePost(post);
                })
                .setNegativeButton("Delete", (dialog, which) -> {
                    deletePost(post);
                })
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void updatePost(Post post) {
        Intent intent = new Intent(getContext(), AddPost.class);
        intent.putExtra("post", post);
        startActivity(intent);
    }

    private void deletePost(Post post) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    performDelete(post);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performDelete(Post post) {
        String collectionName = post instanceof LostPost ? "lostPosts" : "foundPosts";

        // 1. DELETE FROM FIREBASE FIRST (Quick UI feedback)
        dbDeletePost.deletePost(collectionName, post.getPostId(), new CallBackDelete() {
            @Override
            public void onSuccess(Void aVoid) {
                // Remove from cache and reload UI
                updateLocalCacheAfterDelete(post);
                loadUserPosts();
                Toast.makeText(getContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show();

                // 2. DELETE IMAGES VIA FREE VERCEL BACKEND
                if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                    for (String url : post.getImageUrl()) {
                        String publicId = getPublicIdFromUrl(url);
                        if (publicId != null) {
                            deleteCloudinaryImageViaAPI(publicId);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Failed to delete post", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteCloudinaryImageViaAPI(String publicId) {
        if (getContext() == null) return;

        // Replace this with your actual Vercel URL
        String url = "https://vercel.com/asdarabic7-3070s-projects/suraagh-backend/api/deleteImage";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("publicId", publicId);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    response -> {
                        Log.d("CloudinaryDelete", "Image deleted successfully: " + publicId);
                    },
                    error -> {
                        Log.e("CloudinaryDelete", "Failed to delete image: " + publicId, error);
                    }
            );

            // Add request to Volley queue
            RequestQueue queue = Volley.newRequestQueue(getContext());
            queue.add(request);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Keep the helper function to extract the ID from the URL
    private String getPublicIdFromUrl(String imageUrl) {
        try {
            if (imageUrl == null || !imageUrl.contains("/upload/")) return null;
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) return null;
            String segment = parts[1];
            segment = segment.replaceAll("^v\\d+/", "");
            int dotIndex = segment.lastIndexOf(".");
            if (dotIndex != -1) {
                segment = segment.substring(0, dotIndex);
            }
            return segment;
        } catch (Exception e) {
            return null;
        }
    }    private void updateLocalCacheAfterDelete(Post postDeleted) {
        if (postDeleted instanceof LostPost) {
            List<Post> currentList = localStorage.loadLostPosts();
            // Remove the item with matching ID
            currentList.removeIf(p -> p.getPostId().equals(postDeleted.getPostId()));
            localStorage.saveLostPosts(currentList);
        } else {
            List<Post> currentList = localStorage.loadFoundPosts();
            currentList.removeIf(p -> p.getPostId().equals(postDeleted.getPostId()));
            localStorage.saveFoundPosts(currentList);
        }
    }
    private void navigateToAddPost() {
        Intent intent = new Intent(getContext(), AddPost.class);
        startActivity(intent);
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserPosts();
    }

    // --- Safety Class to prevent "Inconsistency detected" crash ---
    public static class WrapContentLinearLayoutManager extends LinearLayoutManager {
        public WrapContentLinearLayoutManager(Context context) {
            super(context);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                Log.e("WrapContentManager", "Inconsistency detected in RecyclerView");
            }
        }
    }
}