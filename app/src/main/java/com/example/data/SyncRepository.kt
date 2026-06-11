package com.example.data

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

object SyncRepository {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val client = OkHttpClient()

    var role: String = "UNKNOWN"
    var childCode: String = ""

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

    private val _audioRequest = MutableStateFlow(false)
    val audioRequest: StateFlow<Boolean> = _audioRequest.asStateFlow()

    private val _audioFileBase64 = MutableStateFlow<String?>(null)
    val audioFileBase64: StateFlow<String?> = _audioFileBase64.asStateFlow()

    private val _screenRequest = MutableStateFlow(false)
    val screenRequest: StateFlow<Boolean> = _screenRequest.asStateFlow()

    private val _screenImageBase64 = MutableStateFlow<String?>(null)
    val screenImageBase64: StateFlow<String?> = _screenImageBase64.asStateFlow()

    private val _wifiInfo = MutableStateFlow<String>("Unknown Setup")
    val wifiInfo: StateFlow<String> = _wifiInfo.asStateFlow()

    private val _contacts = MutableStateFlow<List<ContactItem>>(emptyList())
    val contacts: StateFlow<List<ContactItem>> = _contacts.asStateFlow()

    // Retrieve Firebase DB URL from BuildConfig (injected via Secrets)
    private val baseUrl = com.example.BuildConfig.FIREBASE_DB_URL

    private var prefs: android.content.SharedPreferences? = null

    fun initialize(context: android.content.Context) {
        prefs = context.getSharedPreferences("sync_prefs", android.content.Context.MODE_PRIVATE)
        val savedRole = prefs?.getString("role", null)
        if (savedRole != null) {
            role = savedRole
            if (role == "CHILD") {
                childCode = prefs?.getString("childCode", "") ?: ""
                _paired.value = prefs?.getBoolean("paired", false) ?: false
            }
        }
        
        scope.launch {
            while (true) {
                if (role == "CHILD" && childCode.isNotEmpty()) {
                    pullCommands()
                    if (_paired.value) {
                        // Push status to Firebase periodically
                        _childLocation.value = Pair(37.4221 + Math.random() * 0.01, -122.0841 + Math.random() * 0.01)
                        pushChildStatus()
                    }
                } else if (_paired.value && role == "PARENT" && childCode.isNotEmpty()) {
                    // Pull status from Firebase periodically
                    pullChildStatus()
                }
                delay(3000)
            }
        }
    }

    private fun getDbUrl(path: String) = if (baseUrl.endsWith("/")) "${baseUrl}${path}.json" else "${baseUrl}/${path}.json"

    private suspend fun putData(path: String, json: String): Boolean {
        if (!baseUrl.startsWith("http")) return false
        return kotlinx.coroutines.withContext(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url(getDbUrl(path))
                    .put(json.toRequestBody("application/json".toMediaType()))
                    .build()
                client.newCall(req).execute().use { it.isSuccessful }
            } catch (e: Exception) { false }
        }
    }

    private suspend fun patchData(path: String, json: String): Boolean {
        if (!baseUrl.startsWith("http")) return false
        return kotlinx.coroutines.withContext(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url(getDbUrl(path))
                    .patch(json.toRequestBody("application/json".toMediaType()))
                    .build()
                client.newCall(req).execute().use { it.isSuccessful }
            } catch (e: Exception) { false }
        }
    }

    private suspend fun getData(path: String): String? {
        if (!baseUrl.startsWith("http")) return null
        return kotlinx.coroutines.withContext(Dispatchers.IO) {
            try {
                val req = Request.Builder().url(getDbUrl(path)).get().build()
                client.newCall(req).execute().use { 
                    if (it.isSuccessful) it.body?.string() else null
                }
            } catch (e: Exception) { null }
        }
    }

    // Unchanged random IP address generator just for QR UI fallback
    fun getLocalIpAddress(): String? = "192.168.1.100"
    fun ipToCode(ip: String): String = (1000000000L..9999999999L).random().toString()
    fun codeToIp(code: String): String = code

    fun startChildServer() {
        role = "CHILD"
        childCode = (1000000000L..9999999999L).random().toString()
        _paired.value = false
        prefs?.edit()?.putString("role", "CHILD")?.putString("childCode", childCode)?.putBoolean("paired", false)?.apply()
        scope.launch {
            putData("rooms/$childCode/status", "{\"online\": true}")
            putData("rooms/$childCode/commands", "{}")
        }
    }

    suspend fun connectToChild(code: String): Boolean {
        role = "PARENT"
        prefs?.edit()?.putString("role", "PARENT")?.apply()
        childCode = code
        val res = getData("rooms/$childCode/status/online")
        if (res != null && res.contains("true")) {
            _paired.value = true
            patchData("rooms/$childCode/commands", "{\"isPaired\": true}")
            return true
        }
        return false
    }

    private suspend fun pushChildStatus() {
        val lat = _childLocation.value.first
        val lng = _childLocation.value.second
        patchData("rooms/$childCode/status", "{\"lat\": $lat, \"lng\": $lng}")
    }

    private suspend fun pullChildStatus() {
        val data = getData("rooms/$childCode/status") ?: return
        try {
            val json = JSONObject(data)
            if (json.has("lat") && json.has("lng")) {
                _childLocation.value = Pair(json.getDouble("lat"), json.getDouble("lng"))
            }
            if (json.has("cameraImage")) _cameraImageBase64.value = json.getString("cameraImage")
            if (json.has("screenImage")) _screenImageBase64.value = json.getString("screenImage")
            if (json.has("audioData")) _audioFileBase64.value = json.getString("audioData")
        } catch (e: Exception) {}
    }

    private suspend fun pullCommands() {
        val data = getData("rooms/$childCode/commands") ?: return
        try {
            val json = JSONObject(data)
            if (json.optBoolean("isPaired", false) && !_paired.value) {
                _paired.value = true
                prefs?.edit()?.putBoolean("paired", true)?.apply()
            }
            if (json.optBoolean("requestCamera", false)) {
                _cameraRequest.value = true
                delay(1500)
                completeCameraSnapshot("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=")
                patchData("rooms/$childCode/commands", "{\"requestCamera\": false}")
            }
            if (json.optBoolean("requestScreen", false)) {
                _screenRequest.value = true
                delay(1500)
                completeScreenSnapshot("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+ip1sAAAAASUVORK5CYII=")
                patchData("rooms/$childCode/commands", "{\"requestScreen\": false}")
            }
            if (json.optBoolean("requestAudio", false)) {
                _audioRequest.value = true
                delay(1500)
                completeAudioClip("dummy_audio_base64")
                patchData("rooms/$childCode/commands", "{\"requestAudio\": false}")
            }
        } catch (e: Exception) {}
    }

    fun setPaired(isPaired: Boolean) {
        _paired.value = isPaired
        prefs?.edit()?.putBoolean("paired", isPaired)?.apply()
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
        if (role == "PARENT") {
            _cameraRequest.value = true
            scope.launch { patchData("rooms/$childCode/commands", "{\"requestCamera\": true}") }
        }
    }

    fun completeCameraSnapshot(base64Image: String) {
        if (role == "CHILD") {
            scope.launch { patchData("rooms/$childCode/status", "{\"cameraImage\": \"$base64Image\"}") }
        }
        _cameraRequest.value = false
        _cameraImageBase64.value = base64Image
    }

    fun requestAudioClip() {
        if (role == "PARENT") {
            _audioRequest.value = true
            scope.launch { patchData("rooms/$childCode/commands", "{\"requestAudio\": true}") }
        }
    }

    fun completeAudioClip(base64Audio: String) {
        if (role == "CHILD") {
            scope.launch { patchData("rooms/$childCode/status", "{\"audioData\": \"$base64Audio\"}") }
        }
        _audioRequest.value = false
        _audioFileBase64.value = base64Audio
    }

    fun requestScreenSnapshot() {
        if (role == "PARENT") {
            _screenRequest.value = true
            scope.launch { patchData("rooms/$childCode/commands", "{\"requestScreen\": true}") }
        }
    }

    fun completeScreenSnapshot(base64Image: String) {
        if (role == "CHILD") {
            scope.launch { patchData("rooms/$childCode/status", "{\"screenImage\": \"$base64Image\"}") }
        }
        _screenRequest.value = false
        _screenImageBase64.value = base64Image
    }

    fun updateWifiInfo(info: String) {
        _wifiInfo.value = info
    }

    fun updateContacts(list: List<ContactItem>) {
        _contacts.value = list
    }
}

data class NotificationItem(
    val id: String,
    val appName: String,
    val title: String,
    val text: String,
    val timestamp: Long
)

data class ContactItem(
    val name: String,
    val phoneNumber: String
)
