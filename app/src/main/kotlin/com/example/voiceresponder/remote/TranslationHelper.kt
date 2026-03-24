package com.example.voiceresponder.remote

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Provides cloud-based translation using the MyMemory API (free, no API key needed).
 * Much better translation quality than ML Kit for colloquial Hindi ↔ English.
 * Requires internet connection.
 */
class TranslationHelper {

    private val TAG = "TranslationHelper"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Translates [text] from Hindi to English using MyMemory API.
     * Returns the translated string, or null on failure.
     */
    fun translateToEnglish(text: String): String? = translate(text, from = "hi", to = "en")

    /**
     * Translates [text] from English to Hindi using MyMemory API.
     * Returns the translated string, or null on failure.
     */
    fun translateToHindi(text: String): String? = translate(text, from = "en", to = "hi")

    private fun translate(text: String, from: String, to: String): String? {
        return try {
            val encoded = URLEncoder.encode(text, "UTF-8")
            val url = "https://api.mymemory.translated.net/get?q=$encoded&langpair=$from|$to"

            val request = Request.Builder().url(url).build()
            val responseStr = client.newCall(request).execute().use { it.body?.string() }

            Log.d(TAG, "MyMemory response: $responseStr")

            val json = JSONObject(responseStr ?: return null)
            val translated = json
                .optJSONObject("responseData")
                ?.optString("translatedText")
                ?.takeIf { it.isNotBlank() && !it.equals(text, ignoreCase = true) }

            if (translated == null) {
                Log.e(TAG, "Translation returned null or same text for: $text")
            } else {
                Log.d(TAG, "Translated ($from→$to): $translated")
            }
            translated
        } catch (e: Exception) {
            Log.e(TAG, "Translation failed: ${e.message}", e)
            null
        }
    }
}
