package com.example.voiceresponder.remote

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

/**
 * WorkManager job that deletes a Cloudinary audio file 24 hours after it was
 * uploaded. The [publicId] of the file is passed in as input data.
 *
 * Scheduling:
 * ```
 *   DeleteAudioWorker.schedule(context, publicId)
 * ```
 */
class DeleteAudioWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val TAG        = "DeleteAudioWorker"
    private val CLOUD_NAME = Keys.CLOUDINARY_CLOUD_NAME
    private val API_KEY    = Keys.CLOUDINARY_API_KEY
    private val API_SECRET = Keys.CLOUDINARY_API_SECRET
    private val client     = OkHttpClient()

    override suspend fun doWork(): Result {
        val publicId = inputData.getString(KEY_PUBLIC_ID)
            ?: return Result.failure()

        return try {
            val timestamp = (System.currentTimeMillis() / 1000).toString()
            val signature = sign("public_id=$publicId&timestamp=$timestamp")

            val body = FormBody.Builder()
                .add("public_id", publicId)
                .add("api_key",   API_KEY)
                .add("timestamp", timestamp)
                .add("signature", signature)
                .build()

            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/$CLOUD_NAME/video/destroy")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val responseText = response.body?.string()
                if (response.isSuccessful) {
                    Log.d(TAG, "Auto-deleted Cloudinary file: $publicId | response: $responseText")
                    Result.success()
                } else {
                    Log.e(TAG, "Cloudinary delete failed [${response.code}]: $responseText")
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "DeleteAudioWorker exception for publicId=$publicId", e)
            Result.retry()
        }
    }

    /** SHA-1 signature required by Cloudinary signed API */
    private fun sign(params: String): String {
        val toSign  = "$params$API_SECRET"
        val digest  = MessageDigest.getInstance("SHA-1").digest(toSign.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    companion object {
        const val KEY_PUBLIC_ID = "public_id"

        /**
         * Enqueue a one-time delete job delayed by 24 hours.
         * Safe to call multiple times — each call creates an independent work request.
         */
        fun schedule(context: Context, publicId: String) {
            val data    = workDataOf(KEY_PUBLIC_ID to publicId)
            val request = OneTimeWorkRequestBuilder<DeleteAudioWorker>()
                .setInputData(data)
                .setInitialDelay(24, TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(context).enqueue(request)
            Log.d("DeleteAudioWorker", "Scheduled 24-hr delete for publicId=$publicId")
        }
    }
}
