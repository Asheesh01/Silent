package com.example.voiceresponder.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.room.Room
import com.example.voiceresponder.data.AppDatabase
import com.example.voiceresponder.remote.SyncHelper
import com.example.voiceresponder.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var isLoading    by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }

    val context    = LocalContext.current
    val auth       = remember { FirebaseAuth.getInstance() }
    val syncHelper = remember { SyncHelper() }
    val database   = remember {
        Room.databaseBuilder(context, AppDatabase::class.java, "responder-db").build()
    }

    val bgGradient  = Brush.verticalGradient(listOf(DarkBg, DarkSurface))
    val btnGradient = Brush.horizontalGradient(listOf(Teal400, Color(0xFF7C4DFF)))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Logo ──
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Teal400, Color(0xFF7C4DFF)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
            }

            Spacer(Modifier.height(14.dp))
            Text("Silent Mode",          fontSize = 26.sp, fontWeight = FontWeight.Bold,       color = OnDarkText)
            Text("Smart Auto Responder", fontSize = 13.sp,                                     color = SubText)
            Text("Welcome Back",         fontSize = 15.sp, fontWeight = FontWeight.SemiBold,   color = OnDarkText)

            Spacer(Modifier.height(28.dp))

            // ── Card ──
            Card(
                shape  = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Login", fontWeight = FontWeight.Bold, color = OnDarkText, fontSize = 20.sp)
                    Spacer(Modifier.height(20.dp))

                    // Email
                    OutlinedTextField(
                        value           = email,
                        onValueChange   = { email = it },
                        placeholder     = { Text("Enter your email", color = SubText) },
                        leadingIcon     = { Icon(Icons.Default.Email, contentDescription = null, tint = Teal400) },
                        singleLine      = true,
                        shape           = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors          = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Teal400,
                            unfocusedBorderColor = Color(0xFF2A2A3E),
                            focusedTextColor     = OnDarkText,
                            unfocusedTextColor   = OnDarkText,
                            cursorColor          = Teal400
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(14.dp))

                    // Password
                    OutlinedTextField(
                        value                = password,
                        onValueChange        = { password = it },
                        placeholder          = { Text("Enter your password", color = SubText) },
                        leadingIcon          = { Icon(Icons.Default.Lock, contentDescription = null, tint = Teal400) },
                        trailingIcon         = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null, tint = SubText
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine           = true,
                        shape                = RoundedCornerShape(12.dp),
                        keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors               = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Teal400,
                            unfocusedBorderColor = Color(0xFF2A2A3E),
                            focusedTextColor     = OnDarkText,
                            unfocusedTextColor   = OnDarkText,
                            cursorColor          = Teal400
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Forgot Password?",
                        color     = Teal400,
                        fontSize  = 13.sp,
                        modifier  = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )

                    Spacer(Modifier.height(22.dp))

                    // Gradient Login button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(btnGradient, shape = RoundedCornerShape(14.dp))
                    ) {
                        Button(
                            onClick = {
                                when {
                                    email.isBlank() || password.isBlank() ->
                                        Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                                    password.length < 6 ->
                                        Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                                    else -> {
                                        isLoading = true
                                        auth.signInWithEmailAndPassword(email.trim(), password)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                                                    // Restore cloud data (contacts + audio) before navigating
                                                    CoroutineScope(Dispatchers.IO).launch {
                                                        syncHelper.restoreFromCloud(uid, database.contactDao(), context.filesDir)
                                                        FirebaseFirestore.getInstance()
                                                            .collection("users").document(uid).get()
                                                            .addOnSuccessListener { doc ->
                                                                isLoading = false
                                                                val phone = doc.getString("phoneNumber")
                                                                if (phone.isNullOrEmpty()) {
                                                                    navController.navigate("setup_phone") { popUpTo("login") { inclusive = true } }
                                                                } else {
                                                                    navController.navigate("dashboard") { popUpTo("login") { inclusive = true } }
                                                                }
                                                            }
                                                            .addOnFailureListener {
                                                                isLoading = false
                                                                navController.navigate("setup_phone") { popUpTo("login") { inclusive = true } }
                                                            }
                                                    }
                                                } else {
                                                    isLoading = false
                                                    Toast.makeText(context, "Sign In Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                    }
                                }
                            },
                            modifier  = Modifier.fillMaxSize(),
                            enabled   = !isLoading,
                            colors    = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Login", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Navigate to Sign Up screen
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("Don't have an account? ", color = SubText, fontSize = 13.sp)
                        TextButton(
                            onClick        = { navController.navigate("signup") },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Sign Up", color = Teal400, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
