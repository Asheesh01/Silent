package com.example.voiceresponder.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.voiceresponder.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var smsEnabled  by remember { mutableStateOf(true) }
    var pushEnabled by remember { mutableStateOf(true) }

    val bgGradient = Brush.verticalGradient(listOf(DarkBg, DarkSurface))

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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Permissions & Features",
                    style      = MaterialTheme.typography.titleSmall,
                    color      = SubText,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Card(
                    shape  = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column {
                        ToggleSetting(
                            title     = "SMS Auto-Responder",
                            subtitle  = "Send voice link via SMS to non-app callers",
                            checked   = smsEnabled,
                            onChange  = { smsEnabled = it },
                            icon      = Icons.Default.Sms
                        )
                        HorizontalDivider(color = Color(0xFF1E1E2E), thickness = 0.5.dp)
                        ToggleSetting(
                            title     = "Push Notifications",
                            subtitle  = "Notify app users instantly via FCM",
                            checked   = pushEnabled,
                            onChange  = { pushEnabled = it },
                            icon      = Icons.Default.Notifications
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    "App Info",
                    style      = MaterialTheme.typography.titleSmall,
                    color      = SubText,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

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

                Spacer(modifier = Modifier.weight(1f))

                // Gradient logout button
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
                        onClick = { navController.navigate("login") },
                        modifier = Modifier.fillMaxSize(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
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
