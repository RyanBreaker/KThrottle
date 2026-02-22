package rocks.breaker.kthrottle.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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

@Composable
fun ConnectedScreen(
    trackStatus: String,
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

        var locoAddress by remember { mutableStateOf("") }
        val addressType by remember {
            derivedStateOf {
                val addressInt = locoAddress.toIntOrNull()
                when {
                    addressInt == null -> ""
                    addressInt > 127 -> "L"
                    else -> "S"
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
                if (locoAddress.isNotEmpty()) {
                    onSendCommand("M0+$addressType$locoAddress<;>$addressType$locoAddress")
                    locoAddress = ""
                }
            }) {
                Text("Add")
            }

            FunctionButton(
                0,
                { s -> onSendCommand("M0AL6767<;>$s") },
            )
        }
    }
}
