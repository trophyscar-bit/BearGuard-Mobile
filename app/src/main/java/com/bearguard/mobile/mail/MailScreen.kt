package com.bearguard.mobile.mail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bearguard.mobile.service.BearGuardAccessibilityService
import kotlinx.coroutines.launch

/** matt/2026-08-15: fifth real ported module. Sweeps all 5 mail tabs' Read & Claim All. */
@Composable
fun MailScreen() {
    val scope = rememberCoroutineScope()
    var running by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<MailRewardsRoutine.Result?>(null) }
    val serviceConnected = BearGuardAccessibilityService.instance != null

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Mail, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Mail Rewards", style = MaterialTheme.typography.titleLarge)
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
                "Opens Mail and taps Read & Claim All on the three reward tabs (Wars, Alliance, " +
                    "System). Reports/Starred are skipped -- they have no claim button. Safe " +
                    "no-op on tabs with nothing new. Run from anywhere.",
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
                    Text("Sweep All Mail Tabs", style = MaterialTheme.typography.titleSmall)
                    result?.let {
                        Spacer(Modifier.height(4.dp))
                        if (it.failure != null) {
                            Text(it.failure, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error)
                        } else {
                            Text(
                                "Processed ${it.tabsProcessed} tab(s)",
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
                            result = MailRewardsRoutine.run()
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
