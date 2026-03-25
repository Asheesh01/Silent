package com.example.voiceresponder.ui

import android.Manifest
import android.content.ContentResolver
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.room.Room
import com.example.voiceresponder.data.AppDatabase
import com.example.voiceresponder.data.ContactEntity
import com.example.voiceresponder.data.normalizePhone
import com.example.voiceresponder.remote.FirebaseHelper
import com.example.voiceresponder.remote.SyncHelper
import com.example.voiceresponder.service.ResponderService
import com.example.voiceresponder.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun DashboardScreen(navController: NavController) {
    val context    = LocalContext.current
    val scope      = rememberCoroutineScope()
    val fbHelper   = remember { FirebaseHelper() }
    val uid        = remember { FirebaseAuth.getInstance().currentUser?.uid }

    val database = remember {
        Room.databaseBuilder(context, AppDatabase::class.java, "responder-db").build()
    }
    val contactDao = database.contactDao()

    var isActive         by remember { mutableStateOf(false) }
    var selectedContacts by remember { mutableStateOf(setOf<String>()) }
    var selectedTab      by remember { mutableIntStateOf(0) }
    var deviceContacts   by remember { mutableStateOf(listOf<DeviceContact>()) }

    // Audio state
    val audioFile   = remember { File(context.filesDir, "default_response.mp4") }
    var fileExists  by remember { mutableStateOf(audioFile.exists()) }
    var isPlaying   by remember { mutableStateOf(false) }
    var showDelDlg  by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer?.release() }
    }

    // ── Request permissions once, after the user has logged in ───────────
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    LaunchedEffect(Unit) {
        val perms = mutableListOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permLauncher.launch(perms.toTypedArray())

        withContext(Dispatchers.IO) {
            val contacts = loadDeviceContacts(context.contentResolver)
            val saved    = contactDao.getAllContacts().map { it.phoneNumber }.toSet()
            // Load responder state from Firestore so it syncs across devices
            val savedActive = uid?.let { fbHelper.loadResponderState(it) } ?: false
            withContext(Dispatchers.Main) {
                deviceContacts  = contacts
                selectedContacts = saved
                isActive = savedActive
                if (savedActive) {
                    val intent = Intent(context, ResponderService::class.java).apply {
                        action = "ACTION_START_MONITORING"
                    }
                    context.startForegroundService(intent)
                }
            }
        }
    }

    // ── Delete recording dialog ────────────────────────────────────────────
    if (showDelDlg) {
        AlertDialog(
            onDismissRequest   = { showDelDlg = false },
            containerColor     = DarkCard,
            title              = { Text("Delete Recording?", color = OnDarkText) },
            text               = { Text("This will permanently delete your voice response.", color = SubText) },
            confirmButton = {
                TextButton(onClick = {
                    mediaPlayer?.release(); mediaPlayer = null; isPlaying = false
                    audioFile.delete(); fileExists = false; showDelDlg = false
                    Toast.makeText(context, "Recording deleted", Toast.LENGTH_SHORT).show()
                }) { Text("Delete", color = ErrorRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDelDlg = false }) { Text("Cancel", color = SubText) }
            }
        )
    }

    val bgGradient = Brush.verticalGradient(listOf(DarkBg, DarkSurface))

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            SilentBottomBar(
                selectedTab   = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                    when (tab) {
                        1 -> navController.navigate("contacts")
                        2 -> navController.navigate("record")
                        3 -> navController.navigate("settings")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(Modifier.height(20.dp)) }

                // ── Logo + title ──────────────────────────────────────────
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Teal400, Color(0xFF7C4DFF)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("Silent Mode", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = OnDarkText)
                        Text("Smart Auto Responder", fontSize = 13.sp, color = SubText)
                    }
                }

                // ── Auto Responder toggle card ────────────────────────────
                item {
                    Card(
                        shape  = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier             = Modifier.fillMaxWidth().padding(20.dp),
                            verticalAlignment    = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Auto Responder", fontWeight = FontWeight.SemiBold, color = OnDarkText, fontSize = 16.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    if (isActive) "Currently Active" else "Currently Inactive",
                                    color = if (isActive) SelectedGreen else SubText, fontSize = 13.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = SubText, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("${selectedContacts.size} contact${if (selectedContacts.size == 1) "" else "s"} assigned", color = SubText, fontSize = 12.sp)
                                }
                            }
                            Switch(
                                checked = isActive,
                                onCheckedChange = { checked ->
                                    isActive = checked
                                    val intent = Intent(context, ResponderService::class.java).apply {
                                        action = "ACTION_START_MONITORING"
                                    }
                                    if (checked) {
                                        context.startForegroundService(intent)
                                    } else {
                                        context.stopService(intent)
                                    }
                                    // Save to Firestore for cross-device sync
                                    scope.launch {
                                        uid?.let { fbHelper.saveResponderState(it, checked) }
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Teal400
                                )
                            )
                        }
                    }
                }

                // ── Selected Contacts list ───────────────────────────────
                item {
                    Row(
                        modifier             = Modifier.fillMaxWidth(),
                        verticalAlignment    = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Selected Contacts", fontWeight = FontWeight.SemiBold, color = OnDarkText, fontSize = 15.sp)
                        TextButton(onClick = { navController.navigate("contacts") }) {
                            Text("Manage", color = Teal400, fontSize = 13.sp)
                        }
                    }
                }

                if (selectedContacts.isEmpty()) {
                    item {
                        Card(
                            shape  = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCard),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier          = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = SubText, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Text("No contacts selected yet. Tap Manage.", color = SubText, fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    val phoneToName = deviceContacts.associate { normalizePhone(it.phone) to it.name }
                    items(selectedContacts.toList()) { normalized ->
                        val name    = phoneToName[normalized] ?: normalized
                        val display = deviceContacts.firstOrNull { normalizePhone(it.phone) == normalized }?.phone ?: normalized
                        Card(
                            shape  = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2E42)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier             = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment    = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Teal400.copy(alpha = 0.20f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                            color = Teal400, fontWeight = FontWeight.Bold, fontSize = 16.sp
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(name,    color = OnDarkText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Text(display, color = SubText,    fontSize = 12.sp)
                                    }
                                }
                                // Delete button
                                val syncHelper = remember { SyncHelper() }
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            withContext(Dispatchers.IO) { contactDao.removeContact(ContactEntity(normalized)) }
                                            val updated = selectedContacts - normalized
                                            selectedContacts = updated
                                            uid?.let { syncHelper.pushContactsToCloud(it, updated) }
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        tint     = Color(0xFFEF5350),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Voice Recording card ──────────────────────────────────
                item {
                    Row(
                        modifier             = Modifier.fillMaxWidth(),
                        verticalAlignment    = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Voice Response", fontWeight = FontWeight.SemiBold, color = OnDarkText, fontSize = 15.sp)
                        TextButton(onClick = { navController.navigate("record") }) {
                            Text(if (fileExists) "Re-record" else "Record Now", color = Teal400, fontSize = 13.sp)
                        }
                    }
                }

                item {
                    Card(
                        shape  = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (!fileExists) {
                            Row(
                                modifier          = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.MicNone, contentDescription = null, tint = SubText, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Text("No recording yet. Tap Record Now.", color = SubText, fontSize = 13.sp)
                            }
                        } else {
                            val fileSizeKb = (audioFile.length() / 1024).toInt()
                            Row(
                                modifier             = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment    = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Teal400.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Audiotrack, contentDescription = null, tint = Teal400, modifier = Modifier.size(22.dp))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text("Voice Response", color = OnDarkText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Text("${fileSizeKb} KB • default_response.mp4", color = SubText, fontSize = 12.sp)
                                    }
                                }
                                Row {
                                    // Play / Stop
                                    IconButton(onClick = {
                                        if (isPlaying) {
                                            mediaPlayer?.stop(); mediaPlayer?.release(); mediaPlayer = null; isPlaying = false
                                        } else {
                                            try {
                                                mediaPlayer = MediaPlayer().apply {
                                                    setDataSource(audioFile.absolutePath); prepare(); start()
                                                    isPlaying = true
                                                    setOnCompletionListener { isPlaying = false; mediaPlayer = null }
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error playing audio", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }) {
                                        Icon(
                                            if (isPlaying) Icons.Default.Stop else Icons.Default.PlayCircle,
                                            contentDescription = null, tint = Teal400, modifier = Modifier.size(28.dp)
                                        )
                                    }
                                     // Delete
                                     IconButton(
                                         onClick  = { showDelDlg = true },
                                         modifier = Modifier.size(48.dp)
                                     ) {
                                         Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(32.dp))
                                     }
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun SilentBottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val items = listOf(
        Icons.Default.Home     to "Home",
        Icons.Default.People   to "Contacts",
        Icons.Default.Mic      to "Record",
        Icons.Default.Settings to "Settings"
    )
    NavigationBar(containerColor = DarkCard, tonalElevation = 0.dp) {
        items.forEachIndexed { index, (icon, label) ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick  = { onTabSelected(index) },
                icon     = { Icon(icon, contentDescription = label) },
                label    = { Text(label, fontSize = 11.sp) },
                colors   = NavigationBarItemDefaults.colors(
                    selectedIconColor   = Teal400,
                    selectedTextColor   = Teal400,
                    unselectedIconColor = SubText,
                    unselectedTextColor = SubText,
                    indicatorColor      = Teal400.copy(alpha = 0.15f)
                )
            )
        }
    }
}
