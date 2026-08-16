package com.bearguard.mobile.alliance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bearguard.mobile.config.ConfigCheckboxRow
import com.bearguard.mobile.config.ConfigCheckboxWithOffsetRow
import com.bearguard.mobile.config.ConfigDropdownRow
import com.bearguard.mobile.config.ConfigRadioGroupRow
import com.bearguard.mobile.config.ConfigSectionHeader
import com.bearguard.mobile.scheduler.TaskLiveStatusRow

/** Mirrors AllianceLayout.fxml ("Alliance Workbench"). "Claim chests" is the one real piece,
 * reusing AllianceChestRoutine ("alliance_chests" task). */
@Composable
fun AllianceConfigScreen() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ConfigSectionHeader("Alliance Workbench")
        Column(modifier = Modifier.padding(16.dp)) {
            ConfigCheckboxWithOffsetRow("all_tech_contribution", "Tech contribution", "all_tech_offset")
            Spacer(Modifier.height(8.dp))
            TaskLiveStatusRow("alliance_chests", "Claim chests")
            Spacer(Modifier.height(8.dp))
            ConfigCheckboxRow("all_honor_chest", "Claim honor chest")
            ConfigCheckboxWithOffsetRow("all_triumph", "Claim Triumph", "all_triumph_offset")
            ConfigCheckboxWithOffsetRow("all_allies_essence", "Claim allies essence", "all_allies_essence_offset")

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            Text("Rally Autojoin", style = MaterialTheme.typography.titleSmall)
            ConfigCheckboxRow("all_autojoin_enable", "Enable Autojoin")
            ConfigDropdownRow("all_autojoin_queues", "Queues", listOf("1", "2", "3", "4", "5", "6"))

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            Text("Troop Source", style = MaterialTheme.typography.titleSmall)
            ConfigRadioGroupRow("all_troop_source", listOf("All Troops", "Use Formation"), "All Troops")
            ConfigCheckboxRow("all_help_requests", "Help alliance requests")
        }
    }
}
