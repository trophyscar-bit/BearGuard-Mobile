package com.bearguard.mobile.alliance

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

/** Mirrors AllianceMobilizationLayout.fxml. Config-only. */
@Composable
fun AllianceMobilizationConfigScreen() {
    val missionTypes = listOf(
        "am_gather" to "Gathering", "am_beast_slay" to "Beast Slaying", "am_train" to "Training",
        "am_build_speedups" to "Build Speedups", "am_training_speedups" to "Training Speedups",
        "am_use_speedups" to "Use Speedups", "am_use_gems" to "Use Gems", "am_buy_package" to "Buy Package",
        "am_chief_gear_charm" to "Chief Gear Charm", "am_chief_gear_score" to "Chief Gear Score",
        "am_hero_gear_stone" to "Hero Gear Stone", "am_mythic_shard" to "Mythic Shard",
        "am_fire_crystal" to "Fire Crystal", "am_rally" to "Rally", "am_auto_accept" to "Auto Accept",
        "am_use_gems_accept" to "Use Gems for Accept",
    )

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ConfigSectionHeader("Alliance Mobilization")
        Column(modifier = Modifier.padding(16.dp)) {
            ConfigCheckboxRow("am_enable", "Enable Alliance Mobilization")
            Spacer(Modifier.height(12.dp))
            Text("Mission Types", style = MaterialTheme.typography.titleSmall)
            missionTypes.forEach { (key, label) -> ConfigCheckboxRow(key, label) }
            Spacer(Modifier.height(12.dp))
            ConfigDropdownRow("am_rewards_pct", "Rewards %", listOf("25%", "50%", "75%", "100%"))
            ConfigTextFieldRow("am_min_points_200", "Min Points (200)")
            ConfigTextFieldRow("am_min_points_120", "Min Points (120)")
        }
    }
}
