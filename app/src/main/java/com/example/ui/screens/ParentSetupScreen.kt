package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.SyncRepository
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentSetupScreen(
    onPaired: () -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    var manualCode by remember { mutableStateOf("") }
    
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            SyncRepository.setPaired(true)
            Toast.makeText(context, "Paired successfully via QR Code!", Toast.LENGTH_SHORT).show()
            onPaired()
        }
    }

    fun pairWithCode() {
        if (manualCode.length == 10) {
            SyncRepository.setPaired(true)
            Toast.makeText(context, "Paired successfully with code!", Toast.LENGTH_SHORT).show()
            onPaired()
        } else {
            Toast.makeText(context, "Please enter a valid 10-digit code", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parent Setup", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = "Scan QR Code",
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Pair with Child's Device",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Scan the QR code displayed on the child's device or enter the 10-digit code to connect.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = manualCode,
                onValueChange = { if (it.length <= 10) manualCode = it.filter { char -> char.isDigit() } },
                label = { Text("10-Digit Code") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { pairWithCode() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = manualCode.length == 10
            ) {
                Icon(Icons.Default.VpnKey, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pair with Code")
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text("OR", color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val options = ScanOptions()
                    options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                    options.setPrompt("Scan the child's QR Code")
                    options.setBeepEnabled(true)
                    options.setBarcodeImageEnabled(false)
                    scanLauncher.launch(options)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan QR Code")
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onSkip) {
                Text("Skip for now")
            }
        }
    }
}
