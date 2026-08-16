package com.bearguard.mobile.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bearguard.mobile.config.ConfigCheckboxRow
import com.bearguard.mobile.config.ConfigDropdownRow
import com.bearguard.mobile.config.ConfigPrefs
import com.bearguard.mobile.config.ConfigSectionHeader
import com.bearguard.mobile.config.ConfigTextFieldRow
import kotlinx.coroutines.launch

/** Mirrors BearTrapLayout.fxml. Windows uses a ControlsFX CheckComboBox (multi-select) for "Join
 * Flag" -- approximated here with a row of persisted FilterChips (Android has no equivalent
 * multi-select spinner control). Config-only. */
@Composable
fun BearTrapConfigScreen() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ConfigSectionHeader("Bear Trap")
        Column(modifier = Modifier.padding(16.dp)) {
            ConfigCheckboxRow("bt_enable", "Enable Bear Trap")
            ConfigTextFieldRow("bt_schedule_datetime", "Next Bear Trap (UTC), dd-MM-yyyy HH:mm", numeric = false)
            ConfigTextFieldRow("bt_prep_minutes", "Prep minutes", "5")

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            Text("Preparation", style = MaterialTheme.typography.titleSmall)
            ConfigCheckboxRow("bt_active_pets", "Active Pets")
            ConfigCheckboxRow("bt_recall_troops", "Recall Gather Troops")

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            Text("Trap Target", style = MaterialTheme.typography.titleSmall)
            ConfigDropdownRow("bt_trap_number", "Trap", listOf("1", "2", "3"))
            ConfigCheckboxRow("bt_call_rally", "Call Own Rally")
            ConfigDropdownRow("bt_rally_flag", "Rally Flag", FLAGS)
            ConfigCheckboxRow("bt_enable_join", "Enable Join Rally")

            Spacer(Modifier.height(8.dp))
            Text("Join Flag (multi-select)", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            MultiSelectFlagChips("bt_join_flags")
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun MultiSelectFlagChips(key: String) {
    val context = LocalContext.current
    val prefs = remember { ConfigPrefs(context) }
    val scope = rememberCoroutineScope()
    val selectedCsv by prefs.text(key, "").collectAsState(initial = "")
    val selected = remember(selectedCsv) { selectedCsv.split(",").filter { it.isNotBlank() }.toSet() }

    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        (1..6).map { "Flag $it" }.forEach { flag ->
            FilterChip(
                selected = flag in selected,
                onClick = {
                    val next = if (flag in selected) selected - flag else selected + flag
                    scope.launch { prefs.setText(key, next.joinToString(",")) }
                },
                label = { Text(flag) }
            )
        }
    }
}
