package rocks.breaker.jmri_throttle

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.tooling.preview.Preview

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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("WiThrottle Client", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            if (!isConnected) {
                OutlinedTextField(
                    value = host,
                    onValueChange = { viewModel.setHost(it) },
                    label = { Text("Host") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = port,
                    onValueChange = { viewModel.setPort(it) },
                    label = { Text("Port") },
                    modifier = Modifier.fillMaxWidth()
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = locoAddress,
                        onValueChange = { locoAddress = it },
                        label = { Text("Loco Address") },
                        modifier = Modifier.weight(1f)
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
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    items(roster) { locomotive ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            onClick = { /* Select loco */ }
                        ) {
                            Text(locomotive, modifier = Modifier.padding(8.dp))
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { viewModel.sendCommand("PPA1") }) { Text("Power ON") }
                    Button(onClick = { viewModel.sendCommand("PPA0") }) { Text("Power OFF") }
                }
            }
        }
    }
}