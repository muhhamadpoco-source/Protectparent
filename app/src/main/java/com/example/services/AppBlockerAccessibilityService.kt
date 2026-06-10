package com.example.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.example.data.SyncRepository

class AppBlockerAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            val blockedApps = SyncRepository.blockedApps.value
            if (blockedApps.contains(packageName)) {
                // If the app is in the blocked list, send them home and show a toast
                performGlobalAction(GLOBAL_ACTION_HOME)
                Toast.makeText(this, "ProtectParent: Access to $packageName is blocked.", Toast.LENGTH_SHORT).show()
                
                // Alternatively, we could launch a custom "BlockedActivity"
            }
        }
    }

    override fun onInterrupt() {
        // Handle interruption
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        this.serviceInfo = info
    }
}
