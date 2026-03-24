package com.example.voiceresponder.remote

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat

class SmsHelper(private val context: Context) {

    private val TAG = "SmsHelper"

    /**
     * Sends an automated SMS to the missed caller.
     * @param phoneNumber  The caller's number (e.g. "+919876543210")
     * @param link         Public Cloudinary download URL to the voice message
     * @param englishText  Optional English transcription of the voice message
     * @param hindiText    Optional Hindi translation of the transcription
     */
    fun sendAudioLink(
        phoneNumber: String,
        link: String,
        englishText: String? = null,
        hindiText: String? = null
    ) {
        // ── Runtime permission guard ──────────────────────────────────────────
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "SEND_SMS permission not granted — SMS will NOT be sent to $phoneNumber. " +
                "Grant the permission in device Settings > Apps > Personalized Voice Responder > Permissions.")
            return
        }

        val smsManager: SmsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }

        val sb = StringBuilder()
        sb.appendLine("📞 I missed your call.")

        if (link.isNotBlank()) {
            sb.appendLine("🎙 Voice Message: $link")
        }

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
            val parts = smsManager.divideMessage(fullMessage)
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            Log.d(TAG, "SMS sent successfully to $phoneNumber (${parts.size} part(s))")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException — SEND_SMS permission was revoked mid-session for $phoneNumber", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS to $phoneNumber", e)
        }
    }
}
