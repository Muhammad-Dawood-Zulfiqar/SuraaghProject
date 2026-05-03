package com.example.suraagh_deliverable_1.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.suraagh_deliverable_1.Database.ChatCallbacks;
import com.example.suraagh_deliverable_1.ModelClasses.Message;
import com.example.suraagh_deliverable_1.ModelClasses.User;
import com.example.suraagh_deliverable_1.R;
import com.example.suraagh_deliverable_1.Utilities.LocalStorage;
import com.example.suraagh_deliverable_1.chat.MessageAdapter;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatDetailActivity extends AppCompatActivity {

    private static final String TAG = "ChatDetailActivity";

    // Intent extras
    public static final String EXTRA_CHAT_ID = "extra_chat_id";
    public static final String EXTRA_OTHER_USER_ID = "extra_other_user_id";
    public static final String EXTRA_OTHER_USER_NAME = "extra_other_user_name";
    public static final String EXTRA_POST_ID = "extra_post_id";
    public static final String EXTRA_IS_USER_OWNER = "extra_is_user_owner";

    // Views
    private Toolbar toolbar;
    private TextView tvOtherUserName;
    private TextView tvRoleLabel;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    // Data
    private ChatRepository chatRepository;
    private MessageAdapter messageAdapter;
    private ValueEventListener messagesListener;

    private String chatId;
    private String otherUserId;
    private String otherUserName;
    private String postId;
    private boolean isUserOwner;
    private String currentUserId;
    private String currentUserName;

    // Track previously seen messages to mark new ones as read
    private List<String> previouslySeenMessageIds = new ArrayList<>();
    private boolean isActivityInForeground = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        Log.d(TAG, "onCreate: Activity started");

        initializeExtras();
        initializeViews();
        initializeRepository();
        setupToolbar();
        setupRecyclerView();
        setupSendButton();
        loadMessages();
        markAllMessagesAsRead(); // Mark existing messages as read
    }

    private void initializeExtras() {
        Log.d(TAG, "initializeExtras: Reading Intent extras");
        chatId = getIntent().getStringExtra(EXTRA_CHAT_ID);
        otherUserId = getIntent().getStringExtra(EXTRA_OTHER_USER_ID);
        otherUserName = getIntent().getStringExtra(EXTRA_OTHER_USER_NAME);
        postId = getIntent().getStringExtra(EXTRA_POST_ID);
        isUserOwner = getIntent().getBooleanExtra(EXTRA_IS_USER_OWNER, false);

        Log.d(TAG, "initializeExtras: chatId=" + chatId +
                ", otherUserId=" + otherUserId +
                ", otherUserName=" + otherUserName +
                ", isUserOwner=" + isUserOwner);

        if (chatId == null || chatId.isEmpty()) {
            Log.e(TAG, "initializeExtras: Invalid chat ID, finishing activity");
            Toast.makeText(this, "Invalid chat", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        Log.d(TAG, "initializeViews: Binding views");
        toolbar = findViewById(R.id.toolbar);
        tvOtherUserName = findViewById(R.id.tvOtherUserName);
        tvRoleLabel = findViewById(R.id.tvRoleLabel);
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
    }

    private void initializeRepository() {
        Log.d(TAG, "initializeRepository: Initializing repo and user data");
        chatRepository = ChatRepository.getInstance();
        currentUserId = chatRepository.getCurrentUserId();

        Log.d(TAG, "initializeRepository: currentUserId=" + currentUserId);
        LocalStorage storage = new LocalStorage(this);
        User currentUser = storage.getUser();
        if (currentUser != null) {
            currentUserName = currentUser.getUserName();
        }
        else{
            currentUserName="No user name found";
        }
    }

    private void setupToolbar() {
        Log.d(TAG, "setupToolbar: Setting up action bar");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("");
        }

        // Set other user's name
        tvOtherUserName.setText(otherUserName != null ? otherUserName : "Chat");

        // Set role label based on the current user's role
        if (isUserOwner) {
            Log.d(TAG, "setupToolbar: UI set as OWNER");
            tvRoleLabel.setText("You are the Owner");
            tvRoleLabel.setBackgroundTintList(getColorStateList(R.color.status_found_bg));
        } else {
            Log.d(TAG, "setupToolbar: UI set as FINDER");
            tvRoleLabel.setText("You are the Finder");
            tvRoleLabel.setBackgroundTintList(getColorStateList(R.color.status_lost_bg));
        }
    }

    private void setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView: Configuring RecyclerView");
        messageAdapter = new MessageAdapter(currentUserId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Start from bottom

        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(messageAdapter);
    }

    private void setupSendButton() {
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages() {
        Log.d(TAG, "loadMessages: Fetching messages for chatId=" + chatId);
        showLoading();

        messagesListener = chatRepository.getMessages(chatId, new ChatCallbacks.MessagesListener() {
            @Override
            public void onMessagesUpdated(List<Message> messages) {
                Log.d(TAG, "onMessagesUpdated: Received " + messages.size() + " messages");
                hideLoading();

                if (messages.isEmpty()) {
                    showEmptyState();
                } else {
                    hideEmptyState();

                    // Check for new messages and mark them as read
                    markNewMessagesAsRead(messages);

                    // Store current message IDs for future comparison
                    storeCurrentMessageIds(messages);

                    messageAdapter.updateMessages(messages);
                    // Scroll to bottom
                    rvMessages.scrollToPosition(messages.size() - 1);

                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "loadMessages: onError: " + error);
                hideLoading();
                Toast.makeText(ChatDetailActivity.this,
                        "Error loading messages: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Store message IDs for tracking new messages
     */
    private void storeCurrentMessageIds(List<Message> messages) {
        previouslySeenMessageIds.clear();
        for (Message message : messages) {
            if (message.getMessageId() != null) {
                previouslySeenMessageIds.add(message.getMessageId());
            }
        }
        Log.d(TAG, "storeCurrentMessageIds: Stored " + previouslySeenMessageIds.size() + " message IDs");
    }

    /**
     * Check for new messages and mark them as read
     */
    private void markNewMessagesAsRead(List<Message> currentMessages) {
        if (!isActivityInForeground || currentUserId == null) {
            return;
        }

        List<String> newMessageIds = new ArrayList<>();
        List<String> unreadMessageIds = new ArrayList<>();

        // Find new unread messages from other user
        for (Message message : currentMessages) {
            String messageId = message.getMessageId();
            if (messageId != null &&
                    !message.isSentBy(currentUserId) &&
                    !message.isRead()) {

                unreadMessageIds.add(messageId);

                // Check if this is a new message (not previously seen)
                if (!previouslySeenMessageIds.contains(messageId)) {
                    newMessageIds.add(messageId);
                }
            }
        }

        if (!unreadMessageIds.isEmpty()) {
            Log.d(TAG, "markNewMessagesAsRead: Found " + unreadMessageIds.size() +
                    " unread messages, " + newMessageIds.size() + " are new");

            // Mark all unread messages as read
            markSpecificMessagesAsRead(unreadMessageIds);
        }
    }

    /**
     * Mark specific messages as read
     */
    private void markSpecificMessagesAsRead(List<String> messageIds) {
        if (chatId == null || messageIds.isEmpty() || currentUserId == null) {
            return;
        }

        chatRepository.markSpecificMessagesAsRead(chatId, currentUserId, messageIds);
    }

    /**
     * Mark all existing messages as read
     */
    private void markAllMessagesAsRead() {
        Log.d(TAG, "markAllMessagesAsRead: Marking all messages as read for chatId=" + chatId);
        if (chatId != null && currentUserId != null) {
            // Mark messages as read in database
            chatRepository.markMessagesAsRead(chatId, currentUserId);

            // Reset unread count in chat
            chatRepository.resetUnreadCount(chatId, currentUserId, isUserOwner);
        }
    }

    private void sendMessage() {
        String content = etMessage.getText().toString().trim();

        if (TextUtils.isEmpty(content)) {
            Log.d(TAG, "sendMessage: Content is empty, ignoring");
            return;
        }

        Log.d(TAG, "sendMessage: Sending message: " + content);

        // Clear input immediately for better UX
        etMessage.setText("");

        // Create and send message
        Message message = new Message(
                null, // Will be set by repository
                chatId,
                currentUserId,
                currentUserName,
                content
        );

        chatRepository.sendMessage(message, new ChatCallbacks.ChatCallback<Message>() {
            @Override
            public void onSuccess(Message result) {
                Log.d(TAG, "sendMessage: onSuccess: Message sent");
                chatRepository.incrementUnreadCount(chatId, otherUserId, !isUserOwner);

                // --- ADD result.getMessageId() HERE ---
                Log.d(TAG,"Sender Name: "+ currentUserName);
                sendChatNotification(otherUserId, currentUserName, content, chatId, result.getMessageId());
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "sendMessage: onError: " + error);
                Toast.makeText(ChatDetailActivity.this,
                        "Failed to send message: " + error, Toast.LENGTH_SHORT).show();
                // Optionally restore the message to the input field
                etMessage.setText(content);
            }
        });
    }

    // ==========================================
    // NOTIFICATION HELPERS
    // ==========================================

    private void sendChatNotification(String targetUserId, String senderName, String messageContent, String chatId, String messageId) {        // 1. Fetch the target user's FCM token from Firestore
        FirebaseFirestore.getInstance().collection("users").document(targetUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("fcmToken")) {
                        String fcmToken = documentSnapshot.getString("fcmToken");

                        if (fcmToken != null && !fcmToken.isEmpty()) {
                            // 2. Token found! Trigger the Vercel backend.
                            triggerVercelChatNotification(fcmToken, senderName, messageContent, chatId, messageId);
                        } else {
                            Log.d(TAG, "User does not have an FCM token set.");
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch user token", e));
    }

    private void triggerVercelChatNotification(String fcmToken, String senderName, String messageContent, String chatId, String messageId) {
        String vercelUrl = "https://suraagh-backend.vercel.app/api/sendNotification";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("token", fcmToken);
            jsonBody.put("title", "New message from " + senderName);
            jsonBody.put("body", messageContent);
            jsonBody.put("matchId", "CHAT_" + chatId);

            // --- NEW: Attach the exact message ID ---
            jsonBody.put("messageId", messageId);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, vercelUrl, jsonBody,
                    response -> Log.d(TAG, "Chat Notification successfully sent via Vercel!"),
                    error -> Log.e(TAG, "Failed to trigger chat notification. " + error.toString())
            );

            // Send the request using Volley
            Volley.newRequestQueue(this).add(request);

        } catch (Exception e) {
            Log.e(TAG, "Error creating JSON for notification", e);
        }
    }
    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        rvMessages.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        rvMessages.setVisibility(View.VISIBLE);
    }

    private void showEmptyState() {
        tvEmptyState.setVisibility(View.VISIBLE);
        tvEmptyState.setText("No messages yet.\nSay hello!");
    }

    private void hideEmptyState() {
        tvEmptyState.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.d(TAG, "onOptionsItemSelected: Home/Up pressed");
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Resumed");
        isActivityInForeground = true;

        // Mark all messages as read when activity comes to foreground
        markAllMessagesAsRead();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Paused");
        isActivityInForeground = false;

        // Mark all messages as read when leaving activity
        markAllMessagesAsRead();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Activity destroying");
        isActivityInForeground = false;

        // Clean up Firebase listener
        if (messagesListener != null && chatId != null) {
            Log.d(TAG, "onDestroy: Removing message listener");
            chatRepository.removeMessagesListener(chatId, messagesListener);
        }
    }
}