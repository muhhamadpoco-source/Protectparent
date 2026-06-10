package com.example.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SyncRepository {
    // This is a facade for a real-time cloud database (like Firebase or Supabase).
    // In a production environment spanning two different devices, this state would
    // be synchronized via the cloud backend. Here, it acts locally to demonstrate
    // the functional architecture.
    
    private val _paired = MutableStateFlow(false)
    val paired: StateFlow<Boolean> = _paired.asStateFlow()

    private val _childLocation = MutableStateFlow(Pair(0.0, 0.0))
    val childLocation: StateFlow<Pair<Double, Double>> = _childLocation.asStateFlow()

    private val _blockedApps = MutableStateFlow<Set<String>>(emptySet())
    val blockedApps: StateFlow<Set<String>> = _blockedApps.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()
    
    private val _screenTime = MutableStateFlow<Map<String, Long>>(emptyMap())
    val screenTime: StateFlow<Map<String, Long>> = _screenTime.asStateFlow()

    private val _cameraTorchEnabled = MutableStateFlow(false)
    val cameraTorchEnabled: StateFlow<Boolean> = _cameraTorchEnabled.asStateFlow()

    private val _cameraRequest = MutableStateFlow(false)
    val cameraRequest: StateFlow<Boolean> = _cameraRequest.asStateFlow()
    
    private val _cameraImageBase64 = MutableStateFlow<String?>(null)
    val cameraImageBase64: StateFlow<String?> = _cameraImageBase64.asStateFlow()

    fun setPaired(isPaired: Boolean) {
        _paired.value = isPaired
    }

    fun updateLocation(lat: Double, lng: Double) {
        _childLocation.value = Pair(lat, lng)
    }

    fun addBlockedApp(packageName: String) {
        _blockedApps.value = _blockedApps.value + packageName
    }

    fun removeBlockedApp(packageName: String) {
        _blockedApps.value = _blockedApps.value - packageName
    }

    fun addNotification(item: NotificationItem) {
        _notifications.value = listOf(item) + _notifications.value
    }

    fun updateScreenTime(data: Map<String, Long>) {
        _screenTime.value = data
    }
    
    fun setTorchEnabled(enabled: Boolean) {
        _cameraTorchEnabled.value = enabled
    }

    fun requestCameraSnapshot() {
        _cameraRequest.value = true
    }

    fun completeCameraSnapshot(base64Image: String) {
        _cameraImageBase64.value = base64Image
        _cameraRequest.value = false
    }
}

data class NotificationItem(
    val id: String,
    val appName: String,
    val title: String,
    val text: String,
    val timestamp: Long
)
