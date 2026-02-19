package rocks.breaker.jmri_throttle

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.Duration
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class WiThrottleClient(
    private val scope: CoroutineScope,
    val uuid: String = Uuid.random().toString(),
) {
    private val selectorManager = SelectorManager(Dispatchers.Default)
    private var socket: Socket? = null
    private var readChannel: ByteReadChannel? = null
    private var writeChannel: ByteWriteChannel? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    suspend fun connect(host: String, port: Int, deviceName: String = "ComposeMultiThrottle") {
        try {
            val s = aSocket(selectorManager).tcp().connect(host, port)
            socket = s
            val rc = s.openReadChannel()
            readChannel = rc
            val wc = s.openWriteChannel(autoFlush = true)
            writeChannel = wc

            _isConnected.value = true

            // Initial handshake
            send("HU$uuid")
            send("N$deviceName")
            send("*+")

            // Start reading messages
            scope.launch {
                try {
                    while (isActive) {
                        val line = rc.readLine()?.trim() ?: break
                        if (line.isEmpty()) continue
                        println("Message: $line")
                        _messages.emit(line)
                        handleInternalMessage(line)
                    }
                } catch (e: Exception) {
                    println("Read error: ${e.message}")
                } finally {
                    disconnect()
                }
            }
        } catch (e: Exception) {
            println("Connection error: ${e.message}")
            _isConnected.value = false
            throw e
        }
    }

    private var heartbeatJob: Job? = null
    private var heartbeat: Duration = Duration.ZERO

    private fun handleInternalMessage(line: String) {
        if (line.startsWith("*")) {
            heartbeat = Duration.parseOrNull(line.substring(1) + 's') ?: run {
                println("Invalid heartbeat interval: $line")
                return
            }
            println("Heartbeat interval: $heartbeat")
            heartbeatJob?.cancel()

            // If we received a 0, we're disabling the heartbeat
            if (heartbeat == Duration.ZERO) return

            heartbeatJob = scope.launch {
                while (isActive) {
                    delay(heartbeat / 2)
                    send("*")
                }
            }
        }
    }

    suspend fun send(message: String) {
        println("Sending message: $message")
        writeChannel?.writeString("$message\n")
    }

    fun disconnect() {
        heartbeatJob?.cancel()
        heartbeatJob = null
        _isConnected.value = false
        socket?.close()
        socket = null
        readChannel = null
        writeChannel = null
    }

    fun close() {
        disconnect()
        selectorManager.close()
    }
}
