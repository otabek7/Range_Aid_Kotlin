package com.example.greetingcard.screens

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

@Composable
fun RangeAidScreen(navController: NavHostController) {
    val context = LocalContext.current
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    var devices by remember { mutableStateOf(setOf<BluetoothDevice>()) }
    var isScanning by remember { mutableStateOf(false) }
    var isConnected by remember { mutableStateOf(false) }
    var connectedSocket by remember { mutableStateOf<BluetoothSocket?>(null) }
    var receivedMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val requestBluetoothPermissions = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions[Manifest.permission.BLUETOOTH_SCAN] == true &&
                permissions[Manifest.permission.BLUETOOTH_CONNECT] == true &&
                permissions[Manifest.permission.BLUETOOTH_ADVERTISE] == true &&
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            ) {
                enableBluetooth(context, bluetoothAdapter) {
                    startDeviceScan(context, bluetoothAdapter, onDevicesFound = { foundDevices ->
                        devices = foundDevices.filter { it.name != null }
                            .toSet() // Remove duplicates & filter unnamed devices
                        isScanning = false // Stop scanning message
                    })
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Start Range Aid", style = MaterialTheme.typography.headlineMedium,
            color = Color.White, // White text
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Scan Button
        Button(onClick = {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                ) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestBluetoothPermissions.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_ADVERTISE,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            } else {
                enableBluetooth(context, bluetoothAdapter) {
                    isScanning = true
                    startDeviceScan(context, bluetoothAdapter, onDevicesFound = { foundDevices ->
                        devices = foundDevices.filter { it.name != null }.toSet()
                        isScanning = false
                    })
                }
            }
        }) {
            Text("Scan for Devices")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isScanning) {
            Text(
                "Scanning...",
                color = Color.White, // White text
            )
        }

        // Device List
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(devices.toList()) { device ->
                Text(
                    text = "${device.name} (${device.address})",
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            coroutineScope.launch(Dispatchers.IO) {
                                val socket = connectToDevice(device) { message ->
                                    receivedMessage = message
                                }
                                if (socket != null) {
                                    connectedSocket = socket
                                    isConnected = true
                                }
                            }
                        }
                        .padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isConnected) {
            Text("Connected! Received: $receivedMessage", color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    connectedSocket?.outputStream?.write("Hello from Android!".toByteArray())
                }
            }) {
                Text("Send Message")
            }
        } else {
            Text("Not Connected", color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { navController.popBackStack() }) {
            Text("Go Back")
        }
    }
}

/**
 * Ensure Bluetooth is turned on before scanning.
 */
fun enableBluetooth(context: Context, bluetoothAdapter: BluetoothAdapter?, onEnabled: () -> Unit) {
    if (bluetoothAdapter == null) {
        Log.e("Bluetooth", "Device does not support Bluetooth")
        return
    }

    if (!bluetoothAdapter.isEnabled) {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        context.startActivity(enableBtIntent)
    }

    onEnabled()
}

/**
 * Scan for nearby Bluetooth devices.
 */
@SuppressLint("MissingPermission")
fun startDeviceScan(
    context: Context,
    bluetoothAdapter: BluetoothAdapter?,
    onDevicesFound: (List<BluetoothDevice>) -> Unit
) {
    if (bluetoothAdapter == null) return

    val devices = mutableSetOf<BluetoothDevice>() // Using Set to prevent duplicates
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let { devices.add(it) }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    onDevicesFound(devices.toList()) // Convert Set to List
                    context?.unregisterReceiver(this) // Unregister receiver to prevent memory leaks
                }
            }
        }
    }

    val filter = IntentFilter().apply {
        addAction(BluetoothDevice.ACTION_FOUND)
        addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
    }
    context.registerReceiver(receiver, filter)

    bluetoothAdapter.startDiscovery()
}

/**
 * Connect to a Bluetooth device.
 */
@SuppressLint("MissingPermission")
fun connectToDevice(
    device: BluetoothDevice,
    onMessageReceived: (String) -> Unit
): BluetoothSocket? {
    val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard SPP UUID
    var bluetoothSocket: BluetoothSocket? = null

    try {
        bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
        bluetoothSocket.connect() // Attempt to connect

        val inputStream: InputStream = bluetoothSocket.inputStream
        val outputStream: OutputStream = bluetoothSocket.outputStream

        outputStream.write("Hello Jetson!".toByteArray()) // Send initial message

        // Start a background thread to read incoming data
        Thread {
            try {
                val buffer = ByteArray(1024)
                while (true) {
                    val bytesRead = inputStream.read(buffer)
                    val message = String(buffer, 0, bytesRead)
                    onMessageReceived(message) // Callback to update UI with received message
                }
            } catch (e: IOException) {
                Log.e("Bluetooth", "Connection lost", e)
            }
        }.start()

        return bluetoothSocket // Return socket if connected
    } catch (e: IOException) {
        Log.e("Bluetooth", "Could not connect to device", e)
        bluetoothSocket?.close()
        return null
    }
}


