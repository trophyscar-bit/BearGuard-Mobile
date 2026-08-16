package com.bearguard.mobile.chieforder

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bearguard.mobile.service.BearGuardAccessibilityService
import kotlinx.coroutines.launch

/**
 * matt/2026-08-15: second real ported module. One row per order type, a Run button that drives
 * ChiefOrderRoutine.run() against the live game and reports back what actually happened --
 * Enacted / Scheduled (with the OCR'd cover text) / Failed (with the reason), not a mockup.
 */
@Composable
fun ChiefOrderScreen() {
    val scope = rememberCoroutineScope()
    var running by remember { mutableStateOf<ChiefOrderRoutine.ChiefOrderType?>(null) }
    var results by remember {
        mutableStateOf<Map<ChiefOrderRoutine.ChiefOrderType, ChiefOrderRoutine.Result>>(emptyMap())
    }
    val serviceConnected = BearGuardAccessibilityService.instance != null

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Gavel, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Chief Order", style = MaterialTheme.typography.titleLarge)
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
                "Taps the scales icon to open the shelf, reads each order's cover, and enacts it " +
                    "if it's free. Run from the base World/City screen.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.height(16.dp))
        HorizontalDivider()

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
        ) {
            items(ChiefOrderRoutine.ChiefOrderType.entries) { type ->
                val result = results[type]
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(type.description, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Cooldown: ${type.cooldownHours}h",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            result?.let {
                                Spacer(Modifier.height(4.dp))
                                val (label, color) = when (it) {
                                    is ChiefOrderRoutine.Result.Enacted ->
                                        "Enacted" to MaterialTheme.colorScheme.secondary
                                    is ChiefOrderRoutine.Result.Scheduled ->
                                        it.cover to MaterialTheme.colorScheme.onSurfaceVariant
                                    is ChiefOrderRoutine.Result.Failed ->
                                        it.reason to MaterialTheme.colorScheme.error
                                }
                                Text(label, style = MaterialTheme.typography.bodySmall, color = color)
                            }
                        }
                        Button(
                            onClick = {
                                running = type
                                scope.launch {
                                    val outcome = ChiefOrderRoutine.run(type)
                                    results = results + (type to outcome)
                                    running = null
                                }
                            },
                            enabled = serviceConnected && running == null
                        ) {
                            if (running == type) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Run")
                            }
                        }
                    }
                }
            }
        }
    }
}
