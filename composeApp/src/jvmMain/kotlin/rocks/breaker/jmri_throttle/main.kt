package rocks.breaker.jmri_throttle

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "jmri_throttle",
    ) {
        App()
    }
}