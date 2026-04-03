package com.example.voiceresponder.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.voiceresponder.ui.theme.*
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun SetupPhoneScreen(navController: NavController) {

    // ── UI State ──────────────────────────────────────────────────────────────
    var phoneNumber  by remember { mutableStateOf("+91") }
    var isLoading    by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var hintShown    by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val auth    = FirebaseAuth.getInstance()
    val db      = FirebaseFirestore.getInstance()
    val scope   = rememberCoroutineScope()

    val bgGradient  = Brush.verticalGradient(listOf(DarkBg, DarkSurface))
    val btnGradient = Brush.horizontalGradient(listOf(Teal400, Color(0xFF7C4DFF)))

    // ── Google Phone Number Hint launcher ─────────────────────────────────────
    // When the user selects their number from the system sheet, it comes here.
    val hintLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val phone = Identity.getSignInClient(context)
                .getPhoneNumberFromIntent(result.data)
            if (!phone.isNullOrBlank()) {
                // Normalise to +91XXXXXXXXXX
                val digits = phone.filter { it.isDigit() }
                phoneNumber = when {
                    digits.length == 10               -> "+91$digits"
                    digits.length == 12 && digits.startsWith("91") -> "+$digits"
                    else                              -> phone
                }
                errorMessage = ""
                hintShown    = true
            }
        }
    }

    // ── Launch Google's phone picker ──────────────────────────────────────────
    fun showPhoneHint() {
        val request = GetPhoneNumberHintIntentRequest.builder().build()
        Identity.getSignInClient(context)
            .getPhoneNumberHintIntent(request)
            .addOnSuccessListener { pendingIntent ->
                hintLauncher.launch(
                    IntentSenderRequest.Builder(pendingIntent).build()
                )
            }
            .addOnFailureListener {
                // Device has no SIM or Play Services unavailable → let user type manually
                errorMessage = "Could not detect SIM. Please type your number manually."
            }
    }

    // ── Validate & save phone to Firestore ────────────────────────────────────
    fun confirmNumber() {
        val digits = phoneNumber.removePrefix("+91").trim()
        if (!phoneNumber.startsWith("+91") || digits.length != 10 || !digits.all { it.isDigit() }) {
            errorMessage = "Enter a valid 10-digit Indian number (+91XXXXXXXXXX)"
            return
        }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            errorMessage = "Session expired. Please log in again."
            return
        }

        scope.launch {
            isLoading    = true
            errorMessage = ""
            try {
                val userMap = hashMapOf(
                    "phoneNumber" to phoneNumber.trim(),
                    "email"       to (auth.currentUser?.email ?: ""),
                    "uid"         to uid
                )
                db.collection("users").document(uid).set(userMap).await()
                isLoading = false
                Toast.makeText(context, "Phone number confirmed ✓", Toast.LENGTH_SHORT).show()
                navController.navigate("onboarding") {
                    popUpTo("setup_phone") { inclusive = true }
                }
            } catch (e: Exception) {
                isLoading    = false
                errorMessage = "Save failed: ${e.message}"
            }
        }
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Box(
        modifier         = Modifier.fillMaxSize().background(bgGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Icon ──────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(72.dp).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Teal400, Color(0xFF7C4DFF)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PhonelinkLock, null, tint = Color.White, modifier = Modifier.size(36.dp))
            }
            Spacer(Modifier.height(14.dp))

            Text("Verify Your Number", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OnDarkText)
            Spacer(Modifier.height(4.dp))
            Text(
                "We confirm this number is installed on your device.\nNo SMS or charges required.",
                fontSize  = 13.sp,
                color     = SubText,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))

            Card(
                shape    = RoundedCornerShape(24.dp),
                colors   = CardDefaults.cardColors(containerColor = DarkCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    Text("Mobile Number", fontWeight = FontWeight.Bold, color = OnDarkText, fontSize = 18.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Tap the button below to select your SIM number from this device.",
                        color    = SubText,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(16.dp))

                    // ── Phone Number Field ─────────────────────────────────────
                    OutlinedTextField(
                        value         = phoneNumber,
                        onValueChange = {
                            if (it.startsWith("+91") && it.length <= 13) {
                                phoneNumber  = it
                                errorMessage = ""
                                hintShown    = false
                            }
                        },
                        label           = { Text("Phone Number") },
                        placeholder     = { Text("+91XXXXXXXXXX", color = SubText) },
                        leadingIcon     = { Icon(Icons.Default.Phone, null, tint = Teal400) },
                        trailingIcon    = {
                            if (hintShown) Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50))
                        },
                        singleLine      = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape           = RoundedCornerShape(12.dp),
                        colors          = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Teal400,
                            unfocusedBorderColor = if (hintShown) Color(0xFF4CAF50) else Color(0xFFBBCCDD),
                            focusedTextColor     = OnDarkText,
                            unfocusedTextColor   = OnDarkText,
                            cursorColor          = Teal400
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(10.dp))

                    // ── "Use My SIM Number" pill button ───────────────────────
                    OutlinedButton(
                        onClick  = { showPhoneHint() },
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = Teal400),
                        border   = androidx.compose.foundation.BorderStroke(1.dp, Teal400)
                    ) {
                        Icon(Icons.Default.SimCard, null, tint = Teal400, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Use My SIM Number", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // ── Security badge ────────────────────────────────────────
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier          = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Security, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Verified by Google — only numbers on this device are shown",
                            color    = SubText,
                            fontSize = 11.sp
                        )
                    }

                    // ── Error message ─────────────────────────────────────────
                    if (errorMessage.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = ErrorRed, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(errorMessage, color = ErrorRed, fontSize = 12.sp)
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── Confirm & Continue button ─────────────────────────────
                    GradientButton(
                        text      = "Confirm & Continue",
                        gradient  = btnGradient,
                        isLoading = isLoading,
                        icon      = Icons.Default.Verified,
                        onClick   = { confirmNumber() }
                    )
                }
            }
        }
    }
}

// ── Small reusable gradient button ────────────────────────────────────────────
@Composable
private fun GradientButton(
    text: String,
    gradient: Brush,
    isLoading: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    enabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(
                if (enabled && !isLoading) gradient
                else Brush.horizontalGradient(listOf(Color(0xFFBBCCDD), Color(0xFFBBCCDD))),
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        Button(
            onClick   = onClick,
            modifier  = Modifier.fillMaxSize(),
            enabled   = enabled && !isLoading,
            colors    = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                if (icon != null) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text(text, color = if (enabled) Color.White else SubText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
