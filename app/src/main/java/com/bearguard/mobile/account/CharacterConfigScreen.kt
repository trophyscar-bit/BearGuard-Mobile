package com.bearguard.mobile.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bearguard.mobile.config.ConfigCheckboxRow
import com.bearguard.mobile.config.ConfigSectionHeader
import com.bearguard.mobile.config.ConfigTextFieldRow

/** Mirrors CharacterLayout.fxml. Config-only. */
@Composable
fun CharacterConfigScreen() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ConfigSectionHeader("Character")
        Column(modifier = Modifier.padding(16.dp)) {
            ConfigCheckboxRow("ch_enable_create_character", "Enable Character Creation")
            ConfigCheckboxRow("ch_skip_tutorial", "Skip Tutorial")
            ConfigTextFieldRow("ch_max_age_minutes", "Max Age (minutes)")
        }
    }
}
