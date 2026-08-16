package com.bearguard.mobile.city

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bearguard.mobile.config.ConfigCheckboxRow
import com.bearguard.mobile.config.ConfigSectionHeader

/**
 * matt/2026-08-15: mirrors ResearchLayout.fxml. The Windows version also has a drag-reorderable
 * priority list (PriorityListView) for research categories -- shown here as a plain static list
 * for v1 since drag-reorder isn't built yet; the checkbox itself is real config state.
 */
@Composable
fun ResearchConfigScreen() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ConfigSectionHeader("Enable Research")
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Automates research technology in your city",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            HorizontalDivider()

            ConfigCheckboxRow("research_enable", "Enable Research")

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            Text(
                "Research Priorities",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Drag-to-reorder priority list isn't built yet -- default order shown.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(8.dp))
            listOf("Economy", "Battle", "Defense", "Growth").forEachIndexed { i, category ->
                Text("${i + 1}. $category", style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}
