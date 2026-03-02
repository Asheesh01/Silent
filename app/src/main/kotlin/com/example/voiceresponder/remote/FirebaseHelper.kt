package com.example.voiceresponder.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Handles Firebase Firestore operations only.
 * Audio upload is handled by CloudinaryHelper.
 */
class FirebaseHelper {

    private val auth      = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun checkUserExists(phoneNumber: String): Boolean {
        val result = firestore.collection("users")
            .whereEqualTo("phoneNumber", phoneNumber)
            .get()
            .await()
        return !result.isEmpty
    }

    suspend fun getFCMToken(phoneNumber: String): String? {
        val result = firestore.collection("users")
            .whereEqualTo("phoneNumber", phoneNumber)
            .get()
            .await()
        return if (!result.isEmpty) {
            result.documents[0].getString("fcmToken")
        } else {
            null
        }
    }
}
