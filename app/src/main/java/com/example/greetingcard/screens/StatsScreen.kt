package com.example.greetingcard.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.regex.Pattern

@Composable
fun StatsScreen(navController: NavHostController) {
    val context = LocalContext.current
    var sessionDataList by remember { mutableStateOf<List<Session>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            loadSessionData(context, coroutineScope, onLoaded = {
                sessionDataList = it.sortedByDescending { session ->
                    parseTimestamp(session.timestamp)
                }
                isLoading = false
            }, onError = {
                errorMessage = it
                isLoading = false
            })
        } else {
            errorMessage = "Permission denied. Cannot access files."
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:${context.packageName}")
                context.startActivity(intent)
                errorMessage = "Please grant file access and restart the app."
                isLoading = false
            } else {
                loadSessionData(context, coroutineScope, onLoaded = {
                    sessionDataList = it
                    isLoading = false
                }, onError = {
                    errorMessage = it
                    isLoading = false
                })
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                loadSessionData(context, coroutineScope, onLoaded = {
                    sessionDataList = it
                    isLoading = false
                }, onError = {
                    errorMessage = it
                    isLoading = false
                })
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF800000), Color(0xFF3E3E3E), Color(0xFF1B1B1B))
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "My Stats",
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            modifier = Modifier.padding(16.dp),
            color = Color.White
        )

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (errorMessage != null) {
            Text(text = errorMessage!!, color = Color.Red)
        } else if (sessionDataList.isEmpty()) {
            Text("No Data Available", color = Color.White)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(sessionDataList) { session ->
                    SessionBox(session = session)
                }
            }
        }
    }
}

fun loadSessionData(
    context: Context,
    coroutineScope: CoroutineScope,
    onLoaded: (List<Session>) -> Unit,
    onError: (String) -> Unit
) {
    coroutineScope.launch {
        try {
            val sessions = readAllSessionData(context)
            onLoaded(sessions)
        } catch (e: Exception) {
            onError("Error loading data: ${e.message}")
        }
    }
}

suspend fun readAllSessionData(context: Context): List<Session> = withContext(Dispatchers.IO) {
    val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    Log.d("StatsScreen", "Looking in directory: ${downloadDir.absolutePath}")
    Log.d(
        "StatsScreen",
        "Directory exists: ${downloadDir.exists()}, readable: ${downloadDir.canRead()}"
    )

    val files = downloadDir.listFiles() ?: emptyArray()
    if (files.isEmpty()) {
        Log.w("StatsScreen", "No files found in the directory.")
    }

    val pattern = Pattern.compile("data_\\d{4}-\\d{2}-\\d{2}T\\d{2}-\\d{2}-\\d{2}Z\\.json")
    val matchingFiles = files.filter { pattern.matcher(it.name).matches() }

    Log.d("StatsScreen", "Total matching files: ${matchingFiles.size}")

    return@withContext matchingFiles.mapNotNull { file ->
        try {
            val jsonString = file.readText()
            Json.decodeFromString<SessionData>(jsonString).session
        } catch (e: Exception) {
            Log.e("StatsScreen", "Error parsing file ${file.name}: ${e.message}")
            null
        }
    }.flatten()
}

@Composable
fun SessionBox(session: Session) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Timestamp: ${formatIsoTimestamp(session.timestamp)}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text("Session ID: ${session.session_id}")
            Text("Brand: ${session.brand}")
            Text("Model: ${session.model}")
            Text("Hits: ${session.hits}")
            Text("Misses: ${session.misses}")
            Text("Total Shots: ${session.total_shots}")
            Text("Accuracy: ${session.accuracy}%")
            Text("Center to Center: ${session.center_to_center}")
        }
    }
}

@Serializable
data class SessionData(
    val session: List<Session>
)

@Serializable
data class Session(
    val session_id: String,
    val brand: String,
    val model: String,
    val timestamp: String,
    val hits: Int,
    val misses: Int,
    val total_shots: Int,
    val accuracy: Int,
    val center_to_center: Int
)

fun formatIsoTimestamp(iso: String): String {
    return try {
        val parser = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC") // ensure correct parsing from Zulu time

        val formatter = java.text.SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.getDefault())
        val date = parser.parse(iso)
        formatter.format(date!!)
    } catch (e: Exception) {
        iso // fallback to raw if parsing fails
    }
}
fun parseTimestamp(iso: String): Date? {
    return try {
        val parser = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        parser.parse(iso)
    } catch (e: Exception) {
        null
    }
}
