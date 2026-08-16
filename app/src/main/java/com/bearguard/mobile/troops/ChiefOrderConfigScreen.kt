package com.bearguard.mobile.troops

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bearguard.mobile.config.ConfigCheckboxRow
import com.bearguard.mobile.config.ConfigSectionHeader
import com.bearguard.mobile.scheduler.TaskLiveStatusRow

/**
 * Mirrors ChiefOrderLayout.fxml -- 3 individual per-type checkboxes. The real automation
 * (ChiefOrderRoutine, "chief_order" task) currently always checks all three types together in
 * one pass rather than per-type independent scheduling -- these checkboxes are config-only for
 * now, flagged honestly rather than implying they gate the real toggle below.
 */
@Composable
fun ChiefOrderConfigScreen() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ConfigSectionHeader("Chief Orders")
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Configure which chief orders to automatically complete",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            ConfigCheckboxRow("co_rush_job", "Rush Job (every 24h)")
            ConfigCheckboxRow("co_urgent_mobilisation", "Urgent Mobilisation (every 8h)")
            ConfigCheckboxRow("co_productivity_day", "Productivity Day (every 12h)")

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            TaskLiveStatusRow("chief_order", "Run all three (real)")
        }
    }
}
