package rocks.breaker.kthrottle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WiThrottleViewModel : ViewModel() {
    private val client = WiThrottleClient(viewModelScope)

    val isConnected = client.isConnected

    private val _host = MutableStateFlow("127.0.0.1")
    val host = _host.asStateFlow()

    private val _port = MutableStateFlow("12090")
    val port = _port.asStateFlow()

    private val _trackStatus = MutableStateFlow("Unknown")
    val trackStatus = _trackStatus.asStateFlow()

    private val _roster = MutableStateFlow<List<String>>(emptyList())
    val roster = _roster.asStateFlow()

    init {
        viewModelScope.launch {
            client.messages.collect(::handleMessage)
        }
    }

    fun setHost(value: String) {
        _host.value = value
    }

    fun setPort(value: String) {
        _port.value = value
    }

    fun connect() {
        viewModelScope.launch {
            try {
                client.connect(_host.value, _port.value.toInt())
            } catch (e: Exception) {
                println("Connection error: ${e.message}")
            }
        }
    }

    fun disconnect() {
        client.disconnect()
        _trackStatus.value = "Unknown"
    }

    private fun handleMessage(message: String) {
        when {
            message.startsWith("RL") -> {
                // TODO?
                println("Roster received")
            }

            message.startsWith("PPA") -> {
                _trackStatus.value =
                    when (message.substringAfter("PPA", "2")) {
                        "0" -> "Off"
                        "1" -> "On"
                        else -> "Unknown"
                    }
            }
        }
    }

    fun sendCommand(cmd: String) {
        viewModelScope.launch {
            client.send(cmd)
        }
    }

    override fun onCleared() {
        client.close()
        super.onCleared()
    }
}
