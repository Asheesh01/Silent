package com.example.voiceresponder.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.room.Room
import com.example.voiceresponder.R
import com.example.voiceresponder.data.AppDatabase
import com.example.voiceresponder.remote.SyncHelper
import com.example.voiceresponder.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.voiceresponder.remote.EmailOtpHelper
import com.example.voiceresponder.security.OtpSecurityManager

// ── Forgot-password flow stages ───────────────────────────────────────────────
private enum class ResetStage { EMAIL, OTP_VERIFY, SENT }

@Composable
fun LoginScreen(navController: NavController) {

    var email          by remember { mutableStateOf("") }
    var password       by remember { mutableStateOf("") }
    var isLoading      by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }   // separate Google loader
    var showPassword   by remember { mutableStateOf(false) }
    var errorMsg       by remember { mutableStateOf("") }

    // ── Forgot password state ────────────────────────────────────────
    var showForgotDialog by remember { mutableStateOf(false) }
    var resetStage       by remember { mutableStateOf(ResetStage.EMAIL) }
    var resetEmail       by remember { mutableStateOf("") }
    var resetLoading     by remember { mutableStateOf(false) }
    var resetError       by remember { mutableStateOf("") }
    var resetOtp         by remember { mutableStateOf("") }   // user-typed OTP
    val resetScope       = rememberCoroutineScope()

    val context    = LocalContext.current
    val auth       = remember { FirebaseAuth.getInstance() }
    val syncHelper = remember { SyncHelper() }
    val database   = remember {
        Room.databaseBuilder(context, AppDatabase::class.java, "responder-db").build()
    }

    val bgGradient  = Brush.verticalGradient(listOf(DarkBg, Color(0xFF0D1B2A)))
    val btnGradient = Brush.horizontalGradient(listOf(Color(0xFF00BCD4), Color(0xFF7C4DFF)))

    // ── Google Sign-In setup ──────────────────────────────────────────────────
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isGoogleLoading = false   // picker closed — stop spinner
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account    = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            isLoading = true
            auth.signInWithCredential(credential).addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
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
                    errorMsg  = "Google Sign-In failed. Try again."
                }
            }
        } catch (e: ApiException) {
            errorMsg = "Google Sign-In cancelled."
        }
    }

    // ── Forgot Password Dialog ───────────────────────────────────────────────
    if (showForgotDialog) {
        Dialog(onDismissRequest = {
            if (!resetLoading) {
                showForgotDialog = false
                resetStage   = ResetStage.EMAIL
                resetEmail   = ""
                resetError   = ""
                resetLoading = false
            }
        }) {
            Card(
                shape    = RoundedCornerShape(24.dp),
                colors   = CardDefaults.cardColors(containerColor = Color(0xFF111D2B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier            = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ── Icon ────────────────────────────────────────────────
                    val iconRes  = if (resetStage == ResetStage.SENT) Icons.Default.MarkEmailRead else Icons.Default.LockReset
                    val iconTint = if (resetStage == ResetStage.SENT) Color(0xFF4CAF50) else Color(0xFF00BCD4)
                    Box(
                        modifier = Modifier
                            .size(64.dp).clip(CircleShape)
                            .background(Brush.radialGradient(listOf(iconTint.copy(alpha = 0.22f), Color.Transparent))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(iconRes, null, tint = iconTint, modifier = Modifier.size(36.dp))
                    }
                    Spacer(Modifier.height(16.dp))

                    AnimatedContent(
                        targetState    = resetStage,
                        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                        label          = "resetStage"
                    ) { stage ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            when (stage) {

                                // ─── Enter Email ──────────────────────────────────────
                                ResetStage.EMAIL -> {
                                    Text("Reset Password", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        "Enter your email and we'll send a\npassword reset link.",
                                        fontSize = 13.sp, color = Color(0xFFB0BEC5), textAlign = TextAlign.Center
                                    )
                                    Spacer(Modifier.height(20.dp))

                                    OutlinedTextField(
                                        value           = resetEmail,
                                        onValueChange   = { resetEmail = it; resetError = "" },
                                        placeholder     = { Text("Your email address", color = Color(0xFF6B8499)) },
                                        leadingIcon     = { Icon(Icons.Default.Email, null, tint = Color(0xFF00BCD4)) },
                                        singleLine      = true,
                                        shape           = RoundedCornerShape(12.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                        colors          = resetFieldColors(),
                                        modifier        = Modifier.fillMaxWidth()
                                    )

                                    AnimatedVisibility(visible = resetError.isNotEmpty()) {
                                        Column {
                                            Spacer(Modifier.height(8.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Warning, null, tint = ErrorRed, modifier = Modifier.size(13.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text(resetError, color = ErrorRed, fontSize = 12.sp)
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(20.dp))
                                    GradientDialogButton(
                                        text    = "Send Reset Link",
                                        gradient = btnGradient,
                                        loading = resetLoading,
                                        enabled = resetEmail.isNotBlank() && !resetLoading,
                                        onClick = {
                                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(resetEmail.trim()).matches()) {
                                                resetError = "Enter a valid email address."
                                                return@GradientDialogButton
                                            }
                                            resetLoading = true
                                            resetError   = ""
                                            resetScope.launch {
                                                val otp = OtpSecurityManager.generateOtp()
                                                when (OtpSecurityManager.storeAndHash(otp)) {
                                                    OtpSecurityManager.StoreResult.COOLDOWN -> {
                                                        resetLoading = false
                                                        resetError = "Please wait 60 seconds before trying again."
                                                        return@launch
                                                    }
                                                    OtpSecurityManager.StoreResult.MAX_RESENDS_REACHED -> {
                                                        resetLoading = false
                                                        resetError = "Too many attempts. Try again later."
                                                        return@launch
                                                    }
                                                    OtpSecurityManager.StoreResult.OK -> { /* proceed */ }
                                                }
                                                val result = EmailOtpHelper.sendPasswordResetCode(
                                                    resetEmail.trim(), otp
                                                )
                                                resetLoading = false
                                                if (result.success) {
                                                    resetStage = ResetStage.OTP_VERIFY
                                                } else {
                                                    OtpSecurityManager.clearSession()
                                                    resetError = result.errorMessage
                                                }
                                            }
                                        }
                                    )
                                }

                                // ─── Verify OTP ──────────────────────────────────
                                ResetStage.OTP_VERIFY -> {
                                    Text("Enter Verification Code", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        "We sent a 6-digit code to\n${resetEmail.trim()}",
                                        fontSize = 13.sp, color = Color(0xFFB0BEC5), textAlign = TextAlign.Center
                                    )
                                    Spacer(Modifier.height(20.dp))
                                    OutlinedTextField(
                                        value         = resetOtp,
                                        onValueChange = { if (it.length <= 6) { resetOtp = it.filter { c -> c.isDigit() }; resetError = "" } },
                                        placeholder   = { Text("6-digit code", color = Color(0xFF6B8499)) },
                                        leadingIcon   = { Icon(Icons.Default.Pin, null, tint = Color(0xFF00BCD4)) },
                                        singleLine    = true,
                                        shape         = RoundedCornerShape(12.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                        colors        = resetFieldColors(),
                                        modifier      = Modifier.fillMaxWidth()
                                    )
                                    AnimatedVisibility(visible = resetError.isNotEmpty()) {
                                        Column {
                                            Spacer(Modifier.height(8.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Warning, null, tint = ErrorRed, modifier = Modifier.size(13.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text(resetError, color = ErrorRed, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(20.dp))
                                    GradientDialogButton(
                                        text     = "Verify Code",
                                        gradient = btnGradient,
                                        loading  = resetLoading,
                                        enabled  = resetOtp.length == 6 && !resetLoading,
                                        onClick  = {
                                            if (OtpSecurityManager.verify(resetOtp.trim()) != OtpSecurityManager.VerifyResult.SUCCESS) {
                                                resetError = "Incorrect code. Please try again."
                                                return@GradientDialogButton
                                            }
                                            // OTP verified ✓ — silently send Firebase reset link
                                            resetLoading = true
                                            auth.sendPasswordResetEmail(resetEmail.trim())
                                                .addOnCompleteListener {
                                                    resetLoading = false
                                                    resetStage   = ResetStage.SENT
                                                    OtpSecurityManager.clearSession()
                                                }
                                        }
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    TextButton(onClick = {
                                        resetStage = ResetStage.EMAIL
                                        resetOtp   = ""
                                        resetError = ""
                                    }) {
                                        Text("Didn't receive it? Go back", color = Color(0xFF00BCD4), fontSize = 12.sp)
                                    }

                                } // end OTP_VERIFY

                                // ─── Email Sent Success ───────────────────────────
                                ResetStage.SENT -> {
                                    Text("Reset Link Sent!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Spacer(Modifier.height(10.dp))
                                    Text("We sent a password reset link to", fontSize = 13.sp, color = Color(0xFFB0BEC5), textAlign = TextAlign.Center)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        resetEmail.trim(),
                                        fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF00BCD4), textAlign = TextAlign.Center
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Click the link in the email to set\nyour new password.",
                                        fontSize = 13.sp, color = Color(0xFFB0BEC5), textAlign = TextAlign.Center
                                    )
                                    Spacer(Modifier.height(20.dp))
                                    GradientDialogButton(
                                        text     = "Got It",
                                        gradient = btnGradient,
                                        loading  = false,
                                        enabled  = true,
                                        onClick  = {
                                            showForgotDialog = false
                                            resetStage = ResetStage.EMAIL
                                            resetEmail = ""
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Main UI ───────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier.fillMaxSize().background(bgGradient).drawEdgeGlows()
    ) {
        // Decorative glowing orb top-right
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = 100.dp, y = (-60).dp)
                .background(
                    Brush.radialGradient(listOf(Color(0xFF7C4DFF).copy(alpha = 0.18f), Color.Transparent)),
                    CircleShape
                )
                .align(Alignment.TopEnd)
        )
        // Decorative orb bottom-left
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = (-60).dp, y = 60.dp)
                .background(
                    Brush.radialGradient(listOf(Color(0xFF00BCD4).copy(alpha = 0.14f), Color.Transparent)),
                    CircleShape
                )
                .align(Alignment.BottomStart)
        )

        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Animated logo ─────────────────────────────────────────────────
            val pulse = rememberInfiniteTransition(label = "pulse")
            val scale by pulse.animateFloat(
                initialValue   = 1f,
                targetValue    = 1.06f,
                animationSpec  = infiniteRepeatable(tween(1200, easing = EaseInOut), RepeatMode.Reverse),
                label          = "logoScale"
            )
            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(80.dp).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF00BCD4), Color(0xFF7C4DFF)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Mic, null, tint = Color.White, modifier = Modifier.size(38.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("Replora", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = OnDarkText)
            Text("Smart Auto Responder", fontSize = 13.sp, color = SubText)
            Spacer(Modifier.height(6.dp))
            Text("Welcome back 👋", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFFB347))
            Spacer(Modifier.height(28.dp))

            // ── Login Card ────────────────────────────────────────────────────
            Card(
                shape    = RoundedCornerShape(28.dp),
                colors   = CardDefaults.cardColors(containerColor = Color(0xFF111D2B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Login", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 22.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Sign in to continue", fontSize = 12.sp, color = Color(0xFFB0BEC5))
                                    Spacer(Modifier.height(20.dp))

                    // Email
                    OutlinedTextField(
                        value           = email,
                        onValueChange   = { email = it; errorMsg = "" },
                        placeholder     = { Text("Email address", color = SubText) },
                        leadingIcon     = { Icon(Icons.Default.Email, null, tint = Color(0xFF00BCD4)) },
                        singleLine      = true,
                        shape           = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors          = loginFieldColors(),
                        modifier        = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))

                    // Password
                    OutlinedTextField(
                        value                = password,
                        onValueChange        = { password = it; errorMsg = "" },
                        placeholder          = { Text("Password", color = SubText) },
                        leadingIcon          = { Icon(Icons.Default.Lock, null, tint = Color(0xFF00BCD4)) },
                        trailingIcon         = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    null, tint = SubText
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine           = true,
                        shape                = RoundedCornerShape(14.dp),
                        keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors               = loginFieldColors(),
                        modifier             = Modifier.fillMaxWidth()
                    )

                    // Forgot password
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick        = { showForgotDialog = true; resetEmail = email },
                        modifier       = Modifier.align(Alignment.End),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "Forgot Password?",
                            color      = Color(0xFF00BCD4),
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Error
                    AnimatedVisibility(visible = errorMsg.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                            Icon(Icons.Default.Warning, null, tint = ErrorRed, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(errorMsg, color = ErrorRed, fontSize = 12.sp)
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Login button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth().height(52.dp)
                            .background(btnGradient, RoundedCornerShape(14.dp))
                    ) {
                        Button(
                            onClick = {
                                when {
                                    email.isBlank() || password.isBlank() ->
                                        errorMsg = "Please enter email and password."
                                    password.length < 6 ->
                                        errorMsg = "Password must be at least 6 characters."
                                    else -> {
                                        isLoading = true
                                        errorMsg  = ""
                                        auth.signInWithEmailAndPassword(email.trim(), password)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
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
                                                    errorMsg  = "Wrong email or password."
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
                                Icon(Icons.Default.Login, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Login", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // Divider
                    OrDivider()

                    Spacer(Modifier.height(14.dp))

                    // Google Sign-In button
                    GoogleSignInButton(
                        text      = "Continue with Google",
                        enabled   = !isLoading && !isGoogleLoading,
                        isLoading = isGoogleLoading
                    ) {
                        isGoogleLoading = true
                        googleLauncher.launch(googleClient.signInIntent)
                    }

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("Don't have an account? ", color = SubText, fontSize = 13.sp)
                        TextButton(onClick = { navController.navigate("signup") }, contentPadding = PaddingValues(0.dp)) {
                            Text("Sign Up", color = Color(0xFF00BCD4), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

// ── Shared helpers ─────────────────────────────────────────────────────────────

@Composable
fun OrDivider(textColor: Color = Color(0xFF8899AA)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Divider(modifier = Modifier.weight(1f), color = Color(0xFF1E3048), thickness = 1.dp)
        Text("  OR  ", color = textColor, fontSize = 12.sp)
        Divider(modifier = Modifier.weight(1f), color = Color(0xFF1E3048), thickness = 1.dp)
    }
}

@Composable
fun GoogleSignInButton(
    text: String,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick  = { if (!isLoading) onClick() },
        enabled  = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape  = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor   = Color(0xFF3C4043)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier    = Modifier.size(18.dp),
                color       = Color(0xFF4285F4),
                strokeWidth = 2.dp
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "Signing in...",
                fontSize   = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color(0xFF3C4043)
            )
        } else {
            // Colourful Google "G" icon
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "G",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = Color(0xFF4285F4)   // Google blue
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text,
                fontSize   = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color(0xFF3C4043)       // Google dark grey text
            )
        }
    }
}


@Composable
private fun loginFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = Color(0xFF00BCD4),
    unfocusedBorderColor    = Color(0xFF1E3048),
    focusedTextColor        = Color.White,
    unfocusedTextColor      = Color(0xFFE0E8F0),
    cursorColor             = Color(0xFF00BCD4),
    focusedContainerColor   = Color(0xFF0A1622),
    unfocusedContainerColor = Color(0xFF0D1B2A)
)

@Composable
private fun resetFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = Color(0xFF00BCD4),
    unfocusedBorderColor    = Color(0xFF1E3048),
    focusedTextColor        = Color.White,
    unfocusedTextColor      = Color(0xFFE0E8F0),
    cursorColor             = Color(0xFF00BCD4),
    focusedContainerColor   = Color(0xFF0A1622),
    unfocusedContainerColor = Color(0xFF0D1B2A)
)

// ── Reusable gradient button for dialogs ──────────────────────────────────────
@Composable
private fun GradientDialogButton(
    text    : String,
    gradient: Brush,
    loading : Boolean,
    enabled : Boolean,
    onClick : () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                if (enabled) gradient
                else Brush.horizontalGradient(listOf(Color(0xFF2A3A4A), Color(0xFF2A3A4A))),
                RoundedCornerShape(12.dp)
            )
    ) {
        Button(
            onClick   = { if (!loading) onClick() },
            modifier  = Modifier.fillMaxSize(),
            enabled   = enabled,
            colors    = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text(text, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Update password after OTP verification ────────────────────────────────────
//
// Strategy: Firebase requires a "recent sign-in" to change password.
// Since the user forgot their password, we cannot sign them in with their old
// password.  We therefore use a two-step approach:
//   1. Send a Firebase password-reset email (silent — user won't see it because
//      they verified their identity via our EmailJS OTP already).
//   2. Because we cannot wait for them to click that link, we instead trigger
//      Firebase's Admin-equivalent via signInAnonymously + linkWithCredential
//      — but that won't work without the old credential either.
//
// The CORRECT lightweight approach without a backend:
//   - Use Firebase's "verifyPasswordResetCode + confirmPasswordReset" pair which
//     ONLY works with a link from Firebase's own email.
//   - Since we are NOT using Firebase's email (we use EmailJS), we need one of:
//       a) A Firebase Cloud Function (Admin SDK) — call it from the app.
//       b) Re-sign in with old password (not possible if forgotten).
//
// For this app we use approach (b) fallback:
//   Try signInWithEmailAndPassword with a dummy; if that fails (expected),
//   we fall back to Firebase's own sendPasswordResetEmail so the user
//   still gets a fallback link — but our EmailJS code verified identity first.
//   The cleanest result: after our OTP passes, call Firebase reset as backup.
//
// NOTE: If you add a Firebase Cloud Function with Admin SDK later, replace this.
private fun updatePasswordViaCloudFunction(
    email      : String,
    newPassword: String,
    auth       : FirebaseAuth,
    onSuccess  : () -> Unit,
    onError    : (String) -> Unit
) {
    // We verified the user's identity via EmailJS OTP.
    // Now use Firebase's own password reset as the mechanism to actually
    // change the password — the user will receive a link email, but since
    // we already confirmed they own this email via OTP, this is safe.
    // IMPORTANT: This fires a Firebase email too but that is ok as a fallback.
    // The primary UX (OTP in inbox) has already completed.
    auth.sendPasswordResetEmail(email)
        .addOnSuccessListener {
            // Tell the user the reset email is on its way via the app UI
            // (the Firebase email is a secondary fallback link)
            onSuccess()
        }
        .addOnFailureListener { e ->
            onError("Could not initiate password reset: ${e.message}")
        }
}
