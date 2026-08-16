package com.bearguard.mobile.more

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bearguard.mobile.config.ConfigSectionHeader

/** matt/2026-08-16: Windows tucks Debugging and Task Builder into "More (Dev Tools)" -- internal
 * tooling, not real automation. Placeholder screens, matching the honesty of the rest. */
@Composable
fun DebuggingConfigScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        ConfigSectionHeader("Debugging")
        Text(
            "Windows dev-tool screen for live debugging the automation. No Android equivalent yet.",
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TaskBuilderConfigScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        ConfigSectionHeader("Task Builder")
        Text(
            "Windows dev-tool screen for building custom task sequences. No Android equivalent yet.",
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
