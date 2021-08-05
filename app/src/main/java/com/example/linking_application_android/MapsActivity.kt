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
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.BuildConfig
import com.example.linking_application_android.compass.CompassBottomSheetFragment
import com.example.linking_application_android.databinding.ActivityMapsBinding
import com.example.linking_application_android.profile.ProfileActivity
import com.example.linking_application_android.helper.BitmapHelper
import com.example.linking_application_android.helper.StorageHelper
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
import nl.dionsegijn.konfetti.KonfettiView
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.util.*
import kotlin.jvm.internal.Intrinsics


class MapsActivity : FragmentActivity(), OnMapReadyCallback {

    //Maps
    private var mMap: GoogleMap? = null
    private val sutd = LatLng(1.3414, 103.9633)
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
    private lateinit var accFab // Account and stats
            : FloatingActionButton

    // Animation
    private lateinit var konfettiView
            : KonfettiView

    // Marker Icons for landmarks
    private lateinit var natureIcon : BitmapDescriptor
    private lateinit var playIcon : BitmapDescriptor
    private lateinit var exerciseIcon : BitmapDescriptor
    private lateinit var generalIcon : BitmapDescriptor

    // Marker Visibility
    private var natVisible = true // State - whether nature markers are visible
    private var exVisible = true // State - whether exercise markers are visible
    private var plaVisible = true // State - whether play markers are visible

    // Route
    var isRoute = false // State - whether there is an active route
    val path = ArrayList<Marker>()
    var step : Int = -1
    var curLocation : LatLng = LatLng(0.0,0.0)
    var dstLocation : LatLng = LatLng(0.0,0.0)

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

        if (internetAvailable()) {
            initialiseSheets()
            initialiseMaps()
        } else{
            Log.d("tag", "Internet unavailable")
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
        accFab = findViewById(R.id.accountfab)

        // Marker Icons for landmarks
        natureIcon = getIcon("marker_nature", this, packageName, 67, 100)
        playIcon = getIcon("marker_play", this, packageName, 67, 100)
        exerciseIcon = getIcon("marker_exercise", this, packageName, 67, 100)
        generalIcon = getIcon("marker_gem", this, packageName, 67, 100)

        // Animation
        konfettiView = findViewById(R.id.viewKonfetti)

        // Button Listeners
        natFab.setOnClickListener(View.OnClickListener {
            natVisible = true
            plaVisible = false
            exVisible = false
            changeVisibility(natureMarkers, exerciseMarkers, playMarkers, natVisible, exVisible, plaVisible)
        })

        exFab.setOnClickListener(View.OnClickListener {
            natVisible = false
            plaVisible = false
            exVisible = true
            changeVisibility(natureMarkers, exerciseMarkers, playMarkers, natVisible, exVisible, plaVisible)
        })

        plaFab.setOnClickListener(View.OnClickListener {
            natVisible = false
            plaVisible = true
            exVisible = false
            changeVisibility(natureMarkers, exerciseMarkers, playMarkers, natVisible, exVisible, plaVisible)
        })

        genFab.setOnClickListener(View.OnClickListener {
            natVisible = true
            plaVisible = true
            exVisible = true
            changeVisibility(natureMarkers, exerciseMarkers, playMarkers, natVisible, exVisible, plaVisible)
        })

        bleFab.setOnClickListener(View.OnClickListener {
            if (!isScanning) {
                startBleService()
                Toast.makeText(applicationContext, "Starting Scan",
                    Toast.LENGTH_SHORT).show()
//                readFBData()
//                if (isRoute) {
//                    setReach()
//                }
            }
            else {
                Toast.makeText(applicationContext, "Stopping Scan",
                    Toast.LENGTH_SHORT).show()
                stopBleService()
            }
        })
        // bleFab.setAlpha(0.0f)

        openFab.setOnClickListener(View.OnClickListener {
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

        accFab.setOnClickListener(View.OnClickListener {
//            resetRoute()
            val intent = Intent(this, ProfileActivity::class.java);
            startActivity(intent);

        })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun internetAvailable(): Boolean {
        val internetUtil = InternetUtil()
        return internetUtil.isOnline(this.applicationContext)
    }

    fun getDstTitle() : String {
        return path.get(step+1).snippet
    }

    fun getDst() : LatLng {
        return dstLocation
    }

    fun getCur() : LatLng {
        return curLocation
    }

    private fun startKonfetti() {
        konfettiView.build()
            .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
            .setDirection(0.0, 359.0)
            .setSpeed(1f, 5f)
            .setFadeOutEnabled(true)
            .setTimeToLive(2000L)
            .addShapes(Shape.Square, Shape.Circle)
            .addSizes(Size(12))
            .setPosition(-50f, konfettiView.width + 50f, -50f, -50f)
            .streamFor(300, 5000L)
    }

    // Function to reset route
    private fun resetRoute() {
        openFab.setImageDrawable(getResources().getDrawable(R.drawable.fab_route))
        step = -1
        dstLocation = LatLng(0.0,0.0)
        isRoute = false
        for (mkr in path) {
            if (mkr.snippet.get(0) == 'G') {
                mkr.setIcon(generalIcon)
            } else if (mkr.snippet.get(0) == 'N') {
                mkr.setIcon(natureIcon)
            } else if (mkr.snippet.get(0) == 'P') {
                mkr.setIcon(playIcon)
            } else if (mkr.snippet.get(0) == 'E') {
                mkr.setIcon(exerciseIcon)
            }
        }
        path.clear()
        mMap!!.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(1.3414, 103.9633)
                , 17.0f)
        )
    }

    // Fake function to set destination reach
    private fun setReach() {
        val tick = getIcon("marker_done", this, packageName, 92, 135)
        val length = path!!.size -1
        step += 1
        if (step == length) {
            path!!.get(step).setIcon(tick)
            startKonfetti()
            resetRoute()
        } else if (step < length) {
            dstLocation = path!!.get(step+1).position
            path!!.get(step).setIcon(tick)
            mMap!!.animateCamera(
                CameraUpdateFactory.newLatLngZoom(dstLocation
                    , 18.0f)
            )
        }
    }

    // Dumb function to set route
    fun setRoute( route : ArrayList<String>) {
        isRoute = true
        openFab.setImageDrawable(getResources().getDrawable(R.drawable.fab_compass))
        var i = 0
        for (node in route) {
            val iconName = "marker_${i+1}"
            if (node.get(0) == 'G') {
                for (mkr in generalMarkers!!) {
                    if (mkr.snippet == node) {
                        path.add(mkr)
                        val nodeIcon = getIcon(iconName, this, packageName, 92, 135)
                        mkr.setIcon(nodeIcon)
                        i +=1
                    }
                }

            } else if (node.get(0) == 'N') {
                for (mkr in natureMarkers!!) {
                    if (mkr.snippet == node) {
                        path.add(mkr)
                        val nodeIcon = getIcon(iconName, this, packageName, 92, 135)
                        mkr.setIcon(nodeIcon)
                        i +=1
                    }
                }
            } else if (node.get(0) == 'P') {
                for (mkr in playMarkers!!) {
                    if (mkr.snippet == node) {
                        path.add(mkr)
                        val nodeIcon = getIcon(iconName, this, packageName, 92, 135)
                        mkr.setIcon(nodeIcon)
                        i +=1
                    }
                }
            } else if (node.get(0) == 'E') {
                for (mkr in exerciseMarkers!!) {
                    if (mkr.snippet == node) {
                        path.add(mkr)
                        val nodeIcon = getIcon(iconName, this, packageName, 92, 135)
                        mkr.setIcon(nodeIcon)
                        i +=1
                    }
                }
            }
        }
        dstLocation = path!!.get(0).position
        mMap!!.animateCamera(
            CameraUpdateFactory.newLatLngZoom(dstLocation
            , 18.0f)
        )
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Set map style
        val mapStyleOptions = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style)
        mMap!!.setMapStyle(mapStyleOptions)

        // Set map bounds
        val sutdBounds = LatLngBounds(
                LatLng(1.339988, 103.961206),  // SW bounds
                LatLng(1.342825, 103.966078) // NE bounds
        )
        mMap!!.setLatLngBoundsForCameraTarget(sutdBounds)

        // Get marker values from google sheet
        val getMarkerValues = Thread(object : Runnable {
            var gotData = false
            override fun run() {
                while (!gotData) {
                    natureValues = readSheet("Nature!A2:E", sheetsService, google_api_key, sheet_id)
                    exerciseValues = readSheet("Exercise!A2:E", sheetsService, google_api_key, sheet_id)
                    playValues = readSheet("Play!A2:E", sheetsService, google_api_key, sheet_id)
                    generalValues = readSheet("General!A2:E", sheetsService, google_api_key, sheet_id)
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

        // Center map on sutd and set zoom
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(sutd))
        mMap!!.setMinZoomPreference(17.0f)
        mMap!!.setMaxZoomPreference(30.0f)

    }

    /* BeaconS23 */
    private var b23BatteryLevel = 0.0

    /*  Bluetooth  */
    private var isScanning = false
    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var receiver: BroadcastReceiver? = null
    private var receiver2: BroadcastReceiver? = null // QF added this
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
                b23BatteryLevel = intent.getDoubleExtra("BS3_battery_Level",0.0)
                Log.i("ble23", "Hello from BeaconS23! $b23BatteryLevel")
                stopBleService()
                Toast.makeText(applicationContext, "Successful BLE!",
                        Toast.LENGTH_LONG).show()
                if(isRoute) {
                    setReach()
                }
            }
        }
    }
    // QF added this
    private fun setReceiver2() {
        receiver2 = object : BroadcastReceiver() {
            @SuppressLint("TimberArgCount")
            override fun onReceive(context: Context?, intent: Intent) {
                Intrinsics.checkNotNullParameter(intent, "intent")
                is_ble_connected = intent.getBooleanExtra("is_ble_connected",false)
                Log.i("ble23", "is_ble_connected? $is_ble_connected")
                if(is_ble_connected){
                    count_ble = 0
                }
            }
        }
    }
    // QF added this

    private val isLocationPermissionGranted: Boolean
    private get() = hasPermission(this, "android.permission.ACCESS_FINE_LOCATION")

    private fun startBleService() {
        Timber.i("Start BLE service")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
            requestLocationPermission()
        } else {
            setIsScanning(true)

            /*
                Possible command:
                if message to send cmd is false
                should ignore the command to send
                only if message to send cmd is true
                "1" -> reset the image to default
                "2" -> show the image that was sent over
                "3" -> send image over but do not change the image display
                "4" -> send and change the image
             */
            val message_to_send_cmd = false
            val command_to_send = "3"
            /*
               Can use this for the landmarks ids -> each beacon will follow this ids
               Use https://www.uuidgenerator.net/version1 to generate the UUID
            */
            //UUIDs
            // "b6e4af9e-e48a-11eb-ba80-0242ac130004"
            // "c0b9a99a-e488-11eb-ba80-0242ac130004"
            // “71e81e3a-e48a-11eb-ba80-0242ac130004”

            val uuid = "b6e4af9e-e48a-11eb-ba80-0242ac130004"
            val intent = Intent("BLEServiceAction", "BLEServiceUri".toUri(), this, BLEService::class.java).apply {
                putExtra("DeviceUUID", uuid)
                putExtra("Command_to_send_cmd", command_to_send)
                putExtra("Message_to_send_cmd", message_to_send_cmd) // "")//
                }
            startService(intent)
            isReceiverRegistered = true
            registerReceiver(receiver, IntentFilter("GET_HELLO"))
            registerReceiver(receiver2, IntentFilter("GET_BLE_STATE")) // QF added this
        }
    }
    fun stopBleService() {
        Log.i("BLEService","Stop BLE Service.")
        setIsScanning(false)
        is_ble_connected = false // QF added this
        val intent = Intent(this, BLEService::class.java)
        stopService(intent)
    }

    private fun setIsScanning(isScan: Boolean) {
        isScanning = isScan


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
        setReceiver2()
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
            //<-- Unregister to avoid memoryleak
            unregisterReceiver(receiver)
            unregisterReceiver(receiver2) // QF added this
            isReceiverRegistered = false
        }
    }
    private var count_ble = 0 // QF added this
    private var is_ble_connected = false // QF added this
    // Location services
    private val airLocation = AirLocation(this, object : AirLocation.Callback {
        override fun onSuccess(locations: ArrayList<Location>) {
            curLocation = LatLng(locations.get(0).latitude,locations.get(0).longitude)
            var distLeft = distanceBetween(curLocation, dstLocation)

            if (distLeft < 7.0) {
                if (!isScanning) {
                    startBleService()
                    count_ble = 0  // QF added this
                    Toast.makeText(applicationContext, "Starting Scan",
                        Toast.LENGTH_SHORT).show()
                }


            }
            // QF added this
            if (isScanning){
                if(count_ble>20 && !is_ble_connected){
                    stopBleService()
//                        Toast.makeText(applicationContext, "Starting Scan",
//                            Toast.LENGTH_SHORT).show()
                    count_ble = 0
//                        startBleService()
                }else {
                    count_ble++
                }
            }
            // QF added this
        }

        override fun onFailure(locationFailedEnum: AirLocation.LocationFailedEnum) {
            Log.d("Location", locationFailedEnum.toString())
        }
    })

}
