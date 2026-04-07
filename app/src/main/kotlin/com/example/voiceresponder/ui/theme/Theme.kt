package com.example.voiceresponder.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// -- Replora colour palette --------------------------------------------──
val Teal400       = Color(0xFF00ACC1)   // vibrant cyan-teal
val Teal600       = Color(0xFF00838F)   // deeper teal for gradients
val DarkBg        = Color(0xFFF2F6FF)   // soft cool-white page background (original)
val DarkSurface   = Color(0xFFE4EEFF)   // subtle blue-tinted surface (original)
val DarkCard      = Color(0xFFF5F9FF)   // premium frosted ice-blue cards (original)
val CardInput     = Color(0xFFD6E8FF)   // teal-blue tint for input fields (kept)
val OnDarkText    = Color(0xFF0D1B3E)   // deep navy for titles
val SubText       = Color(0xFF5B7399)   // muted blue-grey subtitle
val ErrorRed      = Color(0xFFD32F2F)
val SelectedGreen = Color(0xFF2E7D32)

// ── Extra accent colours used in gradients ───────────────────────────────────
val AccentPurple  = Color(0xFF7C4DFF)   // purple gradient stop
val AccentTeal    = Color(0xFF00BCD4)   // bright teal gradient stop

private val SilentLightScheme = lightColorScheme(
    primary            = Teal400,
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFB2EBF2),
    onPrimaryContainer = Color(0xFF002B30),
    secondary          = Teal600,
    onSecondary        = Color.White,
    tertiary           = AccentPurple,
    background         = DarkBg,
    surface            = DarkSurface,
    surfaceVariant     = DarkCard,
    onBackground       = OnDarkText,
    onSurface          = OnDarkText,
    onSurfaceVariant   = SubText,
    error              = ErrorRed
)

@Composable
fun PersonalizedVoiceResponderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SilentLightScheme,
        typography  = MaterialTheme.typography,
        content     = content
    )
}
