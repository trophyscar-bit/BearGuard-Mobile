package com.bearguard.mobile.giftcode

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * matt/2026-08-15: first real ported module -- a working end-to-end port of Bearguard-Win's Get
 * Giftcodes feature (fetch active codes from the community aggregator, redeem via Century Games'
 * official signed API), not a mockup. Pure network calls, so this needed zero screen-calibration
 * work, unlike almost everything else in the module list.
 *
 * Not yet live-tested end to end -- profiles.character_id/character_server are empty in
 * Bearguard-Win's own database.db (never set up there either), so there's no known-good player
 * ID/region to redeem against. fetchActiveCodes() itself IS live-tested (real network call,
 * no credentials needed). redeem() is ported faithfully from the same signed-request logic and
 * compiles/runs, but needs a real player ID entered here before its result can be trusted.
 */
@Composable
fun GiftcodeScreen() {
    val context = LocalContext.current
    val prefs = remember { GiftCodePrefs(context) }
    val client = remember { GiftCodeClient() }
    val redeemer = remember { GiftCodeRedeemer() }
    val scope = rememberCoroutineScope()

    val savedPlayerId by prefs.playerId.collectAsState(initial = "")
    val savedRegion by prefs.region.collectAsState(initial = "")
    var playerId by remember(savedPlayerId) { mutableStateOf(savedPlayerId) }
    var region by remember(savedRegion) { mutableStateOf(savedRegion) }

    var codes by remember { mutableStateOf<List<GiftCodeClient.GiftCodeEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var fetchError by remember { mutableStateOf<String?>(null) }
    var results by remember { mutableStateOf<Map<String, GiftCodeRedeemer.RedeemResult>>(emptyMap()) }
    var redeeming by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CardGiftcard, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Get Giftcodes", style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = playerId,
            onValueChange = { playerId = it.filter(Char::isDigit) },
            label = { Text("Player ID (fid)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = region,
            onValueChange = { region = it.filter(Char::isDigit) },
            label = { Text("Region / Kingdom (kid)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { scope.launch { prefs.save(playerId, region) } }) {
            Text("Save")
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                loading = true
                fetchError = null
                scope.launch {
                    try {
                        val fetched = withContext(Dispatchers.IO) { client.fetchActiveCodes() }
                        codes = fetched
                    } catch (e: Exception) {
                        fetchError = e.message ?: "Fetch failed"
                    } finally {
                        loading = false
                    }
                }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
            } else {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text("Fetch Active Codes")
        }

        fetchError?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(12.dp))

        if (codes.isEmpty() && !loading && fetchError == null) {
            Text(
                "Tap \"Fetch Active Codes\" to pull the current list.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // matt/2026-08-15: the Redeem button on the last row was sitting right at the bottom nav
        // boundary with no clearance -- taps there were landing on the nav bar instead of the
        // button. contentPadding guarantees every row, including the last, stays fully clear of it.
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(codes) { entry ->
                val result = results[entry.code]
                val canRedeem = playerId.isNotBlank() && region.isNotBlank()
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entry.code, style = MaterialTheme.typography.titleSmall)
                            Text(
                                result?.let { "${it.outcome}: ${it.message}" } ?: entry.displayDate(),
                                style = MaterialTheme.typography.bodySmall,
                                color = when (result?.outcome) {
                                    GiftCodeRedeemer.RedeemOutcome.REDEEMED,
                                    GiftCodeRedeemer.RedeemOutcome.ALREADY_REDEEMED -> MaterialTheme.colorScheme.secondary
                                    GiftCodeRedeemer.RedeemOutcome.FAILED -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        Button(
                            onClick = {
                                redeeming = entry.code
                                scope.launch {
                                    val outcome = withContext(Dispatchers.IO) {
                                        redeemer.redeem(playerId, region, entry.code)
                                    }
                                    results = results + (entry.code to outcome)
                                    redeeming = null
                                }
                            },
                            enabled = canRedeem && redeeming != entry.code
                        ) {
                            if (redeeming == entry.code) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Redeem")
                            }
                        }
                    }
                }
            }
        }
    }
}
