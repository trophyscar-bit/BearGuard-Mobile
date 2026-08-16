package com.bearguard.mobile.city

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bearguard.mobile.config.ConfigCheckboxRow
import com.bearguard.mobile.config.ConfigCheckboxWithOffsetRow
import com.bearguard.mobile.config.ConfigSectionHeader
import com.bearguard.mobile.scheduler.SchedulerPrefs
import com.bearguard.mobile.scheduler.TaskRegistry
import com.bearguard.mobile.service.BearGuardAccessibilityService
import kotlinx.coroutines.launch

/**
 * matt/2026-08-15: mirrors CityUpgradesLayout.fxml exactly -- same checkboxes, same order.
 * "Upgrade Furnace" / "Reserve training/research" / "Prioritise Furnace" are config-only for now
 * (the real automation still needs UpgradeBuildingsRoutine's picking-a-building logic, which
 * CityUpgradesRoutine.kt's v1 doesn't have yet -- see that file's own doc). "Accept new
 * survivors" is likewise config-only; NewSurvivorsRoutine hasn't been ported. Both persist real
 * state so flipping them now isn't wasted once the automation catches up.
 */
@Composable
fun CityUpgradesConfigScreen() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ConfigSectionHeader("City Upgrades")
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Configure automated building upgrades safely",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            ConfigCheckboxRow("cu_upgrade_furnace", "Upgrade Furnace")
            ConfigCheckboxRow(
                "cu_reserve_production", "Reserve training/research for required upgrades",
                subtext = "When a required camp or Research Center is busy, pause only that " +
                    "production queue until construction can start."
            )
            ConfigCheckboxRow("cu_prioritise_furnace", "Prioritise Furnace")

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            ConfigCheckboxWithOffsetRow(
                "cu_accept_survivors", "Accept new survivors", "cu_survivors_offset_min"
            )

            Spacer(Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // matt/2026-08-15: the one real, working piece of City Upgrades right now -- reads
            // both construction queues live. Inlined here (not the full-page ModuleTaskScreen,
            // which assumes it owns the whole screen) since this is embedded inside a scrolling
            // config page alongside the checkboxes above.
            Text("Live Queue Status (real)", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                "The one piece of City Upgrades with real automation behind it so far.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            CityUpgradesLiveStatusRow()
        }
    }
}

@Composable
private fun CityUpgradesLiveStatusRow() {
    val context = LocalContext.current
    val prefs = remember { SchedulerPrefs(context) }
    val scope = rememberCoroutineScope()
    val task = remember { TaskRegistry.all.first { it.key == "city_upgrades" } }
    val enabled by prefs.enabled(task.key).collectAsState(initial = false)
    val lastResult by prefs.lastResult(task.key).collectAsState(initial = "")
    val serviceConnected = BearGuardAccessibilityService.instance != null

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Enable queue reading", style = MaterialTheme.typography.bodyMedium)
            if (lastResult.isNotBlank()) {
                Text(lastResult, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary)
            }
        }
        Switch(
            checked = enabled,
            onCheckedChange = { checked -> scope.launch { prefs.setEnabled(task.key, checked) } },
            enabled = serviceConnected
        )
    }
}
