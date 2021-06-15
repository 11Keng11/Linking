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
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.BuildConfig
import com.example.linking_application_android.compass.CompassBottomSheetFragment
import com.example.linking_application_android.databinding.ActivityMapsBinding
import com.example.linking_application_android.route.RouteGenFragment
import com.example.linking_application_android.util.InternetUtil
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
import java.util.*
import kotlin.jvm.internal.Intrinsics

class MapsActivity : FragmentActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    private var binding: ActivityMapsBinding? = null

    // Landmarks
    private var natureValues // List of nature landmarks data [name, lon, lat, type]
            : List<List<Any?>>? = null
    private var exerciseValues // List of excercise landmarks data [name, lon, lat, type]
            : List<List<Any?>>? = null
    private var playValues // List of play landmarks data [name, lon, lat, type]
            : List<List<Any?>>? = null
    private var generalValues // List of family landmarks data [name, lon, lat, type]
            : List<List<Any?>>? = null

    // Markers
    private var natureMarkers // ArrayList of nature markers
            : ArrayList<Marker>? = null
    private var playMarkers // ArrayList of play markers
            : ArrayList<Marker>? = null
    private var exerciseMarkers // ArrayList of exercise markers
            : ArrayList<Marker>? = null
    private var generalMarkers // ArrayList of general markers
            : ArrayList<Marker>? = null

    // FABs
    private lateinit var natFab // Nature filter fab
            : FabOption
    private lateinit var exFab // exercise filter fab
            : FabOption
    private lateinit var plaFab // family filter fab
            : FabOption
    private lateinit var genFab // general filter fab
            : FabOption
    private lateinit var bleFab // Test bluetooth scanning
            : FloatingActionButton
    private lateinit var openFab // route gen and compass fab
            : FloatingActionButton


    private var natVisible = true // State - whether nature markers are visible
    private var exVisible = true // State - whether exercise markers are visible
    private var plaVisible = true // State - whether play markers are visible
    private var genVisible = true // State - whether general markers are visible

    var isRoute = false // State - whether there is an active route
    var path // ArrayList of markers for route
        : ArrayList<Marker>? = null

    // Google sheet keys.
    private var google_api_key: String = "AIzaSyDqJlXlJFXnGGjVXJs8maiUP5rE9oKsOB4"
    private var sheet_id: String = "1hMrCgWmaN3hDmQOaIBUBcuqSXWbX8pI6d6WElL7-lrU"

    // Sheets
    private var sheetsService: Sheets? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*  Bluetooth  */
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
        /* ********* */binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        if (networkAvailable()) {
            initialiseSheets()
            initialiseMaps()
        } else{
            Log.d("tag", "Network unavailable")
            Toast.makeText(this.applicationContext, "Please enable internet!", Toast.LENGTH_LONG)
        }

        initialiseUi();

        // Start Location Scanning
        airLocation.start()
    }

    private fun initialiseMaps() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?

        mapFragment!!.getMapAsync(this)
    }

    private fun initialiseSheets() {
        // Initialise google sheets
        val transport = AndroidHttp.newCompatibleTransport()
        val factory: JsonFactory = JacksonFactory.getDefaultInstance()
        sheetsService = Sheets.Builder(transport, factory, null)
            .setApplicationName("Linking")
            .build()
    }

    private fun initialiseUi() {
        // Set listeners for the landmarks filter
        natFab = findViewById(R.id.natfab)
        exFab = findViewById(R.id.exfab)
        plaFab = findViewById(R.id.plafab)
        genFab = findViewById(R.id.genfab)
        bleFab = findViewById(R.id.blefab)
        openFab = findViewById(R.id.openfab)


        natFab.setOnClickListener(View.OnClickListener {
            natVisible = !natVisible
            changeVisibility(natureMarkers, natVisible)
        })

        exFab.setOnClickListener(View.OnClickListener {
            exVisible = !exVisible
            changeVisibility(exerciseMarkers, exVisible)
        })

        plaFab.setOnClickListener(View.OnClickListener {
            plaVisible = !plaVisible
            changeVisibility(playMarkers, plaVisible)
        })

        genFab.setOnClickListener(View.OnClickListener {
            genVisible = !genVisible
            changeVisibility(generalMarkers, genVisible)
        })

        bleFab.setOnClickListener(View.OnClickListener {
            // Run your function to scan and print a toast if successful
            // I will use this as a condition to check whether a landmark has been visited.

            /*  Bluetooth  */
            if (!isScanning) {
                startBleService()
                Toast.makeText(applicationContext, "Starting Scan",
                    Toast.LENGTH_SHORT).show()
            }
            else {
//                Toast.makeText(applicationContext, "Stopping Scan",
//                    Toast.LENGTH_SHORT).show()
                Toast.makeText(applicationContext, "Restarting Scan",
                    Toast.LENGTH_SHORT).show()
                stopBleService()
                startBleService()
            }
        })

        openFab.setOnClickListener(View.OnClickListener { // Run your function to scan and print a toast if successful
            if (isRoute) {
                supportFragmentManager.let {
                    CompassBottomSheetFragment.newInstance(Bundle()).apply {
                        show(it, tag)
                    }
                }
            } else {
                var dialog = RouteGenFragment()
                dialog.show(supportFragmentManager,"RouteGen")
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun networkAvailable(): Boolean {
        val internetUtil = InternetUtil()
        return internetUtil.isOnline(this.applicationContext)
    }

    fun setRoute( route : ArrayList<String>) {

        isRoute = true
        val node_1 = getIcon("marker_1", this, packageName)
        val node_2 = getIcon("marker_2", this, packageName)
        val node_3 = getIcon("marker_3", this, packageName)
        val node_4 = getIcon("marker_4", this, packageName)

        for (mkr in natureMarkers!!) {
            if (mkr.title == route.get(0)) {
                mkr.setIcon(node_1)
            } else if (mkr.title == route.get(3)) {
                mkr.setIcon(node_4)
            } else if (mkr.title == route.get(1)) {
                mkr.setIcon(node_2)
            }
        }
        for (mkr in generalMarkers!!) {
            if (mkr.title == route.get(2)) {
                mkr.setIcon(node_3)
            }
        }
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
        val natureIcon = getIcon("marker_nature", this, packageName)
        val playIcon = getIcon("marker_play", this, packageName)
        val exerciseIcon = getIcon("marker_exercise", this, packageName)
        val generalIcon = getIcon("marker_general", this, packageName)

        // Get marker values from google sheet
        val getMarkerValues = Thread(object : Runnable {
            var gotData = false
            override fun run() {
                while (!gotData) {
                    natureValues = readSheet("Nature!A2:D", sheetsService, google_api_key, sheet_id)
                    exerciseValues = readSheet("Exercise!A2:D", sheetsService, google_api_key, sheet_id)
                    playValues = readSheet("Play!A2:D", sheetsService, google_api_key, sheet_id)
                    generalValues = readSheet("General!A2:D", sheetsService, google_api_key, sheet_id)
                    if (natureValues != null && exerciseValues != null && generalValues != null && generalValues != null) {
                        gotData = true
                        break
                    }
                }
            }
        })
        getMarkerValues.start()

        // Wait till values are read from google sheet
        while (generalValues == null) {
            continue
        }

        // Set the markers in the map
        natureMarkers = setMarkers(natureValues, mMap, natureIcon)
        exerciseMarkers = setMarkers(exerciseValues, mMap, exerciseIcon)
        playMarkers = setMarkers(playValues, mMap, playIcon)
        generalMarkers = setMarkers(generalValues, mMap, generalIcon)

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
    private var isReceiverRegistered = false
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
            setIsScanning(true, bleFab)
            val intent = Intent(this, BLEService::class.java)
            startService(intent)
            isReceiverRegistered = true
            registerReceiver(receiver, IntentFilter("GET_HELLO"))
        }
    }
    fun stopBleService() {
        Log.i("BLEService","Stop BLE Service.")
        setIsScanning(false, bleFab)
        val intent = Intent(this, BLEService::class.java)
        stopService(intent)
        Log.e("onStopr error", "stopping scan")
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
     * For Location Services (this is not a service, it does not run in background)
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
