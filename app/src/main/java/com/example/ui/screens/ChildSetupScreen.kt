package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.QRCodeHelper
import com.example.data.SyncRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildSetupScreen() {
    val context = LocalContext.current
    var qrCodeBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var pairingCode by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        val randomCode = (100000000L..9999999999L).random().toString().padStart(10, '0')
        pairingCode = randomCode
        qrCodeBitmap = QRCodeHelper.generateQRCode("protectparent://pair?id=$randomCode")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set Up Child Device", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        val paired by SyncRepository.paired.collectAsState()

        if (paired) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Protected",
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Device is Protected",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "This device is paired with the parent device. Activity is being monitored.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Simulate responding to requests locally
            val cameraRequest = SyncRepository.cameraRequest.collectAsState()
            val audioRequest = SyncRepository.audioRequest.collectAsState()
            val screenRequest = SyncRepository.screenRequest.collectAsState()

            LaunchedEffect(cameraRequest.value) {
                if (cameraRequest.value) {
                    kotlinx.coroutines.delay(2000)
                    // Generate a simple 1x1 black image in base64 to simulate
                    SyncRepository.completeCameraSnapshot("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=")
                }
            }

            LaunchedEffect(audioRequest.value) {
                if (audioRequest.value) {
                    kotlinx.coroutines.delay(2000)
                    SyncRepository.completeAudioClip("dummy_audio_base64")
                }
            }

            LaunchedEffect(screenRequest.value) {
                if (screenRequest.value) {
                    kotlinx.coroutines.delay(2000)
                    SyncRepository.completeScreenSnapshot("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+ip1sAAAAASUVORK5CYII=") // White pixel
                }
            }

            // Simulate GPS
            LaunchedEffect(Unit) {
                while(true) {
                    kotlinx.coroutines.delay(5000)
                    SyncRepository.updateLocation(37.4221 + Math.random() * 0.01, -122.0841 + Math.random() * 0.01)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                "Step 1: Scan QR or Enter Code",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Use the Parent's phone to scan this code or enter the 10-digit code below to link the devices.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (qrCodeBitmap != null) {
                Image(
                    bitmap = qrCodeBitmap!!.asImageBitmap(),
                    contentDescription = "Pairing QR Code",
                    modifier = Modifier.size(200.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("OR ENTER CODE:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = pairingCode,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // For prototype testing: allow the child device to manually transition to the 
                // protected state without a real backend server.
                Button(
                    onClick = { SyncRepository.setPaired(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("Simulate Connection (Prototype Testing)")
                }
            } else {
                CircularProgressIndicator(modifier = Modifier.padding(64.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                "Step 2: Grant Permissions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "The following permissions are required for ProtectParent features to work locally.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Notifications Access Button
            Button(
                onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text("Grant Notification Access")
            }

            // Usage Stats Access Button
            Button(
                onClick = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text("Grant Usage Access (Screen Time)")
            }

            // Accessibility Access Button
            Button(
                onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text("Grant Accessibility (App Blocker)")
            }
            
            // Location Services
            Button(
                onClick = { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text("Enable Location Services")
            }

            // Microphone Access Button
            Button(
                onClick = { 
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text("Grant Microphone Access (App Info -> Permissions)")
            }

            // Contacts Access Button
            Button(
                onClick = { 
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text("Grant Contacts Access (App Info -> Permissions)")
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedCard(
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Active", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Once all permissions are granted and the parent device is paired via cloud database, monitoring will begin automatically.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        }
    }
}
