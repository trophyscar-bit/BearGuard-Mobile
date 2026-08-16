package com.bearguard.mobile.city

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bearguard.mobile.config.ConfigCheckboxRow
import com.bearguard.mobile.config.ConfigCheckboxWithOffsetRow
import com.bearguard.mobile.config.ConfigSectionHeader

/**
 * matt/2026-08-15: mirrors CityEventsLayout.fxml ("City Events & Missions") row for row. Only
 * "Claim mail rewards" has a real routine behind it on Android (MailRewardsRoutine, reachable
 * from the Mail Rewards module) -- everything else here is config-only until ported. Persists
 * real state regardless so nothing is lost once the automation catches up.
 */
@Composable
fun CityEventsConfigScreen() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ConfigSectionHeader("City Events & Missions")
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Configure automated tasks for internal city events",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            HorizontalDivider()

            ConfigCheckboxWithOffsetRow("ce_daily_mission", "Claim daily missions", "ce_daily_mission_offset")
            ConfigCheckboxRow("ce_auto_schedule_daily_mission", "Auto schedule daily mission claim")

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            ConfigCheckboxRow("ce_do_exploration", "Do Exploration")
            ConfigCheckboxWithOffsetRow("ce_exploration_chest", "Claim exploration chest", "ce_exploration_offset")

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            ConfigCheckboxWithOffsetRow(
                "ce_mail_rewards", "Claim mail rewards (real -- see Mail Rewards module)", "ce_mail_offset"
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            ConfigCheckboxWithOffsetRow("ce_life_essence", "Claim life essence", "ce_life_essence_offset")
            ConfigCheckboxRow("ce_weekly_scroll", "Buy weekly scroll")

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            ConfigCheckboxRow("ce_crystal_lab_fc", "Claim crystal lab FC")
            ConfigCheckboxRow("ce_daily_discounted_rfc", "Claim daily 50% RFC")
            ConfigCheckboxRow("ce_war_academy_shards", "Claim war academy crystal shards")

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            ConfigCheckboxRow(
                "ce_monument", "Explore the World (Monument): claim, Fragment Packs, Alliance Trade"
            )
        }
    }
}
