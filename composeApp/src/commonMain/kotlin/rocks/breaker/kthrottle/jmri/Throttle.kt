package rocks.breaker.kthrottle.jmri

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.setValue

enum class Direction(val value: Char) {
    Reverse('0'),
    Forward('1'),
    ;

    override fun toString() = "R$value"

    companion object {
        fun parse(value: Char) = entries.firstOrNull { it.value == value } ?: Forward
    }
}

data class Throttle(val address: Int) {
    init {
        require(address in 0..9999) { "Throttle address must be between 0 and 9999, was $address" }
    }

    val addressType = getAddressType(address)
    var name: String = "$addressType$address"

    var velocity by mutableStateOf(0)
    val isEmergencyStopped by derivedStateOf { velocity < 0 }

    var direction by mutableStateOf(Direction.Forward)

    private val _pressedFunctions = mutableStateSetOf<Int>()
    val pressedFunctions: Set<Int> = _pressedFunctions

    fun pressFunction(functionNumber: Int) {
        _pressedFunctions.add(functionNumber)
    }

    fun unpressFunction(functionNumber: Int) {
        _pressedFunctions.remove(functionNumber)
    }
}
