package com.bearguard.mobile.economy

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

/**
 * Mirrors DealsLayout.fxml -- Bank (deposit/compound automation) + Random Events (rotating
 * Events-tab claim checks). NOT the same as the "Daily Deals free chest" claim (that's
 * DailyDealsRoutine, which lives under Gem Shop -- matches the real Windows Gem Shop screen's
 * description, confirmed against the actual FXML). Config-only, neither BankRoutine nor
 * EventClaimRoutine is ported yet.
 */
@Composable
fun DealsConfigScreen() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ConfigSectionHeader("Deals")
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Bank", style = MaterialTheme.typography.titleSmall)
            Text(
                "Deposits into Deals > Bank, withdraws when mature, immediately redeposits -- " +
                    "the 1-day tier compounds daily at 5%.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            ConfigCheckboxRow("deals_bank_enable", "Enable Bank")
            ConfigDropdownRow("deals_bank_period", "Period", listOf("1 Day", "3 Days", "7 Days"))

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            Text("Random Events", style = MaterialTheme.typography.titleSmall)
            Text(
                "Rotating limited-time Events-tab events -- only checks for a ready Claim.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            ConfigCheckboxRow("deals_event_hall_of_chiefs", "Hall of Chiefs")
            ConfigCheckboxRow("deals_event_defeat_beasts", "Defeat Nearby Beasts")
            ConfigCheckboxRow("deals_event_hero_rally", "Hero Rally (Claim All)")
            ConfigCheckboxRow("deals_event_lucky_chip", "Lucky Chip Supply (free daily pack only)")
        }
    }
}
