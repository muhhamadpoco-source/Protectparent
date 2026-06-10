package com.example.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashlightOn
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Camera & Flashlight Control") },
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
                .padding(16.dp),
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

            Spacer(modifier = Modifier.height(24.dp))

            if (imageBase64 != null) {
                Text("Latest Snapshot:", style = MaterialTheme.typography.titleMedium)
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
                        contentDescription = "Snapshot",
                        modifier = Modifier.fillMaxWidth().height(300.dp)
                    )
                } else {
                    Text("Error displaying image")
                }
            } else {
                Text(
                    "No snapshot collected yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
