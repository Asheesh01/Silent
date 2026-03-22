package com.example.voiceresponder.ui

import android.content.ContentResolver
import android.provider.ContactsContract
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.room.Room
import com.example.voiceresponder.data.AppDatabase
import com.example.voiceresponder.data.ContactEntity
import com.example.voiceresponder.data.normalizePhone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DeviceContact(val name: String, val phone: String)

/** Load all contacts from the device's address book */
fun loadDeviceContacts(contentResolver: ContentResolver): List<DeviceContact> {
    val contacts = mutableListOf<DeviceContact>()
    val cursor = contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        ),
        null, null,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
    ) ?: return contacts

    cursor.use {
        val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val phoneIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        while (it.moveToNext()) {
            val name = it.getString(nameIdx) ?: continue
            val phone = it.getString(phoneIdx) ?: continue
            contacts.add(DeviceContact(name, phone))
        }
    }
    return contacts
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactListScreen(navController: NavController) {
    val context = LocalContext.current
    val database = remember {
        Room.databaseBuilder(context, AppDatabase::class.java, "responder-db").build()
    }
    val contactDao = database.contactDao()
    val scope = rememberCoroutineScope()

    var deviceContacts by remember { mutableStateOf(listOf<DeviceContact>()) }
    var selectedNumbers by remember { mutableStateOf(setOf<String>()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // Load device contacts and already-selected numbers
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val contacts = loadDeviceContacts(context.contentResolver)
            val saved = contactDao.getAllContacts().map { it.phoneNumber }.toSet()
            withContext(Dispatchers.Main) {
                deviceContacts = contacts
                selectedNumbers = saved
                isLoading = false
            }
        }
    }

    val filtered = deviceContacts.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.phone.contains(searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Select Contacts") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search contacts") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                singleLine = true
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (deviceContacts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No contacts found.\nGrant Contacts permission in Settings.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            } else {
                LazyColumn {
                    items(filtered) { contact ->
                        val normalized = normalizePhone(contact.phone)
                        val isSelected = selectedNumbers.contains(normalized)
                        ContactRow(
                            name = contact.name,
                            phone = contact.phone,
                            isSelected = isSelected,
                            onClick = {
                                scope.launch {
                                    if (isSelected) {
                                        contactDao.removeContact(ContactEntity(normalized))
                                        selectedNumbers = selectedNumbers - normalized
                                    } else {
                                        contactDao.addContact(ContactEntity(normalized))
                                        selectedNumbers = selectedNumbers + normalized
                                    }
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun ContactRow(name: String, phone: String, isSelected: Boolean, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = { Text(name) },
        supportingContent = { Text(phone, style = MaterialTheme.typography.bodySmall) },
        leadingContent = {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}
