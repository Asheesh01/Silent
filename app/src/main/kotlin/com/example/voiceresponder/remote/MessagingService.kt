package com.example.voiceresponder.remote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.voiceresponder.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.data.let { data ->
            val audioUrl = data["audioUrl"]
            if (audioUrl != null) {
                showNotification(audioUrl)
                // TODO: Auto-download and play logic
            }
        }
    }

    private fun showNotification(audioUrl: String) {
        val channelId = "IncomingVoiceMessage"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Voice Messages", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("New Voice Message")
            .setContentText("You received a missed call voice responder message.")
            .setSmallIcon(android.R.drawable.ic_lock_idle_low_battery) // Replace with actual icon
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(2, notification)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: Update token in Firestore for current user
    }
}
