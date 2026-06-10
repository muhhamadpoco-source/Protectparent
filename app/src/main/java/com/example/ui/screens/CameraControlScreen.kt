package com.example.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.SyncRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraControlScreen(onBack: () -> Unit) {
    val torchEnabled by SyncRepository.cameraTorchEnabled.collectAsState()
    val cameraRequest by SyncRepository.cameraRequest.collectAsState()
    val imageBase64 by SyncRepository.cameraImageBase64.collectAsState()
    val audioRequest by SyncRepository.audioRequest.collectAsState()
    val audioBase64 by SyncRepository.audioFileBase64.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Camera & Media Control") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FlashlightOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Device Flashlight", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Switch(
                        checked = torchEnabled,
                        onCheckedChange = { SyncRepository.setTorchEnabled(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { SyncRepository.requestCameraSnapshot() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !cameraRequest
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (cameraRequest) "Requesting Snapshot..." else "Request Camera Snapshot")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { SyncRepository.requestAudioClip() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !audioRequest,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.Mic, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (audioRequest) "Recording Audio..." else "Request Audio Clip")
            }

            Spacer(modifier = Modifier.height(16.dp))

            val screenRequest by SyncRepository.screenRequest.collectAsState()
            val screenBase64 by SyncRepository.screenImageBase64.collectAsState()

            Button(
                onClick = { SyncRepository.requestScreenSnapshot() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !screenRequest,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (screenRequest) "Capturing Screen..." else "Request Screen Capture")
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (audioBase64 != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = MaterialTheme.colorScheme.onTertiaryContainer)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Audio clip captured.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
            }

            if (imageBase64 != null) {
                Text("Latest Camera Snapshot:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                val bitmap = androidx.compose.runtime.remember(imageBase64) {
                    try {
                        val bytes = Base64.decode(imageBase64, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } catch (e: Exception) {
                        null
                    }
                }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Camera Snapshot",
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    )
                } else {
                    Text("Error displaying image")
                }
            } else {
                Text(
                    "No camera snapshot collected yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (screenBase64 != null) {
                Text("Latest Screen Capture:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                val bitmap = androidx.compose.runtime.remember(screenBase64) {
                    try {
                        val bytes = Base64.decode(screenBase64, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } catch (e: Exception) {
                        null
                    }
                }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Screen Capture",
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    )
                } else {
                    Text("Error displaying screen")
                }
            } else {
                Text(
                    "No screen capture collected yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
