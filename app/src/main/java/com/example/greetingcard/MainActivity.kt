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
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.res.painterResource


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
        colors = listOf(Color(0xFF800000), Color(0xFF3E3E3E), Color(0xFF1B1B1B)) // Light blue to green
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

            // Buttons for different actions
            Button(
                onClick = { /* TODO: View Inventory */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF800000)
                )
            )

            {
                Icon(
                    painter = painterResource(id = R.drawable.target_icon),  // Custom gun-related icon
                    contentDescription = "Start Range Aid",
                    modifier = Modifier.size(24.dp),
                    tint = Color.White // Change color if needed
                )
                Spacer(modifier = Modifier.width(8.dp))

                Text("View Inventory")
            }


            Button(
                onClick = { /* TODO: Share with Friends */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF800000)
                )

            ) {
                Icon(
                    painter = painterResource(id = R.drawable.friend_icon),  // Custom gun-related icon
                    contentDescription = "Start Range Aid",
                    modifier = Modifier.size(24.dp),
                    tint = Color.White // Change color if needed
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share with Friends")
            }

            Button(
                onClick = { /* TODO: Start Range Aid */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEE9B00)
                )

            ) {
                Icon(
                    painter = painterResource(id = R.drawable.rangeaid_icon),  // Custom gun-related icon
                    contentDescription = "Start Range Aid",
                    modifier = Modifier.size(24.dp),
                    tint = Color.White // Change color if needed
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Range Aid")
            }

            Button(
                onClick = { /* TODO: Add to Inventory */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF800000)
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.addinventory_icon),  // Custom gun-related icon
                    contentDescription = "Start Range Aid",
                    modifier = Modifier.size(24.dp),
                    tint = Color.White // Change color if needed
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add to Inventory")
            }

            Button(
                onClick = { /* TODO: View Personal Stats */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF800000)
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.stats_icon),  // Custom gun-related icon
                    contentDescription = "Start Range Aid",
                    modifier = Modifier.size(24.dp),
                    tint = Color.White // Change color if needed
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Personal Stats")
            }




            // Logout Button
            Button(
                onClick = { onLogout() },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
            ) {
                Text("Logout")
            }
        }
    }
}
