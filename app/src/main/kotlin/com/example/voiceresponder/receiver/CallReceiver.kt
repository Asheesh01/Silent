package com.example.voiceresponder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.example.voiceresponder.service.ResponderService

class CallReceiver : BroadcastReceiver() {

    companion object {
        private var lastState = TelephonyManager.CALL_STATE_IDLE
        private var isRinging = false
        private var savedNumber: String? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val stateString = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            
            val state = when (stateString) {
                TelephonyManager.EXTRA_STATE_RINGING -> TelephonyManager.CALL_STATE_RINGING
                TelephonyManager.EXTRA_STATE_OFFHOOK -> TelephonyManager.CALL_STATE_OFFHOOK
                TelephonyManager.EXTRA_STATE_IDLE -> TelephonyManager.CALL_STATE_IDLE
                else -> TelephonyManager.CALL_STATE_IDLE
            }

            onCallStateChanged(context, state, number)
        }
    }

    private fun onCallStateChanged(context: Context, state: Int, number: String?) {
        if (lastState == state) {
            return
        }

        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                isRinging = true
                savedNumber = number
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                // Call answered
                isRinging = false
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                // Call ended or missed
                if (isRinging) {
                    // It was a missed call! (RINGING -> IDLE transition without OFFHOOK)
                    val intent = Intent(context, ResponderService::class.java).apply {
                        putExtra("phoneNumber", savedNumber)
                        action = "ACTION_MISSED_CALL"
                    }
                    context.startForegroundService(intent)
                }
                isRinging = false
                savedNumber = null
            }
        }
        lastState = state
    }
}
