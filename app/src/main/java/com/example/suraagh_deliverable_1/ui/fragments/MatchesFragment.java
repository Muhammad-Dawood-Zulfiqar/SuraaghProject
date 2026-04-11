package com.example.suraagh_deliverable_1.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.suraagh_deliverable_1.Adapters.LostPostsAdapter;
import com.example.suraagh_deliverable_1.HelperActivities.DisplayMatchesActivity;
import com.example.suraagh_deliverable_1.ModelClasses.Post;
import com.example.suraagh_deliverable_1.R;
import com.example.suraagh_deliverable_1.Utilities.LocalStorage;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MatchesFragment extends Fragment {

    private RecyclerView matchesRecyclerView;
    private TextView tvEmptyState;
    private TextView tvTitle;
    private LostPostsAdapter adapter;
    private List<Post> myLostPostsList;
    private LocalStorage localStorage; // Helper for reading data

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_matches, container, false);

        matchesRecyclerView = view.findViewById(R.id.matchesRecyclerView);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tvTitle = view.findViewById(R.id.tvTitle);

        tvTitle.setText("My Lost Items");

        // Initialize Local Storage
        localStorage = new LocalStorage(requireContext());

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            setupRecyclerView();
            // Data loading happens in onResume
        } else {
            tvEmptyState.setText("Please login to see your items.");
            tvEmptyState.setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Safety Check: If fragment is not attached, do not run logic
        if (getContext() == null) return;

        // Re-init storage here to be safe
        localStorage = new LocalStorage(getContext());

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            loadMyLostPostsFromStorage();
        }
    }

    private void setupRecyclerView() {
        myLostPostsList = new ArrayList<>();

        adapter = new LostPostsAdapter(getContext(), myLostPostsList, post -> {
            Intent intent = new Intent(getContext(), DisplayMatchesActivity.class);
            intent.putExtra("lostPostId", post.getPostId());
            intent.putExtra("lostPostTitle", post.getThing());
            startActivity(intent);
        });

        matchesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        matchesRecyclerView.setAdapter(adapter);
    }

    // --- NEW LOGIC: READ FROM DISK ---
    private void loadMyLostPostsFromStorage() {
        // 1. Get List from SharedPreferences (Instant)
        List<Post> cachedPosts = localStorage.loadLostPosts();

        // 2. Update UI
        if (cachedPosts.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            matchesRecyclerView.setVisibility(View.GONE);
            tvEmptyState.setText("No lost items found.");
        } else {
            tvEmptyState.setVisibility(View.GONE);
            matchesRecyclerView.setVisibility(View.VISIBLE);

            myLostPostsList.clear();
            myLostPostsList.addAll(cachedPosts);
            adapter.notifyDataSetChanged();
        }
    }
}