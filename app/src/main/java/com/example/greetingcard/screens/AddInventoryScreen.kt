package com.example.greetingcard.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight

import com.example.greetingcard.FirestoreRepository

@Composable
fun AddInventoryScreen(navController: NavHostController) {
    val context = LocalContext.current
    val firestoreRepository = FirestoreRepository()

    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var accuracy by remember { mutableStateOf("") }
    var precision by remember { mutableStateOf("") }

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF800000), Color(0xFF1B1B1B))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Add a New Weapon.",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            CustomTextField("Brand", brand) { brand = it }
            CustomTextField("Model", model) { model = it }
            CustomTextField("Accuracy (%)", accuracy) { accuracy = it }
            CustomTextField("Precision (%)", precision) { precision = it }


            Button(
                onClick = {
                    val accuracyValue = accuracy.toDoubleOrNull()
                    val precisionValue = precision.toDoubleOrNull()

                    if (brand.isNotEmpty() && model.isNotEmpty() && accuracyValue != null && precisionValue != null) {
                        firestoreRepository.addGunData(
                            brand = brand,
                            model = model,
                            accuracy = accuracyValue,
                            precision = precisionValue,
                            onSuccess = {
                                Toast.makeText(
                                    context,
                                    "Gun added successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onFailure = {
                                Toast.makeText(
                                    context,
                                    "Failed to add gun: ${it.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    } else {
                        Toast.makeText(context, "Please enter valid data", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Add Gun")
            }

            Button(onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()

            ) {
                Text("Go Back")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White) },
        textStyle = LocalTextStyle.current.copy(color = Color.White),
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.Gray,
            cursorColor = Color.White
        )
    )
}

