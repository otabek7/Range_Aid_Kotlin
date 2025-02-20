package com.example.greetingcard.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


@Composable
fun RangeAidScreen(navController: NavHostController) {
    val context = LocalContext.current
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    var devices by remember { mutableStateOf(emptyList<BluetoothDevice>()) }
    var isScanning by remember { mutableStateOf(false) }
    var isConnected by remember { mutableStateOf(false) }
    var receivedMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val requestBluetoothPermissions = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions[Manifest.permission.BLUETOOTH_SCAN] == true &&
                permissions[Manifest.permission.BLUETOOTH_CONNECT] == true &&
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                startDeviceScan(bluetoothAdapter) { foundDevices ->
                    devices = foundDevices
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
        Text("Start Range Aid", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestBluetoothPermissions.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            } else {
                startDeviceScan(bluetoothAdapter) { foundDevices ->
                    devices = foundDevices
                }
            }
        }) {
            Text("Scan for Devices")
        }


        Spacer(modifier = Modifier.height(16.dp))

        if (isScanning) {
            Text("Scanning...", color = MaterialTheme.colorScheme.primary)
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(devices) { device ->
                Text(
                    text = "${device.name ?: "Unknown"} (${device.address})",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            coroutineScope.launch(Dispatchers.IO) {
                                val success = connectToDevice(device) { message ->
                                    receivedMessage = message
                                }
                                isConnected = success
                            }
                        }
                        .padding(8.dp)
                )
            }
        }

        if (isConnected) {
            Text("Connected! Received: $receivedMessage", color = MaterialTheme.colorScheme.primary)
        } else {
            Text("Not Connected", color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { navController.popBackStack() }) {
            Text("Go Back")
        }
    }
}

@SuppressLint("MissingPermission")
fun startDeviceScan(bluetoothAdapter: BluetoothAdapter?, onDevicesFound: (List<BluetoothDevice>) -> Unit) {
    val devices = mutableListOf<BluetoothDevice>()
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: android.content.Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let { devices.add(it) }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    onDevicesFound(devices)
                }
            }
        }
    }

    val filter = android.content.IntentFilter(BluetoothDevice.ACTION_FOUND)
    filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
    bluetoothAdapter?.startDiscovery()
}

@SuppressLint("MissingPermission")
fun connectToDevice(device: BluetoothDevice, onMessageReceived: (String) -> Unit): Boolean {
    val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard SPP UUID
    var bluetoothSocket: BluetoothSocket? = null

    try {
        bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
        bluetoothSocket.connect()

        val inputStream: InputStream = bluetoothSocket.inputStream
        val outputStream: OutputStream = bluetoothSocket.outputStream

        outputStream.write("Hello Jetson!".toByteArray())

        Thread {
            try {
                val buffer = ByteArray(1024)
                while (true) {
                    val bytesRead = inputStream.read(buffer)
                    val message = String(buffer, 0, bytesRead)
                    onMessageReceived(message)
                }
            } catch (e: IOException) {
                Log.e("Bluetooth", "Connection lost", e)
            }
        }.start()

        return true
    } catch (e: IOException) {
        Log.e("Bluetooth", "Could not connect to device", e)
        bluetoothSocket?.close()
        return false
    }
}

