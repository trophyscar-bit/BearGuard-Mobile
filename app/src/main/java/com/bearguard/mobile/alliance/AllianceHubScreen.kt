package com.bearguard.mobile.alliance

import androidx.compose.runtime.Composable
import com.bearguard.mobile.hub.HubScreen

/** Mirrors installTabbedHub("Alliance", ...): Alliance, Alliance Championship,
 * Alliance Mobilization, Alliance Shop. */
@Composable
fun AllianceHubScreen() {
    HubScreen("Alliance", listOf("Alliance", "Alliance Championship", "Alliance Mobilization", "Alliance Shop")) { name ->
        when (name) {
            "Alliance" -> AllianceConfigScreen()
            "Alliance Championship" -> AllianceChampionshipConfigScreen()
            "Alliance Mobilization" -> AllianceMobilizationConfigScreen()
            "Alliance Shop" -> AllianceShopConfigScreen()
        }
    }
}
