package com.example.voiceresponder.remote

import android.util.Log
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

/**
 * Provides on-device translation using ML Kit.
 * Supports Hindi ↔ English translation.
 * Language models are downloaded on first use (requires internet for the first time).
 */
class TranslationHelper {

    private val TAG = "TranslationHelper"

    /**
     * Translates [text] from Hindi to English.
     * Use this when AssemblyAI returns a Hindi transcript that needs to be shown in English.
     * Must be called from a coroutine (suspend function).
     */
    suspend fun translateToEnglish(text: String): String? = translate(
        text = text,
        sourceLang = TranslateLanguage.HINDI,
        targetLang = TranslateLanguage.ENGLISH
    )

    /**
     * Translates [text] from English to Hindi.
     * Returns the translated string, or null on failure.
     * Must be called from a coroutine (suspend function).
     */
    suspend fun translateToHindi(text: String): String? = translate(
        text = text,
        sourceLang = TranslateLanguage.ENGLISH,
        targetLang = TranslateLanguage.HINDI
    )

    private suspend fun translate(text: String, sourceLang: String, targetLang: String): String? {
        return try {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLang)
                .setTargetLanguage(targetLang)
                .build()

            val translator = Translation.getClient(options)

            // Download model if not already downloaded
            translator.downloadModelIfNeeded().await()

            val result = translator.translate(text).await()
            translator.close()
            result
        } catch (e: Exception) {
            Log.e(TAG, "Translation failed: ${e.message}", e)
            null
        }
    }
}
