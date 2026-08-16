package com.bearguard.mobile.hub

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
 * matt/2026-08-16: "duplicate what is in the Windows version... top category, subcategory."
 * Generic version of the side-nav pattern Bearguard-Win's installTabbedHub() uses for every hub
 * (City/Alliance/Economy/Troops/Account) -- a named list of sub-screens, tap one to see its real
 * settings. One composable, reused for every hub instead of a bespoke screen per hub.
 */
@Composable
fun HubScreen(hubName: String, subScreens: List<String>, content: @Composable (String) -> Unit) {
    var selected by remember { mutableStateOf<String?>(null) }

    if (selected != null) {
        Column(modifier = Modifier.fillMaxSize()) {
            TextButton(onClick = { selected = null }, modifier = Modifier.padding(start = 8.dp, top = 8.dp)) {
                Text("← $hubName")
            }
            content(selected!!)
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            hubName,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(20.dp)
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(subScreens) { name ->
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
