package com.bearguard.mobile.scheduler

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bearguard.mobile.service.BearGuardAccessibilityService
import kotlinx.coroutines.launch

/**
 * matt/2026-08-16: the compact, embeddable version of ModuleTaskScreen -- a single toggle +
 * status row for a real ported routine, dropped inline into whichever config screen now matches
 * its real Windows location (e.g. Chief Order's toggle lives on the Troops > Chief Order screen,
 * not a standalone top-level entry). Looks up the task by key from TaskRegistry.
 */
@Composable
fun TaskLiveStatusRow(taskKey: String, title: String) {
    val task = remember { TaskRegistry.all.firstOrNull { it.key == taskKey } } ?: return
    val context = LocalContext.current
    val prefs = remember { SchedulerPrefs(context) }
    val scope = rememberCoroutineScope()
    val enabled by prefs.enabled(task.key).collectAsState(initial = false)
    val lastResult by prefs.lastResult(task.key).collectAsState(initial = "")
    val serviceConnected by BearGuardAccessibilityService.connected.collectAsState()

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(
                    "Real automation -- the engine runs this on its own schedule while enabled.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (lastResult.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(lastResult, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary)
                }
            }
            Switch(
                checked = enabled,
                onCheckedChange = { checked -> scope.launch { prefs.setEnabled(task.key, checked) } },
                enabled = serviceConnected
            )
        }
    }
}
