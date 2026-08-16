package com.bearguard.mobile.troops

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bearguard.mobile.config.ConfigCheckboxRow
import com.bearguard.mobile.config.ConfigDropdownRow
import com.bearguard.mobile.config.ConfigSectionHeader
import com.bearguard.mobile.config.ConfigTextFieldRow

/** Mirrors PolarTerrorLayout.fxml -- 3 real tabs. Config-only; deploying real troops to rallies
 * carries real risk (matt flagged this earlier this session), so none of this is wired to
 * automation without his explicit go-ahead. */
@Composable
fun RallyConfigScreen() {
    var tab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        ConfigSectionHeader("Rally")
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Polar Terror") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Manual Rally Join") })
            Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Host Rally") })
        }
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            when (tab) {
                0 -> PolarTerrorTab()
                1 -> ManualRallyJoinTab()
                else -> HostRallyTab()
            }
        }
    }
}

private val FLAGS = (1..6).map { "Flag $it" } + "No Flag"

@Composable
private fun PolarTerrorTab() {
    ConfigCheckboxRow("rl_pt_enable", "Enable Polar Terror Hunting")
    ConfigDropdownRow("rl_pt_mode", "Operation Mode", listOf("Manual", "Auto"))
    ConfigDropdownRow("rl_pt_level", "Level", (1..10).map { it.toString() })
    ConfigCheckboxRow("rl_pt_highest_level", "Always use the highest available level")
    ConfigDropdownRow("rl_pt_marches", "Number of Marches", (1..6).map { it.toString() })
    ConfigTextFieldRow("rl_pt_stamina_reserve", "Stamina Reserve")
    ConfigCheckboxRow("rl_pt_use_stamina_items", "Use stamina items when needed")
    ConfigTextFieldRow("rl_pt_stamina_item_reserve", "Item Reserve")
    Spacer(Modifier.height(12.dp))
    Text("March Flags", style = MaterialTheme.typography.titleSmall)
    (1..6).forEach { n -> ConfigDropdownRow("rl_pt_march${n}_flag", "March $n Flag", FLAGS) }
}

@Composable
private fun ManualRallyJoinTab() {
    ConfigCheckboxRow("rl_mr_enable", "Manual Rally Join (Preemptive)")
    ConfigDropdownRow("rl_mr_mode", "Operation Mode", listOf("Manual", "Auto"))
    ConfigDropdownRow("rl_mr_marches", "Number of Marches", (1..6).map { it.toString() })
    ConfigDropdownRow("rl_mr_target", "Target", listOf("Everything", "Berserk Cryptid", "Cave Lion", "Snow Ape"))
    Spacer(Modifier.height(12.dp))
    Text("March Flags", style = MaterialTheme.typography.titleSmall)
    (1..6).forEach { n -> ConfigDropdownRow("rl_mr_march${n}_flag", "March $n Flag", FLAGS) }
}

@Composable
private fun HostRallyTab() {
    ConfigCheckboxRow("rl_hr_enable", "Berserk Cryptid Hosting (Gina's Revenge)")
    ConfigDropdownRow("rl_hr_runs", "Runs per cycle", (1..10).map { it.toString() })
    Text(
        "Cost: 25 stamina + 1 Horn per run.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    ConfigCheckboxRow("rl_hr_use_stamina_items", "Open Chief Stamina cans when short")
    ConfigDropdownRow("rl_hr_flag", "Flag", FLAGS)
}
