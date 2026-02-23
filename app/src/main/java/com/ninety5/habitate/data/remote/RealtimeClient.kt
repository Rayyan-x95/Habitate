package com.ninety5.habitate.data.remote

import com.ninety5.habitate.BuildConfig
import com.ninety5.habitate.domain.model.Message
import com.ninety5.habitate.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import com.squareup.moshi.Moshi
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

sealed class WsMessage {
    data class NewMessage(val message: Message) : WsMessage()
    data class Typing(val chatId: String, val userId: String, val isTyping: Boolean) : WsMessage()
    data class Presence(val userId: String, val status: PresenceStatus) : WsMessage()
    data class Reaction(val messageId: String, val userId: String, val emoji: String, val action: String) : WsMessage() // action: "ADD" or "REMOVE"
}

enum class PresenceStatus { ONLINE, OFFLINE, AWAY }

enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED, RECONNECTING }

@Singleton
class RealtimeClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val authRepository: AuthRepository,
    private val moshi: Moshi
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _messages = MutableSharedFlow<WsMessage>()
    val messages: SharedFlow<WsMessage> = _messages.asSharedFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private var webSocket: WebSocket? = null
    private var reconnectAttempt = 0
    private val maxReconnectAttempts = 5

    suspend fun connect() {
        if (_connectionState.value == ConnectionState.CONNECTING || _connectionState.value == ConnectionState.CONNECTED) {
            return
        }

        _connectionState.value = ConnectionState.CONNECTING
        val token = authRepository.getAccessToken()
        if (token == null) {
            Timber.w("RealtimeClient: No access token, cannot connect")
            _connectionState.value = ConnectionState.DISCONNECTED
            return
        }

        // Use BuildConfig for base URL, append ws path
        val wsUrl = BuildConfig.BASE_API_URL.replace("https://", "wss://").replace("http://", "ws://") + "ws"
        val request = Request.Builder()
            .url(wsUrl)
            .addHeader("Authorization", "Bearer $token")
            .build()
        
        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                Timber.d("RealtimeClient: Connected")
                _connectionState.value = ConnectionState.CONNECTED
                reconnectAttempt = 0
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val adapter = moshi.adapter(WsMessage::class.java)
                    val message = adapter.fromJson(text)
                    if (message != null) {
                        _messages.tryEmit(message)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "RealtimeClient: Failed to parse message")
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                Timber.d("RealtimeClient: Closing - $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                this@RealtimeClient.webSocket = null
                _connectionState.value = ConnectionState.DISCONNECTED
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                Timber.e(t, "RealtimeClient: Connection failed")
                this@RealtimeClient.webSocket = null
                _connectionState.value = ConnectionState.DISCONNECTED
                attemptReconnect()
            }
        })
    }

    private fun attemptReconnect() {
        if (reconnectAttempt >= maxReconnectAttempts) {
            Timber.w("RealtimeClient: Max reconnect attempts reached")
            return
        }

        scope.launch {
            val delayMs = (1000L * (1 shl reconnectAttempt)).coerceAtMost(30_000L) // Exponential backoff, max 30s
            Timber.d("RealtimeClient: Reconnecting in ${delayMs}ms (attempt ${reconnectAttempt + 1})")
            _connectionState.value = ConnectionState.RECONNECTING
            delay(delayMs)
            reconnectAttempt++
            connect()
        }
    }

    fun sendMessage(text: String) {
        webSocket?.send(text)
    }

    suspend fun sendPresence(status: PresenceStatus) {
        val userId = authRepository.getCurrentUserId() ?: return
        val message = WsMessage.Presence(userId = userId, status = status)
        val json = moshi.adapter(WsMessage::class.java).toJson(message)
        webSocket?.send(json)
    }

    fun close() {
        webSocket?.close(1000, "App closed")
        webSocket = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }
}
