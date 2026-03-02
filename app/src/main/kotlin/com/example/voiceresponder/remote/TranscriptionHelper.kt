package com.example.voiceresponder.remote

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Transcribes an audio file using AssemblyAI (free tier, no billing required).
 * Flow:
 *   1. Upload the audio file to AssemblyAI's upload endpoint
 *   2. Submit a transcription request
 *   3. Poll until the transcription is complete
 */
class TranscriptionHelper {

    private val TAG     = "TranscriptionHelper"
    private val API_KEY = "f334ea6ee7064f3b853487ef0ea8ec2f"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Transcribes [audioFile] and returns the text, or null on failure.
     * Blocking — must be called from Dispatchers.IO.
     */
    fun transcribe(audioFile: File): String? {
        val uploadUrl = uploadFile(audioFile) ?: return null
        val transcriptId = submitTranscription(uploadUrl) ?: return null
        return pollForResult(transcriptId)
    }

    // ── Step 1: Upload file ────────────────────────────────────────────────────
    private fun uploadFile(file: File): String? {
        return try {
            val body = file.asRequestBody("audio/mp4".toMediaType())
            val request = Request.Builder()
                .url("https://api.assemblyai.com/v2/upload")
                .addHeader("authorization", API_KEY)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Upload failed: ${response.code}")
                    return null
                }
                JSONObject(response.body?.string() ?: return null)
                    .optString("upload_url").takeIf { it.isNotEmpty() }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Upload error", e)
            null
        }
    }

    // ── Step 2: Submit transcription request ──────────────────────────────────
    private fun submitTranscription(uploadUrl: String): String? {
        return try {
            val jsonBody = JSONObject().apply {
                put("audio_url", uploadUrl)
                put("language_detection", true)   // auto-detect English / Hindi
            }.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("https://api.assemblyai.com/v2/transcript")
                .addHeader("authorization", API_KEY)
                .post(jsonBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Submit failed: ${response.code}")
                    return null
                }
                JSONObject(response.body?.string() ?: return null)
                    .optString("id").takeIf { it.isNotEmpty() }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Submit error", e)
            null
        }
    }

    // ── Step 3: Poll until complete ───────────────────────────────────────────
    private fun pollForResult(transcriptId: String): String? {
        val pollUrl = "https://api.assemblyai.com/v2/transcript/$transcriptId"
        repeat(30) {                                 // max ~60 sec (30 × 2s)
            Thread.sleep(2000)
            try {
                val request = Request.Builder()
                    .url(pollUrl)
                    .addHeader("authorization", API_KEY)
                    .build()

                client.newCall(request).execute().use { response ->
                    val json = JSONObject(response.body?.string() ?: return@use)
                    when (json.optString("status")) {
                        "completed" -> {
                            val text = json.optString("text").takeIf { it.isNotEmpty() }
                            Log.d(TAG, "Transcription done: $text")
                            return text
                        }
                        "error" -> {
                            Log.e(TAG, "Transcription error: ${json.optString("error")}")
                            return null
                        }
                        else -> Log.d(TAG, "Status: ${json.optString("status")} — waiting…")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Poll error", e)
            }
        }
        Log.e(TAG, "Transcription timed out")
        return null
    }
}
