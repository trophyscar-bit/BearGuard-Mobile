package com.bearguard.mobile.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bearguard.mobile.config.ConfigCheckboxRow
import com.bearguard.mobile.config.ConfigSectionHeader

/** Mirrors SkipTutorialLayout.fxml. Config-only. */
@Composable
fun SkipTutorialConfigScreen() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ConfigSectionHeader("Skip Tutorial")
        Column(modifier = Modifier.padding(16.dp)) {
            ConfigCheckboxRow("st_enable", "Enable Skip Tutorial")
            Spacer(Modifier.height(8.dp))
            Text(
                "Use with caution on new accounts.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
