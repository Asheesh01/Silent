package com.example.voiceresponder.ui

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.voiceresponder.service.ResponderService

@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            LargeTopAppBar(title = { Text("Digital Assistant") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Service Status", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val intent = Intent(context, ResponderService::class.java).apply {
                                action = "ACTION_START_MONITORING"
                            }
                            context.startForegroundService(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Monitoring")
                    }
                }
            }

            DashboardCard(
                title = "Whitelisted Contacts",
                count = "Manage who gets responses",
                icon = Icons.Default.Person,
                onClick = { navController.navigate("contacts") }
            )
            DashboardCard(
                title = "Voice Response",
                count = "Record/Play your message",
                icon = Icons.Default.Mic,
                onClick = { navController.navigate("record") }
            )
            DashboardCard(
                title = "Service Settings",
                count = "App configuration",
                icon = Icons.Default.Settings,
                onClick = { navController.navigate("settings") }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardCard(title: String, count: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(24.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Text(text = count, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

