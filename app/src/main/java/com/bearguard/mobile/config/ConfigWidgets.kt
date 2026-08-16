package com.bearguard.mobile.config

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/** A single persisted checkbox row -- the Android equivalent of a Windows fx:id CheckBox. */
@Composable
fun ConfigCheckboxRow(key: String, label: String, subtext: String? = null) {
    val context = LocalContext.current
    val prefs = remember { ConfigPrefs(context) }
    val scope = rememberCoroutineScope()
    val checked by prefs.bool(key).collectAsState(initial = false)

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = { scope.launch { prefs.setBool(key, it) } })
        Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            if (subtext != null) {
                Text(subtext, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** A checkbox paired with a small "Offset:" minutes text field, matching the Windows rows that
 * combine a CheckBox with a TextField for a scheduling offset. */
@Composable
fun ConfigCheckboxWithOffsetRow(key: String, label: String, offsetKey: String, offsetLabel: String = "Offset (min)") {
    val context = LocalContext.current
    val prefs = remember { ConfigPrefs(context) }
    val scope = rememberCoroutineScope()
    val checked by prefs.bool(key).collectAsState(initial = false)
    val offset by prefs.text(offsetKey, "60").collectAsState(initial = "60")

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = { scope.launch { prefs.setBool(key, it) } })
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f).padding(start = 4.dp))
        OutlinedTextField(
            value = offset,
            onValueChange = { v -> scope.launch { prefs.setText(offsetKey, v.filter(Char::isDigit)) } },
            label = { Text(offsetLabel, style = MaterialTheme.typography.labelSmall) },
            singleLine = true,
            modifier = Modifier.width(110.dp)
        )
    }
}

/** Section header matching the Windows "header-yellow" card title bar. */
@Composable
fun ConfigSectionHeader(title: String) {
    Surface(color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

/** Standalone persisted text field, matching a Windows TextField bound to a numeric/string
 * config key (march counts, stamina reserves, percentages, etc). */
@Composable
fun ConfigTextFieldRow(key: String, label: String, default: String = "", numeric: Boolean = true) {
    val context = LocalContext.current
    val prefs = remember { ConfigPrefs(context) }
    val scope = rememberCoroutineScope()
    val value by prefs.text(key, default).collectAsState(initial = default)

    OutlinedTextField(
        value = value,
        onValueChange = { v ->
            val filtered = if (numeric) v.filter { it.isDigit() || it == '.' } else v
            scope.launch { prefs.setText(key, filtered) }
        },
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    )
}

/** Persisted "dropdown" -- Android equivalent of a Windows ComboBox. Built as a simple
 * exposed-dropdown menu (no extra dependency): a read-only text field that opens a DropdownMenu
 * of the given options on tap. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ConfigDropdownRow(key: String, label: String, options: List<String>, default: String = "") {
    val context = LocalContext.current
    val prefs = remember { ConfigPrefs(context) }
    val scope = rememberCoroutineScope()
    val selected by prefs.text(key, default).collectAsState(initial = default)
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        scope.launch { prefs.setText(key, option) }
                        expanded = false
                    }
                )
            }
        }
    }
}

/** Persisted radio-button group, matching Windows RadioButton toggle groups (e.g. Troop Source:
 * All Troops / Use Formation). */
@Composable
fun ConfigRadioGroupRow(key: String, options: List<String>, default: String) {
    val context = LocalContext.current
    val prefs = remember { ConfigPrefs(context) }
    val scope = rememberCoroutineScope()
    val selected by prefs.text(key, default).collectAsState(initial = default)

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        options.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
            ) {
                RadioButton(
                    selected = selected == option,
                    onClick = { scope.launch { prefs.setText(key, option) } }
                )
                Text(option, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
