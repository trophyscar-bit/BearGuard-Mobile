package com.bearguard.mobile.troops

import androidx.compose.runtime.Composable
import com.bearguard.mobile.hub.HubScreen

/** Mirrors installTabbedHub("Troops", ...): Training, Gather, Intel, Rally, Beast Hunting,
 * Chief Order, Pets. */
@Composable
fun TroopsHubScreen() {
    HubScreen(
        "Troops",
        listOf("Training", "Gather", "Intel", "Rally", "Beast Hunting", "Chief Order", "Pets")
    ) { name ->
        when (name) {
            "Training" -> TrainingConfigScreen()
            "Gather" -> GatherConfigScreen()
            "Intel" -> IntelConfigScreen()
            "Rally" -> RallyConfigScreen()
            "Beast Hunting" -> BeastHuntingConfigScreen()
            "Chief Order" -> ChiefOrderConfigScreen()
            "Pets" -> PetsConfigScreen()
        }
    }
}
