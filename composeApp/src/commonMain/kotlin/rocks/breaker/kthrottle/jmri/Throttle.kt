package rocks.breaker.kthrottle.jmri

enum class Direction(val value: Char) {
    Reverse('0'),
    Forward('1'),
    ;

    companion object {
        fun fromString(value: Char) = entries.firstOrNull { it.value == value } ?: Forward
    }
}

data class Throttle(val address: Int) {
    init {
        require(address in 0..9999) { "Throttle address must be between 0 and 9999, was $address" }
    }

    val addressType = if (address < 128) "S" else "L"
    var name: String = "$addressType$address"

    var velocity = 0
    var direction = Direction.Forward
    private val _pressedFunctions = mutableSetOf<Byte>()
    val pressedFunctions: Set<Byte> = _pressedFunctions

    fun pressFunction(functionNumber: Byte) {
        _pressedFunctions.add(functionNumber)
    }

    fun unpressFunction(functionNumber: Byte) {
        _pressedFunctions.remove(functionNumber)
    }
}
