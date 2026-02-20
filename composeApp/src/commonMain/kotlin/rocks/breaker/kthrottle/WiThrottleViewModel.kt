package rocks.breaker.kthrottle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WiThrottleViewModel : ViewModel() {
    private val client = WiThrottleClient(viewModelScope)

    val isConnected = client.isConnected

    private val _host = MutableStateFlow("127.0.0.1")
    val host: StateFlow<String> = _host.asStateFlow()

    private val _port = MutableStateFlow("12090")
    val port: StateFlow<String> = _port.asStateFlow()

    private val _status = MutableStateFlow("Disconnected")
    val status: StateFlow<String> = _status.asStateFlow()

    private val _roster = MutableStateFlow<List<String>>(emptyList())
    val roster: StateFlow<List<String>> = _roster.asStateFlow()

    init {
        viewModelScope.launch {
            client.messages.collect { message ->
                handleMessage(message)
            }
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
            _status.value = "Connecting..."
            try {
                client.connect(_host.value, _port.value.toInt())
                _status.value = "Connected"
            } catch (e: Exception) {
                _status.value = "Connection failed: ${e.message}"
            }
        }
    }

    fun disconnect() {
        client.disconnect()
        _status.value = "Disconnected"
    }

    private fun handleMessage(message: String) {
        // Basic parsing of WiThrottle protocol
        when {
            message.startsWith("RL") -> {
                // Roster list: RL<Count>|<Name>|<ID>|...
                val parts = message.substring(2).split("|")
                if (parts.isNotEmpty()) {
                    val rosterItems = mutableListOf<String>()
                    for (i in 1 until parts.size step 3) {
                        if (i + 1 < parts.size) {
                            rosterItems.add(parts[i])
                        }
                    }
                    _roster.value = rosterItems
                }
            }

            message.startsWith("PPA") -> {
                val power = message.substring(3) == "1"
                _status.value = "Connected (Power: ${if (power) "ON" else "OFF"})"
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
