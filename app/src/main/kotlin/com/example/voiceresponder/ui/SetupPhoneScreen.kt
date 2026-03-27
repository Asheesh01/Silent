package com.example.voiceresponder.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupPhoneScreen(navController: NavController) {
    var phoneNumber by remember { mutableStateOf("+91") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "One Last Step",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Enter your phone number so the app can identify incoming calls and send voice responses.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = {
                // Never allow user to delete the "+91" prefix
                if (it.startsWith("+91")) phoneNumber = it
            },
            label = { Text("Phone Number") },
            placeholder = { Text("+91XXXXXXXXXX") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (phoneNumber.length < 13 || !phoneNumber.startsWith("+91")) {
                    Toast.makeText(context, "Enter a valid 10-digit Indian mobile number", Toast.LENGTH_LONG).show()
                    return@Button
                }
                val uid = auth.currentUser?.uid
                if (uid == null) {
                    Toast.makeText(context, "Not logged in. Please restart.", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isLoading = true
                val userMap = hashMapOf(
                    "phoneNumber" to phoneNumber.trim(),
                    "email" to (auth.currentUser?.email ?: ""),
                    "uid" to uid
                )
                db.collection("users").document(uid)
                    .set(userMap)
                    .addOnSuccessListener {
                        isLoading = false
                        navController.navigate("onboarding") {
                            popUpTo("setup_phone") { inclusive = true }
                        }
                    }
                    .addOnFailureListener { e ->
                        isLoading = false
                        Toast.makeText(context, "Failed to save: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Save & Continue")
            }
        }
    }
}
