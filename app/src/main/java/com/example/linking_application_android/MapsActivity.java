package com.example.linking_application_android;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.viewbinding.BuildConfig;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.linking_application_android.databinding.ActivityMapsBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.common.collect.Iterables;
import com.nambimobile.widgets.efab.ExpandableFab;
import com.nambimobile.widgets.efab.FabOption;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kotlin.Lazy;
import kotlin.LazyKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import timber.log.Timber;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private List<List<Object>> natureValues; // List of nature landmarks data [name, lon, lat, type]
    private List<List<Object>> exerciseValues; // List of ecercise landmarks data [name, lon, lat, type]
    private List<List<Object>> familyValues; // List of family landmarks data [name, lon, lat, type]
    private ArrayList<Marker> natureMarkers; // ArrayList of nature markers
    private ArrayList<Marker> familyMarkers;  // ArrayList of family markers
    private ArrayList<Marker> exerciseMarkers; // ArrayList of exercise markers
    private FabOption natFab; // Nature filter fab
    private FabOption exFab; // exercise filter fab
    private FabOption famFab; // family filter fab
    private FloatingActionButton bleTest; // Test bluetooth scanning
    private boolean natVisible = true; // State - whether nature markers are visible
    private boolean exVisible = true; // State - whether exercise markers are visible
    private boolean famVisible = true; // State - whether family markers are visible
    private String google_api_key;
    private String sheet_id;
    private Sheets sheetsService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*  Bluetooth  */
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        /* ********* */

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Temporarily store keys and id here. Will shift to a secure config file later on.
        google_api_key = "AIzaSyDqJlXlJFXnGGjVXJs8maiUP5rE9oKsOB4";
        sheet_id = "1hMrCgWmaN3hDmQOaIBUBcuqSXWbX8pI6d6WElL7-lrU";

        // Initialise google sheets
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory factory = JacksonFactory.getDefaultInstance();
        sheetsService = new Sheets.Builder(transport, factory, null)
                .setApplicationName("Linking")
                .build();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Set listeners for the landmarks filter
        natFab = findViewById(R.id.natfab);
        exFab = findViewById(R.id.exfab);
        famFab = findViewById(R.id.famfab);
        bleTest = findViewById(R.id.bletest);

        natFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                natVisible = !natVisible;
                changeVisibility(natFab,natureMarkers, natVisible);
            }
        });

        exFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exVisible = !exVisible;
                changeVisibility(exFab,exerciseMarkers, exVisible);
            }
        });

        famFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                famVisible = !famVisible;
                changeVisibility(famFab,familyMarkers, famVisible);
            }
        });

        bleTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Run your function to scan and print a toast if successful
                // I will use this as a condition to check whether a landmark has been visited.
                Toast.makeText(getApplicationContext(), "Hi QI Feng",
                        Toast.LENGTH_LONG).show();
                /*  Bluetooth  */
                if (!isScanning) {
                    startBleService();
                }
                else {
                    stopBleService();
                }
                /* ********* */
            }
        });

    }

    // Change visibility of markers
    public void changeVisibility(FabOption fab, ArrayList<Marker> markers, boolean isVisible) {
        for (Marker m : markers) {
            m.setVisible(isVisible);
        }
    }

    // This method retrieves the correct icon for the respective markers. ie nature exercise and family
    public BitmapDescriptor getIcon(String item){
        Bitmap marker = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(item, "drawable", getPackageName()));
        Bitmap sizedMarker = Bitmap.createScaledBitmap(marker, 61, 90, false);
        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(sizedMarker);
        return icon;
    }

    public List<List<Object>> readSheet(String sheetRange) {
        List<List<Object>> values = new ArrayList<>();
        try {
            ValueRange data = sheetsService.spreadsheets().values()
                    .get(sheet_id, sheetRange)
                    .setKey(google_api_key)
                    .execute();
            values = data.getValues();
            return values;
        } catch (IOException e) {
            Log.e("Sheets failed", e.getLocalizedMessage());
        }
        return values;
    }

    // Set the markers on the map
    public ArrayList<Marker> setMarkers(List<List<Object>> values, GoogleMap mapObj, BitmapDescriptor markerIcon ) {
        ArrayList<Marker> markers = new ArrayList<Marker>();
        for (List row : values) {
            String name = row.get(0).toString();
            Float lat = Float.parseFloat(row.get(2).toString());
            Float lon = Float.parseFloat(row.get(1).toString());
            LatLng pos = new LatLng(lat,lon);
            String type = row.get(3).toString();
            Marker newMarker;
            newMarker = mapObj.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(name)
                    .snippet(type)
                    .icon(markerIcon));
            markers.add(newMarker);
        }
        return markers;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we set map style, bounds and markers.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set map style
        MapStyleOptions mapStyleOptions=MapStyleOptions.loadRawResourceStyle(this,R.raw.map_style);
        mMap.setMapStyle(mapStyleOptions);

        // Set map bounds
        LatLngBounds tampinesBounds = new LatLngBounds(
                new LatLng(1.343214, 103.925226), // SW bounds
                new LatLng(1.366954, 103.963879)  // NE bounds
        );
        mMap.setLatLngBoundsForCameraTarget(tampinesBounds);

        // Add markers for landmarks
        BitmapDescriptor natureIcon = getIcon("naturemarker");
        BitmapDescriptor familyIcon = getIcon("familymarker");
        BitmapDescriptor exerciseIcon = getIcon("exercisemarker");

        // Get marker values from google sheet
        Thread getMarkerValues = new Thread(new Runnable() {
            boolean gotData = false;
            @Override
            public void run() {
                while (!gotData) {
                    natureValues = readSheet("Nature!A2:D");
                    exerciseValues = readSheet("Exercise!A2:D");
                    familyValues = readSheet("Family!A2:D");
                    if (natureValues != null && exerciseValues != null && familyValues != null) {
                        gotData = true;
                        break;
                    }
                }
            }
        });
        getMarkerValues.start();

        // temporary method
        while (familyValues == null) {
            Log.d("Check Thread:", "thread still running");
        }

        // Set the markers in the map
        natureMarkers = setMarkers(natureValues, mMap, natureIcon);
        exerciseMarkers = setMarkers(exerciseValues, mMap, exerciseIcon);
        familyMarkers = setMarkers(familyValues, mMap, familyIcon);

        // Center map on tampines and set zoom
        LatLng tampines = new LatLng(1.3525, 103.9447);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(tampines));
        mMap.setMinZoomPreference(15.0f);
        mMap.setMaxZoomPreference(25.0f);
    }

    /*  Bluetooth  */
    private boolean isScanning = false;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver receiver;

    private void setBluetoothManager() {
        bluetoothManager =
                (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
    }
    private void setBluetoothAdapter() {
        bluetoothAdapter = bluetoothManager.getAdapter();
    }
    private void setReceiver() {
        receiver = new BroadcastReceiver() {
            @SuppressLint("TimberArgCount")
            public void onReceive(@Nullable Context context, @NotNull Intent intent) {
                Intrinsics.checkNotNullParameter(intent, "intent");
                Timber.i("Hello from BLEService!");
                stopBleService();
                Toast.makeText(getApplicationContext(), "Successful BLE!",
                        Toast.LENGTH_LONG).show();
            }
        };
    }
    private final boolean isLocationPermissionGranted() {
        return this.hasPermission(this, "android.permission.ACCESS_FINE_LOCATION");
    }
    private void startBleService() {
        Timber.i( "Start BLE service");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted()) {
            requestLocationPermission();
        } else {
            setIsScanning(true, bleTest);
            Intent intent = new Intent(this, BLEService.class);
            startService(intent);
            registerReceiver(receiver, new IntentFilter("GET_HELLO"));
        }
    }
    void stopBleService() {
        Timber.i( "Stop BLE Service.");
        setIsScanning(false, bleTest);
        Intent intent = new Intent(this, BLEService.class);
        stopService(intent);
    }
    private void setIsScanning(Boolean isScan, FloatingActionButton button){
        isScanning = isScan;
//        if (isScan){
//            button.setText("Stop scan");
//        }else{
//            button.setText("Start scan");
//        }
    }
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String @NotNull [] permissions,
            int @NotNull [] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.LOCATION_PERMISSION_REQUEST_CODE:
                if (firstOrNull(grantResults) == PackageManager.PERMISSION_DENIED) {
                    requestLocationPermission();
                } else {
//                    startBleService();
                }
        }
    }
    protected int firstOrNull(@NotNull int[] grantResults) {
        return grantResults[0];
    }
    private final void promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE");
            this.startActivityForResult(enableBtIntent, 1);
        }

    }

    private final void requestLocationPermission() {
        if (!this.isLocationPermissionGranted()) {
            this.requestPermission(this, "android.permission.ACCESS_FINE_LOCATION", 2);
        }
    }
    private boolean hasPermission(Context $this$hasPermission, String permissionType) {
        return ContextCompat.checkSelfPermission($this$hasPermission, permissionType) == 0;
    }

    private void requestPermission(Activity $this$requestPermission, String permission, int requestCode) {
        ActivityCompat.requestPermissions($this$requestPermission, new String[]{permission}, requestCode);
    }
    @Override
    protected void onResume() {
        super.onResume();
        setBluetoothManager();
        setBluetoothAdapter();
        setReceiver();
        if (!bluetoothAdapter.isEnabled()) {
            promptEnableBluetooth();
        }else{
//            startBleService();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data ) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.ENABLE_BLUETOOTH_REQUEST_CODE:
                if (resultCode != Activity.RESULT_OK) {
                    promptEnableBluetooth();
                }
        }
    }
    public final class Constants {
        private static final int ENABLE_BLUETOOTH_REQUEST_CODE = 1;
        private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    }
    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver); //<-- Unregister to avoid memoryleak
    }
    /* ********* */

}