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
 * Transcribes audio using AssemblyAI (free tier).
 * Steps are exposed publicly so callers can show per-step debug notifications.
 */
class TranscriptionHelper {

    private val TAG     = "TranscriptionHelper"
    // TODO: Replace with your real AssemblyAI API key from https://www.assemblyai.com/dashboard
    private val API_KEY = "bd148c48a8a14442b077342e327953a7" // ← REPLACE THIS

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    // ── STEP 1: Upload audio file to AssemblyAI ────────────────────────────────
    // Returns an AssemblyAI-hosted URL, or null on failure.
    fun uploadAudio(file: File): String? {
        Log.d(TAG, "Uploading ${file.name} (${file.length()} bytes)")
        return try {
            val body = file.asRequestBody("audio/mpeg".toMediaType())
            val request = Request.Builder()
                .url("https://api.assemblyai.com/v2/upload")
                .addHeader("authorization", API_KEY)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string()
                Log.d(TAG, "Upload HTTP ${response.code}: $bodyStr")
                if (!response.isSuccessful) {
                    Log.e(TAG, "Upload FAILED HTTP ${response.code}")
                    return null
                }
                JSONObject(bodyStr ?: return null)
                    .optString("upload_url").takeIf { it.isNotEmpty() }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Upload exception: ${e.message}", e)
            null
        }
    }

    // ── STEP 2: Submit transcription job ──────────────────────────────────────
    // Takes an AssemblyAI-hosted URL. Returns transcript ID or null.
    // On failure, lastSubmitError / lastPollError contain details for debug notifications.
    var lastSubmitError: String = ""
    var lastPollError:   String = ""

    fun submitJob(audioUrl: String): String? {
        Log.d(TAG, "Submitting transcription for: $audioUrl")
        return try {
            // Both models required: universal-3-pro alone doesn't support Hindi (hi).
            // Adding universal-2 enables 99 languages including Hindi.
            val jsonBody = JSONObject().apply {
                put("audio_url", audioUrl)
                put("speech_models", org.json.JSONArray()
                    .put("universal-3-pro")
                    .put("universal-2"))
            }.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("https://api.assemblyai.com/v2/transcript")
                .addHeader("authorization", API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(jsonBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string()
                Log.d(TAG, "Submit HTTP ${response.code}: $bodyStr")
                if (!response.isSuccessful) {
                    lastSubmitError = "HTTP ${response.code}: $bodyStr"
                    Log.e(TAG, "Submit FAILED — $lastSubmitError")
                    return null
                }
                val json = JSONObject(bodyStr ?: return null)
                val id = json.optString("id").takeIf { it.isNotEmpty() }
                if (id == null) {
                    lastSubmitError = "HTTP 200 but no 'id' in response: $bodyStr"
                    Log.e(TAG, "Submit 200 but missing id — $lastSubmitError")
                }
                id
            }
        } catch (e: Exception) {
            lastSubmitError = "Exception: ${e.message}"
            Log.e(TAG, "Submit exception: ${e.message}", e)
            null
        }
    }

    // ── STEP 3: Poll until status = completed or error ────────────────────────
    // Polls every 4s for up to 6 minutes (universal-3-pro can take 2-4 min).
    // Returns transcript text, or null. lastPollError holds the failure reason.
    fun pollResult(transcriptId: String): String? {
        val pollUrl = "https://api.assemblyai.com/v2/transcript/$transcriptId"
        Log.d(TAG, "Polling transcript $transcriptId")
        repeat(90) { attempt ->           // 90 × 4s = 360s = 6 minutes
            Thread.sleep(4000)
            try {
                val request = Request.Builder()
                    .url(pollUrl)
                    .addHeader("authorization", API_KEY)
                    .build()

                val bodyStr = client.newCall(request).execute().use { it.body?.string() }
                val json = JSONObject(bodyStr ?: return@repeat)
                val status = json.optString("status")
                Log.d(TAG, "Poll[$attempt] status=$status")

                when (status) {
                    "completed" -> {
                        val text = json.optString("text").takeIf { it.isNotEmpty() }
                        if (text == null) {
                            lastPollError = "Completed but transcript text is empty (audio may be silent)"
                            Log.e(TAG, lastPollError)
                        } else {
                            Log.d(TAG, "Transcript: $text")
                        }
                        return text
                    }
                    "error" -> {
                        lastPollError = "AssemblyAI error: ${json.optString("error")}"
                        Log.e(TAG, lastPollError)
                        return null
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Poll[$attempt] exception: ${e.message}")
            }
        }
        lastPollError = "Timed out after 6 minutes — job may still be processing"
        Log.e(TAG, lastPollError)
        return null
    }
}
