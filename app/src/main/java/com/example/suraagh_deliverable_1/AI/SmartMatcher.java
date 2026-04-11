package com.example.suraagh_deliverable_1.AI;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class SmartMatcher {

    private static final String TAG = "SURAAGH_AI_SMART";

    // Standard Embedding Model
    private static final String EMBEDDING_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent";

    // PRODUCTION FIX: Use a stable Vision model version
    // URL 2: Vision (CORRECTED BACK TO ACTIVE 2.5 MODEL)
    private static final String VISION_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    private String apiKey;
    private RequestQueue requestQueue;
    private Gson gson;

    public SmartMatcher(Context context, String apiKey) {
        this.apiKey = apiKey;
        this.requestQueue = Volley.newRequestQueue(context);
        this.gson = new Gson();
        Log.d(TAG, "SmartMatcher initialized. Volley RequestQueue created.");
    }

    // --- FUNCTION 1: Get Description from Image (Vision) ---
    public interface DescriptionCallback {
        void onDescriptionReady(String description);
    }

    public void getDescriptionsFromImages(List<String> base64Images, DescriptionCallback callback) {
        Log.d(TAG, "getDescriptionsFromImages() called. Number of images: " + (base64Images != null ? base64Images.size() : 0));

        if (base64Images == null || base64Images.isEmpty()) {
            Log.w(TAG, "Image list is empty or null. Returning empty description.");
            callback.onDescriptionReady("");
            return;
        }

        try {
            Log.d(TAG, "Building JSON body for Vision API request...");
            JSONObject body = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject contentObj = new JSONObject();
            JSONArray parts = new JSONArray();

            // 1. Add Text Prompt
            JSONObject textPart = new JSONObject();
            textPart.put("text", "Describe these items in detail for a lost and found database. Focus on unique features, brand, color, condition, and text/logos visible. Keep it concise.");
            parts.put(textPart);

            // 2. Add Images
            for (int i = 0; i < base64Images.size(); i++) {
                Log.d(TAG, "Attaching image " + (i + 1) + " to request payload.");
                JSONObject imagePart = new JSONObject();
                JSONObject inlineData = new JSONObject();
                inlineData.put("mime_type", "image/jpeg");
                inlineData.put("data", base64Images.get(i));
                imagePart.put("inline_data", inlineData);
                parts.put(imagePart);
            }

            contentObj.put("parts", parts);
            contents.put(contentObj);
            body.put("contents", contents);

            String url = VISION_URL + "?key=" + apiKey;
            Log.d(TAG, "Dispatching Vision API request to: " + VISION_URL);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                    response -> {
                        Log.d(TAG, "Vision API HTTP Request SUCCESS. Parsing response...");
                        try {
                            JSONArray candidates = response.getJSONArray("candidates");
                            JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
                            JSONArray responseParts = content.getJSONArray("parts");
                            String description = responseParts.getJSONObject(0).getString("text");

                            Log.d(TAG, "✅ Vision AI Result Extracted: " + description);
                            callback.onDescriptionReady(description);

                        } catch (JSONException e) {
                            Log.e(TAG, "❌ Vision Parse Error (Unexpected JSON structure): " + e.getMessage());
                            Log.e(TAG, "Raw Response: " + response.toString());
                            // Fallback: Don't block the user, just return empty
                            callback.onDescriptionReady("");
                        }
                    },
                    error -> {
                        Log.e(TAG, "❌ Vision API Network Error: " + error.toString());
                        if (error.networkResponse != null) {
                            Log.e(TAG, "Status Code: " + error.networkResponse.statusCode);
                        }
                        callback.onDescriptionReady("");
                    }
            );

            // Increase timeout for image processing
            Log.d(TAG, "Setting Vision API retry policy (30s timeout).");
            request.setRetryPolicy(new DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(request);
            Log.d(TAG, "Vision request added to Volley queue.");

        } catch (JSONException e) {
            Log.e(TAG, "❌ Error building JSON request body for Vision API: " + e.getMessage());
            callback.onDescriptionReady("");
        }
    }

    // --- FUNCTION 2: Get Embedding Vector ---
    public interface EmbeddingCallback {
        void onEmbeddingReady(List<Double> vector);
    }

    public void getEmbedding(String text, EmbeddingCallback callback) {
        String logText = (text != null && text.length() > 50) ? text.substring(0, 50) + "..." : text;
        Log.d(TAG, "getEmbedding() called for text: '" + logText + "'");

        if (text == null || text.trim().isEmpty()) {
            Log.w(TAG, "Text provided for embedding is null or empty. Returning null vector.");
            callback.onEmbeddingReady(null);
            return;
        }

        try {
            Log.d(TAG, "Building JSON body for Embedding API request...");
            EmbeddingRequest req = new EmbeddingRequest(text);
            JSONObject jsonBody = new JSONObject(gson.toJson(req));

            String url = EMBEDDING_URL + "?key=" + apiKey;
            Log.d(TAG, "Dispatching Embedding API request to: " + EMBEDDING_URL);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    response -> {
                        Log.d(TAG, "Embedding API HTTP Request SUCCESS. Parsing response...");
                        try {
                            EmbeddingResponse resp = gson.fromJson(response.toString(), EmbeddingResponse.class);
                            if (resp != null && resp.embedding != null && resp.embedding.values != null) {
                                Log.d(TAG, "✅ Embedding vector generated. Size: " + resp.embedding.values.size());
                                callback.onEmbeddingReady(resp.embedding.values);
                            } else {
                                Log.e(TAG, "❌ Embedding response parsed, but vector values are null.");
                                callback.onEmbeddingReady(null);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Embedding Parse Error", e);
                            Log.e(TAG, "Raw Response: " + response.toString());
                            callback.onEmbeddingReady(null);
                        }
                    },
                    error -> {
                        Log.e(TAG, "❌ Embedding API Network Error", error);
                        if (error.networkResponse != null) {
                            Log.e(TAG, "Status Code: " + error.networkResponse.statusCode);
                        }
                        callback.onEmbeddingReady(null);
                    }
            );
            requestQueue.add(request);
            Log.d(TAG, "Embedding request added to Volley queue.");

        } catch (JSONException e) {
            Log.e(TAG, "❌ Error building JSON request body for Embedding API: " + e.getMessage());
            callback.onEmbeddingReady(null);
        }
    }

    // --- FUNCTION 3: Compare Vectors ---
    public double calculateSimilarity(List<Double> vecA, List<Double> vecB) {
        Log.d(TAG, "calculateSimilarity() called.");

        if (vecA == null || vecB == null) {
            Log.e(TAG, "❌ Vector comparison failed: One or both vectors are null. vecA is null: " + (vecA == null) + ", vecB is null: " + (vecB == null));
            return 0.0;
        }

        if (vecA.size() != vecB.size()) {
            Log.e(TAG, "❌ Vector comparison failed: Size mismatch. vecA size: " + vecA.size() + ", vecB size: " + vecB.size());
            return 0.0;
        }

        Log.d(TAG, "Calculating cosine similarity for vectors of size: " + vecA.size());

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vecA.size(); i++) {
            dotProduct += vecA.get(i) * vecB.get(i);
            normA += Math.pow(vecA.get(i), 2);
            normB += Math.pow(vecB.get(i), 2);
        }

        if (normA == 0 || normB == 0) {
            Log.w(TAG, "⚠️ Vector comparison issue: One of the vectors has a magnitude of 0. Returning 0.0 similarity.");
            return 0.0;
        }

        double result = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
        Log.d(TAG, "✅ Cosine similarity result: " + result);

        return result;
    }
}