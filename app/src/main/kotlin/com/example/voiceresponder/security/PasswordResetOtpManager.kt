package com.example.voiceresponder.security

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * In-memory OTP session for the Forgot Password flow.
 * Completely isolated from the signup OtpSecurityManager.
 *
 * Security properties:
 *  - CSPRNG-generated 6-digit code
 *  - Stored as SHA-256 hash only
 *  - 10-minute expiry
 *  - Max 3 wrong attempts (lock-out)
 *  - 60-second cooldown between resends
 *  - Max 3 total sends per session
 */
object PasswordResetOtpManager {

    private const val OTP_EXPIRY_MS      = 10 * 60 * 1_000L   // 10 minutes
    private const val MAX_ATTEMPTS       = 3
    private const val RESEND_COOLDOWN_MS = 60 * 1_000L
    private const val MAX_RESENDS        = 3

    private data class Session(
        val hashedOtp     : String,
        val generatedAt   : Long,
        val lastSentAt    : Long,
        val resendCount   : Int,
        var attemptsLeft  : Int = MAX_ATTEMPTS
    )

    @Volatile private var session: Session? = null

    // ── Public API ─────────────────────────────────────────────────────────────

    fun generateOtp(): String {
        val rng   = SecureRandom()
        val first = rng.nextInt(9) + 1
        val rest  = (1..5).map { rng.nextInt(10) }.joinToString("")
        return "$first$rest"
    }

    enum class StoreResult { OK, COOLDOWN, MAX_RESENDS_REACHED }

    fun storeAndHash(plainOtp: String): StoreResult {
        val now = System.currentTimeMillis()
        val s   = session

        if (s != null && (now - s.lastSentAt) < RESEND_COOLDOWN_MS) return StoreResult.COOLDOWN
        if (s != null && s.resendCount       >= MAX_RESENDS)         return StoreResult.MAX_RESENDS_REACHED

        session = Session(
            hashedOtp   = sha256(plainOtp),
            generatedAt = now,
            lastSentAt  = now,
            resendCount = (s?.resendCount ?: 0) + 1
        )
        return StoreResult.OK
    }

    enum class VerifyResult { SUCCESS, WRONG_OTP, EXPIRED, LOCKED, NO_SESSION }

    fun verify(input: String): VerifyResult {
        val s = session ?: return VerifyResult.NO_SESSION
        if (System.currentTimeMillis() - s.generatedAt > OTP_EXPIRY_MS) {
            session = null; return VerifyResult.EXPIRED
        }
        if (s.attemptsLeft <= 0) return VerifyResult.LOCKED
        return if (sha256(input) == s.hashedOtp) {
            session = null; VerifyResult.SUCCESS
        } else {
            s.attemptsLeft--
            if (s.attemptsLeft <= 0) VerifyResult.LOCKED else VerifyResult.WRONG_OTP
        }
    }

    fun resendCooldownSeconds(): Int {
        val lastSent = session?.lastSentAt ?: return 0
        val remaining = RESEND_COOLDOWN_MS - (System.currentTimeMillis() - lastSent)
        return if (remaining <= 0) 0 else (remaining / 1_000).toInt()
    }

    fun attemptsLeft(): Int = session?.attemptsLeft ?: MAX_ATTEMPTS
    fun isLocked()    : Boolean = (session?.attemptsLeft ?: 1) <= 0
    fun resendCount() : Int = session?.resendCount ?: 0
    fun maxResends()  : Int = MAX_RESENDS
    fun clearSession()      { session = null }

    private fun sha256(input: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
