package com.bearguard.mobile.city

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bearguard.mobile.config.ConfigCheckboxRow
import com.bearguard.mobile.config.ConfigPrefs
import com.bearguard.mobile.config.ConfigSectionHeader
import kotlinx.coroutines.launch

/**
 * matt/2026-08-15: mirrors CityEventsExtraLayout.fxml's two inner tabs (Arena, Other Tasks).
 * Config-only for now -- all-new checkboxes, none wired to automation yet.
 */
@Composable
fun CityEventsExtraConfigScreen() {
    var tab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        ConfigSectionHeader("Extra City Events")
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Arena") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Other Tasks") })
        }
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            if (tab == 0) ArenaTab() else OtherTasksTab()
        }
    }
}

@Composable
private fun ArenaTab() {
    val context = LocalContext.current
    val prefs = remember { ConfigPrefs(context) }
    val scope = rememberCoroutineScope()

    Text(
        "Arena target filters use the selected profile's Character Information: Alliance and Server.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(12.dp))
    HorizontalDivider()
    Spacer(Modifier.height(8.dp))

    ConfigCheckboxRow("cee_arena_enable", "Enable arena")

    val activationHour by prefs.text("cee_arena_activation_hour", "").collectAsState(initial = "")
    OutlinedTextField(
        value = activationHour,
        onValueChange = { scope.launch { prefs.setText("cee_arena_activation_hour", it) } },
        label = { Text("Activation time (UTC), HH:mm") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    )

    val extraAttempts by prefs.text("cee_arena_extra_attempts", "0").collectAsState(initial = "0")
    OutlinedTextField(
        value = extraAttempts,
        onValueChange = { v -> scope.launch { prefs.setText("cee_arena_extra_attempts", v.filter(Char::isDigit)) } },
        label = { Text("Extra attempts") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    )

    ConfigCheckboxRow("cee_arena_paid_refresh", "Allow paid list refreshes")
}

@Composable
private fun OtherTasksTab() {
    Text(
        "Additional daily operations and tasks",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(12.dp))
    HorizontalDivider()
    Spacer(Modifier.height(8.dp))

    ConfigCheckboxRow("cee_daily_vip_rewards", "Claim daily vip rewards")
    ConfigCheckboxRow("cee_storehouse_chest", "Claim storehouse chests + stamina")
    ConfigCheckboxRow("cee_hero_recruitment", "Open recruitment hero chests")
    ConfigCheckboxRow("cee_daily_labyrinth", "Complete daily labyrinth")
    ConfigCheckboxRow("cee_buy_monthly_vip", "Buy monthly vip (10k gems)")
    ConfigCheckboxRow("cee_trek_supplies", "Claim Trek Supplies")
    ConfigCheckboxRow("cee_trek_automation", "Tundra Trek Automation")
}
