package com.example.linking_application_android

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.linking_application_android.ble.ConnectionEventListener
import com.example.linking_application_android.ble.ConnectionManager
import com.example.linking_application_android.ble.findCharacteristic
import com.example.linking_application_android.ble.toHexString
import com.example.linking_application_android.helper.BitmapHelper
import com.example.linking_application_android.helper.StorageHelper
import timber.log.Timber
import java.util.*
import kotlin.math.floor


private  const val FILTER_DEVICE_NAME = "BeaconS23"
private  const val RSSI_THRESHOLD_SCAN_DISTANCE = -80//-70 //Current estimate there should be a better way to ensure the proper scan radius
private  const val CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8"

private var FILTER_DEVICE_UUID: ParcelUuid = ParcelUuid(UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b"))
private val TX_ID = "61"

class BLEService : Service() {

    private var still_sending = false
    private lateinit var message_to_send: ByteArray
    private lateinit var chunked_msg_to_send: Array<ByteArray>
    private var send_count = 0
    private var send_str = listOf<Byte>()

    private var b23BatteryLevel = 0.0

    private fun sendDataToActivity() {
        val sendLevel = Intent()
        sendLevel.action = "GET_HELLO"
        sendLevel.putExtra("BS3_battery_Level", b23BatteryLevel)
        sendBroadcast(sendLevel)
    }

    override fun onBind(intent: Intent): IBinder? {
//        Toast.makeText(applicationContext, "binding", Toast.LENGTH_SHORT).show()
        return null
    }

    private var notifyingCharacteristics = mutableListOf<UUID>()


    private var isScanning: Boolean = false
    private var isDeviceFound: Boolean = false
    private lateinit var scannedResult: ScanResult



    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }
    private val scanSettings = android.bluetooth.le.ScanSettings.Builder()
        .setScanMode(android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()
//    private val filter = android.bluetooth.le.ScanFilter.Builder().setDeviceName(
//        FILTER_DEVICE_NAME
//    ).build()
//    private val filter = android.bluetooth.le.ScanFilter.Builder().setServiceUuid(
//        FILTER_DEVICE_UUID
//    ).build()
    private lateinit var filter: ScanFilter

    override fun onCreate() {
        super.onCreate()
        ConnectionManager.registerListener(connectionEventListener)

        val c = applicationContext
        val message_to_send_str = StorageHelper.getCSVFromUri(c, StorageHelper.getResUri(c, R.raw.test_img))
        message_to_send = cleanListStringToString(message_to_send_str).hexToBytes()
//        println("hello:...... ${message_to_send.size} -> ${message_to_send.toHexString()}")

        // m = 0 : 500*0..500*1-1   : 0..499
        // m = 1 : 500*1..500*2-1   : 500..999
        // m = 2 : 500*2..500*3-1   : 1000..1499
        // m = 3 : 500*3..500*4-1   : 1500..1999
        // m = 4 : 500*4..500*5-1   : 2000..2499
        // m = 50: 500*50..500*51-1 : 25000..25499
        // m = 60: 500*60..500*61-1 : 30000..30499
        // m = 61: 500*61..500*62-1 : 30500..30720 -> 30999
        for (send_count in 0 until message_to_send.size) {
            if (send_count <= 60) {
                send_str +=
                    message_to_send.slice((500 * send_count) until 500 * (send_count + 1))
            } else {
                send_str +=
                    message_to_send.slice((500 * send_count) until (message_to_send.size - 1))
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        isScanning = true
        FILTER_DEVICE_UUID = ParcelUuid(UUID.fromString(intent?.getStringExtra("DeviceUUID")))
//        Log.i("BLEService", "Device UUID: $FILTER_DEVICE_UUID")
        filter = android.bluetooth.le.ScanFilter.Builder().setServiceUuid(
            FILTER_DEVICE_UUID
        ).build()
        bleScanner.startScan(listOf(filter), scanSettings, scanCallback)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopScan()
        isScanning = false
        isDeviceFound = false
        ConnectionManager.unregisterListener(connectionEventListener)
    }

    private val scanCallback = object : ScanCallback() {
        @RequiresApi(Build.VERSION_CODES.O)
        @SuppressLint("BinaryOperationInTimber")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (isScanning) {
                with(result) {
                    val d = device
                    if (d.name == FILTER_DEVICE_NAME && isDeviceFound) {
                        Timber.i("Stopping Scanning")
                        stopScan()
                        startConnect(scannedResult.device)
                    }else if (!isDeviceFound) {
//                        Log.i("BLEService",
//                            "Found BLE device! Name: ${d.name}," +
//                                    " address: ${d.address}, rssi: $rssi, tx_power: $txPower"//, distance: $distance"
//                        )
                        if ((rssi > RSSI_THRESHOLD_SCAN_DISTANCE)) {
//                            val distance = calculateDistance(txPower.toDouble(), rssi.toDouble()) //not working, not accurate
                            scannedResult = result
                            isDeviceFound = true
                        }
                    }
                }
            }
        }
        override fun onScanFailed(errorCode: Int) {
            Timber.e("onScanFailed: code $errorCode")
        }
    }

//    private fun calculateDistance(txPower: Double, rssi: Double): Double {
//        if (rssi == 0.0) {
//            return -1.0 // if we cannot determine distance, return -1.
//        }
//        val ratio = rssi * 1.0 / -txPower
//        return if (ratio < 1.0) {
//            ratio.pow(10.0)
//        } else {
//            0.89976 * ratio.pow(7.7095) + 0.111
//        }
////        val t = -69.0
////        val r = -60.0
////        return 10.toDouble().pow((-txPower-rssi)/(10*3))
//    }

    private fun stopScan(){
        bleScanner.stopScan(scanCallback)
        isScanning = false
    }

    private fun close(){
        stopScan()
        ConnectionManager.unregisterListener(connectionEventListener)
    }

    private fun startConnect(device: BluetoothDevice, context: Context = this){
        ConnectionManager.connect(device,context)
    }

    private fun stopService(){
        sendDataToActivity()
        this.stopSelf()
    }

    private lateinit var bleGatt: BluetoothGatt

    private val connectionEventListener by lazy {
        ConnectionEventListener().apply {
            onDisconnect = {
                Timber.w("Disconnected from ${scannedResult.device}")
                stopService()
            }

            onConnectionSetupComplete = { characteristic ->
                Timber.i("Connected from $scannedResult.device}")
//                val readServiceUuid = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
                bleGatt = characteristic
                val readCharUuid = UUID.fromString(CHARACTERISTIC_UUID)
                characteristic.findCharacteristic(readCharUuid)?.let { it1 ->
                    ConnectionManager.readCharacteristic(characteristic.device,
                        it1
                    )
                }
            }

            onCharacteristicRead = { _, characteristic ->
//                Log.i("ble23", "Read from ${characteristic.uuid}: ${characteristic.value.toHexString()}")
                b23BatteryLevel = characteristic.value.decodeToString().split(" ")[2].toDouble()

                val toSendString = TX_ID.toByteArray() + send_str[send_count]

                still_sending = true
                if (send_count > 60){
                    still_sending = false
                }
                send_count += 1

                ConnectionManager.writeCharacteristic(scannedResult.device,characteristic,toSendString)
            }

            onCharacteristicWrite = { _, characteristic ->
//                Log.i("BLEService", "Wrote to ${characteristic.uuid}, count: $send_count")
                if(!still_sending) {
                    ConnectionManager.teardownConnection(scannedResult.device)
                }else{
                    val readCharUuid = UUID.fromString(CHARACTERISTIC_UUID)
                    bleGatt.findCharacteristic(readCharUuid)?.let { it1 ->
                        ConnectionManager.readCharacteristic(bleGatt.device,
                            it1
                        )
                    }
                }
            }

            onMtuChanged = { _, mtu ->
                Timber.i("MTU updated to $mtu")
            }

            onCharacteristicChanged = { _, characteristic ->
                Timber.i("Value changed on ${characteristic.uuid}: ${characteristic.value.toHexString()}")
            }

            onNotificationsEnabled = { _, characteristic ->
                Timber.i("Enabled notifications on ${characteristic.uuid}")
                notifyingCharacteristics.add(characteristic.uuid)
            }

            onNotificationsDisabled = { _, characteristic ->
                Timber.i("Disabled notifications on ${characteristic.uuid}")
                notifyingCharacteristics.remove(characteristic.uuid)
            }
        }
    }
    private enum class CharacteristicProperty {
        Readable,
        Writable,
        WritableWithoutResponse,
        Notifiable,
        Indicatable;

        val action
            get() = when (this) {
                Readable -> "Read"
                Writable -> "Write"
                WritableWithoutResponse -> "Write Without Response"
                Notifiable -> "Toggle Notifications"
                Indicatable -> "Toggle Indications"
            }
    }
    private fun String.hexToBytes() =
        this.chunked(2).map { it.toUpperCase(Locale.US).toInt(16).toByte() }.toByteArray()

    private fun cleanListStringToString(item: List<String>): String {
//        println("size of msg: ${item.size}")//16
        var str = ""
        for (m in item){
            val list_m = m.split(",")
//            println("item: $list_m")
            for (n in list_m){
//                println("item: $n")
                if (n.isNotEmpty()) {
                    val s = n.slice(2..3)
                    str += s
//                    println("item: $s")
                }
            }
        }
//        println("str size: ${str.length}")
        return str
    }
}