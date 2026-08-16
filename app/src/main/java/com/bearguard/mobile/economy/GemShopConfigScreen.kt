package com.bearguard.mobile.economy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bearguard.mobile.config.ConfigCheckboxRow
import com.bearguard.mobile.config.ConfigSectionHeader
import com.bearguard.mobile.scheduler.TaskLiveStatusRow

/** Mirrors GemShopLayout.fxml -- top-right cart-icon panel. Daily Deals' free-chest claim is the
 * one real piece, reusing the already-ported DailyDealsRoutine ("deals" task). */
@Composable
fun GemShopConfigScreen() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ConfigSectionHeader("Gem Shop")
        Column(modifier = Modifier.padding(16.dp)) {
            ConfigCheckboxRow("gs_custom_armament_chest", "Claim free chest badge (Custom Armament)")
            Spacer(Modifier.height(16.dp))
            TaskLiveStatusRow("deals", "Claim free chest badge (Daily Deals)")
        }
    }
}
