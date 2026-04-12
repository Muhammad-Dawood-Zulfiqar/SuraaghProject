package com.example.suraagh_deliverable_1.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.suraagh_deliverable_1.R;
import com.example.suraagh_deliverable_1.Utilities.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class SelectedImagesAdapter extends RecyclerView.Adapter<SelectedImagesAdapter.ImageViewHolder> {

    private static final String TAG = "SelectedImagesAdapter";
    private List<String> imageUris;
    private OnImageRemoveListener listener;

    public interface OnImageRemoveListener {
        void onImageRemove(int position, String imageUri);
    }

    public SelectedImagesAdapter() {
        this.imageUris = new ArrayList<>();
    }

    public void setOnImageRemoveListener(OnImageRemoveListener listener) {
        this.listener = listener;
    }

    public void addImage(String imageUri) {
        if (imageUris == null) {
            imageUris = new ArrayList<>();
        }
        imageUris.add(imageUri);
        notifyItemInserted(imageUris.size() - 1);
        Log.d(TAG, "Image added, total images: " + imageUris.size());
    }

    public void removeImage(int position) {
        if (imageUris != null && position >= 0 && position < imageUris.size()) {
            String removedUri = imageUris.remove(position);
            notifyItemRemoved(position);
            // Also notify items after the removed position that their positions changed
            if (position < imageUris.size()) {
                notifyItemRangeChanged(position, imageUris.size() - position);
            }
            Log.d(TAG, "Image removed at position: " + position + ", URI: " + removedUri);
        } else {
            Log.e(TAG, "Invalid position for removal: " + position + ", list size: " +
                    (imageUris != null ? imageUris.size() : 0));
        }
    }

    public List<String> getImageUris() {
        return imageUris != null ? new ArrayList<>(imageUris) : new ArrayList<>();
    }

    public String getImageUriAt(int position) {
        if (imageUris != null && position >= 0 && position < imageUris.size()) {
            return imageUris.get(position);
        }
        return null;
    }

    public void setImageUris(List<String> imageUris) {
        this.imageUris = imageUris != null ? new ArrayList<>(imageUris) : new ArrayList<>();
        notifyDataSetChanged();
        Log.d(TAG, "Images set, total: " + this.imageUris.size());
    }

    public void clearImages() {
        if (imageUris != null) {
            int size = imageUris.size();
            imageUris.clear();
            notifyItemRangeRemoved(0, size);
        }
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        if (imageUris == null || position < 0 || position >= imageUris.size()) {
            Log.e(TAG, "Invalid position for binding: " + position);
            return;
        }

        String uriString = imageUris.get(position);

        // --- SAFETY CHECK ---
        if (holder.itemView.getContext() == null) return;

        ImageLoader.loadHotelImage(uriString, holder.imageView);

        // Set remove button click listener - use the actual position from holder
        holder.btnRemove.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            Log.d(TAG, "Remove button clicked. Adapter position: " + adapterPosition);

            if (adapterPosition != RecyclerView.NO_POSITION) {
                String uriToRemove = getImageUriAt(adapterPosition);
                if (listener != null && uriToRemove != null) {
                    listener.onImageRemove(adapterPosition, uriToRemove);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUris != null ? imageUris.size() : 0;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView btnRemove;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            btnRemove = itemView.findViewById(R.id.btnRemove);

            // Make sure the remove button is clickable
            btnRemove.setClickable(true);
            btnRemove.setFocusable(true);
            btnRemove.setFocusableInTouchMode(true);
        }
    }
}