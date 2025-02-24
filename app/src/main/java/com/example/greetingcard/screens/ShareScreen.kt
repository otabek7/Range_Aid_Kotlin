package com.example.greetingcard.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.core.content.ContextCompat.startActivity
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ShareScreen(navController: NavHostController, context: Context) {
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFF800000), Color(0xFF00274D), Color(0xFF4B0082), Color(0xFFFFD700))
    )

    Box(
        modifier = Modifier.fillMaxSize().background(gradient),
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sharing is Caring!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Share Range Aid with your Friends.",
            fontSize = 18.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Share via SMS Button
        Button(onClick = {
            shareText(
                context,
                "I am inviting you to try out the Range Aid app!"
            )
        }) {
            Text("Click to Share")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { navController.popBackStack() }) {
            Text("Go Back")
        }
    }
}

// Function to launch SMS app with a pre-filled message
fun shareText(context: Context, message: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, message)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share via")
    context.startActivity(shareIntent)
}



