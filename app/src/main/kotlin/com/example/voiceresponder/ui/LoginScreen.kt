package com.example.voiceresponder.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

@Composable
fun LoginScreen(navController: NavController) {
    var phoneNumber by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var verificationId by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }

    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // Auto-verification or instant validation
            scopeSignIn(credential, auth, navController, context)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Toast.makeText(context, "Verification Failed: ${e.message}", Toast.LENGTH_LONG).show()
            isOtpSent = false
        }

        override fun onCodeSent(verificationIdStr: String, token: PhoneAuthProvider.ForceResendingToken) {
            verificationId = verificationIdStr
            isOtpSent = true
            Toast.makeText(context, "OTP Sent", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Personalized Voice Responder", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number (with +country code)") },
            modifier = Modifier.fillMaxWidth()
        )

        if (isOtpSent) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = otp,
                onValueChange = { otp = it },
                label = { Text("OTP") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (!isOtpSent) {
                    if (phoneNumber.isNotEmpty()) {
                        val options = PhoneAuthOptions.newBuilder(auth)
                            .setPhoneNumber(phoneNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(context as Activity)
                            .setCallbacks(callbacks)
                            .build()
                        PhoneAuthProvider.verifyPhoneNumber(options)
                    } else {
                        Toast.makeText(context, "Please enter phone number", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (otp.isNotEmpty() && verificationId.isNotEmpty()) {
                        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
                        scopeSignIn(credential, auth, navController, context)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isOtpSent) "Verify OTP" else "Send OTP")
        }
    }
}

private fun scopeSignIn(credential: PhoneAuthCredential, auth: FirebaseAuth, navController: NavController, context: android.content.Context) {
    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                navController.navigate("dashboard") {
                    popUpTo("login") { inclusive = true }
                }
            } else {
                Toast.makeText(context, "Sign In Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
}

