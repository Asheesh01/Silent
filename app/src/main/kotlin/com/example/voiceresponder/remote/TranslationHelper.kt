package com.example.voiceresponder.remote

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Provides cloud-based translation using Google Translate's public Neural Machine Translation (NMT).
 * This provides the most natural, conversational Hindi ↔ English translations (e.g. understanding
 * that "पेपर देने" means "taking an exam", not "handing over a paper").
 */
class TranslationHelper {

    private val TAG = "TranslationHelper"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Translates [text] from Hindi to English using Google NMT.
     */
    fun translateToEnglish(text: String): String? = translate(text, from = "hi", to = "en")

    /**
     * Translates [text] from English to Hindi using Google NMT.
     */
    fun translateToHindi(text: String): String? = translate(text, from = "en", to = "hi")

    private fun translate(text: String, from: String, to: String): String? {
        return try {
            val encoded = URLEncoder.encode(text, "UTF-8")
            // Utilizes the public Google Translate API endpoint (commonly used for free tier translation)
            val url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=$from&tl=$to&dt=t&q=$encoded"

            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0")
                .build()

            val responseStr = client.newCall(request).execute().use { it.body?.string() }
            Log.d(TAG, "Google Translate response: $responseStr")

            // The response is a nested JSON array. 
            // Example: [[["Translated part 1", "Original part 1", ...], ["Translated part 2", ...]]]
            val jsonArray = JSONArray(responseStr ?: return null)
            val textBlocks = jsonArray.optJSONArray(0) ?: return null

            val translatedBuilder = java.lang.StringBuilder()
            for (i in 0 until textBlocks.length()) {
                val sentenceBlock = textBlocks.optJSONArray(i)
                if (sentenceBlock != null && sentenceBlock.length() > 0) {
                    translatedBuilder.append(sentenceBlock.optString(0))
                }
            }

            val translated = translatedBuilder.toString().trim().takeIf { it.isNotBlank() }
            Log.d(TAG, "Translated ($from→$to): $translated")
            
            translated
        } catch (e: Exception) {
            Log.e(TAG, "Translation failed: ${e.message}", e)
            null
        }
    }
}
