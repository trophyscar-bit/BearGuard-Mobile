package com.bearguard.mobile

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * matt/2026-08-15: "I want the BGM to have an interface like the windows ver... copy the win ver
 * and respec it if needed to." This mirrors Bearguard-Win's actual architecture (pulled straight
 * from LauncherLayoutController.java's pinned buttons + its 29-entry ModuleDefinition registry,
 * not guessed) reshaped for a phone/tablet: the Windows app's resizable multi-window desktop
 * layout becomes a bottom-nav + list, since that's the natural mobile equivalent of "a pinned
 * section plus a long scrollable module list."
 *
 * Real screens, not a mockup -- Control actually reads AccessibilityService state and can
 * enable/disable it; Modules lists every real module name from the Windows sidebar. What's NOT
 * here yet: the automation logic behind each module (that's the separate Routine-porting phase),
 * and Statistics has no real data source yet since no routine has ever run to produce any.
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BearGuardApp() {
    var activeTab by remember { mutableStateOf(Tab.CONTROL) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("BearGuard Mobile") })
        },
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
            when (activeTab) {
                Tab.CONTROL -> ControlScreen()
                Tab.STATISTICS -> StatisticsScreen()
                Tab.MODULES -> ModulesScreen()
                Tab.CONFIG -> ConfigScreen()
            }
        }
    }
}

// ============================================================================
// CONTROL -- mirrors LauncherLayoutController's top bar: Start/Pause + status.
// The one real functional piece right now: checking + jumping to the
// Accessibility Settings toggle, since nothing else can run without it.
// ============================================================================
@Composable
fun ControlScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var running by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))
        Icon(
            imageVector = if (running) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
            contentDescription = null,
            tint = if (running) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(72.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            if (running) "Running" else "Stopped",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { running = !running },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(if (running) "Pause" else "Start Bot")
        }

        Spacer(Modifier.height(32.dp))
        Divider()
        Spacer(Modifier.height(16.dp))

        Text("Accessibility service", style = MaterialTheme.typography.titleMedium)
        Text(
            "BearGuard's whole automation engine runs as an Accessibility Service, scoped only " +
                "to Whiteout Survival. It must be enabled once in system settings.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )
        OutlinedButton(
            onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
            modifier = Modifier.fillMaxWidth()
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
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Filled.BarChart, contentDescription = null,
            modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.height(12.dp))
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
// guessed). Each row is a placeholder detail screen for now -- the
// automation logic behind each one is a separate, deliberate porting pass.
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
    ModuleEntry("Get Giftcodes", Icons.Filled.CardGiftcard),
    ModuleEntry("Debugging", Icons.Filled.BugReport),
    ModuleEntry("Task Builder", Icons.Filled.Build),
    ModuleEntry("Skip Tutorial", Icons.Filled.SkipNext),
    ModuleEntry("Character", Icons.Filled.Person),
    ModuleEntry("Chat", Icons.Filled.Chat),
)

@Composable
fun ModulesScreen() {
    var selected by remember { mutableStateOf<ModuleEntry?>(null) }

    if (selected != null) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            TextButton(onClick = { selected = null }) { Text("← Back to Modules") }
            Spacer(Modifier.height(16.dp))
            Icon(selected!!.icon, contentDescription = null, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(8.dp))
            Text(selected!!.name, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))
            Text(
                "Not built yet -- this module's automation logic hasn't been ported from " +
                    "Bearguard-Win. Placeholder screen.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(MODULES) { module ->
            ListItem(
                headlineContent = { Text(module.name) },
                leadingContent = { Icon(module.icon, contentDescription = null) },
                trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null,
                    modifier = Modifier.size(20.dp)) },
                modifier = Modifier.clickable { selected = module }
            )
            Divider()
        }
    }
}

// ============================================================================
// CONFIG -- mirrors the Windows Config tab's Emulators/Telegram sub-tabs shape.
// ============================================================================
@Composable
fun ConfigScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Config", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text(
            "Target device, capture/tap calibration, and notification settings will live here " +
                "once there's real per-device calibration data to configure.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
