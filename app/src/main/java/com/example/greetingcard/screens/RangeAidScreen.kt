package com.example.greetingcard.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import java.io.File

@Composable
fun RangeAidScreen(navController: NavHostController) {
    val context = LocalContext.current
    var fileContents by remember { mutableStateOf(listOf<String>()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Request permissions if not granted (you'll need to handle the result)
                ActivityCompat.requestPermissions(
                    context as android.app.Activity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1
                )
            } else {
                errorMessage = "Permission denied."
            }
            return@LaunchedEffect
        }

        try {
            val usbStorage = Environment.getExternalStorageDirectory()
            val files = usbStorage.listFiles { file ->
                file.isFile && file.name.endsWith(".txt")
            }

            if (files != null) {
                fileContents = files.map { it.readText() }
            } else {
                errorMessage = "No files found."
            }
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (errorMessage != null) {
            Text(text = errorMessage!!, fontSize = 18.sp)
        } else {
            fileContents.forEach { content ->
                Text(text = content, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp)) // Add spacing between files
            }
        }
    }
}