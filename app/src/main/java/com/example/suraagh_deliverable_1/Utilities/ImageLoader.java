package com.example.suraagh_deliverable_1.Utilities;



import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

public class ImageLoader {

    private static final String TAG = "ImageLoader";

    // Common request options for hotel images
    private static final RequestOptions REQUEST_OPTIONS = new RequestOptions()
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_gallery)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop();

    public static void loadHotelImage(String imageUrl, ImageView imageView) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Log.d(TAG, "Loading image with Glide: " + imageUrl);

            Glide.with(imageView.getContext())
                    .load(imageUrl)
                    .apply(REQUEST_OPTIONS)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);

        } else {
            Log.w(TAG, "Image URL is null or empty");
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }


}