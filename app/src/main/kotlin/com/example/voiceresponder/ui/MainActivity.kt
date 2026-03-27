package com.example.voiceresponder.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.voiceresponder.ui.theme.PersonalizedVoiceResponderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash")      { SplashScreen(navController) }
        composable("login")       { LoginScreen(navController) }
        composable("signup")      { SignUpScreen(navController) }
        composable("setup_phone") { SetupPhoneScreen(navController) }
        composable("dashboard")   { DashboardScreen(navController) }
        composable("contacts")    { ContactListScreen(navController) }
        composable("record")      { RecordAudioScreen(navController) }
        composable("settings")    { SettingsScreen(navController) }
        composable("onboarding")   { OnboardingScreen(navController) }
    }
}

