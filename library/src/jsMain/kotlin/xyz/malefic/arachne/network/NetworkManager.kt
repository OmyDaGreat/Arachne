package xyz.malefic.arachne.network

import kotlinx.browser.window
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.w3c.dom.MessageEvent
import org.w3c.dom.WebSocket

@Serializable
data class NetworkMessage(
    val type: String,
    val data: String = "",
    val timestamp: Long = 0,
    val senderId: String = "",
)

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
}

class NetworkManager(
    private val url: String,
) {
    private var socket: WebSocket? = null
    private val json =
        Json {
            ignoreUnknownKeys = true
        }

    var state: ConnectionState = ConnectionState.DISCONNECTED
        private set

    var clientId: String = ""
        private set

    private val messageHandlers = mutableMapOf<String, MutableList<(NetworkMessage) -> Unit>>()
    val onConnect = mutableListOf<() -> Unit>()
    val onDisconnect = mutableListOf<() -> Unit>()
    val onError = mutableListOf<(String) -> Unit>()

    fun connect() {
        if (state != ConnectionState.DISCONNECTED) return

        state = ConnectionState.CONNECTING

        try {
            socket =
                WebSocket(url).apply {
                    onopen = {
                        state = ConnectionState.CONNECTED
                        clientId = "client_${window.performance.now().toLong()}"
                        onConnect.forEach { it() }
                        null
                    }

                    onmessage = { event ->
                        handleMessage(event)
                        null
                    }

                    onclose = {
                        state = ConnectionState.DISCONNECTED
                        onDisconnect.forEach { it() }
                        null
                    }

                    onerror = {
                        onError.forEach { handler -> handler("WebSocket error occurred") }
                        null
                    }
                }
        } catch (e: Exception) {
            state = ConnectionState.DISCONNECTED
            onError.forEach { it("Failed to connect: ${e.message}") }
        }
    }

    fun disconnect() {
        if (state != ConnectionState.CONNECTED) return

        state = ConnectionState.DISCONNECTING
        socket?.close()
        socket = null
    }

    fun send(
        type: String,
        data: String = "",
    ) {
        if (state != ConnectionState.CONNECTED) return

        val message =
            NetworkMessage(
                type = type,
                data = data,
                timestamp = window.performance.now().toLong(),
                senderId = clientId,
            )

        try {
            val jsonString = json.encodeToString(message)
            socket?.send(jsonString)
        } catch (e: Exception) {
            console.error("Failed to send message", e)
        }
    }

    fun sendObject(
        type: String,
        obj: Any,
    ) {
        try {
            val data = JSON.stringify(obj)
            send(type, data)
        } catch (e: Exception) {
            console.error("Failed to send object", e)
        }
    }

    fun on(
        messageType: String,
        handler: (NetworkMessage) -> Unit,
    ) {
        messageHandlers.getOrPut(messageType) { mutableListOf() }.add(handler)
    }

    fun off(
        messageType: String,
        handler: (NetworkMessage) -> Unit,
    ) {
        messageHandlers[messageType]?.remove(handler)
    }

    private fun handleMessage(event: MessageEvent) {
        try {
            val jsonString = event.data as? String ?: return
            val message = json.decodeFromString<NetworkMessage>(jsonString)

            messageHandlers[message.type]?.forEach { handler ->
                handler(message)
            }

            messageHandlers["*"]?.forEach { handler ->
                handler(message)
            }
        } catch (e: Exception) {
            console.error("Failed to handle message", e)
        }
    }

    fun isConnected() = state == ConnectionState.CONNECTED

    fun getReadyState() =
        when (socket?.readyState?.toInt()) {
            0 -> ConnectionState.CONNECTING
            1 -> ConnectionState.CONNECTED
            2 -> ConnectionState.DISCONNECTING
            3 -> ConnectionState.DISCONNECTED
            else -> ConnectionState.DISCONNECTED
        }
}

object NetworkHelper {
    fun createMessage(
        type: String,
        vararg pairs: Pair<String, Any>,
    ): String {
        val map = pairs.toMap()
        return JSON.stringify(map)
    }

    fun parseMessage(data: String): Map<String, Any> =
        try {
            val obj = JSON.parse<dynamic>(data)
            val map = mutableMapOf<String, Any>()
            js("Object").keys(obj).forEach { key: String ->
                map[key] = obj[key]
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
}
