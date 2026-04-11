package com.example.suraagh_deliverable_1.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.example.suraagh_deliverable_1.ModelClasses.Post;
import com.example.suraagh_deliverable_1.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class MatchDetailsAdapter extends RecyclerView.Adapter<MatchDetailsAdapter.ViewHolder> {

    private final String TAG = "MatchAdapter";
    private Context context;
    private List<MatchItem> items;
    private FirebaseFirestore firestore;
    private String userLostPostId; // The lost post that initiated the search

    public MatchDetailsAdapter(Context context, List<MatchItem> items, String userLostPostId) {
        this.context = context;
        this.items = items;
        this.userLostPostId = userLostPostId;
        this.firestore = FirebaseFirestore.getInstance();

        Log.d(TAG, "Constructor: User's lost post ID: " + userLostPostId);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_found_match, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MatchItem item = items.get(position);
        Post foundPost = item.post; // This is a FOUND post

        holder.tvFoundTitle.setText(foundPost.getThing());

        String percentText = String.format("%.0f%% Match", item.matchPercentage);
        holder.tvMatchPercent.setText(percentText);

        String details = foundPost.getAddress();
        if (foundPost.getDate() != null && !foundPost.getDate().isEmpty()) {
            details += " • " + foundPost.getDate();
        }
        holder.tvFoundDetails.setText(details);
        holder.tvFoundDescription.setText(foundPost.getAdditionalInfo());

        if (foundPost.getImageUrl() != null && !foundPost.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(foundPost.getImageUrl().get(0))
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.imgFoundItem);
        }

        holder.btnStartChat.setOnClickListener(v -> {
            holder.btnStartChat.setEnabled(false);
//            startChatWithFinder(foundPost, holder.btnStartChat);
        });

        holder.itemView.setOnClickListener(v -> {
//            //call post detail activity
        });
    }

    /**
     * Create or find existing chat and navigate to ChatDetailActivity
     * User (owner of lost post) is chatting with finder (owner of found post)
     */
//    private void startChatWithFinder(Post foundPost, Button button) {
//        if (currentUserId == null) {
//            Toast.makeText(context, "Please sign in to start a chat", Toast.LENGTH_SHORT).show();
//            button.setEnabled(true);
//            return;
//        }
//
//        if (foundPost.getUserId() == null || foundPost.getUserId().equals(currentUserId)) {
//            Toast.makeText(context, "Cannot chat with yourself", Toast.LENGTH_SHORT).show();
//            button.setEnabled(true);
//            return;
//        }
//
//        if (userLostPostId == null) {
//            Toast.makeText(context, "Lost post ID not available", Toast.LENGTH_SHORT).show();
//            button.setEnabled(true);
//            return;
//        }
//
//        button.setText("Starting...");
//        Log.d(TAG, "Looking for chat with lostPostId=" + userLostPostId + ", foundPostId=" + foundPost.getPostId());
//
//        // Check if chat exists for this lost-found pair
//        chatRepository.findExistingChatByBothPosts(
//                userLostPostId,      // Owner's lost post
//                foundPost.getPostId(), // Finder's found post
//                new ChatCallbacks.ChatCallback<Chat>() {
//                    @Override
//                    public void onSuccess(Chat existingChat) {
//                        if (context instanceof Activity) {
//                            ((Activity) context).runOnUiThread(() -> {
//                                button.setEnabled(true);
//
//                                if (existingChat != null) {
//                                    Log.d(TAG, "Existing chat found: " + existingChat.getChatId());
//                                    openChatDetail(existingChat);
//                                } else {
//                                    Log.d(TAG, "Creating new chat");
//                                    fetchDetailsAndCreateChat(foundPost, button);
//                                }
//                            });
//                        }
//                    }
//
//                    @Override
//                    public void onError(String error) {
//                        Log.e(TAG, "Error finding chat: " + error);
//                        if (context instanceof Activity) {
//                            ((Activity) context).runOnUiThread(() -> {
//                                button.setEnabled(true);
//                                Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
//                            });
//                        }
//                    }
//                }
//        );
//    }

    /**
     * Fetch both post titles and user names, then create chat
     */
//    private void fetchDetailsAndCreateChat(Post foundPost, Button button) {
//        // Get lost post title
//        firestore.collection("lostPosts").document(userLostPostId)
//                .get()
//                .addOnSuccessListener(lostSnapshot -> {
//                    String lostPostTitle = "Lost Item";
//                    if (lostSnapshot.exists()) {
//                        Post lostPost = lostSnapshot.toObject(Post.class);
//                        if (lostPost != null && lostPost.getThing() != null) {
//                            lostPostTitle = lostPost.getThing();
//                        }
//                    }
//
//                    String foundPostTitle = foundPost.getThing() != null ? foundPost.getThing() : "Found Item";
//
//                    // Get finder's name
//                    String finalLostPostTitle = lostPostTitle;
//                    chatRepository.getUserInfo(foundPost.getUserId(), new ChatCallbacks.ChatCallback<Map<String, Object>>() {
//                        @Override
//                        public void onSuccess(Map<String, Object> userData) {
//                            String finderName = (String) userData.get("userName");
//                            if (finderName == null) finderName = "User";
//
//                            createNewChat(foundPost, finalLostPostTitle, foundPostTitle, finderName, button);
//                        }
//
//                        @Override
//                        public void onError(String error) {
//                            createNewChat(foundPost, finalLostPostTitle, foundPostTitle, "User", button);
//                        }
//                    });
//                })
//                .addOnFailureListener(e -> {
//                    Log.e(TAG, "Failed to get lost post: " + e.getMessage());
//                    String foundPostTitle = foundPost.getThing() != null ? foundPost.getThing() : "Found Item";
//                    createNewChat(foundPost, "Lost Item", foundPostTitle, "User", button);
//                });
//    }

    /**
     * Create a new chat with both lost and found posts
     */
//    private void createNewChat(Post foundPost, String lostPostTitle, String foundPostTitle,
//                               String finderName, Button button) {
//
//        Chat newChat = new Chat(
//                null,              // chatId - will be set by repository
//                userLostPostId,    // lostPostId (owner's post)
//                lostPostTitle,     // lostPostTitle
//                foundPost.getPostId(), // foundPostId (finder's post)
//                foundPostTitle,    // foundPostTitle
//                currentUserId,     // ownerId (current user - owner of lost post)
//                currentUserName != null ? currentUserName : "User", // ownerName
//                foundPost.getUserId(), // finderId (owner of found post)
//                finderName         // finderName
//        );
//
//        Log.d(TAG, "Creating chat: owner=" + currentUserId + ", finder=" + foundPost.getUserId());
//
//        chatRepository.createChat(newChat, new ChatCallbacks.ChatCallback<Chat>() {
//            @Override
//            public void onSuccess(Chat createdChat) {
//                Log.d(TAG, "Chat created: " + createdChat.getChatId());
//                if (context instanceof Activity) {
//                    ((Activity) context).runOnUiThread(() -> {
//                        button.setEnabled(true);
//                        openChatDetail(createdChat);
//                    });
//                }
//            }
//
//            @Override
//            public void onError(String error) {
//                Log.e(TAG, "Failed to create chat: " + error);
//                if (context instanceof Activity) {
//                    ((Activity) context).runOnUiThread(() -> {
//                        button.setEnabled(true);
//                        Toast.makeText(context, "Failed to create chat: " + error, Toast.LENGTH_SHORT).show();
//                    });
//                }
//            }
//        });
//    }

    /**
     * Open ChatDetailActivity
     */


    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class MatchItem {
        public Post post;
        public float matchPercentage;

        public MatchItem(Post post, float matchPercentage) {
            this.post = post;
            this.matchPercentage = matchPercentage;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFoundItem;
        TextView tvMatchPercent, tvFoundTitle, tvFoundDetails, tvFoundDescription;
        Button btnStartChat;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFoundItem = itemView.findViewById(R.id.imgFoundItem);
            tvMatchPercent = itemView.findViewById(R.id.tvMatchPercent);
            tvFoundTitle = itemView.findViewById(R.id.tvFoundTitle);
            tvFoundDetails = itemView.findViewById(R.id.tvFoundDetails);
            tvFoundDescription = itemView.findViewById(R.id.tvFoundDescription);
            btnStartChat = itemView.findViewById(R.id.btnStartChat);
        }
    }
}