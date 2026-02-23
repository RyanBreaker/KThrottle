package rocks.breaker.kthrottle.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rocks.breaker.kthrottle.jmri.Direction
import rocks.breaker.kthrottle.jmri.Throttle
import kotlin.math.roundToInt

@Composable
fun ThrottleControls(
    throttle: Throttle,
    throttleId: Int,
    onSendCommand: (String) -> Unit,
    toggledFunctions: Set<Int> = setOf(0, 1),
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Throttle: ${throttle.name}",
            style = MaterialTheme.typography.headlineSmall,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Direction
        Text("Direction", style = MaterialTheme.typography.labelLarge)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            Direction.entries.forEachIndexed { index, direction ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = Direction.entries.size),
                    onClick = {
                        onSendCommand("M${throttleId}A${throttle.name}<;>$direction")
                    },
                    selected = throttle.direction == direction,
                ) {
                    Text(direction.name)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Velocity
        val velocityText = if (throttle.isEmergencyStopped) "E-Stop" else "${throttle.velocity.coerceIn(0..126)}"
        Text("Velocity: $velocityText", style = MaterialTheme.typography.labelLarge)
        Slider(
            value = throttle.velocity.toFloat(),
            onValueChange = { throttle.velocity = it.roundToInt() },
            onValueChangeFinished = { onSendCommand("M${throttleId}A${throttle.name}<;>V${throttle.velocity}") },
            valueRange = 0f..126f,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = {
                    onSendCommand("M${throttleId}A${throttle.name}<;>V0")
                    throttle.velocity = 0
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("Stop")
            }
            Button(
                onClick = {
                    onSendCommand("M${throttleId}A${throttle.name}<;>X")
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (throttle.isEmergencyStopped) {
                        MaterialTheme.colorScheme.error // Normal E-Stop state (red)
                    } else {
                        MaterialTheme.colorScheme.errorContainer // Highlighted state
                    },
                    contentColor = if (throttle.isEmergencyStopped) {
                        MaterialTheme.colorScheme.onError
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    },
                ),
            ) {
                Text("E-Stop")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Functions
        Text("Functions", style = MaterialTheme.typography.labelLarge)
        var functionPage by remember { mutableStateOf(0) }
        val pages = 4
        val functionsPerPage = 8

        TabRow(selectedTabIndex = functionPage) {
            for (page in 0 until pages) {
                Tab(
                    selected = functionPage == page,
                    onClick = { functionPage = page },
                    text = { Text("P${page + 1}") },
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.height(120.dp), // Fixed height to avoid infinite height in Column
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val startFunc = functionPage * functionsPerPage
            items(functionsPerPage) { index ->
                val funcNum = startFunc + index
                val f = if (toggledFunctions.contains(funcNum)) "F" else "f"
                FunctionButton(
                    functionNumber = funcNum,
                    onPress = {
                        onSendCommand("M${throttleId}A${throttle.name}<;>${f}1$funcNum")
                    },
                    onRelease = {
                        onSendCommand("M${throttleId}A${throttle.name}<;>${f}0$funcNum")
                    },
                    isPressed = throttle.pressedFunctions.contains(funcNum),
                )
            }
        }
    }
}
