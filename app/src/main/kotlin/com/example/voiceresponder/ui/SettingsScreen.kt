package com.example.voiceresponder.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SettingsScreen(navController: NavController) {
    var smsEnabled by remember { mutableStateOf(true) }
    var pushEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Permissions & Features", style = MaterialTheme.typography.titleMedium)
            
            ToggleSetting("Enable SMS Responder", smsEnabled, { smsEnabled = it }, Icons.Default.List)
            ToggleSetting("Enable Push Notifications", pushEnabled, { pushEnabled = it }, Icons.Default.Notifications)
            
            Divider()
            
            Text("App Info", style = MaterialTheme.typography.titleMedium)
            ListItem(
                headlineContent = { Text("Version") },
                supportingContent = { Text("1.0.0") },
                leadingContent = { Icon(Icons.Default.Info, contentDescription = null) }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { /* Logout */ navController.navigate("login") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Logout")
            }
        }
    }
}

@Composable
fun ToggleSetting(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    )
}
