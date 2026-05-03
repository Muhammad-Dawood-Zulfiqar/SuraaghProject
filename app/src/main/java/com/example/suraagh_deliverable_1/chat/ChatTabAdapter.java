package com.example.suraagh_deliverable_1.chat;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.suraagh_deliverable_1.chat.ChatsAsFinderFragment;
import com.example.suraagh_deliverable_1.chat.ChatsAsOwnerFragment;

/**
 * ViewPager2 adapter for the Chat tabs.
 * Tab 0: Chats as Finder (user is chatting with post owners)
 * Tab 1: Chats as Owner (user is chatting with finders about their posts)
 */
public class ChatTabAdapter extends FragmentStateAdapter {

    private static final String TAG = "ChatTabAdapter"; // Tag for logging

    public static final int TAB_CHATS_AS_FINDER = 0;
    public static final int TAB_CHATS_AS_OWNER = 1;
    public static final int TOTAL_TABS = 2;

    public ChatTabAdapter(@NonNull Fragment fragment) {
        super(fragment);
        Log.d(TAG, "Constructor: ChatTabAdapter initialized");
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Log.d(TAG, "createFragment: Creating fragment for position " + position);

        switch (position) {
            case TAB_CHATS_AS_FINDER:
                Log.d(TAG, "createFragment: Instantiating ChatsAsFinderFragment (Tab 0)");
                return new ChatsAsFinderFragment();
            case TAB_CHATS_AS_OWNER:
                Log.d(TAG, "createFragment: Instantiating ChatsAsOwnerFragment (Tab 1)");
                return new ChatsAsOwnerFragment();
            default:
                Log.w(TAG, "createFragment: Invalid position " + position + ", defaulting to Finder");
                return new ChatsAsFinderFragment();
        }
    }

    @Override
    public int getItemCount() {
        // Note: getItemCount is called frequently by ViewPager2, keeping this log might spam Logcat
        // Log.d(TAG, "getItemCount: returning " + TOTAL_TABS);
        return TOTAL_TABS;
    }

    /**
     * Get tab title for the specified position
     */
    public static String getTabTitle(int position) {
        Log.d(TAG, "getTabTitle: Requesting title for position " + position);

        switch (position) {
            case TAB_CHATS_AS_FINDER:
                return "Chats as Finder";
            case TAB_CHATS_AS_OWNER:
                return "Chats as Owner";
            default:
                Log.w(TAG, "getTabTitle: Unknown position " + position);
                return "";
        }
    }
}