package com.bearguard.mobile.economy

import androidx.compose.runtime.Composable
import com.bearguard.mobile.giftcode.GiftcodeScreen
import com.bearguard.mobile.hub.HubScreen

/** Mirrors installTabbedHub("Economy", ...): Gem Shop, General Shop, Deals, Get Giftcodes. */
@Composable
fun EconomyHubScreen() {
    HubScreen("Economy", listOf("Gem Shop", "General Shop", "Deals", "Get Giftcodes")) { name ->
        when (name) {
            "Gem Shop" -> GemShopConfigScreen()
            "General Shop" -> GeneralShopConfigScreen()
            "Deals" -> DealsConfigScreen()
            "Get Giftcodes" -> GiftcodeScreen()
        }
    }
}
