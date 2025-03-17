package com.example.greetingcard.screens

import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.DocumentReference

data class GunData(val precision: Double, val accuracy: Double, val timestamp: Any)

@Composable
fun StatsScreen(navController: NavHostController) {
    var precision by remember { mutableStateOf(0.0) }
    var accuracy by remember { mutableStateOf(0.0) }
    var loading by remember { mutableStateOf(true) }

    val db = FirebaseFirestore.getInstance()

    // Example user UID and gun ID (replace with actual values)
    val userUid = "user_12345" // Replace with actual user UID
    val gunId = "gun_1"        // Replace with actual gun ID

    // Firestore reference for the specific gun document
    val gunRef: DocumentReference = db.collection("users")
        .document(userUid)
        .collection("guns")
        .document(gunId)

    // Listen for updates to the Firestore document in real-time
    val listener: ListenerRegistration = gunRef.addSnapshotListener { snapshot, e ->
        if (e != null) {
            Log.w("StatsScreen", "Listen failed.", e)
            loading = false
            return@addSnapshotListener
        }

        if (snapshot != null && snapshot.exists()) {
            val gunData = snapshot.toObject(GunData::class.java)
            if (gunData != null) {
                precision = gunData.precision
                accuracy = gunData.accuracy
            }
        }
        loading = false
    }

    // UI for StatsScreen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Personal Stats",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Show a loading indicator while fetching data
        if (loading) {
            CircularProgressIndicator()
        } else {
            // Display the precision and accuracy values
            Text(
                text = "Precision: $precision",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Accuracy: $accuracy",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Go back button
        Button(onClick = { navController.popBackStack() }) {
            Text("Go Back")
        }
    }

    // Remove listener when the screen is destroyed
    DisposableEffect(Unit) {
        onDispose {
            listener.remove()
        }
    }
}
