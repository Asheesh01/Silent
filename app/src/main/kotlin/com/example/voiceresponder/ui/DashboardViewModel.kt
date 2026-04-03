package com.example.voiceresponder.ui

import android.app.Application
import android.content.ContentResolver
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.voiceresponder.data.AppDatabase
import com.example.voiceresponder.data.ContactEntity
import com.example.voiceresponder.remote.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Survives navigation between Dashboard ↔ Contacts ↔ Record ↔ Settings.
 * Data is loaded ONCE after permissions are granted and cached here so that
 * navigating back to the Dashboard never triggers a Firestore reload.
 */
class DashboardViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getOrCreate(app)
    val contactDao = db.contactDao()
    private val fbHelper = FirebaseHelper()
    val uid: String? = FirebaseAuth.getInstance().currentUser?.uid

    // ── Cached state ──────────────────────────────────────────────────────────
    var isActive         by mutableStateOf(false)
    var selectedContacts by mutableStateOf(setOf<String>())
    var deviceContacts   by mutableStateOf(listOf<DeviceContact>())
    var fileExists       by mutableStateOf(false)

    /** True after the first successful load — prevents re-fetching on recomposition. */
    var dataLoaded       by mutableStateOf(false)
        private set

    // ── Load (called once after permissions granted) ───────────────────────────
    fun loadData(contentResolver: ContentResolver, audioFile: File) {
        if (dataLoaded) {
            // Still refresh fileExists in case user just recorded something
            fileExists = audioFile.exists()
            return
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val contacts    = loadDeviceContacts(contentResolver)
                val savedActive = uid?.let { fbHelper.loadResponderState(it) } ?: false
                val cloudContacts: Set<String> = if (uid != null) {
                    val fromCloud = fbHelper.loadContacts(uid)
                    contactDao.clearAll()
                    fromCloud.forEach { contactDao.insertContact(ContactEntity(it)) }
                    fromCloud.toSet()
                } else {
                    contactDao.getAllContacts().map { it.phoneNumber }.toSet()
                }
                withContext(Dispatchers.Main) {
                    deviceContacts   = contacts
                    selectedContacts = cloudContacts
                    isActive         = savedActive
                    fileExists       = audioFile.exists()
                    dataLoaded       = true
                }
            }
        }
    }

    /** Called when the user deletes a contact from the Dashboard card. */
    fun removeContact(normalized: String) {
        selectedContacts = selectedContacts - normalized
    }

    /** Refresh contacts from Room after returning from the Contacts screen. */
    fun refreshContactsFromRoom() {
        viewModelScope.launch {
            val saved = withContext(Dispatchers.IO) {
                contactDao.getAllContacts().map { it.phoneNumber }.toSet()
            }
            selectedContacts = saved
        }
    }
}
