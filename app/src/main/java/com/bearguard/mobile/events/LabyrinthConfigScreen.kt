package com.bearguard.mobile.events

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
import com.bearguard.mobile.config.ConfigTextFieldRow

/** Mirrors LabyrinthLayout.fxml. Config-only -- real automation for the Windows Labyrinth
 * routine (DailyLabyrinthRoutine.java) isn't ported. Gaia Heart deliberately has no controls
 * here either, matching the Windows comment that it uses whatever formation is already in-game. */
@Composable
fun LabyrinthConfigScreen() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ConfigSectionHeader("Labyrinth")
        Column(modifier = Modifier.padding(16.dp)) {
            ConfigCheckboxRow("lb_enable", "Enable Labyrinth")
            ConfigCheckboxRow("lb_formation_test", "Formation test mode")
            ConfigDropdownRow("lb_generation", "Generation", listOf("1", "2", "3"))
            ConfigTextFieldRow("lb_daily_start_time", "Daily kickoff time (local, HH:mm)", "12:00", numeric = false)

            TwoSquadRatioCard("Land of Heroes", "lb_loh")
            TwoSquadRatioCard("Cave of Monsters", "lb_cave")
            TwoSquadRatioCard("Charm Mine", "lb_charm")
            SingleRatioCard("Research Center", "lb_research", "50", "20", "30")
            SingleRatioCard("Gear Forge", "lb_gearforge", "60", "10", "30")

            Spacer(Modifier.height(8.dp))
            Text(
                "Gaia Heart isn't configured here -- it uses whatever formation is already in-game.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun TwoSquadRatioCard(title: String, keyPrefix: String) {
    Spacer(Modifier.height(16.dp))
    HorizontalDivider()
    Spacer(Modifier.height(12.dp))
    Text("$title — Troop Ratios", style = MaterialTheme.typography.titleSmall)
    Spacer(Modifier.height(4.dp))
    Text("Squad 1 (frontline/tank)", style = MaterialTheme.typography.labelLarge)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(1f)) { ConfigTextFieldRow("${keyPrefix}_s1_inf", "Infantry %", "60") }
        Box(Modifier.weight(1f)) { ConfigTextFieldRow("${keyPrefix}_s1_lan", "Lancer %", "40") }
        Box(Modifier.weight(1f)) { ConfigTextFieldRow("${keyPrefix}_s1_mrk", "Marksman %", "0") }
    }
    Text("Squad 2 (marksman/hybrid)", style = MaterialTheme.typography.labelLarge)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(1f)) { ConfigTextFieldRow("${keyPrefix}_s2_inf", "Infantry %", "50") }
        Box(Modifier.weight(1f)) { ConfigTextFieldRow("${keyPrefix}_s2_lan", "Lancer %", "0") }
        Box(Modifier.weight(1f)) { ConfigTextFieldRow("${keyPrefix}_s2_mrk", "Marksman %", "50") }
    }
}

@Composable
private fun SingleRatioCard(title: String, keyPrefix: String, defInf: String, defLan: String, defMrk: String) {
    Spacer(Modifier.height(16.dp))
    HorizontalDivider()
    Spacer(Modifier.height(12.dp))
    Text("$title — Default Troop Ratio", style = MaterialTheme.typography.titleSmall)
    Spacer(Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(1f)) { ConfigTextFieldRow("${keyPrefix}_inf", "Infantry %", defInf) }
        Box(Modifier.weight(1f)) { ConfigTextFieldRow("${keyPrefix}_lan", "Lancer %", defLan) }
        Box(Modifier.weight(1f)) { ConfigTextFieldRow("${keyPrefix}_mrk", "Marksman %", defMrk) }
    }
}
