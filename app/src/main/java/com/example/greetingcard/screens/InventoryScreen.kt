package com.example.greetingcard.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.greetingcard.FirestoreRepository

@Composable
fun InventoryScreen(navController: NavHostController) {
    val firestoreRepository = FirestoreRepository()
    var gunList by remember { mutableStateOf<List<Gun>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        firestoreRepository.getAllGuns(
            onSuccess = { guns ->
                gunList = guns
                isLoading = false
            },
            onFailure = { error ->
                errorMessage = error.message
                isLoading = false
            }
        )
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF800000), Color(0xFF3E3E3E), Color(0xFF1B1B1B))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "My Inventory",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Inventory List
            Column(
                modifier = Modifier
                    .weight(1f) // Take up all available space except for the button at the bottom
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isLoading) {
                    // Centered CircularProgressIndicator
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center) // Center the spinner
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                } else if (errorMessage != null) {
                    Text(text = "Error: $errorMessage", color = Color.Red, fontSize = 16.sp)
                } else if (gunList.isEmpty()) {
                    Text(text = "No guns in inventory", color = Color.White, fontSize = 18.sp)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(gunList) { gun ->
                            GunCard(gun)
                        }
                    }
                }
            }

            // Go Back button at the bottom
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Go Back")
            }

        }
    }

}

@Composable
fun GunCard(gun: Gun) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF5A5A5A))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Brand: ${gun.brand}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = "Model: ${gun.model}", fontSize = 18.sp, color = Color.White)
            Text(text = "Accuracy: ${gun.accuracy}%", fontSize = 18.sp, color = Color.White)
            Text(text = "Precision: ${gun.precision}%", fontSize = 18.sp, color = Color.White)
        }
    }

}

data class Gun(
    val brand: String = "",
    val model: String = "",
    val accuracy: Double = 0.0,
    val precision: Double = 0.0
)
