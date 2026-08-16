package com.bearguard.mobile.alliancechest

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bearguard.mobile.service.BearGuardAccessibilityService
import kotlinx.coroutines.launch

/** matt/2026-08-15: sixth real ported module. Claims Alliance Gift + Loot Chest tabs. */
@Composable
fun AllianceChestScreen() {
    val scope = rememberCoroutineScope()
    var running by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<AllianceChestRoutine.Result?>(null) }
    val serviceConnected = BearGuardAccessibilityService.instance != null

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Inventory2, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Alliance Chests", style = MaterialTheme.typography.titleLarge)
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
                "Alliance > Chests: taps Claim All on both the Alliance Gift and Loot Chest " +
                    "tabs. Safe no-op on tabs with nothing new. Run from anywhere.",
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
                    Text("Claim Both Tabs", style = MaterialTheme.typography.titleSmall)
                    result?.let {
                        Spacer(Modifier.height(4.dp))
                        if (it.failure != null) {
                            Text(it.failure, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error)
                        } else {
                            Text(
                                "Gift: ${if (it.giftClaimed) "claimed" else "nothing new"} · " +
                                    "Loot: ${if (it.lootClaimed) "claimed" else "nothing new"}",
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
                            result = AllianceChestRoutine.run()
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
