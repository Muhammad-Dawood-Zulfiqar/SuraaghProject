package com.example.suraagh_deliverable_1.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.suraagh_deliverable_1.ModelClasses.Post;
import com.example.suraagh_deliverable_1.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {

    private List<Post> posts;
    private OnPostActionListener listener;
    private Context context;

    public interface OnPostActionListener {
        void onPostClick(Post post);
        void onPostLongClick(Post post);
    }

    public PostsAdapter(List<Post> posts, OnPostActionListener listener) {
        this.posts = posts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_edit_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPostClick(post);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onPostLongClick(post);
            return true;
        });

    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void updatePosts(List<Post> newPosts) {
        if (newPosts == null) return;

        // CRITICAL FIX: Removed Handler to ensure immediate update and avoid sync issues
        this.posts.clear();
        this.posts.addAll(newPosts);
        notifyDataSetChanged();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        // UI Components
        private TextView tvTitle;
        private TextView tvPostStatus;
        private TextView tvAdditionalInfo, tvLocation;
        // Attribute Chips
        private TextView tvType, tvMaterial, tvColor, tvSize;

        private RecyclerView imagesRecyclerView;
        private LinearLayout llDotsIndicator;
        private MaterialCardView statusBadgeCard;
        private ImagesAdapter imagesAdapter;
        private LinearLayoutManager layoutManager;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            // Text Views
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPostStatus = itemView.findViewById(R.id.tvPostStatus);
            tvAdditionalInfo = itemView.findViewById(R.id.tvAdditionalInfo);
            tvLocation = itemView.findViewById(R.id.tvLocation);

            // Chips
            tvType = itemView.findViewById(R.id.tvType);
            tvMaterial = itemView.findViewById(R.id.tvMaterial);
            tvColor = itemView.findViewById(R.id.tvColor);
            tvSize = itemView.findViewById(R.id.tvSize);

            // Containers
            statusBadgeCard = itemView.findViewById(R.id.statusBadgeCard);
            imagesRecyclerView = itemView.findViewById(R.id.imagesRecyclerView);
            llDotsIndicator = itemView.findViewById(R.id.llDotsIndicator);

            // Hide legacy buttons
            View actionButtons = itemView.findViewById(R.id.actionButtonsLayout);
            if (actionButtons != null) actionButtons.setVisibility(View.GONE);

            setupImageSlider();
        }

        private void setupImageSlider() {
            layoutManager = new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false);
            imagesRecyclerView.setLayoutManager(layoutManager);
            imagesAdapter = new ImagesAdapter();
            imagesRecyclerView.setAdapter(imagesAdapter);

            PagerSnapHelper snapHelper = new PagerSnapHelper();
            try {
                snapHelper.attachToRecyclerView(imagesRecyclerView);
            } catch (IllegalStateException e) {
                // Ignore if already attached
            }

            imagesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        int position = layoutManager.findFirstCompletelyVisibleItemPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            updateDots(position);
                        }
                    }
                }
            });
        }

        public void bind(Post post) {
            boolean isLostPost = post instanceof com.example.suraagh_deliverable_1.ModelClasses.LostPost;

            // 1. Status Badge
            tvPostStatus.setText(isLostPost ? "LOST" : "FOUND");

            int badgeColor = isLostPost ? R.color.status_lost : R.color.status_found;
            int badgeBg = isLostPost ? R.color.status_lost_bg : R.color.status_found_bg;

            tvPostStatus.setTextColor(ContextCompat.getColor(context, badgeColor));
            statusBadgeCard.setCardBackgroundColor(ContextCompat.getColor(context, badgeBg));

            // 2. Main Title (The "Thing")
            if (post.getThing() != null && !post.getThing().isEmpty()) {
                tvTitle.setText(post.getThing());
            } else {
                tvTitle.setText(post.getType() != null ? post.getType() : "Unknown Item");
            }

            // 3. Attribute Chips
            setChipView(tvType, post.getType());
            setChipView(tvMaterial, post.getMaterial());
            setChipView(tvColor, post.getColor());
            setChipView(tvSize, post.getSize());

            // 4. Description
            if (post.getAdditionalInfo() != null && !post.getAdditionalInfo().isEmpty()) {
                String additionalInfo;
                if(post.getAdditionalInfo().length() > 30) {
                    additionalInfo = post.getAdditionalInfo().substring(0,30) + "...";
                } else {
                    additionalInfo = post.getAdditionalInfo();
                }
                tvAdditionalInfo.setText(additionalInfo);
                tvAdditionalInfo.setVisibility(View.VISIBLE);
            } else {
                tvAdditionalInfo.setVisibility(View.GONE);
            }

            // 5. Location
            if (post.getAddress() != null && !post.getAddress().isEmpty()) {
                tvLocation.setText(post.getAddress());
                ((View)tvLocation.getParent()).setVisibility(View.VISIBLE);
            } else {
                ((View)tvLocation.getParent()).setVisibility(View.GONE);
            }

            // 6. Image Slider
            if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                imagesRecyclerView.setVisibility(View.VISIBLE);
                imagesAdapter.setImages(post.getImageUrl());
                setupDots(post.getImageUrl().size());
            } else {
                imagesRecyclerView.setVisibility(View.GONE);
                llDotsIndicator.setVisibility(View.GONE);
            }
        }

        private void setChipView(TextView chip, String value) {
            if (value != null && !value.isEmpty()) {
                chip.setText(value);
                chip.setVisibility(View.VISIBLE);
            } else {
                chip.setVisibility(View.GONE);
            }
        }

        private void setupDots(int count) {
            llDotsIndicator.removeAllViews();
            if (count <= 1) {
                llDotsIndicator.setVisibility(View.GONE);
                return;
            }
            llDotsIndicator.setVisibility(View.VISIBLE);

            for (int i = 0; i < count; i++) {
                ImageView dot = new ImageView(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(6, 0, 6, 0);
                dot.setLayoutParams(params);
                dot.setImageResource(R.drawable.indicator_inactive);
                llDotsIndicator.addView(dot);
            }
            updateDots(0);
        }

        private void updateDots(int activePosition) {
            int childCount = llDotsIndicator.getChildCount();
            if (activePosition >= childCount || activePosition < 0) return;

            for (int i = 0; i < childCount; i++) {
                ImageView dot = (ImageView) llDotsIndicator.getChildAt(i);
                if (i == activePosition) {
                    dot.setImageResource(R.drawable.indicator_active);
                } else {
                    dot.setImageResource(R.drawable.indicator_inactive);
                }
            }
        }
    }
}