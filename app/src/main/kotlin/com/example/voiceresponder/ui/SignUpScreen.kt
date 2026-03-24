package com.example.voiceresponder.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.voiceresponder.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SignUpScreen(navController: NavController) {
    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var confirmPwd   by remember { mutableStateOf("") }
    var isLoading    by remember { mutableStateOf(false) }
    var showPwd      by remember { mutableStateOf(false) }
    var showConfPwd  by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val auth    = remember { FirebaseAuth.getInstance() }

    val bgGradient  = Brush.verticalGradient(listOf(DarkBg, DarkSurface))
    val btnGradient = Brush.horizontalGradient(listOf(Teal400, Color(0xFF7C4DFF)))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 32.dp),
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
            Text("Silent Mode",          fontSize = 26.sp, fontWeight = FontWeight.Bold,    color = OnDarkText)
            Text("Smart Auto Responder", fontSize = 13.sp,                                  color = SubText)
            Text("Join Silent Mode today", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnDarkText)

            Spacer(Modifier.height(24.dp))

            // ── Card ──
            Card(
                shape  = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Sign Up", fontWeight = FontWeight.Bold, color = OnDarkText, fontSize = 20.sp)
                    Spacer(Modifier.height(20.dp))

                    // Email
                    OutlinedTextField(
                        value         = email,
                        onValueChange = { email = it },
                        placeholder   = { Text("Email Address", color = SubText) },
                        leadingIcon   = { Icon(Icons.Default.Email, contentDescription = null, tint = Teal400) },
                        singleLine    = true,
                        shape         = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Teal400,
                            unfocusedBorderColor = Color(0xFF2A2A3E),
                            focusedTextColor     = OnDarkText,
                            unfocusedTextColor   = OnDarkText,
                            cursorColor          = Teal400
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    // Password
                    OutlinedTextField(
                        value         = password,
                        onValueChange = { password = it },
                        placeholder   = { Text("Create Password", color = SubText) },
                        leadingIcon   = { Icon(Icons.Default.Lock, contentDescription = null, tint = Teal400) },
                        trailingIcon  = {
                            IconButton(onClick = { showPwd = !showPwd }) {
                                Icon(if (showPwd) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = SubText)
                            }
                        },
                        visualTransformation = if (showPwd) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine    = true,
                        shape         = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Teal400,
                            unfocusedBorderColor = Color(0xFF2A2A3E),
                            focusedTextColor     = OnDarkText,
                            unfocusedTextColor   = OnDarkText,
                            cursorColor          = Teal400
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    // Confirm Password
                    OutlinedTextField(
                        value         = confirmPwd,
                        onValueChange = { confirmPwd = it },
                        placeholder   = { Text("Confirm Password", color = SubText) },
                        leadingIcon   = { Icon(Icons.Default.Lock, contentDescription = null, tint = Teal400) },
                        trailingIcon  = {
                            IconButton(onClick = { showConfPwd = !showConfPwd }) {
                                Icon(if (showConfPwd) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = SubText)
                            }
                        },
                        visualTransformation = if (showConfPwd) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine    = true,
                        shape         = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Teal400,
                            unfocusedBorderColor = Color(0xFF2A2A3E),
                            focusedTextColor     = OnDarkText,
                            unfocusedTextColor   = OnDarkText,
                            cursorColor          = Teal400
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(22.dp))

                    // Gradient button
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
                                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                                    password.length < 6 ->
                                        Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                                    password != confirmPwd ->
                                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                                    else -> {
                                        isLoading = true
                                        auth.createUserWithEmailAndPassword(email.trim(), password)
                                            .addOnCompleteListener { task ->
                                                isLoading = false
                                                if (task.isSuccessful) {
                                                    navController.navigate("setup_phone") {
                                                        popUpTo("signup") { inclusive = true }
                                                    }
                                                } else {
                                                    Toast.makeText(context, "Sign Up Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
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
                                Text("Create Account", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("Already have an account? ", color = SubText, fontSize = 13.sp)
                        TextButton(
                            onClick        = { navController.popBackStack() },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Login", color = Teal400, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
