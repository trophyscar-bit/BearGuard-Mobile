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
