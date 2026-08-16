package com.bearguard.mobile.city

import androidx.compose.runtime.Composable
import com.bearguard.mobile.hub.HubScreen

/** matt/2026-08-15/16: mirrors installTabbedHub("City", ...) exactly. Now built on the shared
 * HubScreen composable (originally bespoke here; generalized once every other hub needed the
 * same shape). */
@Composable
fun CityHubScreen() {
    HubScreen("City", listOf("City Upgrades", "City Events", "Extra City Events", "Research")) { name ->
        when (name) {
            "City Upgrades" -> CityUpgradesConfigScreen()
            "City Events" -> CityEventsConfigScreen()
            "Extra City Events" -> CityEventsExtraConfigScreen()
            "Research" -> ResearchConfigScreen()
        }
    }
}
