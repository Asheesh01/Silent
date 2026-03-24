package com.example.voiceresponder.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.room.Room
import com.example.voiceresponder.data.AppDatabase
import com.example.voiceresponder.remote.CloudinaryHelper
import com.example.voiceresponder.remote.FirebaseHelper
import com.example.voiceresponder.remote.SmsHelper
import com.example.voiceresponder.remote.TranscriptionHelper
import com.example.voiceresponder.remote.TranslationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.voiceresponder.data.normalizePhone
import java.io.File

class ResponderService : Service() {

    private val CHANNEL_ID  = "ResponderServiceChannel"
    private val TAG         = "ResponderService"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val phoneNumber = intent?.getStringExtra("phoneNumber")
        val action      = intent?.action

        Log.d(TAG, "Service started — action: $action | number: $phoneNumber")

        if (action == "ACTION_MISSED_CALL" && phoneNumber != null) {
            processMissedCall(phoneNumber)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Voice Responder Active")
            .setContentText("Monitoring for missed calls…")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
        return START_STICKY
    }

    private fun processMissedCall(phoneNumber: String) {
        val database            = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "responder-db").build()
        val contactDao          = database.contactDao()
        val cloudinaryHelper    = CloudinaryHelper()
        val firebaseHelper      = FirebaseHelper()
        val smsHelper           = SmsHelper(this)
        val transcriptionHelper = TranscriptionHelper()
        val translationHelper   = TranslationHelper()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val normalizedNumber = normalizePhone(phoneNumber)
                Log.d(TAG, "Normalized incoming: $phoneNumber → $normalizedNumber")

                if (!contactDao.isContactSelected(normalizedNumber)) {
                    Log.d(TAG, "Not in whitelist — ignoring: $normalizedNumber")
                    return@launch
                }

                val voiceFile = File(filesDir, "default_response.mp4")
                if (!voiceFile.exists()) {
                    Log.w(TAG, "Voice file not found — sending text-only SMS fallback to $phoneNumber")
                    smsHelper.sendAudioLink(
                        phoneNumber = phoneNumber,
                        link        = "",
                        englishText = "I missed your call. I'll get back to you soon.",
                        hindiText   = "मैंने आपकी कॉल मिस कर दी। मैं जल्द वापस आऊंगा।"
                    )
                    return@launch
                }

                // ── 1. Upload to Cloudinary ───────────────────────────────────
                val downloadUrl = cloudinaryHelper.uploadAudio(voiceFile)
                if (downloadUrl == null) {
                    Log.w(TAG, "Cloudinary upload failed — sending text-only fallback")
                    smsHelper.sendAudioLink(
                        phoneNumber = phoneNumber,
                        link        = "",
                        englishText = "I missed your call. I'll get back to you soon.",
                        hindiText   = "मैंने आपकी कॉल मिस कर दी। मैं जल्द वापस आऊंगा।"
                    )
                    return@launch
                }
                Log.d(TAG, "Audio URL: $downloadUrl")

                // ── 2. Send immediate SMS with audio link (caller doesn't wait) ──
                smsHelper.sendAudioLink(phoneNumber = phoneNumber, link = downloadUrl)
                Log.d(TAG, "Initial SMS sent — now transcribing…")

                // ── 3a. Upload audio to AssemblyAI ───────────────────────────
                val assemblyUrl = transcriptionHelper.uploadAudio(voiceFile)
                if (assemblyUrl == null) {
                    showDebugNotification(
                        "❌ Step 1 FAILED: Upload",
                        "Could not upload audio to AssemblyAI. API key may be invalid (HTTP 401) or network error. Key: bd148c..."
                    )
                    return@launch
                }
                showDebugNotification("✅ Step 1 OK: Upload", assemblyUrl.take(80))

                // ── 3b. Submit transcription job ─────────────────────────────
                val transcriptId = transcriptionHelper.submitJob(assemblyUrl)
                if (transcriptId == null) {
                    showDebugNotification(
                        "❌ Step 2 FAILED: Submit",
                        transcriptionHelper.lastSubmitError.ifBlank { "Unknown error — check Logcat tag=TranscriptionHelper" }
                    )
                    return@launch
                }
                showDebugNotification("✅ Step 2 OK: Submit", "Job ID: $transcriptId")

                // ── 3c. Poll for transcription result ────────────────────────
                // Returns Pair(transcriptText, detectedLanguageCode)
                val pollPair = transcriptionHelper.pollResult(transcriptId)

                if (pollPair == null) {
                    showDebugNotification(
                        "❌ Step 3 FAILED: Poll",
                        transcriptionHelper.lastPollError.ifBlank { "Empty transcript returned" }
                    )
                    return@launch
                }

                val (transcript, detectedLang) = pollPair
                showDebugNotification("✅ Transcription OK ($detectedLang)", transcript.take(120))
                Log.d(TAG, "Transcript ($detectedLang): $transcript")

                // ── 4. Translate in the correct direction based on detected language ──
                // Hindi audio → EN = translate HI→EN,  HI = original
                // English audio → EN = original,        HI = translate EN→HI
                val englishText: String
                val hindiText: String

                if (detectedLang == "hi") {
                    hindiText   = transcript
                    englishText = translationHelper.translateToEnglish(transcript) ?: transcript
                } else {
                    englishText = transcript
                    hindiText   = translationHelper.translateToHindi(transcript) ?: transcript
                }
                Log.d(TAG, "EN: $englishText | HI: $hindiText")

                // ── 5. Send follow-up SMS with transcription text ─────────────
                smsHelper.sendAudioLink(
                    phoneNumber = phoneNumber,
                    link        = "",
                    englishText = englishText,
                    hindiText   = hindiText
                )
                Log.d(TAG, "Follow-up SMS with transcription sent")

                val isAppUser = firebaseHelper.checkUserExists(phoneNumber)
                Log.d(TAG, "Is app user: $isAppUser")

            } catch (e: Exception) {
                Log.e(TAG, "processMissedCall error", e)
            } finally {
                database.close()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Responder Service", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

            // Debug channel for transcription status
            val debugChannel = NotificationChannel(
                "TranscriptionDebug", "Transcription Debug", NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(debugChannel)
        }
    }

    /** Shows a notification on the device — no Logcat needed to diagnose failures. */
    private fun showDebugNotification(title: String, message: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, "TranscriptionDebug")
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        nm.notify(99, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
