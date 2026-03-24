package com.example.voiceresponder.remote

import android.util.Log
import com.example.voiceresponder.data.ContactEntity
import com.example.voiceresponder.data.ContactDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

/**
 * Handles bidirectional sync of user data (contacts + voice recording)
 * between the local device and the cloud (Firestore + Cloudinary).
 */
class SyncHelper {

    private val TAG             = "SyncHelper"
    private val firebaseHelper  = FirebaseHelper()
    private val httpClient      = OkHttpClient()

    /**
     * Push the current selected contact list to Firestore.
     * Call this every time a contact is added or removed.
     */
    suspend fun pushContactsToCloud(uid: String, numbers: Set<String>) {
        firebaseHelper.saveContacts(uid, numbers.toList())
    }

    /**
     * Called right after a successful login on any device.
     * 1. Loads contacts from Firestore → saves into local Room DB.
     * 2. Loads audio URL from Firestore → downloads file to filesDir.
     */
    suspend fun restoreFromCloud(
        uid: String,
        contactDao: ContactDao,
        filesDir: File
    ) {
        withContext(Dispatchers.IO) {
            // ── 1. Restore contacts ───────────────────────────────────────────
            val cloudNumbers = firebaseHelper.loadContacts(uid)
            if (cloudNumbers.isNotEmpty()) {
                // Clear local DB and re-insert cloud list so they stay in sync
                val localNumbers = contactDao.getAllContacts().map { it.phoneNumber }.toSet()
                val toAdd = cloudNumbers.toSet() - localNumbers
                toAdd.forEach { contactDao.addContact(ContactEntity(it)) }
                Log.d(TAG, "Restored ${toAdd.size} contact(s) from cloud (${cloudNumbers.size} total)")
            } else {
                Log.d(TAG, "No cloud contacts to restore")
            }

            // ── 2. Restore voice recording ────────────────────────────────────
            val audioUrl  = firebaseHelper.loadAudioUrl(uid)
            val audioFile = File(filesDir, "default_response.mp4")

            if (!audioUrl.isNullOrBlank() && !audioFile.exists()) {
                try {
                    val request  = Request.Builder().url(audioUrl).build()
                    val response = httpClient.newCall(request).execute()
                    if (response.isSuccessful) {
                        response.body?.byteStream()?.use { input ->
                            FileOutputStream(audioFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                        Log.d(TAG, "Voice recording restored: ${audioFile.length() / 1024} KB")
                    } else {
                        Log.w(TAG, "Audio download failed: ${response.code}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to restore audio from cloud", e)
                }
            } else if (audioFile.exists()) {
                Log.d(TAG, "Voice recording already exists locally — skipping restore")
            } else {
                Log.d(TAG, "No cloud audio URL to restore")
            }
        }
    }
}
