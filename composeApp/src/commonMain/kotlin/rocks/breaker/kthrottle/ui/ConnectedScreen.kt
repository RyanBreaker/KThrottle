package rocks.breaker.kthrottle.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rocks.breaker.kthrottle.jmri.Throttle
import rocks.breaker.kthrottle.jmri.getAddressType

@Composable
fun ConnectedScreen(
    trackStatus: String,
    throttles: List<Throttle?>,
    selectedThrottleId: Int,
    onSelectThrottle: (Int) -> Unit,
    onAddThrottle: (Int, Int) -> Unit,
    onRemoveThrottle: (Int) -> Unit,
    onDisconnect: () -> Unit,
    onSendCommand: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(onClick = onDisconnect) {
            Text("Disconnect")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Status: Connected", style = MaterialTheme.typography.bodyLarge)
        Text("Track Power: $trackStatus", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(16.dp))

        ScrollableTabRow(
            selectedTabIndex = selectedThrottleId,
            modifier = Modifier.fillMaxWidth(),
            edgePadding = 0.dp,
        ) {
            throttles.forEachIndexed { index, throttle ->
                Tab(
                    selected = selectedThrottleId == index,
                    onClick = { onSelectThrottle(index) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(throttle?.name ?: "M$index")
                            if (throttle != null) {
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { onRemoveThrottle(index) },
                                    modifier = Modifier.height(24.dp).width(24.dp),
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove Throttle",
                                        modifier = Modifier.height(16.dp).width(16.dp),
                                    )
                                }
                            }
                        }
                    },
                )
            }
        }

        val selectedThrottle = throttles[selectedThrottleId]

        if (selectedThrottle == null) {
            var locoAddress by remember { mutableStateOf("") }
            val addressType by remember {
                derivedStateOf {
                    val addressInt = locoAddress.toIntOrNull()
                    when {
                        addressInt == null -> ""
                        else -> getAddressType(addressInt)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = locoAddress,
                    onValueChange = { locoAddress = it.filter(Char::isDigit).take(4) },
                    label = { Text("Loco Address") },
                    modifier = Modifier.weight(1f),
                    trailingIcon = { Text(addressType) },
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    val addressInt = locoAddress.toIntOrNull()
                    if (addressInt != null) {
                        onAddThrottle(selectedThrottleId, addressInt)
                        locoAddress = ""
                    }
                }) {
                    Text("Add")
                }
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            ThrottleControls(
                throttle = selectedThrottle,
                throttleId = selectedThrottleId,
                onSendCommand = onSendCommand,
            )
        }
    }
}
