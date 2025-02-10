package com.example.greetingcard

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppNavigation()
        }
    }
}

// Main App Navigation Setup
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("inventory") { com.example.greetingcard.screens.InventoryScreen(navController) }
        composable("share") { com.example.greetingcard.screens.ShareScreen(navController, context=navController.context) }
        composable("range") { com.example.greetingcard.screens.RangeAidScreen(navController) }
        composable("add") { com.example.greetingcard.screens.AddInventoryScreen(navController) }
        composable("stats") { com.example.greetingcard.screens.StatsScreen(navController) }
    }
}

// Home Screen with Navigation Buttons
@Composable
fun HomeScreen(navController: NavHostController) {
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFF800000), Color(0xFF3E3E3E), Color(0xFF1B1B1B))
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
            Text(text = "Range Aid Home", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)

            GunButton(R.drawable.target_icon, "View Inventory") { navController.navigate("inventory") }
            GunButton(R.drawable.friend_icon, "Share with Friends") { navController.navigate("share") }
            GunButton(R.drawable.rangeaid_icon, "Start Range Aid") { navController.navigate("range") }
            GunButton(R.drawable.addinventory_icon, "Add to Inventory") { navController.navigate("add") }
            GunButton(R.drawable.stats_icon, "View Personal Stats") { navController.navigate("stats") }

            // Logout Button
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.context.startActivity(Intent(navController.context, AuthActivity::class.java))
                },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
            ) {
                Text("Logout")
            }
        }
    }
}

// Reusable Button Component
@Composable
fun GunButton(iconRes: Int, text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF800000)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = text,
            modifier = Modifier.size(24.dp),
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}
@Composable
fun InventoryScreen(navController: NavHostController) {
    ScreenContent("Inventory Screen", navController)
}

@Composable
fun ShareScreen(navController: NavHostController) {
    ScreenContent("Share with Friends Screen", navController)
}

@Composable
fun RangeAidScreen(navController: NavHostController) {
    ScreenContent("Range Aid Screen", navController)
}

@Composable
fun AddInventoryScreen(navController: NavHostController) {
    ScreenContent("Add to Inventory Screen", navController)
}

@Composable
fun StatsScreen(navController: NavHostController) {
    ScreenContent("Personal Stats Screen", navController)
}

@Composable
fun ScreenContent(title: String, navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("Go Back")
        }
    }
}

