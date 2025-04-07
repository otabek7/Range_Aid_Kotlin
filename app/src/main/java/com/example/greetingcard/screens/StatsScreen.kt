package com.example.greetingcard.screens

import android.util.Log
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
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.io.File
import java.util.regex.Pattern

suspend fun readAllSessionData(context: android.content.Context): List<Session> = withContext(Dispatchers.IO) {
    val filesDir = context.filesDir
    val files = filesDir.listFiles() ?: emptyArray()

    val pattern = Pattern.compile("data_\\d{4}-\\d{2}-\\d{2}T\\d{2}-\\d{2}-\\d{2}Z\\.json")

    val matchingFiles = files.filter { pattern.matcher(it.name).matches() }

    Log.d("StatsScreen", "Found ${matchingFiles.size} matching files.")

    matchingFiles.forEach { file ->
        Log.d("StatsScreen", "Matching file: ${file.name}")
    }

    return@withContext matchingFiles.mapNotNull { file ->
        try {
            val jsonString = file.readText()
            Json.decodeFromString<SessionData>(jsonString).session
        } catch (e: Exception) {
            Log.e("StatsScreen", "Error parsing file ${file.name}: ${e.message}")
            e.printStackTrace()
            null
        }
    }.flatten()
}

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
    val center_to_center: Double
)

@Serializable
data class SessionData(
    val session: List<Session>
)

@Composable
fun StatsScreen(navController: NavHostController) {
    val context = LocalContext.current
    var sessionDataList by remember { mutableStateOf<List<Session>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                sessionDataList = readAllSessionData(context)
            } catch (e: Exception) {
                errorMessage = "Error loading data: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "My Stats",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.padding(16.dp),
            color = Color.White
        )

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (errorMessage != null) {
            Text(text = errorMessage!!, color = Color.Red)
        } else if (sessionDataList.isEmpty()) {
            Text("No Data Available")
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



@Composable
fun SessionBox(session: Session) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE0E0E0), Color(0xFFF5F5F5)),
                    startY = 0f,
                    endY = 100f
                ),
                shape = RoundedCornerShape(8.dp)
            ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Session ID: ${session.session_id}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = "Timestamp: ${session.timestamp}")
            Text(text = "Brand: ${session.brand}")
            Text(text = "Model: ${session.model}")
            Text(text = "Hits: ${session.hits}")
            Text(text = "Misses: ${session.misses}")
            Text(text = "Total Shots: ${session.total_shots}")
            Text(text = "The Accuracy: ${session.accuracy}%")
            Text(text = "Center to Center: ${session.center_to_center}")
        }
    }
}

suspend fun parseSessionData(context: android.content.Context, filePath: File): SessionData? = withContext(Dispatchers.IO) {
    return@withContext try {
        if (!filePath.exists()) {
            Log.e("StatsScreen", "File not found: ${filePath.absolutePath}")
            return@withContext null
        }
        val jsonString = filePath.readText()
        Json.decodeFromString<SessionData>(jsonString)
    } catch (e: Exception) {
        Log.e("StatsScreen", "Error parsing JSON: ${e.message}")
        e.printStackTrace()
        null
    }
}
