package com.example.greetingcard.screens

import android.graphics.BitmapFactory
import android.os.Environment
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(navController: NavHostController) {
    val imageGroups = remember { mutableStateMapOf<String, List<File>>() }
    var selectedImage by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(Unit) {
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val files = downloadDir.listFiles() ?: emptyArray()

        val grouped = files
            .filter { file ->
                val name = file.name
                name.matches(Regex("""\d{4}-\d{2}-\d{2}T\d{2}-\d{2}-\d{2}Z_shot_\d+.*"""))
            }
            .groupBy { file ->
                file.name.substringBefore("T") // e.g., 2025-04-14
            }
            .mapValues { (_, files) ->
                files.sortedBy { file ->
                    file.name.substringAfter("_shot_").takeWhile { it.isDigit() }.toLongOrNull() ?: Long.MAX_VALUE
                }
            }


        imageGroups.clear()
        imageGroups.putAll(grouped)
    }


    Scaffold(
        topBar = {

            Text(
                text = "Session Snapshots",
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                modifier = Modifier.padding(16.dp),
                color = Color.White
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF800000), Color(0xFF3E3E3E), Color(0xFF1B1B1B))
                    )
                )
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                imageGroups.toList().forEach { (rawDate, images) ->
                    val formattedDate = formatDate(rawDate)

                    item {
                        Text(
                            text = formattedDate,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(images.chunked(2)) { rowImages ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowImages.forEach { file ->
                                val bitmap = remember(file.path) {
                                    BitmapFactory.decodeFile(file.absolutePath)
                                }
                                bitmap?.let {
                                    Image(
                                        bitmap = it.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(150.dp)
                                            .clickable { selectedImage = file }
                                            .background(Color.DarkGray)
                                    )
                                }
                            }
                            if (rowImages.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Fullscreen Preview Dialog
            if (selectedImage != null) {
                FullscreenImageDialog(
                    file = selectedImage!!,
                    onDismiss = { selectedImage = null }
                )
            }
        }
    }
}

@Composable
fun FullscreenImageDialog(file: File, onDismiss: () -> Unit) {
    val bitmap = remember(file.path) {
        BitmapFactory.decodeFile(file.absolutePath)
    }
    var scale by remember { mutableStateOf(1f) }

    if (bitmap != null) {
        Dialog(onDismissRequest = { onDismiss() }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                        }
                    }
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(scale)
                        .clickable { onDismiss() }
                )
            }
        }
    }
}

fun formatDate(raw: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = parser.parse(raw)
        val formatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        formatter.format(date!!)
    } catch (e: Exception) {
        raw // fallback to original
    }
}
