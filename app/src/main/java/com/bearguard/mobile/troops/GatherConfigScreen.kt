package com.bearguard.mobile.troops

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bearguard.mobile.config.ConfigCheckboxRow
import com.bearguard.mobile.config.ConfigDropdownRow
import com.bearguard.mobile.config.ConfigSectionHeader

/** Mirrors GatherLayout.fxml. Config-only -- GatherRoutine.java is the largest/most stateful
 * routine in the codebase (deliberately not ported yet, see earlier session notes). The Java
 * version's debug-only "TEST: Trigger Preemption" checkbox is deliberately excluded here. */
@Composable
fun GatherConfigScreen() {
    val levels = (1..10).map { it.toString() }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ConfigSectionHeader("Gather")
        Column(modifier = Modifier.padding(16.dp)) {
            ConfigCheckboxRow("ga_enable", "Activate Gathering")
            Spacer(Modifier.height(8.dp))

            ConfigCheckboxRow("ga_meat", "Meat")
            ConfigDropdownRow("ga_meat_level", "Meat Level", levels)
            ConfigCheckboxRow("ga_wood", "Wood")
            ConfigDropdownRow("ga_wood_level", "Wood Level", levels)
            ConfigCheckboxRow("ga_coal", "Coal")
            ConfigDropdownRow("ga_coal_level", "Coal Level", levels)
            ConfigCheckboxRow("ga_iron", "Iron")
            ConfigDropdownRow("ga_iron_level", "Iron Level", levels)

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            Text("March Settings", style = MaterialTheme.typography.titleSmall)
            ConfigDropdownRow("ga_active_queues", "Active queues", (1..6).map { it.toString() })
            ConfigCheckboxRow("ga_only_full_resources", "Only search for full resources")
            ConfigCheckboxRow("ga_downgrade_level", "Downgrade level if no node is found")
            ConfigCheckboxRow("ga_remove_heroes", "Remove 2nd and 3rd hero from march")
            ConfigCheckboxRow(
                "ga_smart_priority", "Smart Gathering (prioritize scarcest resource by value)"
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            Text("Boosts", style = MaterialTheme.typography.titleSmall)
            ConfigCheckboxRow("ga_speed_boost", "Gather Speed Boost")
            ConfigDropdownRow("ga_speed_boost_type", "Boost Type", listOf("Free", "Paid"))
        }
    }
}
