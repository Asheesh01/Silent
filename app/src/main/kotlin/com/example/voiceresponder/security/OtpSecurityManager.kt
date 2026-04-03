package com.example.voiceresponder.security

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Manages OTP security in-memory.
 *
 * Security properties:
 *  - OTP generated with SecureRandom (CSPRNG), not Math.random
 *  - OTP stored ONLY as SHA-256 hash — plain-text OTP is never retained
 *  - 5-minute expiry enforced on every verification attempt
 *  - Max 3 wrong attempts before lock-out (brute-force protection)
 *  - 60-second cooldown between SMS sends (spam protection)
 *  - Max 3 total SMS sends per session (abuse protection)
 */
object OtpSecurityManager {

    // ── Tuneable constants ────────────────────────────────────────────────────
    private const val OTP_EXPIRY_MS       = 5 * 60 * 1_000L   // 5 minutes
    private const val MAX_VERIFY_ATTEMPTS = 3
    private const val RESEND_COOLDOWN_MS  = 60 * 1_000L        // 60 seconds
    private const val MAX_RESENDS         = 3

    // ── Internal session ──────────────────────────────────────────────────────
    private data class OtpSession(
        val hashedOtp: String,
        val generatedAtMs: Long,
        val lastSentAtMs: Long,
        val resendCount: Int,
        var attemptsRemaining: Int = MAX_VERIFY_ATTEMPTS
    )

    @Volatile private var session: OtpSession? = null

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Generate a cryptographically secure 6-digit OTP.
     * Returns the PLAIN-TEXT string — caller must pass it to [storeAndHash]
     * and then immediately send it; do NOT store or log it.
     */
    fun generateOtp(): String {
        val rng = SecureRandom()
        // Ensure first digit is 1-9 (no leading zero) for a stronger-looking code
        val first = rng.nextInt(9) + 1
        val rest  = (1..5).map { rng.nextInt(10) }.joinToString("")
        return "$first$rest"
    }

    /**
     * Hash [plainOtp] with SHA-256 and store it in the session.
     *
     * Returns [StoreResult.OK] on success.
     * Returns [StoreResult.COOLDOWN] if the previous SMS was sent < 60 s ago.
     * Returns [StoreResult.MAX_RESENDS_REACHED] if 3 SMS already sent this session.
     *
     * IMPORTANT: the plain OTP must be sent to the user immediately after this
     * call and then discarded — it is no longer accessible here.
     */
    fun storeAndHash(plainOtp: String): StoreResult {
        val now = System.currentTimeMillis()
        val s   = session

        // Rate-limit: cooldown between resends
        if (s != null && (now - s.lastSentAtMs) < RESEND_COOLDOWN_MS) {
            return StoreResult.COOLDOWN
        }

        // Rate-limit: absolute max resends
        if (s != null && s.resendCount >= MAX_RESENDS) {
            return StoreResult.MAX_RESENDS_REACHED
        }

        session = OtpSession(
            hashedOtp      = sha256(plainOtp),
            generatedAtMs  = now,
            lastSentAtMs   = now,
            resendCount    = (s?.resendCount ?: 0) + 1
        )
        return StoreResult.OK
    }

    /**
     * Verify a user-entered [inputOtp] against the stored hash.
     *
     * Checks expiry, attempt count, and constant-time hash equality.
     * Clears the session on [VerifyResult.SUCCESS].
     */
    fun verify(inputOtp: String): VerifyResult {
        val s = session ?: return VerifyResult.NO_SESSION

        // Expiry check
        if (System.currentTimeMillis() - s.generatedAtMs > OTP_EXPIRY_MS) {
            session = null
            return VerifyResult.EXPIRED
        }

        // Lock-out check
        if (s.attemptsRemaining <= 0) {
            return VerifyResult.LOCKED
        }

        // Constant-time comparison via hashing the input too
        return if (sha256(inputOtp) == s.hashedOtp) {
            session = null           // clear on success
            VerifyResult.SUCCESS
        } else {
            s.attemptsRemaining--
            if (s.attemptsRemaining <= 0) VerifyResult.LOCKED
            else VerifyResult.WRONG_OTP
        }
    }

    /** Seconds remaining before a resend is allowed (0 = can resend now). */
    fun resendCooldownSeconds(): Int {
        val lastSent = session?.lastSentAtMs ?: return 0
        val remaining = RESEND_COOLDOWN_MS - (System.currentTimeMillis() - lastSent)
        return if (remaining <= 0) 0 else (remaining / 1_000).toInt()
    }

    fun attemptsRemaining(): Int = session?.attemptsRemaining ?: MAX_VERIFY_ATTEMPTS
    fun resendCount()       : Int = session?.resendCount ?: 0
    fun maxResends()        : Int = MAX_RESENDS
    fun isLocked()          : Boolean = (session?.attemptsRemaining ?: 1) <= 0

    /** Force-clear the current session (e.g. on cancel or back navigation). */
    fun clearSession() { session = null }

    // ── Internals ─────────────────────────────────────────────────────────────

    /** SHA-256 of [input] as a lowercase hex string. */
    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    // ── Result types ──────────────────────────────────────────────────────────

    enum class StoreResult {
        OK,
        COOLDOWN,
        MAX_RESENDS_REACHED
    }

    enum class VerifyResult {
        SUCCESS,
        WRONG_OTP,
        EXPIRED,
        LOCKED,
        NO_SESSION
    }
}
