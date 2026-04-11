package com.example.suraagh_deliverable_1.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.suraagh_deliverable_1.ModelClasses.Post;
import com.example.suraagh_deliverable_1.R;

import java.util.List;

public class LostPostsAdapter extends RecyclerView.Adapter<LostPostsAdapter.LostPostViewHolder> {

    private Context context;
    private List<Post> myLostPosts;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Post post);
    }

    public LostPostsAdapter(Context context, List<Post> myLostPosts, OnItemClickListener listener) {
        this.context = context;
        this.myLostPosts = myLostPosts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LostPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the new layout file: item_lost_post.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_lost_post, parent, false);
        return new LostPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LostPostViewHolder holder, int position) {
        Post post = myLostPosts.get(position);

        holder.tvItemName.setText(post.getThing());

        // Set Image
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(post.getImageUrl().get(0))
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.imgPost);
        } else {
            holder.imgPost.setImageResource(R.drawable.ic_launcher_background);
        }

        // Set Details
        String details = post.getAddress();
        if (post.getDate() != null) {
            details += " • " + post.getDate();
        }
        holder.tvPostDetails.setText(details);

        // Click Listener
        holder.itemView.setOnClickListener(v -> listener.onItemClick(post));
    }

    @Override
    public int getItemCount() {
        return myLostPosts.size();
    }

    public static class LostPostViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPost;
        TextView tvItemName, tvActionPrompt, tvPostDetails;

        public LostPostViewHolder(@NonNull View itemView) {
            super(itemView);
            // Bind to IDs in item_lost_post.xml
            imgPost = itemView.findViewById(R.id.imgPost);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvActionPrompt = itemView.findViewById(R.id.tvActionPrompt);
            tvPostDetails = itemView.findViewById(R.id.tvPostDetails);
        }
    }
}