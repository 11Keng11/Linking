package com.example.linking_application_android

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.BuildConfig
import com.example.linking_application_android.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.nambimobile.widgets.efab.FabOption
import mumayank.com.airlocationlibrary.AirLocation
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.io.IOException
import java.util.*
import kotlin.jvm.internal.Intrinsics

class MapsActivity : FragmentActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    private var binding: ActivityMapsBinding? = null

    // Landmarks
    private var natureValues // List of nature landmarks data [name, lon, lat, type]
            : List<List<Any?>>? = null
    private var exerciseValues // List of ecercise landmarks data [name, lon, lat, type]
            : List<List<Any?>>? = null
    private var familyValues // List of family landmarks data [name, lon, lat, type]
            : List<List<Any?>>? = null

    // Markers
    private var natureMarkers // ArrayList of nature markers
            : ArrayList<Marker>? = null
    private var familyMarkers // ArrayList of family markers
            : ArrayList<Marker>? = null
    private var exerciseMarkers // ArrayList of exercise markers
            : ArrayList<Marker>? = null

    // FABs
    private lateinit var natFab // Nature filter fab
            : FabOption
    private lateinit var exFab // exercise filter fab
            : FabOption
    private lateinit var famFab // family filter fab
            : FabOption
    private lateinit var bleTest // Test bluetooth scanning
            : FloatingActionButton
    private lateinit var compassFab // family filter fab
            : FloatingActionButton
    private var natVisible = true // State - whether nature markers are visible
    private var exVisible = true // State - whether exercise markers are visible
    private var famVisible = true // State - whether family markers are visible

    // API Keys
    private var google_api_key: String? = null

    // Sheets
    private var sheet_id: String? = null
    private var sheetsService: Sheets? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*  Bluetooth  */if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
        /* ********* */binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        // Temporarily store keys and id here. Will shift to a secure config file later on.
        google_api_key = "AIzaSyDqJlXlJFXnGGjVXJs8maiUP5rE9oKsOB4"
        sheet_id = "1hMrCgWmaN3hDmQOaIBUBcuqSXWbX8pI6d6WElL7-lrU"

        // Initialise google sheets
        val transport = AndroidHttp.newCompatibleTransport()
        val factory: JsonFactory = JacksonFactory.getDefaultInstance()
        sheetsService = Sheets.Builder(transport, factory, null)
                .setApplicationName("Linking")
                .build()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        // Set listeners for the landmarks filter
        natFab = findViewById(R.id.natfab)
        exFab = findViewById(R.id.exfab)
        famFab = findViewById(R.id.famfab)
        bleTest = findViewById(R.id.bletest)
        compassFab = findViewById(R.id.compassFab)

        natFab.setOnClickListener(View.OnClickListener {
            natVisible = !natVisible
            changeVisibility(natFab, natureMarkers, natVisible)
        })

        exFab.setOnClickListener(View.OnClickListener {
            exVisible = !exVisible
            changeVisibility(exFab, exerciseMarkers, exVisible)
        })

        famFab.setOnClickListener(View.OnClickListener {
            famVisible = !famVisible
            changeVisibility(famFab, familyMarkers, famVisible)
        })

        bleTest.setOnClickListener(View.OnClickListener { // Run your function to scan and print a toast if successful
            // I will use this as a condition to check whether a landmark has been visited.

            /*  Bluetooth  */
            if (!isScanning) {
                Toast.makeText(applicationContext, "Starting Scan",
                    Toast.LENGTH_SHORT).show()
                startBleService()
            } else {
                Toast.makeText(applicationContext, "Stopping Scan",
                    Toast.LENGTH_SHORT).show()
                stopBleService()
            }
            /* ********* */
        })

        compassFab.setOnClickListener(View.OnClickListener { // Run your function to scan and print a toast if successful
            Toast.makeText(applicationContext, "Open Bottom Sheet Dialog",
                    Toast.LENGTH_LONG).show()
        })

        // Start Location Scanning
        airLocation.start()
    }

    // Change visibility of markers
    fun changeVisibility(fab: FabOption?, markers: ArrayList<Marker>?, isVisible: Boolean) {
        for (m in markers!!) {
            m.isVisible = isVisible
        }
    }

    // This method retrieves the correct icon for the respective markers. ie nature exercise and family
    fun getIcon(item: String?): BitmapDescriptor {
        val marker = BitmapFactory.decodeResource(resources, resources.getIdentifier(item, "drawable", packageName))
        val sizedMarker = Bitmap.createScaledBitmap(marker, 61, 90, false)
        return BitmapDescriptorFactory.fromBitmap(sizedMarker)
    }

    fun readSheet(sheetRange: String?): List<List<Any?>> {
        var values: List<List<Any?>> = ArrayList()
        try {
            val data = sheetsService!!.spreadsheets().values()[sheet_id, sheetRange]
                    .setKey(google_api_key)
                    .execute()
            values = data.getValues()
            return values
        } catch (e: IOException) {
            Log.e("Sheets failed", e.localizedMessage)
        }
        return values
    }

    // Set the markers on the map
    fun setMarkers(values: List<List<Any?>>?, mapObj: GoogleMap?, markerIcon: BitmapDescriptor?): ArrayList<Marker> {
        val markers = ArrayList<Marker>()
        for (row in values!!) {
            val name = row[0].toString()
            val lat = row[2].toString().toFloat()
            val lon = row[1].toString().toFloat()
            val pos = LatLng(lat.toDouble(), lon.toDouble())
            val type = row[3].toString()
            var newMarker: Marker
            newMarker = mapObj!!.addMarker(MarkerOptions()
                    .position(pos)
                    .title(name)
                    .snippet(type)
                    .icon(markerIcon))
            markers.add(newMarker)
        }
        return markers
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Set map style
        val mapStyleOptions = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style)
        mMap!!.setMapStyle(mapStyleOptions)

        // Set map bounds
        val tampinesBounds = LatLngBounds(
                LatLng(1.343214, 103.925226),  // SW bounds
                LatLng(1.366954, 103.963879) // NE bounds
        )
        mMap!!.setLatLngBoundsForCameraTarget(tampinesBounds)

        // Add markers for landmarks
        val natureIcon = getIcon("naturemarker")
        val familyIcon = getIcon("familymarker")
        val exerciseIcon = getIcon("exercisemarker")

        // Get marker values from google sheet
        val getMarkerValues = Thread(object : Runnable {
            var gotData = false
            override fun run() {
                while (!gotData) {
                    natureValues = readSheet("Nature!A2:D")
                    exerciseValues = readSheet("Exercise!A2:D")
                    familyValues = readSheet("Family!A2:D")
                    if (natureValues != null && exerciseValues != null && familyValues != null) {
                        gotData = true
                        break
                    }
                }
            }
        })
        getMarkerValues.start()

        // temporary method
        while (familyValues == null) {
            Log.d("Check Thread:", "thread still running")
        }

        // Set the markers in the map
        natureMarkers = setMarkers(natureValues, mMap, natureIcon)
        exerciseMarkers = setMarkers(exerciseValues, mMap, exerciseIcon)
        familyMarkers = setMarkers(familyValues, mMap, familyIcon)

        // Center map on tampines and set zoom
        val tampines = LatLng(1.3525, 103.9447)
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(tampines))
        mMap!!.setMinZoomPreference(15.0f)
        mMap!!.setMaxZoomPreference(25.0f)
    }

    /*  Bluetooth  */
    private var isScanning = false
    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var receiver: BroadcastReceiver? = null
    private var isReceiverRegistered = false;
    private fun setBluetoothManager() {
        bluetoothManager = this.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
    }

    private fun setBluetoothAdapter() {
        bluetoothAdapter = bluetoothManager!!.adapter
    }

    private fun setReceiver() {
        receiver = object : BroadcastReceiver() {
            @SuppressLint("TimberArgCount")
            override fun onReceive(context: Context?, intent: Intent) {
                Intrinsics.checkNotNullParameter(intent, "intent")
                Timber.i("Hello from BLEService!")
                stopBleService()
                Toast.makeText(applicationContext, "Successful BLE!",
                        Toast.LENGTH_LONG).show()
            }
        }
    }

    private val isLocationPermissionGranted: Boolean
    private get() = hasPermission(this, "android.permission.ACCESS_FINE_LOCATION")

    private fun startBleService() {
        Timber.i("Start BLE service")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
            requestLocationPermission()
        } else {
            setIsScanning(true, bleTest)
            val intent = Intent(this, BLEService::class.java)
            startService(intent)
            isReceiverRegistered = true
            registerReceiver(receiver, IntentFilter("GET_HELLO"))
        }
    }

    fun stopBleService() {
        Timber.i("Stop BLE Service.")
        setIsScanning(false, bleTest)
        val intent = Intent(this, BLEService::class.java)
        stopService(intent)
    }

    private fun setIsScanning(isScan: Boolean, button: FloatingActionButton?) {
        isScanning = isScan

//        if (isScan){
//            button.setText("Stop scan");
//        } else{
//            button.setText("Start scan");
//        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // AirLocation requests location permissions.
        airLocation.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // TODO: For Review: Lines below may be obsolete as location is already requested above.
        when (requestCode) {
            Constants.LOCATION_PERMISSION_REQUEST_CODE -> if (firstOrNull(grantResults) == PackageManager.PERMISSION_DENIED) {
                requestLocationPermission()
            } else {
                // startBleService();
            }
        }
    }

    protected fun firstOrNull(grantResults: IntArray): Int {
        return grantResults[0]
    }

    private fun promptEnableBluetooth() {
        if (!bluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent("android.bluetooth.adapter.action.REQUEST_ENABLE")
            this.startActivityForResult(enableBtIntent, 1)
        }
    }

    private fun requestLocationPermission() {
        if (!isLocationPermissionGranted) {
            requestPermission(this, "android.permission.ACCESS_FINE_LOCATION", 2)
        }
    }

    private fun hasPermission(`$this$hasPermission`: Context, permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(`$this$hasPermission`, permissionType) == 0
    }

    private fun requestPermission(`$this$requestPermission`: Activity, permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(`$this$requestPermission`, arrayOf(permission), requestCode)
    }

    override fun onResume() {
        super.onResume()
        setBluetoothManager()
        setBluetoothAdapter()
        setReceiver()
        if (!bluetoothAdapter!!.isEnabled) {
            promptEnableBluetooth()
        } else {
            // startBleService();
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // AirLocation
        airLocation.onActivityResult(requestCode, resultCode, data)

        // Bluetooth
        when (requestCode) {
            Constants.ENABLE_BLUETOOTH_REQUEST_CODE -> if (resultCode != RESULT_OK) {
                promptEnableBluetooth()
            }
        }
    }

    object Constants {
        const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
        const val LOCATION_PERMISSION_REQUEST_CODE = 2
    }

    override fun onStop() {
        super.onStop()
        if (isReceiverRegistered){
            unregisterReceiver(receiver) //<-- Unregister to avoid memoryleak
            isReceiverRegistered = false
        }
    }

    /**
     * For Location Services
     */
    private val airLocation = AirLocation(this, object : AirLocation.Callback {

        override fun onSuccess(locations: ArrayList<Location>) {
            Log.d("TAG", locations.toString())
        }

        override fun onFailure(locationFailedEnum: AirLocation.LocationFailedEnum) {
            Log.d("TAG", locationFailedEnum.toString())
        }
    })

}
