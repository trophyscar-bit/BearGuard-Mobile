package com.bearguard.mobile.config

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.configDataStore by preferencesDataStore(name = "config_prefs")

/**
 * matt/2026-08-15: "mirror what we already have for modules... the city module has city
 * upgrades, city events, extra city events, and research. Click into one of those sub tabs, and
 * it gives you options." Generic per-checkbox/per-field persistence, one key per fx:id from the
 * Windows FXML layouts (CityUpgradesLayout.fxml, CityEventsLayout.fxml, etc.) -- same shape as
 * SchedulerPrefs but for arbitrary config fields rather than task enable/next-run/last-result.
 */
class ConfigPrefs(private val context: Context) {

    fun bool(key: String, default: Boolean = false): Flow<Boolean> =
        context.configDataStore.data.map { it[booleanPreferencesKey(key)] ?: default }

    suspend fun setBool(key: String, value: Boolean) {
        context.configDataStore.edit { it[booleanPreferencesKey(key)] = value }
    }

    fun text(key: String, default: String = ""): Flow<String> =
        context.configDataStore.data.map { it[stringPreferencesKey(key)] ?: default }

    suspend fun setText(key: String, value: String) {
        context.configDataStore.edit { it[stringPreferencesKey(key)] = value }
    }
}
