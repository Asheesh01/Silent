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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Delete
    suspend fun removeContact(contact: ContactEntity)

    @Query("DELETE FROM selected_contacts")
    suspend fun clearAll()
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

// ── Cached Transcription ────────────────────────────────────────────────────────

@Entity(tableName = "cached_response")
data class CachedResponseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val englishText: String,
    val hindiText: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Dao
interface CachedResponseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(response: CachedResponseEntity)

    @Query("SELECT * FROM cached_response ORDER BY cachedAt DESC LIMIT 1")
    suspend fun getLatest(): CachedResponseEntity?

    @Query("DELETE FROM cached_response")
    suspend fun clearAll()
}

// ── Helpers ───────────────────────────────────────────────────────────────────

/** Strip country code and keep only the last 10 digits for consistent matching */
fun normalizePhone(number: String): String {
    val digits = number.filter { it.isDigit() }
    return if (digits.length > 10) digits.takeLast(10) else digits
}

// ── Database ──────────────────────────────────────────────────────────────────

@Database(
    entities = [ContactEntity::class, FeedbackEntity::class, CachedResponseEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun feedbackDao(): FeedbackDao
    abstract fun cachedResponseDao(): CachedResponseDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getOrCreate(context: android.content.Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: androidx.room.Room
                    .databaseBuilder(context.applicationContext, AppDatabase::class.java, "responder-db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
