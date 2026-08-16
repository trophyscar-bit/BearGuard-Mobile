package com.bearguard.mobile.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bearguard.mobile.city.ReorderablePriorityList
import com.bearguard.mobile.config.ConfigCheckboxRow
import com.bearguard.mobile.config.ConfigDropdownRow
import com.bearguard.mobile.config.ConfigSectionHeader

/** Mirrors ExpertsLayout.fxml, including the real drag-reorder skill-training priority list. */
@Composable
fun ExpertsConfigScreen() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ConfigSectionHeader("Experts")
        Column(modifier = Modifier.padding(16.dp)) {
            ConfigCheckboxRow("ex_claim_intel", "Claim Intel (Agnes)")
            ConfigCheckboxRow("ex_claim_loyalty_tag", "Claim Loyalty Tag (Romulus)")
            ConfigCheckboxRow("ex_claim_troops", "Claim Troops (Romulus)")
            ConfigDropdownRow("ex_troop_type", "Troop Type", listOf("Infantry", "Lancer", "Marksman"))
            ConfigCheckboxRow("ex_enable_skill_training", "Enable Expert Skill Training")

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            Text("Skill Training Priorities", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            ReorderablePriorityList(
                prefsKey = "ex_skill_priority_order",
                defaultOrder = listOf(
                    "combat" to "Combat", "development" to "Development", "leadership" to "Leadership",
                ),
            )
        }
    }
}
