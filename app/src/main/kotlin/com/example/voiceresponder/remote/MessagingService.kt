package com.example.voiceresponder.remote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.voiceresponder.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "FCMService"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Message received from: ${remoteMessage.from}")
        remoteMessage.data.let { data ->
            val audioUrl = data["audioUrl"]
            val senderPhone = data["senderPhone"]
            Log.d(TAG, "audioUrl=$audioUrl  senderPhone=$senderPhone")
            if (audioUrl != null) {
                showNotification(audioUrl, senderPhone)
            }
        }
    }

    private fun showNotification(audioUrl: String, senderPhone: String?) {
        val channelId = "IncomingVoiceMessage"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Voice Messages",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val callerLabel = if (!senderPhone.isNullOrBlank()) "from $senderPhone" else ""
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("🎙 New Voice Message $callerLabel")
            .setContentText("Tap to listen — $audioUrl")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Voice message $callerLabel\n$audioUrl"))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(2, notification)
    }

    /**
     * Called whenever the FCM token is refreshed.
     * We save it to Firestore so other devices can send push messages to this one.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            CoroutineScope(Dispatchers.IO).launch {
                FirebaseHelper().saveFcmToken(uid, token)
                Log.d(TAG, "FCM token saved to Firestore for uid=$uid")
            }
        } else {
            Log.w(TAG, "onNewToken: user not logged in — token NOT saved yet. Will be saved on next login.")
        }
    }
}
