package com.example.voiceresponder.data

import androidx.room.*

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

@Database(entities = [ContactEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
}
