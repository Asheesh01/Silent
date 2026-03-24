package com.example.voiceresponder.remote

import  android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

/**
 * Uploads audio files to Cloudinary using signed uploads.
 * Returns a public HTTPS URL to the uploaded file, or null on failure.
 */
class CloudinaryHelper {

    private val TAG         = "CloudinaryHelper"
    private val CLOUD_NAME  = "duiqjdicz"
    private val API_KEY     = "435849175891229"
    private val API_SECRET  = "IhKcfQwNycVr9JDDzu7sXLciULM"

    private val client = OkHttpClient()
    private val uploadUrl = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/video/upload"

    /**
     * Uploads [audioFile] to Cloudinary and returns its public download URL.
     * Blocking — call from Dispatchers.IO coroutine.
     */
    fun uploadAudio(audioFile: File): String? {
        return try {
            val timestamp = (System.currentTimeMillis() / 1000).toString()
            val signature = sign("timestamp=$timestamp")

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file", audioFile.name,
                    audioFile.asRequestBody("audio/mp4".toMediaType())
                )
                .addFormDataPart("api_key",   API_KEY)
                .addFormDataPart("timestamp",  timestamp)
                .addFormDataPart("signature",  signature)
                .build()

            val request = Request.Builder()
                .url(uploadUrl)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string()
                if (!response.isSuccessful) {
                    Log.e(TAG, "Upload failed ${response.code}: $body")
                    return null
                }
                val json = JSONObject(body ?: return null)
                val url = json.optString("secure_url").takeIf { it.isNotEmpty() }
                Log.d(TAG, "Uploaded to Cloudinary: $url")
                url
            }
        } catch (e: Exception) {
            Log.e(TAG, "Cloudinary upload error", e)
            null
        }
    }

    /** SHA-1 signature required by Cloudinary signed-upload API */
    private fun sign(params: String): String {
        val toSign = "$params$API_SECRET"
        val digest = MessageDigest.getInstance("SHA-1").digest(toSign.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
