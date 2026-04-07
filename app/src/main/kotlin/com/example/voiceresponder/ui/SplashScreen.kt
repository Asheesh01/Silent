@file:Suppress("UNUSED_VALUE", "SpellCheckingInspection")
package com.example.voiceresponder.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.voiceresponder.R
import com.example.voiceresponder.ui.theme.drawEdgeGlows
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.cos
import kotlin.math.sin

// ── Floating background dots ──────────────────────────────────────────────────
private data class FloatDot(val x: Float, val y: Float, val size: Dp, val alpha: Float)

private val splashDots = listOf(
    FloatDot(0.08f, 0.10f, 5.dp,  0.40f),
    FloatDot(0.82f, 0.07f, 4.dp,  0.35f),
    FloatDot(0.70f, 0.20f, 3.dp,  0.30f),
    FloatDot(0.05f, 0.48f, 4.dp,  0.40f),
    FloatDot(0.90f, 0.42f, 5.dp,  0.30f),
    FloatDot(0.22f, 0.68f, 3.dp,  0.35f),
    FloatDot(0.60f, 0.73f, 4.dp,  0.30f),
    FloatDot(0.88f, 0.75f, 3.dp,  0.35f),
    FloatDot(0.35f, 0.88f, 5.dp,  0.28f),
    FloatDot(0.12f, 0.83f, 4.dp,  0.38f),
    FloatDot(0.55f, 0.30f, 3.dp,  0.25f),
    FloatDot(0.45f, 0.55f, 4.dp,  0.30f),
)

// ── Particle data for burst effect ───────────────────────────────────────────
private data class Particle(val angleDeg: Float, val distance: Float, val size: Dp, val color: Color)

private val burstParticles = listOf(
    Particle(0f,   90f, 5.dp, Color(0xFF00E5FF)),
    Particle(30f,  80f, 4.dp, Color(0xFF4DD0E1)),
    Particle(60f,  95f, 6.dp, Color(0xFF80DEEA)),
    Particle(90f,  75f, 4.dp, Color(0xFF00BCD4)),
    Particle(120f, 85f, 5.dp, Color(0xFF00E5FF)),
    Particle(150f, 70f, 3.dp, Color(0xFF4DD0E1)),
    Particle(180f, 90f, 5.dp, Color(0xFF80DEEA)),
    Particle(210f, 80f, 4.dp, Color(0xFF00BCD4)),
    Particle(240f, 88f, 6.dp, Color(0xFF00E5FF)),
    Particle(270f, 72f, 4.dp, Color(0xFF4DD0E1)),
    Particle(300f, 92f, 5.dp, Color(0xFF80DEEA)),
    Particle(330f, 78f, 3.dp, Color(0xFF00BCD4)),
)

@Composable
fun SplashScreen(navController: NavController) {
    val auth = remember { FirebaseAuth.getInstance() }

    // ── Phase flags ───────────────────────────────────────────────────────────
    var logoVisible   by remember { mutableStateOf(false) }
    var burstVisible  by remember { mutableStateOf(false) }
    var textVisible   by remember { mutableStateOf(false) }

    // ── Logo entrance: scale + alpha (bouncy spring) ──────────────────────────
    val logoScale by animateFloatAsState(
        targetValue    = if (logoVisible) 1f else 0f,
        animationSpec  = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessLow
        ),
        label = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue   = if (logoVisible) 1f else 0f,
        animationSpec = tween(500),
        label         = "logoAlpha"
    )

    // ── Particle burst: radial expand + fade ──────────────────────────────────
    val burstProgress by animateFloatAsState(
        targetValue   = if (burstVisible) 1f else 0f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label         = "burst"
    )

    // ── Text entrance ─────────────────────────────────────────────────────────
    val textAlpha by animateFloatAsState(
        targetValue   = if (textVisible) 1f else 0f,
        animationSpec = tween(700),
        label         = "textAlpha"
    )
    val textOffset by animateFloatAsState(
        targetValue   = if (textVisible) 0f else 28f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label         = "textOffset"
    )

    // ── Continuous: outer ring rotation ──────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "continuous")
    val ringRotation by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 360f,
        animationSpec = infiniteRepeatable(
            animation  = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringRotation"
    )

    // ── Continuous: inner ring counter-rotation ───────────────────────────────
    val innerRingRotation by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = -360f,
        animationSpec = infiniteRepeatable(
            animation  = tween(4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "innerRingRotation"
    )

    // ── Continuous: glow pulse ────────────────────────────────────────────────
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.35f,
        targetValue   = 0.80f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1400, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // ── Continuous: mic icon pulse scale ─────────────────────────────────────
    val micPulse by infiniteTransition.animateFloat(
        initialValue  = 0.92f,
        targetValue   = 1.05f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "micPulse"
    )

    // ── Continuous: dot twinkle ───────────────────────────────────────────────
    val dotTwinkle by infiniteTransition.animateFloat(
        initialValue  = 0.5f,
        targetValue   = 1.0f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotTwinkle"
    )

    // ── Continuous: ripple wave expanding from logo ───────────────────────────
    val ripple1 by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple1"
    )
    val ripple2 by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1800, delayMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple2"
    )
    val ripple3 by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1800, delayMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple3"
    )

    // ── Continuous: logo circle hue shift ────────────────────────────────────
    val hueShift by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hueShift"
    )
    // Interpolate between teal and purple
    val logoColor1 = androidx.compose.ui.graphics.lerp(Color(0xFF80DEEA), Color(0xFFB39DDB), hueShift)
    val logoColor2 = androidx.compose.ui.graphics.lerp(Color(0xFF00BCD4), Color(0xFF7C4DFF), hueShift)

    // ── Navigation sequence ───────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        logoVisible  = true
        delay(350)
        burstVisible = true
        delay(500)
        textVisible  = true
        delay(1800)
        val destination = if (auth.currentUser != null) "dashboard" else "login"
        navController.navigate(destination) {
            popUpTo("splash") { inclusive = true }
        }
    }

    // ── Background ────────────────────────────────────────────────────────────
    val bg = Brush.verticalGradient(
        listOf(
            Color(0xFF0A1628),
            Color(0xFF0D1B3E),
            Color(0xFF111827),
        )
    )

    Box(
        modifier          = Modifier.fillMaxSize().background(bg).drawEdgeGlows(),
        contentAlignment  = Alignment.Center
    ) {

        // ── Canvas dots (twinkling background particles) ─────────────────────
        val density = LocalDensity.current
        Canvas(modifier = Modifier.fillMaxSize()) {
            splashDots.forEachIndexed { i, dot ->
                val twinkleOffset = if (i % 2 == 0) dotTwinkle else (1.5f - dotTwinkle)
                val alpha   = dot.alpha * twinkleOffset.coerceIn(0.1f, 1f)
                val radiusPx = with(density) { dot.size.toPx() / 2f }
                drawCircle(
                    color  = Color(0xFF00E5FF).copy(alpha = alpha),
                    radius = radiusPx,
                    center = Offset(size.width * dot.x, size.height * dot.y)
                )
            }
        }

        // ── Center column ─────────────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier            = Modifier.fillMaxWidth()
        ) {

            // ── Logo area ─────────────────────────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .scale(logoScale)
                    .alpha(logoAlpha)
            ) {

                // Particle burst (drawn relative to logo centre)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier.size(220.dp)
                ) {
                    burstParticles.forEach { p ->
                        val angleRad = Math.toRadians(p.angleDeg.toDouble()).toFloat()
                        val dist     = p.distance * burstProgress
                        val x        = (cos(angleRad) * dist).dp
                        val y        = (sin(angleRad) * dist).dp
                        Box(
                            modifier = Modifier
                                .offset(x = x, y = y)
                                .size(p.size)
                                .clip(CircleShape)
                                .alpha((1f - burstProgress * 0.6f).coerceIn(0f, 1f))
                                .background(p.color)
                        )
                    }
                }

                // Ripple waves
                listOf(ripple1, ripple2, ripple3).forEach { progress ->
                    val rippleSize    = (96.dp + (120.dp * progress))
                    val rippleAlpha  = (1f - progress) * 0.45f
                    Box(
                        modifier = Modifier
                            .size(rippleSize)
                            .clip(CircleShape)
                            .alpha(rippleAlpha)
                            .background(Color(0xFF00E5FF).copy(alpha = rippleAlpha))
                    )
                }

                // Outer ambient glow
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape)
                        .alpha(glowAlpha * 0.3f)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF00E5FF), Color.Transparent)
                            )
                        )
                )

                // Outer dashed rotating ring (Canvas-drawn arc dashes)
                Canvas(
                    modifier = Modifier
                        .size(150.dp)
                        .rotate(ringRotation)
                        .alpha(logoAlpha)
                ) {
                    val stroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    // Draw 8 arcs as "dashes"
                    for (i in 0 until 8) {
                        val startAngle = i * 45f
                        drawArc(
                            brush       = Brush.sweepGradient(
                                listOf(Color(0xFF00E5FF), Color(0xFF4DD0E1))
                            ),
                            startAngle  = startAngle,
                            sweepAngle  = 28f,
                            useCenter   = false,
                            style       = stroke
                        )
                    }
                }

                // Inner counter-rotating ring (solid thin ring)
                Canvas(
                    modifier = Modifier
                        .size(120.dp)
                        .rotate(innerRingRotation)
                        .alpha(logoAlpha * 0.7f)
                ) {
                    val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    for (i in 0 until 6) {
                        val startAngle = i * 60f
                        drawArc(
                            color      = Color(0xFF80DEEA),
                            startAngle = startAngle,
                            sweepAngle = 22f,
                            useCenter  = false,
                            style      = stroke
                        )
                    }
                }

                // Main icon circle (animated gradient background)
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    logoColor1,
                                    logoColor2,
                                    Color(0xFF006064),
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Microphone icon using the app's vector drawable
                    Icon(
                        imageVector        = ImageVector.vectorResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Microphone logo",
                        tint               = Color(0xFF0D1B3E),
                        modifier           = Modifier
                            .size(56.dp)
                            .scale(micPulse)
                    )
                }
            }

            Spacer(Modifier.height(44.dp))

            // ── App name ──────────────────────────────────────────────────────
            Text(
                text       = "Replora",
                fontSize   = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = Color.White,
                textAlign  = TextAlign.Center,
                modifier   = Modifier
                    .alpha(textAlpha)
                    .offset(y = textOffset.dp)
            )

            Spacer(Modifier.height(10.dp))

            // ── Tagline ───────────────────────────────────────────────────────
            Text(
                text      = "Smart auto-replies for missed calls",
                fontSize  = 14.sp,
                color     = Color(0xFF90A4AE),
                textAlign = TextAlign.Center,
                modifier  = Modifier
                    .alpha(textAlpha)
                    .offset(y = textOffset.dp)
                    .padding(horizontal = 32.dp)
            )

            Spacer(Modifier.height(20.dp))

            // ── Loading dots ──────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier              = Modifier
                    .alpha(textAlpha)
                    .offset(y = textOffset.dp)
            ) {
                listOf(0, 200, 400).forEachIndexed { idx, delayMs ->
                    val dotProgress by infiniteTransition.animateFloat(
                        initialValue  = 0.3f,
                        targetValue   = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation  = tween(600, delayMillis = delayMs, easing = EaseInOut),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "loadDot$idx"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .alpha(dotProgress)
                            .background(Color(0xFF00BCD4))
                    )
                }
            }
        }
    }
}
