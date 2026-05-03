package com.example.suraagh_deliverable_1.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.suraagh_deliverable_1.ModelClasses.Chat;
import com.example.suraagh_deliverable_1.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * RecyclerView adapter for displaying chat items in the chat list.
 * Shows user profile/name, last message, unread badge, timestamp,
 * and a card/button to view the associated post.
 */
public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    private List<Chat> chats;
    private final String currentUserId;
    private final boolean isFinderView; // true = Chats as Finder tab, false = Chats as Owner tab
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(Chat chat);
        void onPostCardClick(Chat chat);
    }

    public ChatListAdapter(String currentUserId, boolean isFinderView) {
        this.chats = new ArrayList<>();
        this.currentUserId = currentUserId;
        this.isFinderView = isFinderView;
    }

    public void setOnChatClickListener(OnChatClickListener listener) {
        this.listener = listener;
    }

    public void updateChats(List<Chat> newChats) {
        this.chats = newChats;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chats.get(position);
        holder.bind(chat);
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUserName;
        private final TextView tvLastMessage;
        private final TextView tvTimestamp;
        private final TextView tvUnreadBadge;
        private final ImageView ivUserAvatar;
        private final CardView cvPostPreview;
        private final TextView tvPostType;
        private final TextView tvPostTitle;
        private final TextView tvRoleBadge;
        private final View rootView;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            rootView = itemView;
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvUnreadBadge = itemView.findViewById(R.id.tvUnreadBadge);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            cvPostPreview = itemView.findViewById(R.id.cvPostPreview);
            tvPostType = itemView.findViewById(R.id.tvPostType);
            tvPostTitle = itemView.findViewById(R.id.tvPostTitle);
            tvRoleBadge = itemView.findViewById(R.id.tvRoleBadge);
        }

        public void bind(Chat chat) {
            Context context = itemView.getContext();

            // Set the other participant's name
            String otherUserName = chat.getOtherParticipantName(currentUserId);
            tvUserName.setText(otherUserName != null ? otherUserName : "Unknown User");

            // Set role badge
            if (isFinderView) {
                tvRoleBadge.setText("Owner");
                tvRoleBadge.setBackgroundTintList(
                        context.getColorStateList(R.color.status_found));
            } else {
                tvRoleBadge.setText("Finder");
                tvRoleBadge.setBackgroundTintList(
                        context.getColorStateList(R.color.status_lost));
            }

            // Set last message
            String lastMessage = chat.getLastMessage();
            if (lastMessage != null && !lastMessage.isEmpty()) {
                tvLastMessage.setText(lastMessage);
            } else {
                tvLastMessage.setText("No messages yet");
            }

            // Set timestamp
            tvTimestamp.setText(formatTimestamp(chat.getLastMessageTimestamp()));

            // Set unread badge
            int unreadCount = chat.getUnreadCountForUser(currentUserId);
            if (unreadCount > 0) {
                tvUnreadBadge.setVisibility(View.VISIBLE);
                tvUnreadBadge.setText(unreadCount > 99 ? "99+" : String.valueOf(unreadCount));
                // Make last message bold for unread
                tvLastMessage.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                tvUnreadBadge.setVisibility(View.GONE);
                tvLastMessage.setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            // Set post preview card
            String postType = chat.getPostIdForUser(currentUserId);
            if (postType != null) {
                tvPostType.setText(postType.equalsIgnoreCase("lost") ? "LOST" : "FOUND");
                tvPostType.setBackgroundTintList(context.getColorStateList(
                        postType.equalsIgnoreCase("lost") ?
                                R.color.status_lost : R.color.status_found));
            }

            String postTitle = chat.getPostTitleForUser(currentUserId);
            tvPostTitle.setText(postTitle != null ? postTitle : "View Post");

            // Set avatar placeholder with first letter
            setAvatarPlaceholder(otherUserName);

            // Click listener for the whole item -> opens ChatDetailActivity
            rootView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onChatClick(chat);
                }
            });

            // Click listener for post preview card -> opens PostDetailActivity
            cvPostPreview.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPostCardClick(chat);
                }
            });
        }

        private void setAvatarPlaceholder(String name) {
            // For now, just set a colored background
            // In a real app, you'd load the user's profile image here
            if (name != null && !name.isEmpty()) {
                // Generate a color based on the name
                int[] colors = {
                        0xFF1565C0, 0xFF388E3C, 0xFFD32F2F, 
                        0xFF7B1FA2, 0xFFFF8F00, 0xFF00796B
                };
                int colorIndex = Math.abs(name.hashCode()) % colors.length;
                ivUserAvatar.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(colors[colorIndex]));
            }
        }

        private String formatTimestamp(long timestamp) {
            if (timestamp == 0) return "";

            long now = System.currentTimeMillis();
            long diff = now - timestamp;

            // Less than a minute
            if (diff < TimeUnit.MINUTES.toMillis(1)) {
                return "Just now";
            }
            // Less than an hour
            if (diff < TimeUnit.HOURS.toMillis(1)) {
                long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
                return minutes + "m";
            }
            // Less than a day
            if (diff < TimeUnit.DAYS.toMillis(1)) {
                long hours = TimeUnit.MILLISECONDS.toHours(diff);
                return hours + "h";
            }
            // Less than a week
            if (diff < TimeUnit.DAYS.toMillis(7)) {
                long days = TimeUnit.MILLISECONDS.toDays(diff);
                return days + "d";
            }
            // More than a week - show date
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
}

