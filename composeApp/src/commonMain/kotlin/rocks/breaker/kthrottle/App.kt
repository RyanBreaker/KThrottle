package rocks.breaker.kthrottle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Suppress("ktlint:standard:function-naming")
@Composable
@Preview
fun App(viewModel: WiThrottleViewModel = viewModel { WiThrottleViewModel() }) {
    MaterialTheme {
        val isConnected by viewModel.isConnected.collectAsState()
        val host by viewModel.host.collectAsState()
        val port by viewModel.port.collectAsState()
        val status by viewModel.status.collectAsState()
        val roster by viewModel.roster.collectAsState()

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
                OutlinedTextField(
                    value = host,
                    onValueChange = { viewModel.setHost(it) },
                    label = { Text("Host") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = port,
                    onValueChange = { viewModel.setPort(it) },
                    label = { Text("Port") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.connect() }) {
                    Text("Connect")
                }
            } else {
                Button(onClick = { viewModel.disconnect() }) {
                    Text("Disconnect")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Status: $status", style = MaterialTheme.typography.bodyLarge)

            if (isConnected) {
                var locoAddress by remember { mutableStateOf("") }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = locoAddress,
                        onValueChange = { locoAddress = it },
                        label = { Text("Loco Address") },
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (locoAddress.isNotEmpty()) {
                            viewModel.sendCommand("M0+L$locoAddress<;>L$locoAddress")
                            locoAddress = ""
                        }
                    }) {
                        Text("Add")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Locomotive Roster", style = MaterialTheme.typography.titleMedium)
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                ) {
                    items(roster) { locomotive ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            onClick = { /* Select loco */ },
                        ) {
                            Text(locomotive, modifier = Modifier.padding(8.dp))
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Button(onClick = { viewModel.sendCommand("PPA1") }) { Text("Power ON") }
                    Button(onClick = { viewModel.sendCommand("PPA0") }) { Text("Power OFF") }
                }
            }
        }
    }
}
