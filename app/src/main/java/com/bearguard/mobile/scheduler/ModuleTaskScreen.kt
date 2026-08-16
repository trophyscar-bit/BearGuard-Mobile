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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * matt/2026-08-15: "I want a toggle button to turn whatever it is on and off, not just a run
 * queue... mirror the Windows version." Every scheduled module gets this exact same screen --
 * a switch bound to SchedulerPrefs, and the engine (BearGuardAccessibilityService.startEngine())
 * picks it up and runs it on its own schedule from then on. No manual trigger here anymore.
 */
@Composable
fun ModuleTaskScreen(task: RoutineTask) {
    val context = LocalContext.current
    val prefs = remember { SchedulerPrefs(context) }
    val scope = rememberCoroutineScope()

    val enabled by prefs.enabled(task.key).collectAsState(initial = false)
    val nextRunAt by prefs.nextRunAt(task.key).collectAsState(initial = 0L)
    val lastResult by prefs.lastResult(task.key).collectAsState(initial = "")
    val serviceConnected = BearGuardAccessibilityService.instance != null

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text(task.displayName, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        if (!serviceConnected) {
            Text(
                "Accessibility service isn't connected -- enable BearGuard Mobile under " +
                    "Settings > Accessibility.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(16.dp))
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Enabled", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "The engine runs this automatically on its own schedule while enabled.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = { checked ->
                        scope.launch { prefs.setEnabled(task.key, checked) }
                    },
                    enabled = serviceConnected
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        Text("Status", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(6.dp))
        Text(
            if (enabled) "Next run: ${formatWhen(nextRunAt)}" else "Disabled",
            style = MaterialTheme.typography.bodyMedium
        )
        if (lastResult.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                "Last result: $lastResult",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

private fun formatWhen(epochMillis: Long): String {
    if (epochMillis <= 0L) return "as soon as the engine ticks"
    val now = System.currentTimeMillis()
    if (epochMillis <= now) return "as soon as the engine ticks"
    val fmt = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return fmt.format(Date(epochMillis))
}
