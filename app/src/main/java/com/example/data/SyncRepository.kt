package com.example.data

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.ServerSocket
import java.net.HttpURLConnection
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.util.concurrent.CountDownLatch
import java.net.NetworkInterface
import java.net.Inet4Address

object SyncRepository {
    private val scope = CoroutineScope(Dispatchers.IO)

    var role: String = "UNKNOWN"
    var childIp: String = ""

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

    private var serverSocket: ServerSocket? = null
    private var cameraLatch: CountDownLatch? = null
    private var screenLatch: CountDownLatch? = null
    private var audioLatch: CountDownLatch? = null

    init {
        scope.launch {
            while (true) {
                if (_paired.value && role == "CHILD") {
                    _childLocation.value = Pair(37.4221 + Math.random() * 0.01, -122.0841 + Math.random() * 0.01)
                }
                delay(5000)
            }
        }
    }

    fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (intf in interfaces) {
                for (enumIpAddr in intf.inetAddresses) {
                    if (!enumIpAddr.isLoopbackAddress && enumIpAddr is Inet4Address) {
                        return enumIpAddr.hostAddress
                    }
                }
            }
        } catch (ex: Exception) { ex.printStackTrace() }
        return null
    }

    fun ipToCode(ip: String): String {
        val parts = ip.split(".")
        if (parts.size != 4) return "0000000000"
        val ipAsLong = (parts[0].toLong() shl 24) + (parts[1].toLong() shl 16) + (parts[2].toLong() shl 8) + parts[3].toLong()
        return String.format("%010d", ipAsLong)
    }

    fun codeToIp(code: String): String {
        val ipAsLong = code.toLongOrNull() ?: return "127.0.0.1"
        val p1 = (ipAsLong shr 24) and 0xFF
        val p2 = (ipAsLong shr 16) and 0xFF
        val p3 = (ipAsLong shr 8) and 0xFF
        val p4 = ipAsLong and 0xFF
        return "$p1.$p2.$p3.$p4"
    }

    fun startChildServer() {
        if (serverSocket != null) return
        role = "CHILD"
        Thread {
            try {
                serverSocket = ServerSocket(8888)
                while (true) {
                    val client = serverSocket!!.accept()
                    scope.launch {
                        try {
                            val reader = BufferedReader(InputStreamReader(client.inputStream))
                            val writer = PrintWriter(client.outputStream)
                            val requestLine = reader.readLine()
                            if (requestLine != null) {
                                val parts = requestLine.split(" ")
                                if (parts.size >= 2) {
                                    val path = parts[1]
                                    var responseBody = ""
                                    if (path == "/ping") {
                                        _paired.value = true
                                        responseBody = "OK"
                                    } else if (path == "/camera") {
                                        _cameraRequest.value = true
                                        delay(1500)
                                        responseBody = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="
                                        _cameraRequest.value = false
                                    } else if (path == "/screen") {
                                        _screenRequest.value = true
                                        delay(1500)
                                        responseBody = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+ip1sAAAAASUVORK5CYII="
                                        _screenRequest.value = false
                                    } else if (path == "/audio") {
                                        _audioRequest.value = true
                                        delay(1500)
                                        responseBody = "dummy_audio_base64"
                                        _audioRequest.value = false
                                    } else if (path == "/location") {
                                        val loc = _childLocation.value
                                        responseBody = if (loc != null) "${loc.first},${loc.second}" else "unknown"
                                    }
                                    writer.print("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: ${responseBody.length}\r\n\r\n$responseBody")
                                    writer.flush()
                                }
                            }
                            client.close()
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }.start()
    }

    suspend fun connectToChild(ip: String): Boolean = kotlinx.coroutines.withContext(Dispatchers.IO) {
        role = "PARENT"
        try {
            val url = URL("http://$ip:8888/ping")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            val reader = BufferedReader(InputStreamReader(conn.inputStream))
            val response = reader.readLine()
            if (response == "OK") {
                childIp = ip
                _paired.value = true
                true
            } else false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

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
        if (role == "PARENT") {
            _cameraRequest.value = true
            scope.launch {
                try {
                    val url = URL("http://$childIp:8888/camera")
                    val conn = url.openConnection() as HttpURLConnection
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    _cameraImageBase64.value = reader.readText()
                } catch (e: Exception) { e.printStackTrace() }
                _cameraRequest.value = false
            }
        }
    }

    fun completeCameraSnapshot(base64Image: String) {
        _cameraImageBase64.value = base64Image
        cameraLatch?.countDown()
    }

    fun requestAudioClip() {
        if (role == "PARENT") {
            _audioRequest.value = true
            scope.launch {
                try {
                    val url = URL("http://$childIp:8888/audio")
                    val conn = url.openConnection() as HttpURLConnection
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    _audioFileBase64.value = reader.readText()
                } catch (e: Exception) { e.printStackTrace() }
                _audioRequest.value = false
            }
        }
    }

    fun completeAudioClip(base64Audio: String) {
        _audioFileBase64.value = base64Audio
        audioLatch?.countDown()
    }

    fun requestScreenSnapshot() {
        if (role == "PARENT") {
            _screenRequest.value = true
            scope.launch {
                try {
                    val url = URL("http://$childIp:8888/screen")
                    val conn = url.openConnection() as HttpURLConnection
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    _screenImageBase64.value = reader.readText()
                } catch (e: Exception) { e.printStackTrace() }
                _screenRequest.value = false
            }
        }
    }

    fun completeScreenSnapshot(base64Image: String) {
        _screenImageBase64.value = base64Image
        screenLatch?.countDown()
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
