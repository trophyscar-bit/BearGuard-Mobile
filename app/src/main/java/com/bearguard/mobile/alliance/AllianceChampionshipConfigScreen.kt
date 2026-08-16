package com.bearguard.mobile.alliance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bearguard.mobile.config.ConfigCheckboxRow
import com.bearguard.mobile.config.ConfigDropdownRow
import com.bearguard.mobile.config.ConfigSectionHeader
import com.bearguard.mobile.config.ConfigTextFieldRow

/** Mirrors AllianceChampionshipLayout.fxml. Config-only. Shared entry -- also reachable from
 * the Events hub in Windows; here it's just this one screen, reachable from Alliance. */
@Composable
fun AllianceChampionshipConfigScreen() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ConfigSectionHeader("Alliance Championship")
        Column(modifier = Modifier.padding(16.dp)) {
            ConfigCheckboxRow("ac_enable", "Enable Alliance Championship")
            ConfigCheckboxRow("ac_override_deploy", "Override Current Deployment")
            ConfigDropdownRow("ac_position", "Position", listOf("Front", "Middle", "Back"))
            ConfigDropdownRow("ac_flag", "Flag Selection", (1..6).map { "Flag $it" } + "No Flag")
            ConfigTextFieldRow("ac_infantry_pct", "Infantry %", "33")
            ConfigTextFieldRow("ac_lancer_pct", "Lancers %", "33")
            ConfigTextFieldRow("ac_marksman_pct", "Marksmen %", "34")
        }
    }
}
