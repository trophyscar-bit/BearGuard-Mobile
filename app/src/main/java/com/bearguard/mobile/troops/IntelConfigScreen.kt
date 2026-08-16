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

/** Mirrors IntelLayout.fxml. Config-only. */
@Composable
fun IntelConfigScreen() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ConfigSectionHeader("Intel")
        Column(modifier = Modifier.padding(16.dp)) {
            ConfigCheckboxRow("in_enable", "Enable Intel Runs")
            Spacer(Modifier.height(12.dp))
            Text("Categories", style = MaterialTheme.typography.titleSmall)
            ConfigCheckboxRow("in_fire_crystal_era", "Fire Crystal Era")
            ConfigCheckboxRow("in_fire_beast", "Fire Beast")
            ConfigCheckboxRow("in_beast", "Beast")
            ConfigCheckboxRow("in_survivors", "Survivors")
            ConfigCheckboxRow("in_journey", "Journey")

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            Text("Processing", style = MaterialTheme.typography.titleSmall)
            ConfigCheckboxRow("in_smart_processing", "Use Smart Intel Processing")
            ConfigCheckboxRow("in_recall_gather", "Recall Gather Troops")
            ConfigCheckboxRow("in_use_flag", "Use Beast Flag")
            ConfigDropdownRow("in_beast_flag", "Flag", (1..6).map { "Flag $it" } + "No Flag")
        }
    }
}
