package com.example.suraagh_deliverable_1.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.suraagh_deliverable_1.chat.ChatFragment;
import com.example.suraagh_deliverable_1.ui.fragments.ManagePostsFragment;
import com.example.suraagh_deliverable_1.ui.fragments.MatchesFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // We only have 4 fragments in the slider.
        // The "Add" button launches an Activity, so it is not part of this adapter.
        switch (position) {
            case 0:
                return new ManagePostsFragment();
            case 1:
                return new ChatFragment();
            case 2:
                // Matches is now index 2 because we removed AddFragment
                return new MatchesFragment();
            case 3:
                // Account is now index 3
                return new AccountFragment();
            default:
                return new ManagePostsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4; // Reduced from 5 to 4
    }
}