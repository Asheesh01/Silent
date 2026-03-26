package com.example.voiceresponder.data

import androidx.room.*

// ── Contact ───────────────────────────────────────────────────────────────────

@Entity(tableName = "selected_contacts")
data class ContactEntity(
    @PrimaryKey val phoneNumber: String
)

@Dao
interface ContactDao {
    @Query("SELECT * FROM selected_contacts")
    suspend fun getAllContacts(): List<ContactEntity>

    @Query("SELECT EXISTS(SELECT * FROM selected_contacts WHERE phoneNumber = :number)")
    suspend fun isContactSelected(number: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addContact(contact: ContactEntity)

    @Delete
    suspend fun removeContact(contact: ContactEntity)
}

// ── Feedback ──────────────────────────────────────────────────────────────────

@Entity(tableName = "feedback")
data class FeedbackEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rating: Int,               // 1–5 stars
    val easeOfUse: String,         // "Easy" | "Moderate" | "Difficult"
    val smsWorking: String,        // "Yes" | "Mostly" | "No"
    val mostUsefulFeature: String, // free chip selection
    val suggestions: String,       // open text
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface FeedbackDao {
    @Insert
    suspend fun insertFeedback(feedback: FeedbackEntity)

    @Query("SELECT * FROM feedback ORDER BY timestamp DESC")
    suspend fun getAllFeedback(): List<FeedbackEntity>
}

// ── Helpers ───────────────────────────────────────────────────────────────────

/** Strip country code and keep only the last 10 digits for consistent matching */
fun normalizePhone(number: String): String {
    val digits = number.filter { it.isDigit() }
    return if (digits.length > 10) digits.takeLast(10) else digits
}

// ── Database ──────────────────────────────────────────────────────────────────

@Database(entities = [ContactEntity::class, FeedbackEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun feedbackDao(): FeedbackDao
}
