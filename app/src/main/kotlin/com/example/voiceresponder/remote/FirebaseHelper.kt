package com.example.voiceresponder.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * Handles Firebase Firestore operations and FCM push delivery.
 * Audio upload is handled by CloudinaryHelper.
 */
class FirebaseHelper {

    private val TAG       = "FirebaseHelper"
    private val auth      = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val httpClient = OkHttpClient()

    // ── User lookup (UID-scoped — compatible with Firestore per-UID rules) ──────

    /**
     * Check if a user document exists for the given UID.
     * Only reads the caller's own document — safe under per-UID Firestore rules.
     */
    suspend fun checkUserExistsByUid(uid: String): Boolean {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            doc.exists()
        } catch (e: Exception) {
            Log.e(TAG, "checkUserExistsByUid failed for uid=$uid", e)
            false
        }
    }

    /**
     * Fetch the FCM token for the given UID.
     * Only reads the user's own document — safe under per-UID Firestore rules.
     * The caller must already know the recipient's UID (e.g. stored locally or
     * shared out-of-band) — cross-user phone lookups are intentionally blocked
     * by security rules to protect user privacy.
     */
    suspend fun getFCMTokenByUid(uid: String): String? {
        return try {
            firestore.collection("users").document(uid).get().await()
                .getString("fcmToken")
        } catch (e: Exception) {
            Log.e(TAG, "getFCMTokenByUid failed for uid=$uid", e)
            null
        }
    }

    // ── FCM token management ──────────────────────────────────────────────────

    /**
     * Saves this device's FCM token to Firestore under the user's UID document.
     * Also stores the user's phoneNumber so other devices can look up the token.
     */
    suspend fun saveFcmToken(uid: String, token: String) {
        try {
            // Get current user's phone from FirebaseAuth (populated after phone login)
            val phoneNumber = auth.currentUser?.phoneNumber ?: ""
            firestore.collection("users").document(uid)
                .set(
                    mapOf(
                        "fcmToken"    to token,
                        "phoneNumber" to phoneNumber
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
                )
                .await()
            Log.d(TAG, "FCM token + phoneNumber saved for uid=$uid")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save FCM token", e)
        }
    }

    /**
     * Fetches the current device FCM token and saves it to Firestore.
     * Call this right after every successful login.
     */
    suspend fun refreshAndSaveToken(uid: String) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "Current FCM token: $token")
            saveFcmToken(uid, token)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch/save FCM token", e)
        }
    }

    // ── FCM push delivery (direct device-to-device via FCM HTTP) ─────────────

    /**
     * Sends a push notification to the receiver's device using FCM Legacy HTTP API.
     *
     * NOTE: For production apps use Firebase Cloud Functions with the Admin SDK
     * so that the server key is not embedded in the APK. For development/testing
     * this direct approach is the simplest way to verify the flow end-to-end.
     *
     * @param receiverToken  The recipient's FCM token (stored in their Firestore doc)
     * @param audioUrl       Cloudinary URL of the voice message
     * @param senderPhone    The caller's phone number (for notification label)
     * @param serverKey      Your Firebase project's legacy server key
     *                       (Firebase Console → Project settings → Cloud Messaging →
     *                        "Cloud Messaging API (Legacy)" → Server key)
     */
    fun sendFcmMessage(
        receiverToken: String,
        audioUrl: String,
        senderPhone: String,
        serverKey: String
    ): Boolean {
        return try {
            val payload = JSONObject().apply {
                put("to", receiverToken)
                put("data", JSONObject().apply {
                    put("audioUrl",    audioUrl)
                    put("senderPhone", senderPhone)
                })
                put("notification", JSONObject().apply {
                    put("title", "🎙 New Voice Message")
                    put("body",  "You have a voice message from $senderPhone")
                })
                put("priority", "high")
            }

            val body = payload.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url("https://fcm.googleapis.com/fcm/send")
                .addHeader("Authorization", "key=$serverKey")
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string()
            Log.d(TAG, "FCM send response [${response.code}]: $responseBody")

            if (response.isSuccessful) {
                Log.d(TAG, "FCM message sent successfully to $senderPhone")
                true
            } else {
                Log.e(TAG, "FCM send failed: ${response.code} — $responseBody")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception sending FCM message", e)
            false
        }
    }

    // ── Cross-device sync helpers ─────────────────────────────────────────────

    /** Save the full list of selected contact numbers to Firestore for this UID. */
    suspend fun saveContacts(uid: String, numbers: List<String>) {
        try {
            firestore.collection("users").document(uid)
                .set(mapOf("selectedContacts" to numbers), com.google.firebase.firestore.SetOptions.merge())
                .await()
            Log.d(TAG, "Contacts saved to Firestore: ${numbers.size} numbers")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save contacts", e)
        }
    }

    /** Load the saved contact number list for this UID. Returns empty list on failure. */
    @Suppress("UNCHECKED_CAST")
    suspend fun loadContacts(uid: String): List<String> {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            (doc.get("selectedContacts") as? List<String>) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load contacts", e)
            emptyList()
        }
    }

    /** Persist the Cloudinary URL of the user's voice message. */
    suspend fun saveAudioUrl(uid: String, url: String) {
        try {
            firestore.collection("users").document(uid)
                .set(mapOf("voiceMessageUrl" to url), com.google.firebase.firestore.SetOptions.merge())
                .await()
            Log.d(TAG, "Audio URL saved: $url")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save audio URL", e)
        }
    }

    /** Load the Cloudinary URL of the user's voice message. Returns null if not set. */
    suspend fun loadAudioUrl(uid: String): String? {
        return try {
            firestore.collection("users").document(uid).get().await()
                .getString("voiceMessageUrl")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load audio URL", e)
            null
        }
    }

    /** Save whether the Auto Responder is enabled for this UID. */
    suspend fun saveResponderState(uid: String, isActive: Boolean) {
        try {
            firestore.collection("users").document(uid)
                .set(mapOf("responderActive" to isActive), com.google.firebase.firestore.SetOptions.merge())
                .await()
            Log.d(TAG, "Responder state saved: $isActive")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save responder state", e)
        }
    }

    /** Load the Auto Responder state for this UID. Returns false if not set or on error. */
    suspend fun loadResponderState(uid: String): Boolean {
        return try {
            firestore.collection("users").document(uid).get().await()
                .getBoolean("responderActive") ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load responder state", e)
            false
        }
    }
}
