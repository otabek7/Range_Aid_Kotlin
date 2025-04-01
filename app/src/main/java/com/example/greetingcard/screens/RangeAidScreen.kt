package com.example.greetingcard.screens

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RangeAidScreen(navController: NavHostController) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                runTermuxCommand(context, "termux-vibrate")
            }
        ) {
            Text("Run Termux Command")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun runTermuxCommand(context: Context, command: String) {
    try {
        // Create the execution intent
        val serviceIntent = Intent().apply {
            setClassName(
                "com.termux",
                "com.termux.app.RunCommandService"
            )
            action = "com.termux.RUN_COMMAND"
            putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/bash")
            putExtra("com.termux.RUN_COMMAND_ARGUMENTS", arrayOf("-c", command))
            putExtra("com.termux.RUN_COMMAND_WORKDIR", "/data/data/com.termux/files/home")
            putExtra("com.termux.RUN_COMMAND_BACKGROUND", true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // Verify components exist
    } catch (e: Exception) {
        Toast.makeText(context, "Error: ${e.javaClass.simpleName}: ${e.message}",
            Toast.LENGTH_LONG).show()
    }
}

private fun isPackageInstalled(context: Context, packageName: String): Boolean {
    return try {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

private fun showTermuxPermissionInstructions(context: Context) {
    AlertDialog.Builder(context)
        .setTitle("Permission Required")
        .setMessage("1. Open Termux\n2. Run: 'termux-setup-storage'\n3. Approve the notification when you first run a command")
        .setPositiveButton("OK") { _, _ -> }
        .show()
}
private fun showInstallPrompt(context: Context, packageName: String = "com.termux") {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://f-droid.org/packages/$packageName/")
            setPackage("org.fdroid.fdroid")
        }
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://f-droid.org/packages/$packageName/")
        }
        context.startActivity(intent)
    }
}