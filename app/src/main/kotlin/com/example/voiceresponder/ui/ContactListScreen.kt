package com.example.voiceresponder.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.room.Room
import com.example.voiceresponder.data.AppDatabase
import com.example.voiceresponder.data.ContactEntity
import com.example.voiceresponder.data.normalizePhone
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactListScreen(navController: NavController) {
    val context = navController.context
    val database = remember {
        Room.databaseBuilder(context, AppDatabase::class.java, "responder-db").build()
    }
    val contactDao = database.contactDao()
    val scope = rememberCoroutineScope()
    var contacts by remember { mutableStateOf(listOf<ContactEntity>()) }
    var showDialog by remember { mutableStateOf(false) }
    var newNumber by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        contacts = contactDao.getAllContacts()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Selected Contacts") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Add Contact") },
                    text = {
                        OutlinedTextField(
                            value = newNumber,
                            onValueChange = { newNumber = it },
                            label = { Text("Phone Number") }
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            scope.launch {
                                val normalized = normalizePhone(newNumber)
                                contactDao.addContact(ContactEntity(normalized))
                                contacts = contactDao.getAllContacts()
                                showDialog = false
                                newNumber = ""
                            }
                        }) { Text("Add") }
                    }
                )
            }

            LazyColumn {
                items(contacts) { contact ->
                    ContactItem(contact.phoneNumber) {
                        scope.launch {
                            contactDao.removeContact(contact)
                            contacts = contactDao.getAllContacts()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactItem(number: String, onDelete: () -> Unit) {
    ListItem(
        headlineContent = { Text(number) },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    )
}
