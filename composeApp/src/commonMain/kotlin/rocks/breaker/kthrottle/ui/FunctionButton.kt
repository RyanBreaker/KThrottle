package rocks.breaker.kthrottle.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun FunctionButton(
    functionNumber: Int,
    onPressOrRelease: (String) -> Unit,
    isToggled: Boolean = false,
) {
    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        // F = tell JMRI what our state is
                        // f = force JMRI to use the state we give it
                        val f = if (isToggled) "F" else "f"
                        onPressOrRelease("${f}1$functionNumber")
                        tryAwaitRelease()
                        onPressOrRelease("${f}0$functionNumber")
                    },
                )
            }
            .then(Modifier.defaultMinSize(minWidth = 64.dp, minHeight = 48.dp)),
    ) {
        Text("F$functionNumber")
    }
}
