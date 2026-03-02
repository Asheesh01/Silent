package com.example.voiceresponder.ui

import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.voiceresponder.audio.AudioRecorder
import java.io.File

@Composable
fun RecordAudioScreen(navController: NavController) {
    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val audioFile = remember { File(context.filesDir, "default_response.mp4") }
    val recorder = remember { AudioRecorder(context) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Record Response Message", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(64.dp))

        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            FilledIconButton(
                onClick = {
                    if (isRecording) {
                        recorder.stopRecording()
                        Toast.makeText(context, "Recording Saved", Toast.LENGTH_SHORT).show()
                    } else {
                        recorder.startRecording(audioFile)
                    }
                    isRecording = !isRecording 
                },
                modifier = Modifier.size(120.dp),
                enabled = !isPlaying
            ) {
                Icon(
                    if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(if (isRecording) "Recording..." else "Tap to record")

        if (!isRecording && audioFile.exists()) {
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    if (isPlaying) {
                        mediaPlayer?.stop()
                        mediaPlayer?.release()
                        mediaPlayer = null
                        isPlaying = false
                    } else {
                        try {
                            mediaPlayer = MediaPlayer().apply {
                                setDataSource(audioFile.absolutePath)
                                prepare()
                                start()
                                isPlaying = true
                                setOnCompletionListener {
                                    isPlaying = false
                                    mediaPlayer = null
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error playing audio", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            ) {
                Icon(if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isPlaying) "Stop Playback" else "Play Recording")
            }
        }
    }
}

