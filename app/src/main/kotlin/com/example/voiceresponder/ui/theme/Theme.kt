package com.example.voiceresponder.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Silent Mode colour palette ──────────────────────────────────────────────
val Teal400      = Color(0xFF00BCD4)
val Teal600      = Color(0xFF00838F)
val DarkBg       = Color(0xFF0F172A)
val DarkSurface  = Color(0xFF1E293B)
val DarkCard     = Color(0xFF1E2D45)
val OnDarkText   = Color(0xFFECEFF1)
val SubText      = Color(0xFF94A3B8)
val ErrorRed     = Color(0xFFCF6679)
val SelectedGreen = Color(0xFF00E676)

private val SilentDarkScheme = darkColorScheme(
    primary          = Teal400,
    onPrimary        = Color(0xFF003040),
    primaryContainer = Color(0xFF004D5C),
    onPrimaryContainer = OnDarkText,
    secondary        = Teal600,
    onSecondary      = Color.White,
    tertiary         = Color(0xFF7C4DFF),
    background       = DarkBg,
    surface          = DarkSurface,
    surfaceVariant   = DarkCard,
    onBackground     = OnDarkText,
    onSurface        = OnDarkText,
    onSurfaceVariant = SubText,
    error            = ErrorRed
)

@Composable
fun PersonalizedVoiceResponderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SilentDarkScheme,
        typography  = MaterialTheme.typography,
        content     = content
    )
}
