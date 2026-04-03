package com.example.voiceresponder.remote

import android.util.Log
import com.example.voiceresponder.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Sends OTP emails via EmailJS REST API.
 *
 * EmailJS uses your connected Gmail account to send emails,
 * so they land in inbox — not spam — because they come from
 * a real Gmail address that the recipient trusts.
 *
 * Credentials are read from BuildConfig (set in build.gradle.kts).
 */
object EmailOtpHelper {

    private const val TAG       = "EmailOtp"
    private const val API_URL   = "https://api.emailjs.com/api/v1.0/email/send"

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    data class EmailResult(
        val success: Boolean,
        val errorMessage: String = ""
    )

    /**
     * Sends a 6-digit [otp] to [toEmail] via EmailJS.
     * The plain OTP is sent here and must never be stored — only its
     * SHA-256 hash is kept by OtpSecurityManager.
     */
    suspend fun sendOtp(toEmail: String, otp: String): EmailResult = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("service_id",   BuildConfig.EMAILJS_SERVICE_ID)
                put("template_id",  BuildConfig.EMAILJS_TEMPLATE_ID)
                put("user_id",      BuildConfig.EMAILJS_PUBLIC_KEY)
                put("template_params", JSONObject().apply {
                    put("to_email",  toEmail)
                    put("otp_code",  otp)
                    put("app_name",  "Silent Mode")
                })
            }

            val body = payload.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build()

            val responseText = client.newCall(request).execute().use { response ->
                val text = response.body?.string() ?: ""
                Log.d(TAG, "EmailJS [${response.code}]: $text")
                text
            }

            return@withContext when {
                responseText.contains("OK", ignoreCase = true) ->
                    EmailResult(true)
                responseText.contains("service", ignoreCase = true) ||
                responseText.contains("template", ignoreCase = true) ->
                    EmailResult(false, "EmailJS not configured — check Service/Template/Key IDs.")
                responseText.contains("limit", ignoreCase = true) ->
                    EmailResult(false, "EmailJS monthly limit reached. Try again next month.")
                else ->
                    EmailResult(false, "Email send failed: $responseText")
            }

        } catch (e: java.net.UnknownHostException) {
            EmailResult(false, "No internet connection.")
        } catch (e: java.net.SocketTimeoutException) {
            EmailResult(false, "Request timed out. Try again.")
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
            EmailResult(false, "Error: ${e.message}")
        }
    }
}
