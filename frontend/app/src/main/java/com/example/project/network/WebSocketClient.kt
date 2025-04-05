package com.example.project.network

import android.util.Base64
import android.util.Log
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class WebSocketClient(
    private val onMessageReceived: (String) -> Unit,
    private val username: String,
    private val password: String
) {

    private var client: WebSocketClient? = null
    //private val uri = URI.create("wss://20.30.69.128:8443/ws/rooms")
    private val uri = URI.create("wss://20.30.0.165:8443/ws/rooms")

    fun connect() {
        client = object : WebSocketClient(uri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d("WebSocket", "Connected")
                subscribeToTopic()
            }

            override fun onMessage(message: String?) {
                Log.d("WebSocket", "Message received: $message")
                message?.let { onMessageReceived(it) }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d("WebSocket", "Closed: $reason (code: $code)")
            }

            override fun onError(ex: Exception?) {
                Log.e("WebSocket", "Error: ${ex?.message}")
            }

            override fun addHeader(key: String, value: String) {
                super.addHeader(key, value)
            }
        }.apply {
            // Add Basic Auth header using android.util.Base64
            val auth = "Basic " + Base64.encodeToString("$username:$password".toByteArray(), Base64.NO_WRAP)
            addHeader("Authorization", auth)
        }
        client?.connect()
    }

    private fun subscribeToTopic() {
        val message = "SUBSCRIBE\nid:sub-0\ndestination:/topic/rooms\n\n"
        client?.send(message.toByteArray(Charsets.UTF_8) + byteArrayOf(0))
    }

    fun disconnect() {
        client?.close()
        client = null
    }

    fun isConnected(): Boolean = client?.isOpen == true
}