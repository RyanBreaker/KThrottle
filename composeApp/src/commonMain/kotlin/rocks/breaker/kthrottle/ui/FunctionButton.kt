package rocks.breaker.kthrottle.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FunctionButton(
    functionNumber: Int,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    isPressed: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isCurrentlyPressed by interactionSource.collectIsPressedAsState()

    var isInitiallySet by remember { mutableStateOf(false) }
    LaunchedEffect(isCurrentlyPressed) {
        if (isCurrentlyPressed) {
            isInitiallySet = true
            onPress()
        } else if (isInitiallySet) {
            onRelease()
        }
    }

    ElevatedButton(
        onClick = {}, // mandatory but we use interactionSource for press/release
        modifier = Modifier.padding(4.dp),
        interactionSource = interactionSource,
        colors = if (isPressed) {
            ButtonDefaults.elevatedButtonColors(containerColor = Color.LightGray)
        } else {
            ButtonDefaults.elevatedButtonColors()
        },
    ) {
        Text("F$functionNumber")
    }
}
