package com.example.suraagh_deliverable_1.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.suraagh_deliverable_1.Database.ChatCallbacks;
import com.example.suraagh_deliverable_1.HelperActivities.PostDetailActivity;
import com.example.suraagh_deliverable_1.ModelClasses.Chat;
import com.example.suraagh_deliverable_1.ModelClasses.Post;
import com.example.suraagh_deliverable_1.R;
import com.example.suraagh_deliverable_1.Utilities.FirebaseManager;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ChatsAsFinderFragment extends Fragment implements ChatListAdapter.OnChatClickListener {

    private RecyclerView rvChats;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private View layoutEmptyState;

    private ChatListAdapter chatListAdapter;
    private ChatRepository chatRepository;
    FirebaseManager manager;
    private FirebaseFirestore firestore;
    private ValueEventListener chatsListener;
    private String currentUserId;

    public ChatsAsFinderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        initializeRepository();
        setupRecyclerView();
        setupSwipeRefresh();
        loadChats();
    }

    private void initializeViews(View view) {
        rvChats = view.findViewById(R.id.rvChats);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
    }

    private void initializeRepository() {
        chatRepository = ChatRepository.getInstance();
        manager = FirebaseManager.getInstance();
        firestore = manager.db;
        currentUserId = chatRepository.getCurrentUserId();
    }

    private void setupRecyclerView() {
        chatListAdapter = new ChatListAdapter(currentUserId, true); // true = finder view
        chatListAdapter.setOnChatClickListener(this);

        rvChats.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChats.setAdapter(chatListAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorSecondary
        );
        swipeRefreshLayout.setOnRefreshListener(this::loadChats);
    }

    private void loadChats() {
        if (currentUserId == null) {
            showEmptyState("Please sign in to view chats");
            return;
        }

        showLoading();

        // Remove existing listener if any
        if (chatsListener != null) {
            chatRepository.removeChatsListener(currentUserId, "finderId", chatsListener);
        }

        // Add new listener for real-time updates
        chatsListener = chatRepository.getChatsAsFinder(currentUserId, new ChatCallbacks.ChatsListener() {
            @Override
            public void onChatsUpdated(List<Chat> chats) {
                hideLoading();
                swipeRefreshLayout.setRefreshing(false);

                if (chats.isEmpty()) {
                    showEmptyState("No chats yet.\nStart chatting by contacting post owners!");
                } else {
                    hideEmptyState();
                    chatListAdapter.updateChats(chats);
                }
            }

            @Override
            public void onError(String error) {
                hideLoading();
                swipeRefreshLayout.setRefreshing(false);
                showEmptyState("Error loading chats: " + error);
            }
        });
    }

    @Override
    public void onChatClick(Chat chat) {
        // Open ChatDetailActivity for Finder → Owner communication
        Intent intent = new Intent(getContext(), ChatDetailActivity.class);
        intent.putExtra(ChatDetailActivity.EXTRA_CHAT_ID, chat.getChatId());
        intent.putExtra(ChatDetailActivity.EXTRA_OTHER_USER_ID, chat.getOwnerId());
        intent.putExtra(ChatDetailActivity.EXTRA_OTHER_USER_NAME, chat.getOwnerName());
        // Use chat.getPostIdForUser to get correct post ID for finder
        intent.putExtra(ChatDetailActivity.EXTRA_POST_ID, chat.getPostIdForUser(currentUserId));
        intent.putExtra(ChatDetailActivity.EXTRA_IS_USER_OWNER, false); // User is finder
        startActivity(intent);
    }

    @Override
    public void onPostCardClick(Chat chat) {
        // Finder should see LOST posts (owner lost something, finder may have found it)
        String postId = chat.getLostPostId(); // Directly get lost post ID
        if (postId != null && !postId.isEmpty()) {
            loadAndShowLostPost(postId);
        } else {
            Toast.makeText(getContext(), "Post not available", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Load LOST post from Firestore
     */
    private void loadAndShowLostPost(String postId) {
        if (postId == null || postId.isEmpty()) {
            Toast.makeText(getContext(), "Post not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        Toast.makeText(getContext(), "Loading lost item...", Toast.LENGTH_SHORT).show();

        // Load from LOST posts collection
        firestore.collection("lostPosts").document(postId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Post post = documentSnapshot.toObject(Post.class);
                        if (post != null) {
                            openPostDetail(post);
                        } else {
                            Toast.makeText(getContext(), "Lost post not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Lost post not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading post: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void openPostDetail(Post post) {
        Intent intent = new Intent(getContext(), PostDetailActivity.class);
        intent.putExtra("post", post);
        startActivity(intent);
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        rvChats.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        rvChats.setVisibility(View.VISIBLE);
    }

    private void showEmptyState(String message) {
        tvEmptyState.setText(message);
        layoutEmptyState.setVisibility(View.VISIBLE);
        rvChats.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        layoutEmptyState.setVisibility(View.GONE);
        rvChats.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up Firebase listener
        if (chatsListener != null && currentUserId != null) {
            chatRepository.removeChatsListener(currentUserId, "finderId", chatsListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to this fragment
        if (chatListAdapter != null && chatRepository != null) {
            loadChats();
        }
    }
}