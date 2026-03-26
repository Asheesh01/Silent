package com.example.voiceresponder.ui

import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.voiceresponder.audio.AudioRecorder
import com.example.voiceresponder.remote.CloudinaryHelper
import com.example.voiceresponder.remote.FirebaseHelper
import com.example.voiceresponder.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RecordAudioScreen(navController: NavController) {
    var isRecording   by remember { mutableStateOf(false) }
    var isPlaying     by remember { mutableStateOf(false) }
    var showDeleteDlg by remember { mutableStateOf(false) }
    var fileExists    by remember { mutableStateOf(false) }

    val context     = LocalContext.current
    val audioFile   = remember { File(context.filesDir, "default_response.mp4") }
    val recorder    = remember { AudioRecorder(context) }
    val scope       = rememberCoroutineScope()
    val cloudHelper = remember { CloudinaryHelper() }
    val fbHelper    = remember { FirebaseHelper() }
    val uid         = remember { FirebaseAuth.getInstance().currentUser?.uid }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // Refresh file status whenever we come back to this state
    LaunchedEffect(isRecording) {
        fileExists = audioFile.exists()
    }

    DisposableEffect(Unit) {
        fileExists = audioFile.exists()
        onDispose { mediaPlayer?.release() }
    }

    // Pulse animation for recording button
    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val bgGradient = Brush.verticalGradient(listOf(DarkBg, DarkSurface))

    // ── Delete confirmation dialog ──────────────────────────────────────────
    if (showDeleteDlg) {
        AlertDialog(
            onDismissRequest   = { showDeleteDlg = false },
            containerColor     = DarkCard,
            title              = { Text("Delete Recording?", color = OnDarkText) },
            text               = { Text("This will permanently delete your voice response.", color = SubText) },
            confirmButton = {
                TextButton(onClick = {
                    mediaPlayer?.release()
                    mediaPlayer = null
                    isPlaying   = false
                    audioFile.delete()
                    fileExists    = false
                    showDeleteDlg = false
                    Toast.makeText(context, "Recording deleted", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Delete", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDlg = false }) {
                    Text("Cancel", color = SubText)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(12.dp))

            // Back arrow + title row
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = OnDarkText)
                }
                Text(
                    "Voice Response",
                    fontWeight = FontWeight.Bold,
                    color      = OnDarkText,
                    fontSize   = 20.sp
                )
            }

            Spacer(Modifier.height(48.dp))

            // ── Big pulse record button ──
            Box(contentAlignment = Alignment.Center) {
                // Outer pulse ring (only while recording)
                if (isRecording) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(ErrorRed.copy(alpha = 0.2f))
                    )
                }
                // Inner button
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                if (isRecording) listOf(ErrorRed, Color(0xFF880000))
                                else             listOf(Teal400,  Teal600)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            if (isRecording) {
                                recorder.stopRecording()
                                isRecording = false
                                fileExists  = true
                                Toast.makeText(context, "Recording saved!", Toast.LENGTH_SHORT).show()
                                // Upload to Cloudinary + save URL to Firestore for cross-device restore
                                scope.launch {
                                    val result = withContext(Dispatchers.IO) {
                                        cloudHelper.uploadAudio(audioFile)
                                    }
                                    if (result != null && uid != null) {
                                        withContext(Dispatchers.IO) { fbHelper.saveAudioUrl(uid, result.url) }
                                    }
                                }
                            } else {
                                if (!isPlaying) {
                                    recorder.startRecording(audioFile)
                                    isRecording = true
                                }
                            }
                        },
                        modifier = Modifier.size(120.dp),
                        enabled  = !isPlaying
                    ) {
                        Icon(
                            if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = null,
                            tint     = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                when {
                    isRecording -> "Recording… tap to stop"
                    isPlaying   -> "Playing back…"
                    else        -> "Tap to record your message"
                },
                color    = SubText,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(40.dp))

            // ── Audio file card (appears when recording exists) ──
            if (fileExists && !isRecording) {
                val fileSizeKb = (audioFile.length() / 1024).toInt()

                Card(
                    shape  = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier             = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment    = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // File icon + info
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Teal400.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AudioFile,
                                    contentDescription = null,
                                    tint     = Teal400,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Voice Response",
                                    color      = OnDarkText,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize   = 14.sp
                                )
                                Text(
                                    "${fileSizeKb} KB  •  default_response.mp4",
                                    color    = SubText,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Action buttons
                        Row {
                            // Play / Stop
                            IconButton(onClick = {
                                if (isPlaying) {
                                    mediaPlayer?.stop()
                                    mediaPlayer?.release()
                                    mediaPlayer = null
                                    isPlaying   = false
                                } else {
                                    try {
                                        mediaPlayer = MediaPlayer().apply {
                                            setDataSource(audioFile.absolutePath)
                                            prepare()
                                            start()
                                            isPlaying = true
                                            setOnCompletionListener {
                                                isPlaying   = false
                                                mediaPlayer = null
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error playing audio", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }) {
                                Icon(
                                    if (isPlaying) Icons.Default.Stop else Icons.Default.PlayCircle,
                                    contentDescription = if (isPlaying) "Stop" else "Play",
                                    tint = Teal400,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            // Delete
                            IconButton(onClick = { showDeleteDlg = true }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete recording",
                                    tint = ErrorRed,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
