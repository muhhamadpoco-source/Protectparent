package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme

import com.example.data.SyncRepository
import com.example.ui.navigation.Routes

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    SyncRepository.initialize(this)
    val startDest = when (SyncRepository.role) {
        "PARENT" -> Routes.PARENT_DASHBOARD
        "CHILD" -> Routes.CHILD_SETUP
        else -> Routes.ROLE_SELECTION
    }

    setContent {
      MyApplicationTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppNavigation(startDestination = startDest)
        }
      }
    }
  }
}
