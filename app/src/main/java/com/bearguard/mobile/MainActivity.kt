package com.bearguard.mobile

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * matt/2026-08-15: "has to be completely redone based on the resolution... auto detect it...
 * there's gonna be two versions here, the new Android [tablet] and MuMu, and this resolution is
 * way different." Measured live rather than guessed: `adb shell wm size`/`wm density` on MuMu
 * reports an override of 720x1280 @ 320dpi on BOTH instances -- a completely standard ~360dp-wide
 * phone screen class. The "too big" look matt was seeing is almost certainly MuMu's own emulator
 * window being scaled up on his 34" monitor (outside what an Android app can see or control, since
 * apps only ever know their own reported dp/density, never how the host window frames it) -- but
 * the underlying ask (build responsively, don't hardcode one screen shape) is real and worth doing
 * regardless, since the tablet (2000x1200, a genuinely different aspect ratio and width class) is
 * a real second target.
 *
 * Compose already scales dp/sp with density automatically -- what it does NOT do on its own is
 * change LAYOUT SHAPE (nav position, column count) between a narrow phone and a wide tablet. This
 * adds that: LocalConfiguration.screenWidthDp picks Compact (bottom nav, 1-column list, matches
 * MuMu's ~360dp width) vs Expanded (side nav rail, 2-3 column grid, matches the tablet's much wider
 * class) at runtime, on whichever device it's actually running on -- no per-target build flavor
 * needed, no manual switch.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BearGuardTheme {
                BearGuardApp()
            }
        }
    }
}

@Composable
fun BearGuardTheme(content: @Composable () -> Unit) {
    val colors = darkColorScheme(
        primary = androidx.compose.ui.graphics.Color(0xFF4FC3F7),
        secondary = androidx.compose.ui.graphics.Color(0xFF81C784),
        background = androidx.compose.ui.graphics.Color(0xFF12181F),
        surface = androidx.compose.ui.graphics.Color(0xFF1A222C),
    )
    MaterialTheme(colorScheme = colors, content = content)
}

private enum class Tab(val label: String, val icon: ImageVector) {
    CONTROL("Control", Icons.Filled.PlayArrow),
    STATISTICS("Statistics", Icons.Filled.BarChart),
    MODULES("Modules", Icons.Filled.List),
    CONFIG("Config", Icons.Filled.Settings),
}

/** Compact = phone-class width (MuMu's real 360dp). Expanded = tablet-class width (the A16). */
enum class WidthClass { COMPACT, EXPANDED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BearGuardApp() {
    var activeTab by remember { mutableStateOf(Tab.CONTROL) }
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    // Material's own compact/medium/expanded breakpoint is 600dp -- reused here rather than
    // inventing a new threshold.
    val widthClass = if (screenWidthDp < 600) WidthClass.COMPACT else WidthClass.EXPANDED

    if (widthClass == WidthClass.COMPACT) {
        Scaffold(
            topBar = { TopAppBar(title = { Text("BearGuard Mobile") }) },
            bottomBar = {
                NavigationBar {
                    Tab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = activeTab == tab,
                            onClick = { activeTab = tab },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                ScreenFor(activeTab, widthClass)
            }
        }
    } else {
        Scaffold(
            topBar = { TopAppBar(title = { Text("BearGuard Mobile") }) },
        ) { padding ->
            Row(modifier = Modifier.padding(padding).fillMaxSize()) {
                NavigationRail {
                    Spacer(Modifier.height(8.dp))
                    Tab.entries.forEach { tab ->
                        NavigationRailItem(
                            selected = activeTab == tab,
                            onClick = { activeTab = tab },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    ScreenFor(activeTab, widthClass)
                }
            }
        }
    }
}

@Composable
private fun ScreenFor(tab: Tab, widthClass: WidthClass) {
    when (tab) {
        Tab.CONTROL -> ControlScreen()
        Tab.STATISTICS -> StatisticsScreen()
        Tab.MODULES -> ModulesScreen(widthClass)
        Tab.CONFIG -> ConfigScreen()
    }
}

// ============================================================================
// CONTROL -- mirrors LauncherLayoutController's top bar: Start/Pause + status.
// Sizes tightened from the first pass (72dp status icon -> 56dp, 24dp padding
// -> 20dp) -- matt's "too big, want it clear/concise" note, independent of
// the width-class work above.
// ============================================================================
@Composable
fun ControlScreen() {
    val context = LocalContext.current
    var running by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))
        Icon(
            imageVector = if (running) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
            contentDescription = null,
            tint = if (running) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (running) "Running" else "Stopped",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { running = !running },
            modifier = Modifier.fillMaxWidth().height(44.dp)
        ) {
            Text(if (running) "Pause" else "Start Bot")
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        Text("Accessibility service", style = MaterialTheme.typography.titleSmall)
        Text(
            "BearGuard's whole automation engine runs as an Accessibility Service, scoped only " +
                "to Whiteout Survival. It must be enabled once in system settings.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
        )
        OutlinedButton(
            onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
            modifier = Modifier.fillMaxWidth().height(40.dp)
        ) {
            Text("Open Accessibility Settings")
        }
    }
}

// ============================================================================
// STATISTICS -- placeholder shape matching the Windows Statistics tab's
// "what the bot did for you" cards. No real data source yet (nothing has run).
// ============================================================================
@Composable
fun StatisticsScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Filled.BarChart, contentDescription = null,
            modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.height(10.dp))
        Text("No data yet", style = MaterialTheme.typography.titleMedium)
        Text(
            "Statistics fill in once real automation routines start running.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ============================================================================
// MODULES -- the real 29-entry list from LauncherLayoutController's
// ModuleDefinition registry (pulled from the actual Windows source, not
// guessed). Compact width = single-column list; expanded width = a grid, so
// the tablet's extra horizontal room gets used instead of wasted.
// ============================================================================
private data class ModuleEntry(val name: String, val icon: ImageVector)

private val MODULES = listOf(
    ModuleEntry("City Upgrades", Icons.Filled.LocationCity),
    ModuleEntry("City Events", Icons.Filled.Event),
    ModuleEntry("Extra City Events", Icons.Filled.EventNote),
    ModuleEntry("Rally", Icons.Filled.Flag),
    ModuleEntry("General Shop", Icons.Filled.Store),
    ModuleEntry("Gem Shop", Icons.Filled.Diamond),
    ModuleEntry("Deals", Icons.Filled.LocalOffer),
    ModuleEntry("Gather", Icons.Filled.Agriculture),
    ModuleEntry("Intel", Icons.Filled.Visibility),
    ModuleEntry("Alliance", Icons.Filled.Groups),
    ModuleEntry("Alliance Championship", Icons.Filled.EmojiEvents),
    ModuleEntry("Alliance Shop", Icons.Filled.ShoppingCart),
    ModuleEntry("Alliance Mobilization", Icons.Filled.NotificationsActive),
    ModuleEntry("Bear Trap", Icons.Filled.Build),
    ModuleEntry("Beast Hunting", Icons.Filled.Pets),
    ModuleEntry("Fishing Tournament", Icons.Filled.SetMeal),
    ModuleEntry("Training", Icons.Filled.FitnessCenter),
    ModuleEntry("Research", Icons.Filled.Science),
    ModuleEntry("Pets", Icons.Filled.Cottage),
    ModuleEntry("Events", Icons.Filled.CalendarMonth),
    ModuleEntry("Experts", Icons.Filled.School),
    ModuleEntry("Chief Order", Icons.Filled.Gavel),
    ModuleEntry("VIP", Icons.Filled.WorkspacePremium),
    ModuleEntry("Mail Rewards", Icons.Filled.Mail),
    ModuleEntry("Get Giftcodes", Icons.Filled.CardGiftcard),
    ModuleEntry("Debugging", Icons.Filled.BugReport),
    ModuleEntry("Task Builder", Icons.Filled.Build),
    ModuleEntry("Skip Tutorial", Icons.Filled.SkipNext),
    ModuleEntry("Character", Icons.Filled.Person),
    ModuleEntry("Chat", Icons.Filled.Chat),
)

@Composable
fun ModulesScreen(widthClass: WidthClass) {
    var selected by remember { mutableStateOf<ModuleEntry?>(null) }

    if (selected != null) {
        // matt/2026-08-15: "start porting the routines" -- Get Giftcodes is the first real one,
        // a full working port (fetch + redeem), not a placeholder. Everything else in the list
        // still is, until it's ported the same deliberate way.
        if (selected!!.name == "Get Giftcodes") {
            Column(modifier = Modifier.fillMaxSize()) {
                TextButton(onClick = { selected = null }, modifier = Modifier.padding(start = 12.dp, top = 12.dp)) {
                    Text("← Back to Modules")
                }
                com.bearguard.mobile.giftcode.GiftcodeScreen()
            }
            return
        }

        // matt/2026-08-15: "port chief order next" -- second real ported module.
        if (selected!!.name == "Chief Order") {
            Column(modifier = Modifier.fillMaxSize()) {
                TextButton(onClick = { selected = null }, modifier = Modifier.padding(start = 12.dp, top = 12.dp)) {
                    Text("← Back to Modules")
                }
                com.bearguard.mobile.chieforder.ChiefOrderScreen()
            }
            return
        }

        // matt/2026-08-15: "gogogogo" continuous porting pass -- third real ported module.
        if (selected!!.name == "Deals") {
            Column(modifier = Modifier.fillMaxSize()) {
                TextButton(onClick = { selected = null }, modifier = Modifier.padding(start = 12.dp, top = 12.dp)) {
                    Text("← Back to Modules")
                }
                com.bearguard.mobile.deals.DealsScreen()
            }
            return
        }

        // matt/2026-08-15: fourth real ported module -- free claims only, no purchase flow.
        if (selected!!.name == "VIP") {
            Column(modifier = Modifier.fillMaxSize()) {
                TextButton(onClick = { selected = null }, modifier = Modifier.padding(start = 12.dp, top = 12.dp)) {
                    Text("← Back to Modules")
                }
                com.bearguard.mobile.vip.VipScreen()
            }
            return
        }

        // matt/2026-08-15: fifth real ported module.
        if (selected!!.name == "Mail Rewards") {
            Column(modifier = Modifier.fillMaxSize()) {
                TextButton(onClick = { selected = null }, modifier = Modifier.padding(start = 12.dp, top = 12.dp)) {
                    Text("← Back to Modules")
                }
                com.bearguard.mobile.mail.MailScreen()
            }
            return
        }

        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            TextButton(onClick = { selected = null }) { Text("← Back to Modules") }
            Spacer(Modifier.height(12.dp))
            Icon(selected!!.icon, contentDescription = null, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(8.dp))
            Text(selected!!.name, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(10.dp))
            Text(
                "Not built yet -- this module's automation logic hasn't been ported from " +
                    "Bearguard-Win. Placeholder screen.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    if (widthClass == WidthClass.COMPACT) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(MODULES) { module ->
                ListItem(
                    headlineContent = { Text(module.name) },
                    leadingContent = { Icon(module.icon, contentDescription = null) },
                    trailingContent = {
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier.clickable { selected = module }
                )
                HorizontalDivider()
            }
        }
    } else {
        // Tablet: use the extra width for a grid instead of one long narrow column.
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 220.dp),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(MODULES) { module ->
                Card(
                    onClick = { selected = module },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(module.icon, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(module.name, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

// ============================================================================
// CONFIG -- mirrors the Windows Config tab's Emulators/Telegram sub-tabs shape.
// ============================================================================
@Composable
fun ConfigScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Config", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Text(
            "Target device, capture/tap calibration, and notification settings will live here " +
                "once there's real per-device calibration data to configure.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
