@file:Suppress("ktlint:standard:function-naming")

package rocks.breaker.kthrottle

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
@Preview
fun App(viewModel: KThrottleViewModel = viewModel { KThrottleViewModel() }) {
    MaterialTheme {
        val isConnected by viewModel.isConnected.collectAsState()
        val host by viewModel.host.collectAsState()
        val port by viewModel.port.collectAsState()
        val trackStatus by viewModel.trackStatus.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("KThrottle", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            if (!isConnected) {
                ConnectionScreen(
                    host = host,
                    port = port,
                    onHostChange = viewModel::setHost,
                    onPortChange = viewModel::setPort,
                    onConnect = viewModel::connect,
                )
            } else {
                ConnectedScreen(
                    trackStatus,
                    onDisconnect = viewModel::disconnect,
                    onSendCommand = viewModel::sendCommand,
                )
            }
        }
    }
}

@Composable
fun ConnectionScreen(
    host: String,
    port: String,
    onHostChange: (String) -> Unit,
    onPortChange: (String) -> Unit,
    onConnect: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OutlinedTextField(
            value = host,
            onValueChange = onHostChange,
            label = { Text("Host") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = port,
            onValueChange = onPortChange,
            label = { Text("Port") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onConnect) {
            Text("Connect")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Status: Disconnected", style = MaterialTheme.typography.bodyLarge)
    }
}

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

            Button(
                onClick = { onSendCommand("M0AL6767<;>F15") },
            ) {
                Text("F5")
            }
        }
    }
}
