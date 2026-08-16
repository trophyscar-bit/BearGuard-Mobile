package com.bearguard.mobile.economy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bearguard.mobile.config.ConfigCheckboxRow
import com.bearguard.mobile.config.ConfigSectionHeader

/** Mirrors ShopLayout.fxml (Nomadic Merchant + Mystery Shop). Config-only -- neither routine is
 * ported yet. */
@Composable
fun GeneralShopConfigScreen() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ConfigSectionHeader("General Shop")
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Nomadic Merchant", style = MaterialTheme.typography.titleSmall)
            ConfigCheckboxRow("gen_nomadic_buy_resources", "Buy resources")
            ConfigCheckboxRow("gen_nomadic_buy_vip", "Buy VIP points")

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            Text("Mystery Shop", style = MaterialTheme.typography.titleSmall)
            ConfigCheckboxRow("gen_mystery_shop_enable", "Enable Mystery Shop")
            ConfigCheckboxRow("gen_mystery_shop_50_gear", "Buy 50% discounted hero gear (250 badges)")
        }
    }
}
