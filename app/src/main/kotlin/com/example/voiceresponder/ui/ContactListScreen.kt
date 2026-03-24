package com.example.voiceresponder.ui

import android.content.ContentResolver
import android.provider.ContactsContract
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.room.Room
import com.example.voiceresponder.data.AppDatabase
import com.example.voiceresponder.data.ContactEntity
import com.example.voiceresponder.data.normalizePhone
import com.example.voiceresponder.remote.SyncHelper
import com.example.voiceresponder.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DeviceContact(val name: String, val phone: String)

fun loadDeviceContacts(contentResolver: ContentResolver): List<DeviceContact> {
    val seen     = mutableSetOf<String>()   // normalized numbers already added
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
        val nameIdx  = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val phoneIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        while (it.moveToNext()) {
            val name  = it.getString(nameIdx)  ?: continue
            val phone = it.getString(phoneIdx) ?: continue
            // Normalize to last 10 digits for deduplication
            val normalized = normalizePhone(phone)
            if (normalized.isNotBlank() && seen.add(normalized)) {
                contacts.add(DeviceContact(name, phone))
            }
        }
    }
    return contacts
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactListScreen(navController: NavController) {
    val context    = LocalContext.current
    val database   = remember {
        Room.databaseBuilder(context, AppDatabase::class.java, "responder-db").build()
    }
    val contactDao = database.contactDao()
    val scope      = rememberCoroutineScope()
    val syncHelper = remember { SyncHelper() }
    val uid        = remember { FirebaseAuth.getInstance().currentUser?.uid }

    var deviceContacts  by remember { mutableStateOf(listOf<DeviceContact>()) }
    var selectedNumbers by remember { mutableStateOf(setOf<String>()) }
    var isLoading       by remember { mutableStateOf(true) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val contacts = loadDeviceContacts(context.contentResolver)
            val saved    = contactDao.getAllContacts().map { it.phoneNumber }.toSet()
            withContext(Dispatchers.Main) {
                deviceContacts  = contacts
                selectedNumbers = saved
                isLoading       = false
            }
        }
    }

    val bgGradient = Brush.verticalGradient(listOf(DarkBg, DarkSurface))

    Scaffold(
        containerColor   = Color.Transparent,
        snackbarHost     = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Select Contacts",
                        fontWeight = FontWeight.Bold,
                        color      = OnDarkText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = OnDarkText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkCard
                )
            )
        },
        floatingActionButton = {
            if (selectedNumbers.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "${selectedNumbers.size} contact${if (selectedNumbers.size == 1) "" else "s"} saved ✓"
                            )
                        }
                    },
                    icon = {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                    },
                    text = {
                        Text(
                            "${selectedNumbers.size} Selected",
                            color      = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    containerColor = Teal400,
                    contentColor   = Color.White
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {


                if (selectedNumbers.isNotEmpty()) {
                    Surface(
                        color = Teal400.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            "${selectedNumbers.size} contact${if (selectedNumbers.size == 1) "" else "s"} will receive your voice response",
                            color    = Teal400,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }

                when {
                    isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Teal400)
                        }
                    }
                    deviceContacts.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "No contacts found.\nGrant Contacts permission in Settings.",
                                color     = SubText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    else -> {
                        LazyColumn {
                            items(deviceContacts) { contact ->
                                val normalized = normalizePhone(contact.phone)
                                val isSelected = selectedNumbers.contains(normalized)
                                ContactRow(
                                    name       = contact.name,
                                    phone      = contact.phone,
                                    isSelected = isSelected,
                                    onClick    = {
                                        scope.launch {
                                            val updatedSet: Set<String>
                                            if (isSelected) {
                                                withContext(Dispatchers.IO) { contactDao.removeContact(ContactEntity(normalized)) }
                                                updatedSet = selectedNumbers - normalized
                                            } else {
                                                withContext(Dispatchers.IO) { contactDao.addContact(ContactEntity(normalized)) }
                                                updatedSet = selectedNumbers + normalized
                                            }
                                            selectedNumbers = updatedSet
                                            // Push updated list to Firestore for cross-device sync
                                            uid?.let { syncHelper.pushContactsToCloud(it, updatedSet) }
                                        }
                                    }
                                )
                                HorizontalDivider(color = Color(0xFF1E1E2E), thickness = 0.5.dp)
                            }
                            item { Spacer(Modifier.height(88.dp)) } // FAB clearance
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactRow(name: String, phone: String, isSelected: Boolean, onClick: () -> Unit) {
    val bg = if (isSelected) Teal400.copy(alpha = 0.08f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar circle
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (isSelected) Teal400 else Color(0xFF2A2A3E)),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Text(
                    name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    color      = OnDarkText,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Text(name,  color = OnDarkText, fontWeight = FontWeight.Medium, fontSize = 15.sp)
            Text(phone, color = SubText,    fontSize   = 12.sp)
        }

        if (isSelected) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint     = Teal400,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
