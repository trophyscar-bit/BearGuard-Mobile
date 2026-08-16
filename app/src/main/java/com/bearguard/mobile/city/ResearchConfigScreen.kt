package com.bearguard.mobile.city

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.bearguard.mobile.config.ConfigCheckboxRow
import com.bearguard.mobile.config.ConfigPrefs
import com.bearguard.mobile.config.ConfigSectionHeader
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * matt/2026-08-15: mirrors ResearchLayout.fxml, including the real drag-reorderable priority
 * list (Windows' PriorityListView, bound to ResearchCategoryEnum -- Growth/Economy/Battle, in
 * that declared default order -- via ConfigurationKeyEnum.RESEARCH_PRIORITIES_STRING).
 */
@Composable
fun ResearchConfigScreen() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ConfigSectionHeader("Enable Research")
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Automates research technology in your city",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            HorizontalDivider()

            ConfigCheckboxRow("research_enable", "Enable Research")

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            Text(
                "Research Priorities",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Long-press and drag to reorder.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(8.dp))

            ReorderablePriorityList(
                prefsKey = "research_priority_order",
                defaultOrder = listOf("growth" to "Growth", "economy" to "Economy", "battle" to "Battle"),
            )
        }
    }
}

/** matt/2026-08-15: "you can build those" -- long-press-then-drag (avoids fighting the outer
 * scrollable Column's own drag gesture), swaps live as the dragged row crosses a neighbor's
 * midpoint, persists the final order to ConfigPrefs on release. */
@Composable
fun ReorderablePriorityList(prefsKey: String, defaultOrder: List<Pair<String, String>>) {
    val context = LocalContext.current
    val prefs = remember { ConfigPrefs(context) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val rowHeightPx = with(density) { 52.dp.toPx() }

    val defaultKeyOrder = remember(defaultOrder) { defaultOrder.joinToString(",") { it.first } }
    val savedOrder by prefs.text(prefsKey, defaultKeyOrder).collectAsState(initial = defaultKeyOrder)

    var items by remember { mutableStateOf(defaultOrder) }
    var initialized by remember { mutableStateOf(false) }
    if (!initialized) {
        items = savedOrder.split(",")
            .mapNotNull { k -> defaultOrder.find { it.first == k } }
            .ifEmpty { defaultOrder }
        initialized = true
    }

    var draggingIndex by remember { mutableStateOf(-1) }
    var dragOffset by remember { mutableStateOf(0f) }

    Column {
        items.forEachIndexed { index, (key, label) ->
            val isDragging = index == draggingIndex
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer { translationY = if (isDragging) dragOffset else 0f }
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        if (isDragging) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent
                    )
                    .pointerInput(index, items.size) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggingIndex = index
                                dragOffset = 0f
                            },
                            onDrag = { change, delta ->
                                change.consume()
                                dragOffset += delta.y
                                val current = draggingIndex
                                if (current < 0) return@detectDragGesturesAfterLongPress
                                val shift = (dragOffset / rowHeightPx).roundToInt()
                                if (shift != 0) {
                                    val target = (current + shift).coerceIn(0, items.size - 1)
                                    if (target != current) {
                                        items = items.toMutableList().apply {
                                            add(target, removeAt(current))
                                        }
                                        dragOffset -= shift * rowHeightPx
                                        draggingIndex = target
                                    }
                                }
                            },
                            onDragEnd = {
                                draggingIndex = -1
                                dragOffset = 0f
                                scope.launch { prefs.setText(prefsKey, items.joinToString(",") { it.first }) }
                            },
                            onDragCancel = {
                                draggingIndex = -1
                                dragOffset = 0f
                            }
                        )
                    }
            ) {
                Icon(
                    Icons.Filled.DragHandle, contentDescription = "Drag to reorder",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(end = 10.dp)
                )
                Text("${index + 1}. $label", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
