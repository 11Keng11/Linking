package com.example.linking_application_android.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.linking_application_android.MapsActivity
import com.example.linking_application_android.R
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.math.roundToInt


class CompassBottomSheetFragment : BottomSheetDialogFragment(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var degrees: Int = 0

    lateinit var mainHandler: Handler

    private val compassUtil = CompassUtil()

    private var imageView: ImageView? = null
    private var textView: TextView? = null
    private var destination : TextView? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        var root = inflater.inflate(R.layout.compass_bottom_sheet_layout, container, false)

        sensorManager = this.activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        mainHandler = Handler(Looper.getMainLooper())

        imageView = root.findViewById(R.id.compassImageView)
        textView = root.findViewById(R.id.compassTextView)
        destination = root.findViewById(R.id.dstName)

        textView!!.text = "300m"
        imageView!!.rotation = 0F

        return root
    }

    override fun onResume() {
        super.onResume()

        // Get updates from the accelerometer and magnetometer at a constant rate.
        // To make batch operations more efficient and reduce power consumption,
        // provide support for delaying updates to the application.
        //
        // In this example, the sensor reporting delay is small enough such that
        // the application receives an update before the system checks the sensor
        // readings again.
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_FASTEST,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }

        mainHandler.post(updateCompassTask)
    }

    override fun onPause() {
        super.onPause()

        mainHandler.removeCallbacks(updateCompassTask)

        // Don't receive any more updates from either sensor.
        sensorManager.unregisterListener(this)
    }

    // Get readings from accelerometer and magnetometer. To simplify calculations,
    // consider storing these readings as unit vectors.
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // To be implemented
        Log.d("sensor", accuracy.toString())
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    fun updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )

        // "rotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        // "orientationAngles" now has up-to-date information.

        // orientationAngles[0] points to North!
//        Log.d("Compass -Orientation",
//            "FST["
//                    + orientationAngles[0].toString()
//                    + "] SEC["
//                    + orientationAngles[1].toString()
//                    + "] TRD["
//                    + orientationAngles[2].toString()
//                    + "]")
    }

    /**
     * This function runs every 25ms.
     */
    private val updateCompassTask = object : Runnable {
        override fun run() {
            updateOrientationAngles()

            var dstTitle = (activity as MapsActivity?)!!.getDstTitle()
            if (dstTitle == "Done!") {
                dismiss()
            }

            degrees = compassUtil.radianToDegrees(orientationAngles[0])
            var curLoc = (activity as MapsActivity?)!!.getCur()
            var dstLoc = (activity as MapsActivity?)!!.getDst()
            var bearing = compassUtil.angleFromCoordinate(curLoc,dstLoc)
            var rot = compassUtil.getAssetRotation(degrees.toFloat(),curLoc,dstLoc)
            imageView?.rotation = rot

            var dst = compassUtil.distance(curLoc,dstLoc).roundToInt()
            textView!!.text = "${dst.toString()}m"

//            Log.d("Azimuth", degrees.toString())
//            Log.d("curLoc", curLoc.toString())
//            Log.d("dstLoc", dstLoc.toString())
//            Log.d("rot", rot.toString())

            destination!!.text = dstTitle

            // Use degrees here for azimuth in degrees

            mainHandler.postDelayed(this, 25)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): CompassBottomSheetFragment {
            val fragment = CompassBottomSheetFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}
