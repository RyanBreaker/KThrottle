package rocks.breaker.kthrottle

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import rocks.breaker.kthrottle.ui.ConnectedScreen
import rocks.breaker.kthrottle.ui.ConnectionScreen

@Composable
@Preview
fun App(viewModel: KThrottleViewModel = viewModel { KThrottleViewModel() }) {
    MaterialTheme {
        val isConnected by viewModel.isConnected.collectAsState()
        val host by viewModel.host.collectAsState()
        val port by viewModel.port.collectAsState()
        val trackStatus by viewModel.trackStatus.collectAsState()
        val throttles by viewModel.throttles.collectAsState()
        val selectedThrottleId by viewModel.selectedThrottleId.collectAsState()

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
                    throttles = throttles,
                    selectedThrottleId = selectedThrottleId,
                    onSelectThrottle = viewModel::selectThrottle,
                    onAddThrottle = viewModel::addThrottle,
                    onRemoveThrottle = viewModel::removeThrottle,
                    onDisconnect = viewModel::disconnect,
                    onSendCommand = viewModel::sendCommand,
                )
            }
        }
    }
}
