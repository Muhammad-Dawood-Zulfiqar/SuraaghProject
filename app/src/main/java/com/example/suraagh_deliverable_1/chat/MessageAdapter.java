package com.example.suraagh_deliverable_1.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.suraagh_deliverable_1.ModelClasses.Message;
import com.example.suraagh_deliverable_1.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying messages in a chat conversation.
 * Shows sent messages on the right and received messages on the left.
 */
public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<Message> messages;
    private final String currentUserId;

    public MessageAdapter(String currentUserId) {
        this.messages = new ArrayList<>();
        this.currentUserId = currentUserId;
    }

    public void updateMessages(List<Message> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (message.isSentBy(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    /**
     * ViewHolder for sent messages (displayed on the right)
     */
    /**
     * ViewHolder for sent messages (displayed on the right)
     */
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMessage;
        private final TextView tvTimestamp;
        private final ImageView ivMessageStatus; // <-- ADD THIS

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            // <-- ADD THIS (Make sure the ID matches what you put in XML)
            ivMessageStatus = itemView.findViewById(R.id.ivMessageStatus);
        }

        public void bind(Message message) {
            tvMessage.setText(message.getContent());
            tvTimestamp.setText(formatTimestamp(message.getTimestamp()));

            // --- NEW TICK LOGIC ---
            if (ivMessageStatus != null) {
                if (message.isRead()) {
                    // Double Blue Ticks
                    ivMessageStatus.setImageResource(R.drawable.ic_double_tick_blue);
                } else if (message.isDelivered()) {
                    // Double Gray Ticks
                    ivMessageStatus.setImageResource(R.drawable.ic_double_tick_gray);
                } else {
                    // Single Gray Tick
                    ivMessageStatus.setImageResource(R.drawable.ic_single_tick);
                }
            }
        }

        private String formatTimestamp(long timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
    /**
     * ViewHolder for received messages (displayed on the left)
     */
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMessage;
        private final TextView tvTimestamp;
        private final TextView tvSenderName;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
        }

        public void bind(Message message) {
            tvMessage.setText(message.getContent());
            tvTimestamp.setText(formatTimestamp(message.getTimestamp()));

            if (tvSenderName != null && message.getSenderName() != null) {
                tvSenderName.setText(message.getSenderName());
            }
        }

        private String formatTimestamp(long timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
}

