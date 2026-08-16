package com.bearguard.mobile.troops

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bearguard.mobile.config.ConfigCheckboxRow
import com.bearguard.mobile.config.ConfigDropdownRow
import com.bearguard.mobile.config.ConfigSectionHeader

/** Mirrors PetsLayout.fxml. Config-only. */
@Composable
fun PetsConfigScreen() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ConfigSectionHeader("Pets")
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Pet Automation", style = MaterialTheme.typography.titleSmall)
            ConfigCheckboxRow("pt_enable_management", "Enable Pet Management")
            ConfigCheckboxRow("pt_alliance_treasure", "Alliance Treasure")
            ConfigCheckboxRow("pt_personal_treasure", "Personal Treasure")

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            Text("Skill Configuration", style = MaterialTheme.typography.titleSmall)
            ConfigCheckboxRow("pt_food_skill", "Food Skill")
            ConfigCheckboxRow("pt_gathering_skill", "Gathering Skill")
            ConfigCheckboxRow("pt_stamina_skill", "Stamina Skill")
            ConfigCheckboxRow("pt_treasure_skill", "Treasure Skill")
            ConfigDropdownRow("pt_gathering_resource", "Gathering Resource",
                listOf("Meat", "Wood", "Coal", "Iron"))
        }
    }
}
