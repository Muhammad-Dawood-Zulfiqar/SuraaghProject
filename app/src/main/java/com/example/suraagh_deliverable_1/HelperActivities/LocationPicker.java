package com.example.suraagh_deliverable_1.HelperActivities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.suraagh_deliverable_1.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocationPicker extends AppCompatActivity {

    private static final String TAG = "LocationPicker";
    private static final int PERMISSION_REQUEST_CODE = 100;

    private MapView mapView;
    private IMapController mapController;
    private Marker locationMarker;
    private TextView tvSelectedLocation;
    private EditText etSearchLocation;
    private ImageView btnSearch;
    private Button btnDone;
    private FloatingActionButton btnCurrentLocation; // Updated type to FAB

    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;
    private String selectedAddress = "";

    private FusedLocationProviderClient fusedLocationClient;

    // Background Threading for Network Calls
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // OSM Configuration for User Agent
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_location_picker);

        // Initialize Views
        mapView = findViewById(R.id.mapView);
        tvSelectedLocation = findViewById(R.id.tvSelectedLocation);
        etSearchLocation = findViewById(R.id.etSearchLocation);
        btnSearch = findViewById(R.id.btnSearch);
        btnDone = findViewById(R.id.btnDone);
        btnCurrentLocation = findViewById(R.id.btnCurrentLocation);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupMap();
        setupListeners();
        checkLocationPermission();
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapController = mapView.getController();
        mapController.setZoom(15.0);

        // Default start point (Lahore)
        GeoPoint startPoint = new GeoPoint(31.5204, 74.3587);
        mapController.setCenter(startPoint);

        // Tap Listener
        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                handleMapTap(p);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };

        MapEventsOverlay OverlayEvents = new MapEventsOverlay(mReceive);
        mapView.getOverlays().add(OverlayEvents);
    }

    private void setupListeners() {
        // Search Button Logic
        btnSearch.setOnClickListener(v -> {
            String query = etSearchLocation.getText().toString().trim();
            if (!query.isEmpty()) {
                performSearch(query);
            }
        });

        // Done Button Logic
        btnDone.setOnClickListener(v -> returnLocationToCaller());

        // Current Location FAB Logic
        btnCurrentLocation.setOnClickListener(v -> getCurrentLocation());
    }

    private void handleMapTap(GeoPoint geoPoint) {
        // 1. Visual Update
        updateMarker(geoPoint);
        selectedLatitude = geoPoint.getLatitude();
        selectedLongitude = geoPoint.getLongitude();

        tvSelectedLocation.setText("Fetching address...");

        // 2. Background Network Call
        executor.execute(() -> {
            String addressResult = "Unknown Location";
            try {
                Geocoder geocoder = new Geocoder(LocationPicker.this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(geoPoint.getLatitude(), geoPoint.getLongitude(), 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    if (address.getMaxAddressLineIndex() >= 0) {
                        addressResult = address.getAddressLine(0);
                    } else {
                        addressResult = address.getLocality();
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Geocoding failed", e);
                addressResult = "Location Selected"; // Fallback text
            }

            // 3. Update UI
            String finalAddress = addressResult;
            mainHandler.post(() -> {
                selectedAddress = finalAddress;
                tvSelectedLocation.setText(finalAddress);
            });
        });
    }

    private void performSearch(String locationName) {
        Toast.makeText(this, "Searching...", Toast.LENGTH_SHORT).show();

        executor.execute(() -> {
            try {
                Geocoder geocoder = new Geocoder(LocationPicker.this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocationName(locationName, 1);

                mainHandler.post(() -> {
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        GeoPoint geoPoint = new GeoPoint(address.getLatitude(), address.getLongitude());

                        mapController.setCenter(geoPoint);
                        mapController.setZoom(18.0);
                        handleMapTap(geoPoint);
                    } else {
                        Toast.makeText(LocationPicker.this, "Location not found", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (IOException e) {
                Log.e(TAG, "Search failed", e);
                mainHandler.post(() ->
                        Toast.makeText(LocationPicker.this, "Search error", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void updateMarker(GeoPoint geoPoint) {
        if (locationMarker == null) {
            locationMarker = new Marker(mapView);
            locationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            locationMarker.setTitle("Selected Location");
            mapView.getOverlays().add(locationMarker);
        }
        locationMarker.setPosition(geoPoint);
        mapView.invalidate();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission needed", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                GeoPoint currentGeoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                mapController.setCenter(currentGeoPoint);
                mapController.setZoom(18.0);
                handleMapTap(currentGeoPoint);
            } else {
                Toast.makeText(this, "Waiting for location...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void returnLocationToCaller() {
        if (selectedLatitude == 0.0 && selectedLongitude == 0.0) {
            Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("latitude", selectedLatitude);
        resultIntent.putExtra("longitude", selectedLongitude);
        resultIntent.putExtra("address", selectedAddress);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown(); // Clean up threads
    }
}