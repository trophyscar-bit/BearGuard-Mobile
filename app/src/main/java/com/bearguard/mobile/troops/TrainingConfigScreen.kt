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

/** Mirrors TrainingLayout.fxml. Config-only -- includes the separate "Heal Injured Troops"
 * toggle (matches HealInjuredRoutine.kt, ported but not yet wired into the scheduler). */
@Composable
fun TrainingConfigScreen() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ConfigSectionHeader("Training")
        Column(modifier = Modifier.padding(16.dp)) {
            ConfigCheckboxRow("tr_enable", "Enable Training")
            Spacer(Modifier.height(8.dp))
            ConfigCheckboxRow("tr_train_infantry", "Train Infantry")
            ConfigCheckboxRow("tr_train_marksman", "Train Marksmen")
            ConfigCheckboxRow("tr_train_lancer", "Train Lancers")
            Spacer(Modifier.height(8.dp))
            ConfigCheckboxRow("tr_prioritize_promotion", "Prioritize Troop Promotion")
            ConfigCheckboxRow("tr_appoint_minister", "Appoint Minister of Education")

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            Text("Heal Injured Troops", style = MaterialTheme.typography.titleSmall)
            Text(
                "Ported (HealInjuredRoutine.kt) but not yet wired into the engine -- no idle " +
                    "queue was available live to build the actual heal-start flow against.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            ConfigCheckboxRow("tr_heal_injured", "Enable Heal Injured Troops")
        }
    }
}
