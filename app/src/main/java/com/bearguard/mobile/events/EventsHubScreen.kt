package com.bearguard.mobile.events

import androidx.compose.runtime.Composable
import com.bearguard.mobile.alliance.AllianceChampionshipConfigScreen
import com.bearguard.mobile.alliance.AllianceMobilizationConfigScreen
import com.bearguard.mobile.hub.HubScreen

/**
 * Mirrors installEventsHub(...): Events, Alliance Championship, Alliance Mobilization, Bear
 * Trap, Fishing Tournament, Labyrinth. Windows' comment explains this is a side-nav list (not
 * tabs) specifically because it grew too wide for a horizontal TabPane -- same shape HubScreen
 * already provides everywhere. Alliance Championship/Mobilization are shared screens -- same
 * composables reused here and under the Alliance hub, same as Windows reuses the same loaded
 * root in both places.
 */
@Composable
fun EventsHubScreen() {
    HubScreen(
        "Events",
        listOf("Events", "Alliance Championship", "Alliance Mobilization", "Bear Trap", "Fishing Tournament", "Labyrinth")
    ) { name ->
        when (name) {
            "Events" -> EventsConfigScreen()
            "Alliance Championship" -> AllianceChampionshipConfigScreen()
            "Alliance Mobilization" -> AllianceMobilizationConfigScreen()
            "Bear Trap" -> BearTrapConfigScreen()
            "Fishing Tournament" -> FishingConfigScreen()
            "Labyrinth" -> LabyrinthConfigScreen()
        }
    }
}
