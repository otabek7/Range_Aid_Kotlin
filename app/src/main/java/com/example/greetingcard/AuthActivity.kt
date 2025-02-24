package com.example.greetingcard

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle



class AuthActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            AuthScreen(
                onLoginSuccess = {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()  // Close AuthActivity to prevent back navigation
                }
            )
        }
    }
}

@Composable
fun AuthScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoggedIn by remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()

    // Check if user is already logged in
    LaunchedEffect(auth.currentUser) {
        if (auth.currentUser != null) {
            isLoggedIn = true
            onLoginSuccess() // Navigate to MainActivity
        }
    }

    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFF800000), Color(0xFF000000)) // Light blue to green
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
            Text(text = if (isLoggedIn) "Welcome, ${auth.currentUser?.email}" else "Welcome to Range Aid", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                colors= OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(255, 255, 255),
                    focusedContainerColor = Color(255, 255, 255)

                )

            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors= OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(255, 255, 255),
                    focusedContainerColor = Color(255, 255, 255)
                )
            )

            Button(
                onClick = { loginUser(auth, email, password, onLoginSuccess) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(255, 99, 71)
                )


            ) {
                Text(text = "Login", fontSize = 18.sp)
            }

            Button(
                onClick = { signUpUser(auth, email, password, onLoginSuccess) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(255, 99, 71)
                )
            ) {
                Text(text = "Create Account", fontSize = 18.sp)
            }

            if (isLoggedIn) {
                Button(
                    onClick = {
                        auth.signOut()
                        isLoggedIn = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(text = "Logout", fontSize = 18.sp)
                }
            }
        }
    }
}

private fun loginUser(auth: FirebaseAuth, email: String, password: String, onSuccess: () -> Unit) {
    if (email.isEmpty() || password.isEmpty()) return

    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess()
            }
        }
}

private fun signUpUser(auth: FirebaseAuth, email: String, password: String, onSuccess: () -> Unit) {
    if (email.isEmpty() || password.isEmpty()) return

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess()
            }
        }
}
