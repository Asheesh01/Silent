package com.example.voiceresponder.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.voiceresponder.ui.theme.PersonalizedVoiceResponderTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // If user is NOT signed in, ignore saved nav state so they always
        // start fresh at splash → login instead of being restored to dashboard
        val isSignedIn = FirebaseAuth.getInstance().currentUser != null
        super.onCreate(if (isSignedIn) savedInstanceState else null)

        setContent {
            PersonalizedVoiceResponderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val auth          = remember { FirebaseAuth.getInstance() }

    // ── Global auth guard ─────────────────────────────────────────────────────
    // Listens for sign-out events at any point (including token expiry).
    // If no user is authenticated, immediately force back to login and clear
    // the entire back stack — no matter which screen the user is currently on.
    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser == null) {
                val currentRoute = navController.currentBackStackEntry?.destination?.route
                // Only redirect from protected screens — don't interfere with
                // the initial splash → login unauthenticated flow
                if (currentRoute != null &&
                    currentRoute != "login" &&
                    currentRoute != "signup" &&
                    currentRoute != "splash"
                ) {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash")      { SplashScreen(navController) }
        composable("login")       { LoginScreen(navController) }
        composable("signup")      { SignUpScreen(navController) }
        composable("setup_phone") { SetupPhoneScreen(navController) }
        composable("dashboard")   { DashboardScreen(navController) }
        composable("contacts")    { ContactListScreen(navController) }
        composable("record")      { RecordAudioScreen(navController) }
        composable("settings")    { SettingsScreen(navController) }
        composable("onboarding")  { OnboardingScreen(navController) }
    }
}
