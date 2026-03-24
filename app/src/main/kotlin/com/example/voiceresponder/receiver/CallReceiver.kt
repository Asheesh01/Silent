package com.example.voiceresponder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.telephony.TelephonyManager
import android.util.Log
import com.example.voiceresponder.service.ResponderService

class CallReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "CallReceiver"
        private var lastState = TelephonyManager.CALL_STATE_IDLE
        private var isRinging = false
        private var savedNumber: String? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val stateString = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            // NOTE: EXTRA_INCOMING_NUMBER is null on Android 9+ unless app is default dialer.
            val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            val state = when (stateString) {
                TelephonyManager.EXTRA_STATE_RINGING -> TelephonyManager.CALL_STATE_RINGING
                TelephonyManager.EXTRA_STATE_OFFHOOK -> TelephonyManager.CALL_STATE_OFFHOOK
                TelephonyManager.EXTRA_STATE_IDLE    -> TelephonyManager.CALL_STATE_IDLE
                else                                 -> TelephonyManager.CALL_STATE_IDLE
            }

            onCallStateChanged(context, state, number)
        }
    }

    private fun onCallStateChanged(context: Context, state: Int, number: String?) {
        if (lastState == state) return

        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                isRinging = true
                savedNumber = number   // may be null on Android 9+; that's OK
                Log.d(TAG, "RINGING — number from intent: $number")
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                isRinging = false
                Log.d(TAG, "OFFHOOK — call answered")
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                if (isRinging) {
                    // Missed call: RINGING → IDLE without OFFHOOK
                    Log.d(TAG, "IDLE after RINGING — missed call, savedNumber=$savedNumber")

                    if (!savedNumber.isNullOrBlank()) {
                        // We have the number already — fire service immediately
                        fireMissedCallService(context, savedNumber!!)
                    } else {
                        // Android 9+ hid the number; wait a moment for CallLog to update, then read it
                        Log.d(TAG, "Number is null — will read from CallLog in 1s")
                        val appContext = context.applicationContext
                        Handler(Looper.getMainLooper()).postDelayed({
                            val callerNumber = getLastMissedCallerFromLog(appContext)
                            Log.d(TAG, "CallLog resolved number: $callerNumber")
                            if (!callerNumber.isNullOrBlank()) {
                                fireMissedCallService(appContext, callerNumber)
                            } else {
                                Log.e(TAG, "Could not resolve caller number from CallLog — SMS not sent")
                            }
                        }, 1_000L)
                    }
                }
                isRinging = false
                savedNumber = null
            }
        }
        lastState = state
    }

    private fun fireMissedCallService(context: Context, phoneNumber: String) {
        Log.d(TAG, "Starting ResponderService for missed call from $phoneNumber")
        val serviceIntent = Intent(context, ResponderService::class.java).apply {
            action = "ACTION_MISSED_CALL"
            putExtra("phoneNumber", phoneNumber)
        }
        context.startForegroundService(serviceIntent)
    }

    /**
     * Reads the most recent missed call entry from the system CallLog.
     * Requires READ_CALL_LOG permission (already declared in manifest).
     */
    private fun getLastMissedCallerFromLog(context: Context): String? {
        return try {
            val cursor: Cursor? = context.contentResolver.query(
                Uri.parse("content://call_log/calls"),
                arrayOf(CallLog.Calls.NUMBER),
                "${CallLog.Calls.TYPE} = ?",
                arrayOf(CallLog.Calls.MISSED_TYPE.toString()),
                "${CallLog.Calls.DATE} DESC"
            )
            cursor?.use {
                if (it.moveToFirst()) it.getString(0) else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading CallLog", e)
            null
        }
    }
}
