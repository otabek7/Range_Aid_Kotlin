package com.example.greetingcard

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HomeScreen(
                onLogout = {
                    FirebaseAuth.getInstance().signOut()  // Sign out the user
                    startActivity(Intent(this, AuthActivity::class.java))  // Redirect to login screen
                    finish()  // Close HomeActivity
                }
            )
        }
    }
}

@Composable
fun HomeScreen(onLogout: () -> Unit) {

    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFF42A5F5), Color(0xFF66BB6A)) // Light blue to green
    )

    Box(
        modifier = Modifier.fillMaxSize().background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text(text = "Home Page", fontSize = 24.sp)

            // Buttons for different actions
            Button(onClick = { /* TODO: View Inventory */ }, modifier = Modifier.fillMaxWidth()) {
                Text("View Inventory")
            }

            Button(onClick = { /* TODO: Share with Friends */ }, modifier = Modifier.fillMaxWidth()) {
                Text("Share with Friends")
            }

            Button(onClick = { /* TODO: View Personal Stats */ }, modifier = Modifier.fillMaxWidth()) {
                Text("View Personal Stats")
            }

            Button(onClick = { /* TODO: Start Range Aid */ }, modifier = Modifier.fillMaxWidth()) {
                Text("Start Range Aid")
            }

            Button(onClick = { /* TODO: Add to Inventory */ }, modifier = Modifier.fillMaxWidth()) {
                Text("Add to Inventory")
            }

            // Logout Button
            Button(
                onClick = { onLogout() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
            ) {
                Text("Logout")
            }
        }
    }
}
