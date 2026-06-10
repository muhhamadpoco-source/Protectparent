package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.SyncRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllFeaturesScreen(onBack: () -> Unit) {
    val location by SyncRepository.childLocation.collectAsState()
    val blockedApps by SyncRepository.blockedApps.collectAsState()
    val wifiInfo by SyncRepository.wifiInfo.collectAsState()
    val contacts by SyncRepository.contacts.collectAsState()
    var packageNameToBlock by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Features") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Feature: Power Controls
                Text("Remote Power Controls", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { /* In production: send remote command */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Shutdown")
                    }
                    Button(
                        onClick = { /* In production: send remote command */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                    ) {
                        Text("Restart")
                    }
                }
            }

            item {
                // Feature: GPS Location
                Text("GPS Tracking", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            if (location.first == 0.0 && location.second == 0.0) {
                                Text("Awaiting location data...", style = MaterialTheme.typography.bodyMedium)
                            } else {
                                Text("Lat: ${location.first}", style = MaterialTheme.typography.bodyMedium)
                                Text("Lng: ${location.second}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            item {
                // Feature: Wi-Fi Info
                Text("Wi-Fi Connection", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Wifi, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(wifiInfo, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            item {
                // Feature: App Blocker
                Text("App Blocker", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = packageNameToBlock,
                        onValueChange = { packageNameToBlock = it },
                        label = { Text("App Package (e.g. com.facebook.katana)") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (packageNameToBlock.isNotBlank()) {
                            SyncRepository.addBlockedApp(packageNameToBlock.trim())
                            packageNameToBlock = ""
                        }
                    }) {
                        Text("Block")
                    }
                }
            }

            items(blockedApps.toList()) { app ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Block, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(app)
                        }
                        TextButton(onClick = { SyncRepository.removeBlockedApp(app) }) {
                            Text("Unblock", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            item {
                // Feature: Contacts
                Text("Contacts", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                if (contacts.isEmpty()) {
                    Text("No contacts tracked or permission denied.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            items(contacts) { contact ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Contacts, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(contact.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(contact.phoneNumber, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
