package com.bearguard.mobile.account

import androidx.compose.runtime.Composable
import com.bearguard.mobile.hub.HubScreen

/** Mirrors installTabbedHub("Account", ...): Character, Skip Tutorial, Experts. */
@Composable
fun AccountHubScreen() {
    HubScreen("Account", listOf("Character", "Skip Tutorial", "Experts")) { name ->
        when (name) {
            "Character" -> CharacterConfigScreen()
            "Skip Tutorial" -> SkipTutorialConfigScreen()
            "Experts" -> ExpertsConfigScreen()
        }
    }
}
