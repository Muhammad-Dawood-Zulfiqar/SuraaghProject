// ... (Imports remain the same)
package com.example.suraagh_deliverable_1;

import static android.widget.Toast.LENGTH_LONG;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.suraagh_deliverable_1.AI.PostRepository;
import com.example.suraagh_deliverable_1.AI.SmartMatcher;
import com.example.suraagh_deliverable_1.Adapters.SelectedImagesAdapter;
import com.example.suraagh_deliverable_1.Database.DataBaseGetFoundPosts;
import com.example.suraagh_deliverable_1.Database.DatabaseCreatePost;
import com.example.suraagh_deliverable_1.Database.DatabaseGetLostPosts;
import com.example.suraagh_deliverable_1.Firebase.CallBack;
import com.example.suraagh_deliverable_1.Firebase.CallBackForGetPost;
import com.example.suraagh_deliverable_1.Firebase.FirebaseCreatePost;
import com.example.suraagh_deliverable_1.Firebase.FirebaseGetFoundPosts;
import com.example.suraagh_deliverable_1.Firebase.FirebaseGetLostPosts;
import com.example.suraagh_deliverable_1.HelperActivities.LocationPicker;
import com.example.suraagh_deliverable_1.ModelClasses.FoundPost;
import com.example.suraagh_deliverable_1.ModelClasses.LostPost;
import com.example.suraagh_deliverable_1.ModelClasses.Post;
import com.example.suraagh_deliverable_1.R;
import com.example.suraagh_deliverable_1.Utilities.LocalStorage;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.osmdroid.config.Configuration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddPost extends AppCompatActivity {

    private static final String TAG = "SURAAGH_ADDPOST";
    private static final String GEMINI_API_KEY = "AIzaSyBIncP9Bd_rehSHagUY3_m7vrBsIizEsVc";

    // ... (UI Components and Variables same as before) ...
    private MaterialButtonToggleGroup togglePostType;
    private RecyclerView imagesRecyclerView;
    private View btnAddImage, btnSelectLocation;
    private ExtendedFloatingActionButton btnSubmit;
    private AutoCompleteTextView spinnerType, spinnerMaterial, spinnerColor, spinnerSize;
    private TextInputEditText etThing, etDate, etAdditionalInfo;
    private TextView tvSelectedLocation;

    private SelectedImagesAdapter imagesAdapter;
    private ArrayAdapter<String> typeAdapter, materialAdapter, colorAdapter, sizeAdapter;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private double selectedLatitude = 0.0, selectedLongitude = 0.0;
    private String selectedAddress = "";
    private boolean isLostPost = true;

    private static final int REQUEST_SELECT_LOCATION = 3;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    private final Calendar myCalendar = Calendar.getInstance();
    private boolean isEditMode = false;
    private String existingPostId = "";
    private List<String> existingImageUrls = new ArrayList<>();
    private String currentPhotoPath;

    private DatabaseCreatePost dbCreatePost;
    private PostRepository postRepository;
    private FirebaseAuth auth;

    // Your arrays are perfect for this model
    private final String[] TYPES = {"Others", "Wallet", "Phone", "Keys", "Pet", "Bag", "Document", "Jewelry", "Clothing", "Electronics", "Glasses"};
    private final String[] MATERIALS = {"Others", "Leather", "Metal", "Plastic", "Fabric", "Glass", "Wood", "Paper", "Rubber"};
    private final String[] COLORS = {"Others", "Black", "White", "Blue", "Red", "Brown", "Green", "Yellow", "Silver", "Gold", "Gray"};
    private final String[] SIZES = {"Others", "Tiny", "Small", "Medium", "Large", "Extra Large"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE));
        initViews();
        SmartMatcher smartMatcher=new SmartMatcher(this,GEMINI_API_KEY);
//        smartMatcher.listAvailableModels();
        setupAdapters();
        setupListeners();
        setupBackend();
        checkEditMode();

    }

    private void setupBackend() {
        auth = FirebaseAuth.getInstance();
        dbCreatePost = new FirebaseCreatePost();
        postRepository = new PostRepository(this, GEMINI_API_KEY);
    }

    private void initViews() {
        togglePostType = findViewById(R.id.togglePostType);
        etThing = findViewById(R.id.etThing);
        etDate = findViewById(R.id.etDate);
        etAdditionalInfo = findViewById(R.id.etAdditionalInfo);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerMaterial = findViewById(R.id.spinnerMaterial);
        spinnerColor = findViewById(R.id.spinnerColor);
        spinnerSize = findViewById(R.id.spinnerSize);
        btnAddImage = findViewById(R.id.btnAddImage);
        btnSelectLocation = findViewById(R.id.btnSelectLocation);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvSelectedLocation = findViewById(R.id.tvSelectedLocation);
        imagesRecyclerView = findViewById(R.id.imagesRecyclerView);

        imagesAdapter = new SelectedImagesAdapter();
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imagesRecyclerView.setAdapter(imagesAdapter);
    }

    private void setupAdapters() {
        typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, TYPES);
        materialAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, MATERIALS);
        colorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, COLORS);
        sizeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, SIZES);
        spinnerType.setAdapter(typeAdapter);
        spinnerMaterial.setAdapter(materialAdapter);
        spinnerColor.setAdapter(colorAdapter);
        spinnerSize.setAdapter(sizeAdapter);
    }

    private void setupListeners() {
        togglePostType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnLost) {
                    isLostPost = true;
                    updateToggleVisuals(true); // Switch to Red Theme
                } else if (checkedId == R.id.btnFound) {
                    isLostPost = false;
                    updateToggleVisuals(false); // Switch to Green Theme
                }
            }
        });

        // Initialize state and visuals
        if (!isEditMode) {
            // Default to Lost
            togglePostType.check(R.id.btnLost);
            updateToggleVisuals(true);
        } else {
            // If editing, respect the loaded state
            if(isLostPost) {
                togglePostType.check(R.id.btnLost);
                updateToggleVisuals(true);
            } else {
                togglePostType.check(R.id.btnFound);
                updateToggleVisuals(false);
            }
        }

        btnAddImage.setOnClickListener(v -> showImageSourceDialog());
        btnSelectLocation.setOnClickListener(v -> selectLocation());
        btnSubmit.setOnClickListener(v -> submitPost());

        imagesAdapter.setOnImageRemoveListener((position, imageUri) -> {
            imagesAdapter.removeImage(position);
            boolean removed = false;
            for (int i = 0; i < selectedImageUris.size(); i++) {
                if (selectedImageUris.get(i).toString().equals(imageUri)) {
                    selectedImageUris.remove(i);
                    removed = true;
                    break;
                }
            }
            if (!removed && isEditMode && existingImageUrls != null) {
                existingImageUrls.remove(imageUri);
            }
        });

        // Date Picker updates BOTH the calendar variable (for logic) and the EditText (for UI)
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, day) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, day);
            updateDateLabel();
        };
        etDate.setOnClickListener(v -> new DatePickerDialog(AddPost.this, dateSetListener,
                myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show());
    }
    private void updateToggleVisuals(boolean isLostActive) {
        MaterialButton btnLost = findViewById(R.id.btnLost);
        MaterialButton btnFound = findViewById(R.id.btnFound);

        int colorLost = ContextCompat.getColor(this, R.color.status_lost);
        int colorFound = ContextCompat.getColor(this, R.color.status_found);
        int white = Color.WHITE;
        int transparent = Color.TRANSPARENT;

        if (isLostActive) {
            // "Searching" is Active -> Filled Red, White Text
            btnLost.setBackgroundTintList(ColorStateList.valueOf(colorLost));
            btnLost.setTextColor(white);
            btnLost.setStrokeColor(ColorStateList.valueOf(colorLost));

            // "Found" is Inactive -> Transparent, Green Text, Green Outline
            btnFound.setBackgroundTintList(ColorStateList.valueOf(transparent));
            btnFound.setTextColor(colorFound);
            btnFound.setStrokeColor(ColorStateList.valueOf(colorFound));
        } else {
            // "Searching" is Inactive -> Transparent, Red Text, Red Outline
            btnLost.setBackgroundTintList(ColorStateList.valueOf(transparent));
            btnLost.setTextColor(colorLost);
            btnLost.setStrokeColor(ColorStateList.valueOf(colorLost));

            // "Found" is Active -> Filled Green, White Text
            btnFound.setBackgroundTintList(ColorStateList.valueOf(colorFound));
            btnFound.setTextColor(white);
            btnFound.setStrokeColor(ColorStateList.valueOf(colorFound));
        }
    }
    private void updateDateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etDate.setText(sdf.format(myCalendar.getTime()));
    }

    // --- Image & Location Helpers (Same as before) ---
    private void showImageSourceDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Add Photo")
                .setItems(new String[]{"Take Photo", "Choose from Gallery"}, (dialog, which) -> {
                    if (which == 0) checkCameraPermissionAndOpen();
                    else pickImageFromGallery();
                }).show();
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            takePhotoWithCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhotoWithCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), REQUEST_IMAGE_PICK);
    }

    private void takePhotoWithCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) { return; }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.suraagh.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void selectLocation() {
        Intent intent = new Intent(this, LocationPicker.class);
        if (selectedLatitude != 0.0) {
            intent.putExtra("currentLatitude", selectedLatitude);
            intent.putExtra("currentLongitude", selectedLongitude);
        }
        startActivityForResult(intent, REQUEST_SELECT_LOCATION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                if (data.getClipData() != null) {
                    for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                        selectedImageUris.add(data.getClipData().getItemAt(i).getUri());
                        imagesAdapter.addImage(data.getClipData().getItemAt(i).getUri().toString());
                    }
                } else if (data.getData() != null) {
                    selectedImageUris.add(data.getData());
                    imagesAdapter.addImage(data.getData().toString());
                }
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (currentPhotoPath != null) {
                    Uri uri = Uri.fromFile(new File(currentPhotoPath));
                    selectedImageUris.add(uri);
                    imagesAdapter.addImage(uri.toString());
                }
            } else if (requestCode == REQUEST_SELECT_LOCATION && data != null) {
                selectedLatitude = data.getDoubleExtra("latitude", 0.0);
                selectedLongitude = data.getDoubleExtra("longitude", 0.0);
                selectedAddress = data.getStringExtra("address");
                tvSelectedLocation.setText(selectedAddress);
            }
        }
    }

    // --- Edit Mode Logic (Updated for Timestamp) ---
    private void checkEditMode() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("post")) {
            isEditMode = true;
            Post post = (Post) intent.getSerializableExtra("post");
            if (post != null) {
                loadExistingPostData(post);
                btnSubmit.setText("Update Post");
            }
        } else {
            isEditMode = false;
            btnSubmit.setText("Post Item");
        }
    }

    private void loadExistingPostData(Post post) {
        isLostPost = post instanceof LostPost;
        togglePostType.check(isLostPost ? R.id.btnLost : R.id.btnFound);
        if (post.getThing() != null) etThing.setText(post.getThing());
        if (post.getType() != null) spinnerType.setText(post.getType(), false);
        if (post.getMaterial() != null) spinnerMaterial.setText(post.getMaterial(), false);
        if (post.getColor() != null) spinnerColor.setText(post.getColor(), false);
        if (post.getSize() != null) spinnerSize.setText(post.getSize(), false);

        // UPDATED: Convert long timestamp -> String for UI
        if (post.getTimestamp() != 0) {
            Date date = new Date(post.getTimestamp());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            etDate.setText(sdf.format(date));
            myCalendar.setTime(date); // Sync logic variable
        }

        if (post.getAdditionalInfo() != null) etAdditionalInfo.setText(post.getAdditionalInfo());
        selectedLatitude = post.getLatitude();
        selectedLongitude = post.getLongitude();
        if (post.getAddress() != null) {
            selectedAddress = post.getAddress();
            tvSelectedLocation.setText(selectedAddress);
        }
        if (post.getImageUrl() != null) {
            existingImageUrls = new ArrayList<>(post.getImageUrl());
            for (String url : existingImageUrls) {
                imagesAdapter.addImage(url);
            }
        }
        existingPostId = post.getPostId();
    }

    // --- AI Helper ---
    private String encodeImageToBase64(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float bitmapRatio = (float) width / (float) height;
            if (bitmapRatio > 1) { width = 1024; height = (int) (width / bitmapRatio); }
            else { height = 1024; width = (int) (height * bitmapRatio); }
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.NO_WRAP);
        } catch (IOException e) { return null; }
    }

    // --- Submit Logic ---
    private void submitPost() {
        if (!validateForm())
        {
            Log.d(TAG,"Error in validate post ");
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText(isEditMode ? "Updating..." : "Processing AI & Uploading...");

        List<String> base64ImagesForAI = new ArrayList<>();
        if (!selectedImageUris.isEmpty()) {
            for (Uri uri : selectedImageUris) {
                String base64 = encodeImageToBase64(uri);
                if (base64 != null) base64ImagesForAI.add(base64);
            }
        }

        if (selectedImageUris.isEmpty()) {
            List<String> finalUrls = isEditMode ? existingImageUrls : new ArrayList<>();
            savePostToFirestore(finalUrls, base64ImagesForAI);
        } else {
            uploadImagesToCloudinary(base64ImagesForAI);
        }
    }

    private boolean validateForm() {
        if (etThing.getText().toString().trim().isEmpty()) {
            etThing.setError("Required"); return false;
        }
        if (etDate.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show(); return false;
        }
        if (selectedLatitude == 0.0) {
            Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show(); return false;
        }
        return true;
    }

    private void uploadImagesToCloudinary(List<String> base64ImagesForAI) {
        List<String> uploadedImageUrls = new ArrayList<>();
        if (isEditMode) uploadedImageUrls.addAll(existingImageUrls);

        final int totalImages = selectedImageUris.size();
        final int targetCount = totalImages + uploadedImageUrls.size();

        for (Uri imageUri : selectedImageUris) {
            MediaManager.get().upload(imageUri)
                    .unsigned("rljdzzcv") // <--- ADD THIS LINE (use the exact preset name you created)
                    .option("folder", "suraagh_posts")
                    .callback(new UploadCallback() {
                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String url = (String) resultData.get("secure_url");
                            if (url == null) url = (String) resultData.get("url");
                            uploadedImageUrls.add(url);

                            if (uploadedImageUrls.size() == targetCount) {
                                runOnUiThread(() -> savePostToFirestore(uploadedImageUrls, base64ImagesForAI));
                            }
                        }
                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            runOnUiThread(() -> {
                                Toast.makeText(AddPost.this, "Upload failed", Toast.LENGTH_SHORT).show();
                                btnSubmit.setEnabled(true);
                                btnSubmit.setText("Post Item");
                            });
                        }
                        @Override public void onStart(String requestId) {}
                        @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
                        @Override public void onReschedule(String requestId, ErrorInfo error) {}
                    }).dispatch();
        }
    }

    /**
     * Fetches the latest list of posts for this user from Firebase,
     * saves it to SharedPreferences, and then closes the activity.
     */
    private void refreshCacheAndFinish(String successMessage) {
        String userId = com.example.suraagh_deliverable_1.Utilities.FirebaseManager.getInstance().auth.getUid();
        LocalStorage storage = new LocalStorage(this);

        // We only need to refresh the specific list (Lost or Found) that was modified.
        if (isLostPost) {
            DatabaseGetLostPosts db = new FirebaseGetLostPosts();
            db.getLostPosts(userId, new CallBackForGetPost() {
                @Override
                public void onComplete(Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<Post> list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            LostPost p = doc.toObject(LostPost.class);
                            p.setPostId(doc.getId());
                            list.add(p);
                        }
                        // SAVE TO DISK
                        storage.saveLostPosts(list);
                        Log.d(TAG, "Cache updated for Lost Posts");
                    }
                    finishWithToast(successMessage);
                }
            });
        } else {
            DataBaseGetFoundPosts db = new FirebaseGetFoundPosts();
            db.getFoundPosts(userId, new CallBackForGetPost() {
                @Override
                public void onComplete(Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<Post> list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            FoundPost p = doc.toObject(FoundPost.class);
                            p.setPostId(doc.getId());
                            list.add(p);
                        }
                        // SAVE TO DISK
                        storage.saveFoundPosts(list);
                        Log.d(TAG, "Cache updated for Found Posts");
                    }
                    finishWithToast(successMessage);
                }
            });
        }
    }

    private void finishWithToast(String msg) {
        Toast.makeText(AddPost.this, msg, Toast.LENGTH_LONG).show();
        finish();
    }
    private void savePostToFirestore(List<String> imageUrls, List<String> base64ImagesForAI) {
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "unknown";

        Post post = isLostPost ? new LostPost() : new FoundPost();
        // ... (Set all fields: thing, timestamp, userId, etc.) ...
        post.setUserId(currentUserId);
        post.setThing(etThing.getText().toString().trim());
        post.setType(spinnerType.getText().toString());
        post.setMaterial(spinnerMaterial.getText().toString());
        post.setColor(spinnerColor.getText().toString());
        post.setSize(spinnerSize.getText().toString());
        post.setTimestamp(myCalendar.getTimeInMillis());
        post.setAdditionalInfo(etAdditionalInfo.getText().toString().trim());
        post.setLongitude(selectedLongitude);
        post.setLatitude(selectedLatitude);
        post.setAddress(selectedAddress);
        post.setImageUrl(imageUrls);

        String collectionName = isLostPost ? "lostPosts" : "foundPosts";

        if (isEditMode && existingPostId != null) {
            // --- UPDATE (Now uses AI Repo) ---
            Log.d(TAG, "Updating with AI Logic...");
            postRepository.updatePostWithAI(post, existingPostId, base64ImagesForAI, isLostPost, new PostRepository.PostCallback() {
                @Override
                public void onSuccess(String message) {
                    // OLD: Toast.makeText(AddPost.this, message, Toast.LENGTH_SHORT).show();
                    // OLD: finish();

                    // NEW: Update Cache first
                    refreshCacheAndFinish(message);
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(AddPost.this, "Update Failed: " + error, Toast.LENGTH_LONG).show();
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Update Post");
                }
            });
        } else {
            // --- CREATE LOGIC ---
            Log.d(TAG, "Creating with AI Logic...");
            postRepository.createPostWithAI(post, base64ImagesForAI, isLostPost, new PostRepository.PostCallback() {
                @Override
                public void onSuccess(String message) {
                    // OLD: Toast.makeText(AddPost.this, message, Toast.LENGTH_LONG).show();
                    // OLD: finish();

                    // NEW: Update Cache first
                    refreshCacheAndFinish(message);
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(AddPost.this, "Create Failed: " + error, Toast.LENGTH_LONG).show();
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Post Item");
                }
            });
        }
    }}