package com.example.voiceresponder.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.voiceresponder.data.FeedbackEntity
import com.example.voiceresponder.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

// ── How-To step data ──────────────────────────────────────────────────────────

private data class HowToStep(
    val number: String,
    val title: String,
    val description: String
)

private val howToSteps = listOf(
    HowToStep("1", "Record Your Voice Response",
        "Go to the Record tab and tap the microphone. Speak your message, then tap Stop when done."),
    HowToStep("2", "Select Contacts",
        "Open the Contacts tab and choose the people you want auto-replies sent to when they call and you miss it."),
    HowToStep("3", "Enable Auto Responder",
        "On the Home screen, flip the Auto Responder toggle to ON. The service runs silently in the background."),
    HowToStep("4", "Miss a Call \u2192 SMS is Sent",
        "When a selected contact calls and you can't answer, the app automatically sends them an SMS with your voice message link."),
    HowToStep("5", "Manage Anytime",
        "You can re-record your message, add or remove contacts, or turn the responder off at any time from the Home screen."),
)

// ── Feature chip options ──────────────────────────────────────────────────────

private val featureOptions   = listOf("Auto SMS", "Voice Recording", "Contact Groups", "Notifications")
private val easeOptions      = listOf("Easy", "Moderate", "Difficult")
private val smsOptions       = listOf("Yes", "Mostly", "No")

// ── Main Screen ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    // Preferences state
    var smsEnabled  by remember { mutableStateOf(true) }
    var pushEnabled by remember { mutableStateOf(true) }

    // Section expand state
    var howToExpanded      by remember { mutableStateOf(false) }
    var feedbackExpanded   by remember { mutableStateOf(false) }

    // Feedback form state
    var starRating         by remember { mutableIntStateOf(0) }
    var easeOfUse          by remember { mutableStateOf("") }
    var smsWorking         by remember { mutableStateOf("") }
    var mostUsefulFeature  by remember { mutableStateOf("") }
    var suggestions        by remember { mutableStateOf("") }

    // Firebase user info
    val firebaseUser = remember { FirebaseAuth.getInstance().currentUser }
    val userEmail    = firebaseUser?.email ?: "—"
    val userPhone    = firebaseUser?.phoneNumber ?: ""

    // Load phone from Firestore if not in Auth (email-auth users store it there)
    var profilePhone by remember { mutableStateOf(userPhone) }
    LaunchedEffect(firebaseUser?.uid) {
        if (profilePhone.isBlank() && firebaseUser?.uid != null) {
            try {
                val doc = FirebaseFirestore.getInstance()
                    .collection("users").document(firebaseUser.uid).get().await()
                profilePhone = (doc.getString("phoneNumber") ?: doc.getString("phone") ?: "").ifBlank { "—" }
            } catch (_: Exception) { profilePhone = "—" }
        } else if (profilePhone.isBlank()) {
            profilePhone = "—"
        }
    }

    // Room DB
    val db = remember {
        Room.databaseBuilder(context, AppDatabase::class.java, "responder-db")
            .fallbackToDestructiveMigration()
            .build()
    }

    val bgGradient = Brush.verticalGradient(listOf(Color(0xFFF0F6FF), Color(0xFFEBF4FF), Color(0xFFF5F0FF)))

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, color = OnDarkText) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = OnDarkText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCard)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
                .drawEdgeGlows()
        ) {
            LazyColumn(
                modifier             = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement  = Arrangement.spacedBy(16.dp),
                contentPadding       = PaddingValues(vertical = 16.dp)
            ) {

                // ── Profile Card ──────────────────────────────────────────────
                item {
                    Card(
                        shape  = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar circle with initials
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        androidx.compose.ui.graphics.Brush.linearGradient(
                                            listOf(Teal400, Color(0xFF7C4DFF))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text       = userEmail.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                                    fontSize   = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = Color.White
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text       = userEmail,
                                    fontSize   = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = OnDarkText
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = Teal400, modifier = Modifier.size(13.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(profilePhone, fontSize = 12.sp, color = SubText)
                                }
                            }
                        }
                    }
                }

                // ── Permissions & Features ────────────────────────────────────
                item {
                    SectionLabel("Permissions & Features")
                }
                item {
                    Card(
                        shape  = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard)
                    ) {
                        Column {
                            ToggleSetting(
                                title    = "SMS Auto-Responder",
                                subtitle = "Send voice link via SMS to non-app callers",
                                checked  = smsEnabled,
                                onChange = { smsEnabled = it },
                                icon     = Icons.Default.Sms
                            )
                            HorizontalDivider(color = Color(0xFFDDE6EF), thickness = 0.5.dp)
                            ToggleSetting(
                                title    = "Push Notifications",
                                subtitle = "Notify app users instantly via FCM",
                                checked  = pushEnabled,
                                onChange = { pushEnabled = it },
                                icon     = Icons.Default.Notifications
                            )
                        }
                    }
                }

                // ── How to Use ────────────────────────────────────────────────
                item {
                    SectionLabel("Guide")
                }
                item {
                    ExpandableCard(
                        title      = "How to Use the App",
                        subtitle   = "Step-by-step setup guide",
                        icon       = Icons.Default.MenuBook,
                        expanded   = howToExpanded,
                        onToggle   = { howToExpanded = !howToExpanded }
                    ) {
                        Column(
                            modifier            = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            howToSteps.forEach { step ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Teal400.copy(alpha = 0.18f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            step.number,
                                            color      = Teal400,
                                            fontWeight = FontWeight.Bold,
                                            fontSize   = 13.sp
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            step.title,
                                            color      = OnDarkText,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize   = 14.sp
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            step.description,
                                            color    = SubText,
                                            fontSize = 12.sp,
                                            lineHeight = 17.sp
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }

                // ── Feedback ──────────────────────────────────────────────────
                item {
                    SectionLabel("Feedback")
                }
                item {
                    ExpandableCard(
                        title    = "Share Your Feedback",
                        subtitle = "Help us improve Zyntra",
                        icon     = Icons.Default.Feedback,
                        expanded = feedbackExpanded,
                        onToggle = { feedbackExpanded = !feedbackExpanded }
                    ) {
                        Column(
                            modifier            = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // ── 1. Star rating ────────────────────────────────
                            FeedbackSection("Overall Rating") {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    (1..5).forEach { star ->
                                        Icon(
                                            imageVector        = if (star <= starRating) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = "Star $star",
                                            tint               = if (star <= starRating) Color(0xFFFFD700) else SubText,
                                            modifier           = Modifier
                                                .size(34.dp)
                                                .clickable { starRating = star }
                                        )
                                    }
                                }
                            }

                            // ── 2. Ease of setup ──────────────────────────────
                            FeedbackSection("How easy was the app to set up?") {
                                ChipRow(
                                    options   = easeOptions,
                                    selected  = easeOfUse,
                                    onSelect  = { easeOfUse = it }
                                )
                            }

                            // ── 3. SMS working ────────────────────────────────
                            FeedbackSection("Are the SMS auto-replies working for you?") {
                                ChipRow(
                                    options  = smsOptions,
                                    selected = smsWorking,
                                    onSelect = { smsWorking = it }
                                )
                            }

                            // ── 4. Most useful feature ────────────────────────
                            FeedbackSection("What feature do you use the most?") {
                                ChipRow(
                                    options  = featureOptions,
                                    selected = mostUsefulFeature,
                                    onSelect = { mostUsefulFeature = it }
                                )
                            }

                            // ── 5. Suggestions ────────────────────────────────
                            FeedbackSection("Any suggestions for improvement?") {
                                OutlinedTextField(
                                    value           = suggestions,
                                    onValueChange   = { suggestions = it },
                                    placeholder     = { Text("Type here…", color = SubText) },
                                    modifier        = Modifier.fillMaxWidth(),
                                    minLines        = 3,
                                    maxLines        = 5,
                                    shape           = RoundedCornerShape(12.dp),
                                    colors          = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor   = Teal400,
                                        unfocusedBorderColor = Color(0xFFBBCCDD),
                                        focusedTextColor     = OnDarkText,
                                        unfocusedTextColor   = OnDarkText,
                                        cursorColor          = Teal400
                                    )
                                )
                            }

                            // ── Submit button ─────────────────────────────────
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .background(
                                        Brush.horizontalGradient(listOf(Teal400, Color(0xFF006064))),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Button(
                                    onClick = {
                                        if (starRating == 0) {
                                            Toast.makeText(context, "Please select a star rating", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        scope.launch {
                                            val uid = FirebaseAuth.getInstance().currentUser?.uid
                                            val feedbackMap = mapOf(
                                                "rating"            to starRating,
                                                "easeOfUse"         to easeOfUse.ifBlank { "Not answered" },
                                                "smsWorking"        to smsWorking.ifBlank { "Not answered" },
                                                "mostUsefulFeature" to mostUsefulFeature.ifBlank { "Not answered" },
                                                "suggestions"       to suggestions.ifBlank { "—" },
                                                "timestamp"         to System.currentTimeMillis()
                                            )
                                            withContext(Dispatchers.IO) {
                                                // Save to local Room DB
                                                db.feedbackDao().insertFeedback(
                                                    FeedbackEntity(
                                                        rating              = starRating,
                                                        easeOfUse           = easeOfUse.ifBlank { "Not answered" },
                                                        smsWorking          = smsWorking.ifBlank { "Not answered" },
                                                        mostUsefulFeature   = mostUsefulFeature.ifBlank { "Not answered" },
                                                        suggestions         = suggestions.ifBlank { "—" }
                                                    )
                                                )
                                            }
                                                                                                                                                                                // Save to Firestore root-level /feedback/ collection
                                            val firestoreMap = feedbackMap + mapOf("uid" to (uid ?: "anonymous"))
                                            try {
                                                val ref = FirebaseFirestore.getInstance()
                                                    .collection("feedback")
                                                    .add(firestoreMap)
                                                    .await()
                                                android.util.Log.d("Feedback", "Saved OK id=${ref.id}")
                                            } catch (e: Exception) {
                                                android.util.Log.e("Feedback", "FAILED: ${e.message}")
                                                Toast.makeText(context, "Feedback error: ${e.message}", Toast.LENGTH_LONG).show()
                                            }
                                            // Reset form
                                            starRating        = 0
                                            easeOfUse         = ""
                                            smsWorking        = ""
                                            mostUsefulFeature = ""
                                            suggestions       = ""
                                            feedbackExpanded  = false
                                            Toast.makeText(context, "Thank you for your feedback! 🎉", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    modifier  = Modifier.fillMaxSize(),
                                    colors    = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    elevation = ButtonDefaults.buttonElevation(0.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Submit Feedback", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }

                // ── App Info ──────────────────────────────────────────────────
                item {
                    SectionLabel("App Info")
                }
                item {
                    Card(
                        shape  = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard)
                    ) {
                        ListItem(
                            headlineContent   = { Text("Version", color = OnDarkText) },
                            supportingContent = { Text("1.0.0", color = SubText) },
                            leadingContent    = { Icon(Icons.Default.Info, contentDescription = null, tint = Teal400) },
                            colors            = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }

                // ── Logout ────────────────────────────────────────────────────
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFFCF6679), Color(0xFF880000))),
                                shape = RoundedCornerShape(14.dp)
                            )
                    ) {
                        Button(
                            onClick   = {
                                FirebaseAuth.getInstance().signOut()
                                // Also clear Google session so account picker shows next login
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                                GoogleSignIn.getClient(context, gso).signOut()
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            modifier  = Modifier.fillMaxSize(),
                            colors    = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Logout", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ── Reusable composables ──────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style      = MaterialTheme.typography.titleSmall,
        color      = SubText,
        fontSize   = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier   = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun FeedbackSection(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = OnDarkText, fontWeight = FontWeight.Medium, fontSize = 13.sp)
        content()
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun ChipRow(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement   = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) Teal400.copy(alpha = 0.15f) else Color(0xFFEEF4FA))
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Teal400 else Color(0xFFBBCCDD),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelect(option) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    option,
                    color      = if (isSelected) Teal400 else SubText,
                    fontSize   = 13.sp,
                    softWrap   = false,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun ExpandableCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column {
            // Header row
            ListItem(
                headlineContent   = { Text(title, color = OnDarkText, fontWeight = FontWeight.Medium) },
                supportingContent = { Text(subtitle, color = SubText, fontSize = 12.sp) },
                leadingContent    = { Icon(icon, contentDescription = null, tint = Teal400) },
                trailingContent   = {
                    Icon(
                        imageVector        = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint               = SubText
                    )
                },
                colors   = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.clickable { onToggle() }
            )
            // Animated collapsible body
            AnimatedVisibility(
                visible  = expanded,
                enter    = expandVertically(tween(220)) + fadeIn(tween(220)),
                exit     = shrinkVertically(tween(180)) + fadeOut(tween(180))
            ) {
                Column {
                    HorizontalDivider(color = Color(0xFFDDE6EF), thickness = 0.5.dp)
                    content()
                }
            }
        }
    }
}

// ── ToggleSetting (kept for Permissions card) ─────────────────────────────────

@Composable
fun ToggleSetting(
    title: String,
    subtitle: String = "",
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    ListItem(
        headlineContent   = { Text(title, color = OnDarkText, fontWeight = FontWeight.Medium) },
        supportingContent = if (subtitle.isNotBlank()) ({ Text(subtitle, color = SubText, fontSize = 12.sp) }) else null,
        leadingContent    = { Icon(icon, contentDescription = null, tint = Teal400) },
        trailingContent   = {
            Switch(
                checked         = checked,
                onCheckedChange = onChange,
                colors          = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Teal400
                )
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}
