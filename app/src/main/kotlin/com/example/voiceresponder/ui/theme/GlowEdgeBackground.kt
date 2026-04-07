package com.example.voiceresponder.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ── Edge glow palette ─────────────────────────────────────────────────────────
private val TopGlow    = Color(0x1800E5FF)   // cyan-teal   (top edge)    ~9% opacity
private val BottomGlow = Color(0x187C4DFF)   // violet       (bottom edge) ~9% opacity
private val LeftGlow   = Color(0x14E040FB)   // purple-pink  (left edge)   ~8% opacity
private val RightGlow  = Color(0x1400BCD4)   // teal         (right edge)  ~8% opacity
private val Clear      = Color(0x00000000)

/**
 * Modifier extension — add to any Box/Surface/Scaffold modifier to overlay
 * four radial edge glows ON TOP of all drawn content.
 * Each glow is brightest at the midpoint of its edge, fading toward corners.
 *
 * Usage:
 *   Box(modifier = Modifier.fillMaxSize().background(gradient).drawEdgeGlows()) { ... }
 *   Scaffold(modifier = Modifier.fillMaxSize().drawEdgeGlows()) { ... }
 */
fun Modifier.drawEdgeGlows(): Modifier = this.drawWithContent {
    drawContent()               // draw everything first (background + children)

    val w = size.width
    val h = size.height

    // Top edge — teal radial centred at (w/2, 0)
    drawCircle(
        brush  = Brush.radialGradient(listOf(TopGlow, Clear), Offset(w / 2f, 0f), w * 0.75f),
        radius = w * 0.75f,
        center = Offset(w / 2f, 0f)
    )
    // Bottom edge — violet radial centred at (w/2, h)
    drawCircle(
        brush  = Brush.radialGradient(listOf(BottomGlow, Clear), Offset(w / 2f, h), w * 0.75f),
        radius = w * 0.75f,
        center = Offset(w / 2f, h)
    )
    // Left edge — purple-pink radial centred at (0, h/2)
    drawCircle(
        brush  = Brush.radialGradient(listOf(LeftGlow, Clear), Offset(0f, h / 2f), h * 0.52f),
        radius = h * 0.52f,
        center = Offset(0f, h / 2f)
    )
    // Right edge — teal radial centred at (w, h/2)
    drawCircle(
        brush  = Brush.radialGradient(listOf(RightGlow, Clear), Offset(w, h / 2f), h * 0.52f),
        radius = h * 0.52f,
        center = Offset(w, h / 2f)
    )
}

// ── Legacy wrappers (kept for compatibility) ──────────────────────────────────

@Composable
fun GlowEdgeBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize().drawEdgeGlows()) {
        content()
    }
}

@Composable
fun BoxScope.EdgeGlowOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawCircle(
            brush  = Brush.radialGradient(listOf(TopGlow, Clear), Offset(w / 2f, 0f), w * 0.75f),
            radius = w * 0.75f, center = Offset(w / 2f, 0f)
        )
        drawCircle(
            brush  = Brush.radialGradient(listOf(BottomGlow, Clear), Offset(w / 2f, h), w * 0.75f),
            radius = w * 0.75f, center = Offset(w / 2f, h)
        )
        drawCircle(
            brush  = Brush.radialGradient(listOf(LeftGlow, Clear), Offset(0f, h / 2f), h * 0.52f),
            radius = h * 0.52f, center = Offset(0f, h / 2f)
        )
        drawCircle(
            brush  = Brush.radialGradient(listOf(RightGlow, Clear), Offset(w, h / 2f), h * 0.52f),
            radius = h * 0.52f, center = Offset(w, h / 2f)
        )
    }
}
