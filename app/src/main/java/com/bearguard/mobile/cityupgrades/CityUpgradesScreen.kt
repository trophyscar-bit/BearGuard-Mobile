package com.bearguard.mobile.cityupgrades

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bearguard.mobile.service.BearGuardAccessibilityService
import kotlinx.coroutines.launch

/**
 * matt/2026-08-15: seventh real ported module -- scoped v1, read-only. Shows both construction
 * queues' live status. The "start next upgrade" action isn't built yet (see CityUpgradesRoutine
 * doc) -- honest placeholder note shown here rather than a Run button that pretends to do more.
 */
@Composable
fun CityUpgradesScreen() {
    val scope = rememberCoroutineScope()
    var running by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<CityUpgradesRoutine.Result?>(null) }
    val serviceConnected = BearGuardAccessibilityService.instance != null

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.LocationCity, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("City Upgrades", style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(8.dp))

        if (!serviceConnected) {
            Text(
                "Accessibility service isn't connected -- enable BearGuard Mobile under " +
                    "Settings > Accessibility, with Whiteout Survival open.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            Text(
                "v1, read-only: opens the left-menu City tab and reads both construction " +
                    "queues' status. Starting a new upgrade when a queue goes idle isn't built " +
                    "yet -- no idle queue was available to calibrate that flow against live.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Read Construction Queues", style = MaterialTheme.typography.titleSmall)
                    result?.let { r ->
                        Spacer(Modifier.height(4.dp))
                        if (r.failure != null) {
                            Text(r.failure, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error)
                        } else {
                            Text(
                                "Queue 1: ${r.queue1?.status} ${r.queue1?.rawText ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                "Queue 2: ${r.queue2?.status} ${r.queue2?.rawText ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
                Button(
                    onClick = {
                        running = true
                        scope.launch {
                            result = CityUpgradesRoutine.run()
                            running = false
                        }
                    },
                    enabled = serviceConnected && !running
                ) {
                    if (running) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Run")
                    }
                }
            }
        }
    }
}
