package com.bearguard.mobile.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bearguard.mobile.config.ConfigCheckboxRow
import com.bearguard.mobile.config.ConfigDropdownRow
import com.bearguard.mobile.config.ConfigSectionHeader
import com.bearguard.mobile.config.ConfigTextFieldRow

/** Mirrors EventsLayout.fxml -- 5 tabs, all config-only. */
@Composable
fun EventsConfigScreen() {
    var tab by remember { mutableStateOf(0) }
    val titles = listOf("Myriad Bazaar", "Journey of Light", "Mercenary Event", "Hero's Mission", "Tundra Truck")

    Column(modifier = Modifier.fillMaxSize()) {
        ConfigSectionHeader("Events")
        ScrollableTabRow(selectedTabIndex = tab) {
            titles.forEachIndexed { i, title ->
                Tab(selected = tab == i, onClick = { tab = i }, text = { Text(title) })
            }
        }
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            when (tab) {
                0 -> ConfigCheckboxRow("ev_myriad_bazaar", "Myriad Bazaar")
                1 -> ConfigCheckboxRow("ev_journey_of_light", "Journey of Light")
                2 -> {
                    ConfigCheckboxRow("ev_mercenary_event", "Mercenary Event")
                    ConfigDropdownRow("ev_mercenary_flag", "Flag Selection", FLAGS)
                }
                3 -> {
                    ConfigCheckboxRow("ev_hero_mission", "Hero's Mission Event")
                    ConfigDropdownRow("ev_hero_mission_flag", "Flag Selection", FLAGS)
                }
                4 -> {
                    ConfigCheckboxRow("ev_tundra_event", "Tundra Truck Event")
                    ConfigCheckboxRow("ev_tundra_use_gems", "Use gems for refresh")
                    ConfigCheckboxRow("ev_tundra_ssr", "Go only for SSR trucks")
                    ConfigCheckboxRow("ev_tundra_use_activation_hour", "Use activation time")
                    ConfigTextFieldRow("ev_tundra_activation_hour", "Activation time (UTC), HH:mm", numeric = false)
                }
            }
        }
    }
}

internal val FLAGS = (1..6).map { "Flag $it" } + "No Flag"
