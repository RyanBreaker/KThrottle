package rocks.breaker.kthrottle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rocks.breaker.kthrottle.jmri.Direction
import rocks.breaker.kthrottle.jmri.Throttle

class KThrottleViewModel : ViewModel() {
    data class ParsedThrottleMessage(
        val id: Int,
        val command: String,
        val address: String,
        val additional: String,
    )

    object ThrottleMessageParsing {
        private const val ID = "id"
        private const val COMMAND = "command"
        private const val ADDRESS = "address"
        private const val ADDITIONAL = "additional"

        val regex = Regex("""^M(?<$ID>\d)(?<$COMMAND>[A+\-S])(?<$ADDRESS>[SL]\d{1,4})<;>(?<$ADDITIONAL>.*)$""")

        fun parse(message: String): ParsedThrottleMessage? {
            val groups = regex.matchEntire(message)?.groups ?: return null
            val id = groups[ID]?.value?.toIntOrNull() ?: return null
            val command = groups[COMMAND]?.value ?: return null
            val address = groups[ADDRESS]?.value ?: return null
            val additional = groups[ADDITIONAL]?.value ?: return null
            return ParsedThrottleMessage(id, command, address, additional)
        }
    }

    private val client = WiThrottleClient(viewModelScope)

    val isConnected = client.isConnected

    private val _host = MutableStateFlow("127.0.0.1")
    val host = _host.asStateFlow()

    private val _port = MutableStateFlow("12090")
    val port = _port.asStateFlow()

    private val _trackStatus = MutableStateFlow("Unknown")
    val trackStatus = _trackStatus.asStateFlow()

    private val _throttles = MutableStateFlow<List<Throttle?>>(List(9) { null })
    val throttles = _throttles.asStateFlow()

    private val _selectedThrottleId = MutableStateFlow(0)
    val selectedThrottleId = _selectedThrottleId.asStateFlow()

    init {
        viewModelScope.launch {
            client.messages.collect(::handleMessage)
        }
    }

    fun setHost(value: String) {
        _host.value = value
    }

    fun setPort(value: String) {
        _port.value = value.filter(Char::isDigit)
    }

    fun connect() {
        viewModelScope.launch {
            try {
                client.connect(_host.value, _port.value.toInt())
            } catch (e: Exception) {
                Logger.e(e) { "Connection error: ${e.message}" }
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
                Logger.d { "Roster received" }
            }

            message.startsWith("PPA") -> {
                _trackStatus.value =
                    when (message.substringAfter("PPA", "2")) {
                        "0" -> "Off"
                        "1" -> "On"
                        else -> "Unknown"
                    }
            }

            message.matches(ThrottleMessageParsing.regex) -> parseThrottleMessage(message)
        }
    }

    fun parseThrottleMessage(message: String) {
        val parsedMessage = ThrottleMessageParsing.parse(message) ?: run {
            Logger.w { "Could not parse throttle message: $message" }
            return
        }

        val throttle = _throttles.value.getOrNull(parsedMessage.id) ?: return
        val additional = parsedMessage.additional
        if (additional.isEmpty()) return

        when (parsedMessage.additional.first()) {
            // Velocity: V[0-126]
            'V' -> throttle.velocity = additional.substring(1).toInt().coerceIn(-1..126)

            // Direction: R[0|1]
            'R' -> throttle.direction = Direction.parse(additional.last())

            // Function: F[0|1][0-31]
            'F' -> {
                val isPressed = additional[1] == '1'
                val functionNumber = additional.substring(2).toInt()
                if (isPressed) {
                    throttle.pressFunction(functionNumber)
                } else {
                    throttle.unpressFunction(functionNumber)
                }
            }
        }
    }

    fun addThrottle(id: Int, address: Int) {
        val currentThrottles = _throttles.value.toMutableList()
        if (id in 0..8 && currentThrottles[id] == null) {
            val throttle = Throttle(address)
            currentThrottles[id] = throttle
            _throttles.value = currentThrottles
            _selectedThrottleId.value = id

            val addressType = throttle.addressType
            sendCommand("M$id+$addressType$address<;>$addressType$address")
        }
    }

    fun removeThrottle(id: Int) {
        val currentThrottles = _throttles.value.toMutableList()
        val throttle = currentThrottles[id]
        if (throttle != null) {
            val addressType = throttle.addressType
            sendCommand("M$id-$addressType${throttle.address}<;>r")
            currentThrottles[id] = null
            _throttles.value = currentThrottles
        }
    }

    fun selectThrottle(id: Int) {
        if (id in 0..8) {
            _selectedThrottleId.value = id
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
