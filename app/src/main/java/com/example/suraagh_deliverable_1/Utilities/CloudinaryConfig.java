package com.example.suraagh_deliverable_1.Utilities;

import android.content.Context;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryConfig {
    static boolean isInitialized = false;

    public static void initCloudinary(Context context) {
        if (isInitialized) return;

        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", "dekbifsyf");
        config.put("api_key", "444897415266765");
        config.put("secure", true);

        MediaManager.init(context, config);
        isInitialized = true;
    }
}