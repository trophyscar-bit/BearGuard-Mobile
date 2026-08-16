package com.bearguard.mobile.deals

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bearguard.mobile.service.BearGuardAccessibilityService
import kotlinx.coroutines.launch

/** matt/2026-08-15: third real ported module. Single free-chest claim button. */
@Composable
fun DealsScreen() {
    val scope = rememberCoroutineScope()
    var running by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<DailyDealsRoutine.Result?>(null) }
    val serviceConnected = BearGuardAccessibilityService.instance != null

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CardGiftcard, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Deals", style = MaterialTheme.typography.titleLarge)
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
                "Opens the cart icon's Daily Deals tab and claims the once-a-day Free chest " +
                    "badge, if it's still there. Never touches the paid packs. Run from World.",
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
                    Text("Daily Deals: Free Chest", style = MaterialTheme.typography.titleSmall)
                    result?.let {
                        Spacer(Modifier.height(4.dp))
                        val (label, color) = when (it) {
                            is DailyDealsRoutine.Result.Claimed ->
                                "Claimed" to MaterialTheme.colorScheme.secondary
                            is DailyDealsRoutine.Result.AlreadyClaimed ->
                                "Already claimed today" to MaterialTheme.colorScheme.onSurfaceVariant
                            is DailyDealsRoutine.Result.Failed ->
                                it.reason to MaterialTheme.colorScheme.error
                        }
                        Text(label, style = MaterialTheme.typography.bodySmall, color = color)
                    }
                }
                Button(
                    onClick = {
                        running = true
                        scope.launch {
                            result = DailyDealsRoutine.run()
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
