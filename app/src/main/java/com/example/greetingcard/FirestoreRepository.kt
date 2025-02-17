package com.example.greetingcard

import android.util.Log
import com.example.greetingcard.screens.Gun
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun addGunData(brand: String, model: String, accuracy: Double, precision: Double, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Log.e("Firestore", "User not logged in")
            onFailure(Exception("User not authenticated"))
            return
        }

        val gunData = hashMapOf(
            "brand" to brand,
            "model" to model,
            "accuracy" to accuracy,
            "precision" to precision,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users").document(userId).collection("guns")
            .add(gunData)
            .addOnSuccessListener {
                Log.d("Firestore", "Gun added successfully")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error adding gun", exception)
                onFailure(exception)
            }
    }

    fun getAllGuns(onSuccess: (List<Gun>) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("guns")
            .get()
            .addOnSuccessListener { result ->
                val guns = result.map { it.toObject(Gun::class.java) }
                onSuccess(guns)
            }
            .addOnFailureListener { onFailure(it) }
    }


}
