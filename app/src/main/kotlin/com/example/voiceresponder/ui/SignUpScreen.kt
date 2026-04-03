package com.example.voiceresponder.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
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
import com.example.voiceresponder.R
import com.example.voiceresponder.remote.EmailOtpHelper
import com.example.voiceresponder.security.OtpSecurityManager
import com.example.voiceresponder.security.OtpSecurityManager.StoreResult
import com.example.voiceresponder.security.OtpSecurityManager.VerifyResult
import com.example.voiceresponder.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Signup stages ─────────────────────────────────────────────────────────────
private enum class SignUpStage { FORM, OTP_VERIFY, CREATING }

@Composable
fun SignUpScreen(navController: NavController) {

    // ── Form state ────────────────────────────────────────────────────────────
    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var confirmPwd   by remember { mutableStateOf("") }
    var showPwd      by remember { mutableStateOf(false) }
    var showConfPwd  by remember { mutableStateOf(false) }
    val passwordMismatch = confirmPwd.isNotEmpty() && password != confirmPwd

    // ── Flow state ────────────────────────────────────────────────────────────
    var stage          by remember { mutableStateOf(SignUpStage.FORM) }
    var isLoading      by remember { mutableStateOf(false) }
    var otpCode        by remember { mutableStateOf("") }
    var errorMessage   by remember { mutableStateOf("") }
    var resendCooldown by remember { mutableIntStateOf(0) }
    var attemptsLeft   by remember { mutableIntStateOf(3) }

    val context = LocalContext.current
    val auth    = remember { FirebaseAuth.getInstance() }
    val scope   = rememberCoroutineScope()

    val bgGradient  = Brush.verticalGradient(listOf(DarkBg, Color(0xFF0D1B2A)))
    val btnGradient = Brush.horizontalGradient(listOf(Color(0xFF00BCD4), Color(0xFF7C4DFF)))

    DisposableEffect(Unit) { onDispose { OtpSecurityManager.clearSession() } }

    // ── Tick cooldown & attempts ──────────────────────────────────────────────
    LaunchedEffect(stage) {
        if (stage != SignUpStage.OTP_VERIFY) return@LaunchedEffect
        while (true) {
            resendCooldown = OtpSecurityManager.resendCooldownSeconds()
            attemptsLeft   = OtpSecurityManager.attemptsRemaining()
            delay(1_000)
        }
    }

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
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account    = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            isLoading = true
            auth.signInWithCredential(credential).addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    FirebaseFirestore.getInstance()
                        .collection("users").document(uid).get()
                        .addOnSuccessListener { doc ->
                            isLoading = false
                            val phone = doc.getString("phoneNumber")
                            if (phone.isNullOrEmpty()) {
                                navController.navigate("setup_phone") { popUpTo("signup") { inclusive = true } }
                            } else {
                                navController.navigate("dashboard") { popUpTo("signup") { inclusive = true } }
                            }
                        }
                        .addOnFailureListener {
                            isLoading = false
                            navController.navigate("setup_phone") { popUpTo("signup") { inclusive = true } }
                        }
                } else {
                    isLoading    = false
                    errorMessage = "Google Sign-Up failed. Try again."
                }
            }
        } catch (e: ApiException) {
            errorMessage = "Google Sign-Up cancelled."
        }
    }

    // ── Send email OTP ────────────────────────────────────────────────────────
    fun sendEmailOtp() {
        scope.launch {
            isLoading    = true
            errorMessage = ""
            val otp = OtpSecurityManager.generateOtp()
            when (OtpSecurityManager.storeAndHash(otp)) {
                StoreResult.COOLDOWN -> {
                    isLoading    = false
                    errorMessage = "Wait ${OtpSecurityManager.resendCooldownSeconds()}s before resending."
                    return@launch
                }
                StoreResult.MAX_RESENDS_REACHED -> {
                    isLoading    = false
                    errorMessage = "Too many attempts. Please restart."
                    return@launch
                }
                StoreResult.OK -> { /* proceed */ }
            }
            val result = EmailOtpHelper.sendOtp(email.trim(), otp)
            isLoading = false
            if (result.success) {
                stage          = SignUpStage.OTP_VERIFY
                resendCooldown = 60
                errorMessage   = ""
                Toast.makeText(context, "Code sent to ${email.trim()}", Toast.LENGTH_SHORT).show()
            } else {
                OtpSecurityManager.clearSession()
                errorMessage = result.errorMessage
            }
        }
    }

    // ── Verify OTP then create Firebase account ───────────────────────────────
    fun verifyAndCreate() {
        if (otpCode.length != 6) { errorMessage = "Enter the 6-digit code."; return }
        errorMessage = ""
        when (OtpSecurityManager.verify(otpCode)) {
            VerifyResult.SUCCESS -> {
                stage     = SignUpStage.CREATING
                isLoading = true
                auth.createUserWithEmailAndPassword(email.trim(), password)
                    .addOnCompleteListener { task ->
                        isLoading = false
                        if (task.isSuccessful) {
                            navController.navigate("setup_phone") { popUpTo("signup") { inclusive = true } }
                        } else {
                            stage        = SignUpStage.OTP_VERIFY
                            errorMessage = "Account creation failed: ${task.exception?.message}"
                        }
                    }
            }
            VerifyResult.WRONG_OTP -> {
                attemptsLeft = OtpSecurityManager.attemptsRemaining()
                errorMessage = "Wrong code. $attemptsLeft attempt${if (attemptsLeft == 1) "" else "s"} left."
            }
            VerifyResult.EXPIRED -> {
                stage        = SignUpStage.FORM
                otpCode      = ""
                errorMessage = "Code expired (5 min). Request a new one."
            }
            VerifyResult.LOCKED  -> errorMessage = "Too many wrong attempts. Go back and try a new code."
            VerifyResult.NO_SESSION -> errorMessage = "No active code. Please go back and resend."
        }
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {

        // Decorative glowing orb — top-left
        Box(
            modifier = Modifier
                .size(200.dp).offset(x = (-70).dp, y = (-50).dp)
                .background(Brush.radialGradient(listOf(Color(0xFF00BCD4).copy(alpha = 0.15f), Color.Transparent)), CircleShape)
                .align(Alignment.TopStart)
        )
        // Decorative orb — bottom-right
        Box(
            modifier = Modifier
                .size(200.dp).offset(x = 70.dp, y = 50.dp)
                .background(Brush.radialGradient(listOf(Color(0xFF7C4DFF).copy(alpha = 0.15f), Color.Transparent)), CircleShape)
                .align(Alignment.BottomEnd)
        )

        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pulsing logo
            val pulse = rememberInfiniteTransition(label = "pulse")
            val scale by pulse.animateFloat(
                initialValue  = 1f,
                targetValue   = 1.06f,
                animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOut), RepeatMode.Reverse),
                label         = "logoScale"
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
            Text("Silent Mode",          fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = OnDarkText)
            Text("Smart Auto Responder", fontSize = 13.sp,                                    color = SubText)
            Spacer(Modifier.height(6.dp))
            Text("Create your account ✨", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF00BCD4))

            AnimatedContent(
                targetState   = stage,
                transitionSpec = { fadeIn(tween(350)) togetherWith fadeOut(tween(200)) },
                label         = "signupStage"
            ) { currentStage ->

                when (currentStage) {

                    // ─────────────────────────────────────────────────────────
                    //  STAGE 1 — Registration form
                    // ─────────────────────────────────────────────────────────
                    SignUpStage.FORM -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(Modifier.height(24.dp))
                            Card(
                                shape    = RoundedCornerShape(28.dp),
                                colors   = CardDefaults.cardColors(containerColor = Color(0xFF111D2B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    Text("Sign Up", fontWeight = FontWeight.Bold, color = OnDarkText, fontSize = 22.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Join Silent Mode today", fontSize = 12.sp, color = SubText)
                                    Spacer(Modifier.height(20.dp))

                                    // Email
                                    OutlinedTextField(
                                        value           = email,
                                        onValueChange   = { email = it; errorMessage = "" },
                                        placeholder     = { Text("Email address", color = SubText) },
                                        leadingIcon     = { Icon(Icons.Default.Email, null, tint = Color(0xFF00BCD4)) },
                                        singleLine      = true,
                                        shape           = RoundedCornerShape(14.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                        colors          = signupFieldColors(),
                                        modifier        = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(12.dp))

                                    // Password
                                    OutlinedTextField(
                                        value                = password,
                                        onValueChange        = { password = it; errorMessage = "" },
                                        placeholder          = { Text("Create password", color = SubText) },
                                        leadingIcon          = { Icon(Icons.Default.Lock, null, tint = Color(0xFF00BCD4)) },
                                        trailingIcon         = {
                                            IconButton(onClick = { showPwd = !showPwd }) {
                                                Icon(if (showPwd) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = SubText)
                                            }
                                        },
                                        visualTransformation = if (showPwd) VisualTransformation.None else PasswordVisualTransformation(),
                                        singleLine           = true,
                                        shape                = RoundedCornerShape(14.dp),
                                        keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        colors               = signupFieldColors(),
                                        modifier             = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(12.dp))

                                    // Confirm Password
                                    OutlinedTextField(
                                        value                = confirmPwd,
                                        onValueChange        = { confirmPwd = it; errorMessage = "" },
                                        placeholder          = { Text("Confirm password", color = SubText) },
                                        leadingIcon          = {
                                            Icon(Icons.Default.Lock, null,
                                                tint = if (passwordMismatch) ErrorRed else Color(0xFF00BCD4))
                                        },
                                        trailingIcon         = {
                                            if (confirmPwd.isNotEmpty()) {
                                                Icon(
                                                    if (passwordMismatch) Icons.Default.Cancel else Icons.Default.CheckCircle,
                                                    null,
                                                    tint = if (passwordMismatch) ErrorRed else Color(0xFF4CAF50)
                                                )
                                            }
                                        },
                                        visualTransformation = if (showConfPwd) VisualTransformation.None else PasswordVisualTransformation(),
                                        singleLine           = true,
                                        shape                = RoundedCornerShape(14.dp),
                                        isError              = passwordMismatch,
                                        keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        colors               = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor      = if (passwordMismatch) ErrorRed else Color(0xFF00BCD4),
                                            unfocusedBorderColor    = if (passwordMismatch) ErrorRed else Color(0xFF1E3048),
                                            focusedTextColor        = OnDarkText,
                                            unfocusedTextColor      = OnDarkText,
                                            cursorColor             = Color(0xFF00BCD4),
                                            focusedContainerColor   = Color(0xFF0A1622),
                                            unfocusedContainerColor = Color(0xFF0A1622)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    if (passwordMismatch) {
                                        Spacer(Modifier.height(4.dp))
                                        Text("Passwords do not match", color = ErrorRed, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                                    }

                                    // Error
                                    AnimatedVisibility(visible = errorMessage.isNotEmpty()) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                                            Icon(Icons.Default.Warning, null, tint = ErrorRed, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(errorMessage, color = ErrorRed, fontSize = 12.sp)
                                        }
                                    }

                                    Spacer(Modifier.height(22.dp))

                                    // Send Verification Code
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(52.dp)
                                            .background(btnGradient, RoundedCornerShape(14.dp))
                                    ) {
                                        Button(
                                            onClick = {
                                                when {
                                                    email.isBlank() || password.isBlank() ->
                                                        errorMessage = "Please fill in all fields."
                                                    !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() ->
                                                        errorMessage = "Enter a valid email address."
                                                    password.length < 6 ->
                                                        errorMessage = "Password must be at least 6 characters."
                                                    password != confirmPwd ->
                                                        errorMessage = "Passwords do not match."
                                                    else -> sendEmailOtp()
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
                                                Icon(Icons.Default.Send, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text("Send Verification Code", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(18.dp))
                                    OrDivider()
                                    Spacer(Modifier.height(14.dp))

                                    // Google Sign-Up
                                    GoogleSignInButton(
                                        text    = "Sign up with Google",
                                        enabled = !isLoading
                                    ) {
                                        googleClient.signOut().addOnCompleteListener {
                                            googleLauncher.launch(googleClient.signInIntent)
                                        }
                                    }

                                    Spacer(Modifier.height(20.dp))

                                    Row(
                                        modifier              = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment     = Alignment.CenterVertically
                                    ) {
                                        Text("Already have an account? ", color = SubText, fontSize = 13.sp)
                                        TextButton(onClick = { navController.popBackStack() }, contentPadding = PaddingValues(0.dp)) {
                                            Text("Login", color = Color(0xFF00BCD4), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ─────────────────────────────────────────────────────────
                    //  STAGE 2 — Email OTP
                    // ─────────────────────────────────────────────────────────
                    SignUpStage.OTP_VERIFY -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(Modifier.height(24.dp))
                            Card(
                                shape    = RoundedCornerShape(28.dp),
                                colors   = CardDefaults.cardColors(containerColor = Color(0xFF111D2B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier            = Modifier.padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp).clip(CircleShape)
                                            .background(Brush.radialGradient(listOf(Color(0xFF00BCD4).copy(alpha = 0.20f), Color.Transparent))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.MarkEmailRead, null, tint = Color(0xFF00BCD4), modifier = Modifier.size(40.dp))
                                    }
                                    Spacer(Modifier.height(16.dp))
                                    Text("Check Your Email", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnDarkText)
                                    Spacer(Modifier.height(8.dp))
                                    Text("We sent a 6-digit code to", fontSize = 13.sp, color = SubText, textAlign = TextAlign.Center)
                                    Text(email.trim(), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF00BCD4), textAlign = TextAlign.Center)
                                    Spacer(Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Timer, null, tint = SubText, modifier = Modifier.size(13.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Expires in 5 min  •  $attemptsLeft attempt${if (attemptsLeft == 1) "" else "s"} left", color = SubText, fontSize = 11.sp)
                                    }
                                    Spacer(Modifier.height(20.dp))

                                    OutlinedTextField(
                                        value           = otpCode,
                                        onValueChange   = { if (it.length <= 6 && it.all { c -> c.isDigit() }) { otpCode = it; errorMessage = "" } },
                                        label           = { Text("6-digit code") },
                                        placeholder     = { Text("••••••", color = SubText) },
                                        leadingIcon     = { Icon(Icons.Default.Lock, null, tint = Color(0xFF00BCD4)) },
                                        singleLine      = true,
                                        isError         = errorMessage.isNotEmpty(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                        shape           = RoundedCornerShape(14.dp),
                                        colors          = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor      = Color(0xFF00BCD4),
                                            unfocusedBorderColor    = Color(0xFF1E3048),
                                            errorBorderColor        = ErrorRed,
                                            focusedTextColor        = OnDarkText,
                                            unfocusedTextColor      = OnDarkText,
                                            cursorColor             = Color(0xFF00BCD4),
                                            focusedContainerColor   = Color(0xFF0A1622),
                                            unfocusedContainerColor = Color(0xFF0A1622)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    AnimatedVisibility(visible = errorMessage.isNotEmpty()) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                                            Icon(Icons.Default.Warning, null, tint = ErrorRed, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(errorMessage, color = ErrorRed, fontSize = 12.sp)
                                        }
                                    }

                                    Spacer(Modifier.height(20.dp))

                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(52.dp).background(
                                            if (otpCode.length == 6 && !OtpSecurityManager.isLocked()) btnGradient
                                            else Brush.horizontalGradient(listOf(Color(0xFFBBCCDD), Color(0xFFBBCCDD))),
                                            RoundedCornerShape(14.dp)
                                        )
                                    ) {
                                        Button(
                                            onClick   = { verifyAndCreate() },
                                            modifier  = Modifier.fillMaxSize(),
                                            enabled   = otpCode.length == 6 && !isLoading && !OtpSecurityManager.isLocked(),
                                            colors    = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                            elevation = ButtonDefaults.buttonElevation(0.dp)
                                        ) {
                                            if (isLoading) {
                                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                            } else {
                                                Icon(Icons.Default.Verified, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text("Verify & Create Account", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(14.dp))

                                    Row(
                                        modifier              = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment     = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = { stage = SignUpStage.FORM; otpCode = ""; errorMessage = ""; OtpSecurityManager.clearSession() }) {
                                            Icon(Icons.Default.ArrowBack, null, tint = SubText, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Change Email", color = SubText, fontSize = 12.sp)
                                        }

                                        val canResend = resendCooldown == 0 && OtpSecurityManager.resendCount() < OtpSecurityManager.maxResends()
                                        Box(
                                            modifier = Modifier.height(36.dp).background(
                                                if (canResend) Brush.horizontalGradient(listOf(Color(0xFF00BCD4), Color(0xFF006D7E)))
                                                else Brush.horizontalGradient(listOf(Color(0xFF1E3048), Color(0xFF1E3048))),
                                                RoundedCornerShape(10.dp)
                                            )
                                        ) {
                                            Button(
                                                onClick   = { sendEmailOtp() },
                                                enabled   = canResend && !isLoading,
                                                modifier  = Modifier.fillMaxSize(),
                                                colors    = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                                elevation = ButtonDefaults.buttonElevation(0.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp)
                                            ) {
                                                Text(
                                                    when {
                                                        OtpSecurityManager.resendCount() >= OtpSecurityManager.maxResends() -> "Max resends reached"
                                                        resendCooldown > 0 -> "Resend in ${resendCooldown}s"
                                                        else               -> "Resend Code"
                                                    },
                                                    color      = if (canResend) Color.White else SubText,
                                                    fontSize   = 12.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ─────────────────────────────────────────────────────────
                    //  STAGE 3 — Creating account
                    // ─────────────────────────────────────────────────────────
                    SignUpStage.CREATING -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 60.dp)
                        ) {
                            CircularProgressIndicator(color = Color(0xFF00BCD4), modifier = Modifier.size(52.dp), strokeWidth = 4.dp)
                            Spacer(Modifier.height(20.dp))
                            Text("Creating your account…", color = OnDarkText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Text("Just a moment ✨", color = SubText, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

// ── Shared field colours ──────────────────────────────────────────────────────
@Composable
private fun signupFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = Color(0xFF00BCD4),
    unfocusedBorderColor    = Color(0xFF1E3048),
    focusedTextColor        = OnDarkText,
    unfocusedTextColor      = OnDarkText,
    cursorColor             = Color(0xFF00BCD4),
    focusedContainerColor   = Color(0xFF0A1622),
    unfocusedContainerColor = Color(0xFF0A1622)
)
