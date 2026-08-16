package com.bearguard.mobile.alliance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bearguard.mobile.city.ReorderablePriorityList
import com.bearguard.mobile.config.ConfigCheckboxRow
import com.bearguard.mobile.config.ConfigDropdownRow
import com.bearguard.mobile.config.ConfigSectionHeader
import com.bearguard.mobile.config.ConfigTextFieldRow

/** Mirrors AllianceShop.fxml, including the real drag-reorder purchase-priority list. */
@Composable
fun AllianceShopConfigScreen() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ConfigSectionHeader("Alliance Shop")
        Column(modifier = Modifier.padding(16.dp)) {
            ConfigCheckboxRow("as_enable", "Enable Alliance Shop")
            ConfigTextFieldRow("as_min_coins_activate", "Min Coins to Activate")
            ConfigTextFieldRow("as_min_coins", "Min Coins")
            ConfigDropdownRow("as_min_pct", "Min Percentage", listOf("10%", "25%", "50%", "75%", "100%"))

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            Text("Purchase Priority", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "Drag items to reorder purchase priority. Higher items are bought first.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(8.dp))
            ReorderablePriorityList(
                prefsKey = "as_purchase_priority_order",
                defaultOrder = listOf(
                    "speedups" to "Speedups", "gear" to "Gear", "resources" to "Resources",
                    "gems" to "Gems", "other" to "Other",
                ),
            )
        }
    }
}
