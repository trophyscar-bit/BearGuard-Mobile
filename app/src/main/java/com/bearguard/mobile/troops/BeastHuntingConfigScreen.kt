package com.bearguard.mobile.troops

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

/** Mirrors BeastHuntingLayout.fxml. Config-only. */
@Composable
fun BeastHuntingConfigScreen() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ConfigSectionHeader("Beast Hunting")
        Column(modifier = Modifier.padding(16.dp)) {
            ConfigCheckboxRow("bh_enable", "Enable Beast Hunting")
            ConfigDropdownRow("bh_marches", "Active Marches", (1..6).map { it.toString() })
            ConfigDropdownRow("bh_level", "Beast Level", (1..10).map { it.toString() })
            ConfigTextFieldRow("bh_stamina_reserve", "Stamina Reserve")
        }
    }
}
