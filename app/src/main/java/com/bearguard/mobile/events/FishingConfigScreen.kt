package com.bearguard.mobile.events

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bearguard.mobile.config.ConfigCheckboxRow
import com.bearguard.mobile.config.ConfigSectionHeader

/** Mirrors FishingLayout.fxml -- Windows itself flags this screen "UNFINISHED — DOES NOT WORK"
 * with a red warning banner. Carried over honestly rather than silently dropped. */
@Composable
fun FishingConfigScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        ConfigSectionHeader("Fishing Tournament")
        Surface(color = Color(0xFF5A1A1A), modifier = Modifier.fillMaxWidth()) {
            Text(
                "⚠ UNFINISHED — DOES NOT WORK",
                color = Color(0xFFFF8080),
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Column(modifier = Modifier.padding(16.dp)) {
            ConfigCheckboxRow("fi_enable_fishing", "Enable Fishing Minigame Task")
            Spacer(Modifier.height(12.dp))
            ConfigCheckboxRow("fi_enable_test_hook_loop", "Enable Test Hook Loop")
        }
    }
}
