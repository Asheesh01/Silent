package com.example.voiceresponder.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.voiceresponder.ui.theme.*

// ── Slide data ────────────────────────────────────────────────────────────────

private data class OnboardingSlide(
    val icon: ImageVector,
    val iconTint: Color,
    val title: String,
    val description: String
)

private val slides = listOf(
    OnboardingSlide(
        icon        = Icons.Default.Mic,
        iconTint    = Color(0xFF00BCD4),
        title       = "Welcome to Zyntra 👋",
        description = "Zyntra automatically replies to missed calls with your personal voice message — so callers are never left wondering."
    ),
    OnboardingSlide(
        icon        = Icons.Default.RadioButtonChecked,
        iconTint    = Color(0xFFEF5350),
        title       = "🔴 Record Button",
        description = "Tap the red microphone on the Record tab. Speak your message, then tap Stop. Your voice is saved instantly and ready to go."
    ),
    OnboardingSlide(
        icon        = Icons.Default.Contacts,
        iconTint    = Color(0xFF7C4DFF),
        title       = "👥 Contacts Toggle",
        description = "Go to the Contacts tab and toggle ON the people you want to auto-reply to. Only selected contacts receive your voice message."
    ),
    OnboardingSlide(
        icon        = Icons.Default.ToggleOn,
        iconTint    = Color(0xFF00BCD4),
        title       = "⚡ Auto Responder Toggle",
        description = "On the Home screen, flip the Auto Responder switch to ON. The service runs silently in the background — no battery drain."
    ),
    OnboardingSlide(
        icon        = Icons.Default.Settings,
        iconTint    = Color(0xFFFFB300),
        title       = "⚙️ Settings & Feedback",
        description = "In Settings, manage permissions, read the how-to guide, and send us feedback. We read every suggestion."
    )
)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(navController: NavController) {
    var currentPage by remember { mutableIntStateOf(0) }

    val isLastPage = currentPage == slides.lastIndex
    val bgGradient = Brush.verticalGradient(listOf(DarkBg, DarkSurface))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // ── Top: dot indicators ──────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                slides.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (index == currentPage) 24.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == currentPage) Teal400
                                else Color(0xFFBBCCDD)
                            )
                    )
                }
            }

            // ── Middle: slide content ────────────────────────────────────────
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 4 } togetherWith
                    fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { -it / 4 }
                },
                label = "slide"
            ) { page ->
                val s = slides[page]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(s.iconTint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = s.icon,
                            contentDescription = null,
                            tint               = s.iconTint,
                            modifier           = Modifier.size(52.dp)
                        )
                    }

                    Text(
                        text       = s.title,
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = OnDarkText,
                        textAlign  = TextAlign.Center
                    )

                    Text(
                        text       = s.description,
                        fontSize   = 14.sp,
                        color      = SubText,
                        textAlign  = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }

            // ── Bottom: Next / Get Started ───────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(
                        Brush.horizontalGradient(listOf(Teal400, Color(0xFF006064))),
                        shape = RoundedCornerShape(14.dp)
                    )
            ) {
                Button(
                    onClick = {
                        if (isLastPage) {
                            navController.navigate("dashboard") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        } else {
                            currentPage++
                        }
                    },
                    modifier  = Modifier.fillMaxSize(),
                    colors    = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        if (isLastPage) "🚀  Get Started" else "Next  →",
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp
                    )
                }
            }
        }
    }
}
