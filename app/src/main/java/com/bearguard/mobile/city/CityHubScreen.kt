package com.bearguard.mobile.city

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * matt/2026-08-15: "there's a city module in the Windows version... you click into one of those
 * sub tabs, and then it gives you options. That same thing is gonna be carried over." This
 * mirrors installTabbedHub("City", ...) -- a named list of sub-screens, select one to see its
 * real settings on the right (here: pushed to full-screen, since phone width can't do side-by-
 * side the way the Windows hub's side-nav does).
 */
private val CITY_SUB_SCREENS = listOf("City Upgrades", "City Events", "Extra City Events", "Research")

@Composable
fun CityHubScreen() {
    var selected by remember { mutableStateOf<String?>(null) }

    if (selected != null) {
        Column(modifier = Modifier.fillMaxSize()) {
            TextButton(onClick = { selected = null }, modifier = Modifier.padding(start = 8.dp, top = 8.dp)) {
                Text("← City")
            }
            when (selected) {
                "City Upgrades" -> CityUpgradesConfigScreen()
                "City Events" -> CityEventsConfigScreen()
                "Extra City Events" -> CityEventsExtraConfigScreen()
                "Research" -> ResearchConfigScreen()
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "City",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(20.dp)
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(CITY_SUB_SCREENS) { name ->
                ListItem(
                    headlineContent = { Text(name) },
                    trailingContent = {
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier.clickable { selected = name }
                )
                HorizontalDivider()
            }
        }
    }
}
