package com.example.greetingcard.screens

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.rememberCoroutineScope
import com.example.greetingcard.FirestoreRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RangeAidScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scriptPath = "/home/mitchell/Downloads/isdV3.py"
    val firestoreRepository = FirestoreRepository()
    var expanded by remember { mutableStateOf(false) }
    var selectedGun by remember { mutableStateOf<Gun?>(null) }
    var gunInventory by remember { mutableStateOf<List<Gun>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var textFieldSize by remember { mutableStateOf(IntSize.Zero) }

    // Define the desired width for the dropdown and text field
    val dropdownWidth: Dp = 300.dp // You can adjust this value

    LaunchedEffect(Unit) {
        firestoreRepository.getAllGuns(
            onSuccess = { guns ->
                gunInventory = guns
                isLoading = false
            },
            onFailure = { error ->
                errorMessage = error.message
                isLoading = false
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else if (errorMessage != null) {
            Text(text = "Error: $errorMessage")
        } else {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.width(dropdownWidth) // Apply the desired width
            ) {
                TextField(
                    value = selectedGun?.let { "${it.brand} ${it.model}" } ?: "Select a Gun",
                    onValueChange = { /* Do nothing, it's a dropdown */ },
                    readOnly = true,
                    label = { Text("Select Gun") },
                    trailingIcon = {
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Dropdown")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            textFieldSize = coordinates.size
                        }
                        .menuAnchor()
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                ) {
                    gunInventory.forEach { gun ->
                        DropdownMenuItem(
                            text = { Text("${gun.brand} ${gun.model}") },
                            onClick = {
                                selectedGun = gun
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    runSSHCommand(context, "ssh mitchell@10.42.0.1")
                },
                enabled = selectedGun != null,
                modifier = Modifier.width(dropdownWidth) // Apply the same width to the button for consistency
            ) {
                Text("Run SSH Command")
            }
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (selectedGun != null) {
                        val brand = selectedGun?.brand ?: ""
                        val model = selectedGun?.model ?: ""
                        // Add single quotes around the double quotes to preserve them for the remote shell
                        val pythonCommand = "python3.8 $scriptPath --brand '\\\"$brand\\\"' --model '\\\"$model\\\"'"
                        copyPythonCommand(context, pythonCommand)
                    } else {
                        Toast.makeText(context, "Please select a gun first.", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = selectedGun != null,
                modifier = Modifier.width(dropdownWidth) // Apply the same width to the button
            ) {
                Text("Copy Python Command with Gun")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun copyPythonCommand(context: Context, command: String) {
    try {
        val serviceIntent = Intent().apply {
            setClassName(
                "com.termux",
                "com.termux.app.RunCommandService"
            )
            action = "com.termux.RUN_COMMAND"
            putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/bash")
            putExtra(
                "com.termux.RUN_COMMAND_ARGUMENTS",
                arrayOf("-c", "termux-clipboard-set \"$command\"")
            )
            putExtra("com.termux.RUN_COMMAND_WORKDIR", "/data/data/com.termux/files/home")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
        Toast.makeText(context, "Command copied. Paste in Termux and press Enter.", Toast.LENGTH_LONG).show()

    } catch (e: Exception) {
        Toast.makeText(context, "Error: ${e.javaClass.simpleName}: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun runSSHCommand(context: Context, command: String) {
    try {
        val serviceIntent = Intent().apply {
            setClassName(
                "com.termux",
                "com.termux.app.RunCommandService"
            )
            action = "com.termux.RUN_COMMAND"
            putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/bash")
            putExtra("com.termux.RUN_COMMAND_ARGUMENTS", arrayOf("-c", command))
            putExtra("com.termux.RUN_COMMAND_WORKDIR", "/data/data/com.termux/files/home")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error: ${e.javaClass.simpleName}: ${e.message}", Toast.LENGTH_LONG).show()
    }
}