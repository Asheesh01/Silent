package com.example.voiceresponder.service

import android.app.*
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
        val cloudinaryHelper    = CloudinaryHelper()          // ← replaces Firebase Storage
        val firebaseHelper      = FirebaseHelper()            // still used for checkUserExists / getFCMToken
        val smsHelper           = SmsHelper(this)
        val transcriptionHelper = TranscriptionHelper()          // ← AssemblyAI (free)
        val translationHelper   = TranslationHelper()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Normalize number to last 10 digits for consistent matching
                val normalizedNumber = normalizePhone(phoneNumber)
                Log.d(TAG, "Normalized incoming: $phoneNumber → $normalizedNumber")

                // Only respond to whitelisted contacts
                if (!contactDao.isContactSelected(normalizedNumber)) {
                    Log.d(TAG, "Not in whitelist — ignoring: $normalizedNumber")
                    return@launch
                }

                val voiceFile = File(filesDir, "default_response.mp4")
                if (!voiceFile.exists()) {
                    Log.e(TAG, "Voice file not found: ${voiceFile.absolutePath}")
                    return@launch
                }

                // ── 1. Upload audio to Cloudinary (free, no billing required) ──
                val downloadUrl = cloudinaryHelper.uploadAudio(voiceFile)
                if (downloadUrl == null) {
                    Log.w(TAG, "Cloudinary upload failed — sending text-only SMS fallback")
                    // Fallback: send a simple text SMS so caller is not left hanging
                    smsHelper.sendAudioLink(
                        phoneNumber = phoneNumber,
                        link        = "",
                        englishText = "I missed your call. I'll get back to you soon.",
                        hindiText   = "मैंने आपकी कॉल मिस कर दी। मैं जल्द वापस आऊंगा।"
                    )
                    return@launch
                }
                Log.d(TAG, "Audio URL: $downloadUrl")

                val isAppUser = firebaseHelper.checkUserExists(phoneNumber)

                if (isAppUser) {
                    // App user → FCM push (Cloud Function trigger in production)
                    Log.d(TAG, "App user — FCM trigger ready")
                } else {
                    // ── 2. Transcribe voice message → English text ──
                    val englishText = transcriptionHelper.transcribe(voiceFile)
                    Log.d(TAG, "Transcript EN: $englishText")

                    // ── 3. Translate English text → Hindi ──
                    val hindiText = englishText?.let { translationHelper.translateToHindi(it) }
                    Log.d(TAG, "Translation HI: $hindiText")

                    // ── 4. Send enriched SMS: audio link + EN + HI ──
                    smsHelper.sendAudioLink(
                        phoneNumber = phoneNumber,
                        link        = downloadUrl,
                        englishText = englishText,
                        hindiText   = hindiText
                    )
                }
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
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
