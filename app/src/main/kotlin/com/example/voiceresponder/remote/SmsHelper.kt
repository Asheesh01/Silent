package com.example.voiceresponder.remote

import android.content.Context
import android.telephony.SmsManager
import android.util.Log

class SmsHelper(private val context: Context) {

    private val TAG = "SmsHelper"

    /**
     * Sends an automated SMS to the missed caller.
     * @param phoneNumber  The caller's number (e.g. "+919876543210")
     * @param link         Public Firebase Storage download URL to the voice message
     * @param englishText  Optional English transcription of the voice message
     * @param hindiText    Optional Hindi translation of the transcription
     */
    fun sendAudioLink(
        phoneNumber: String,
        link: String,
        englishText: String? = null,
        hindiText: String? = null
    ) {
        val smsManager: SmsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }

        val sb = StringBuilder()
        sb.appendLine("📞 I missed your call.")
        sb.appendLine("🎙 Voice Message: $link")

        if (!englishText.isNullOrBlank()) {
            sb.appendLine()
            sb.appendLine("📝 Message (EN): $englishText")
        }
        if (!hindiText.isNullOrBlank()) {
            sb.appendLine()
            sb.appendLine("📝 संदेश (HI): $hindiText")
        }

        val fullMessage = sb.toString().trim()

        try {
            // sendMultipartTextMessage handles messages > 160 chars automatically
            val parts = smsManager.divideMessage(fullMessage)
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            Log.d(TAG, "SMS sent to $phoneNumber")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS to $phoneNumber", e)
        }
    }
}

