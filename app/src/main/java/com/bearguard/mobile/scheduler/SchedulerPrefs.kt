package com.bearguard.mobile.scheduler

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.schedulerDataStore by preferencesDataStore(name = "scheduler_prefs")

/**
 * matt/2026-08-15: per-task toggle + schedule state, persisted so it survives app restarts --
 * same idea as Bearguard-Win's TpDailyTaskEnum enabled-flags + each DelayedTask's reschedule()
 * time living in the profile's own config/database, just backed by DataStore instead of SQLite.
 * Also holds the master engine-running flag (mirrors "Start Bot" / "Pause" in the Windows app).
 */
class SchedulerPrefs(private val context: Context) {

    fun enabled(taskKey: String): Flow<Boolean> =
        context.schedulerDataStore.data.map { it[enabledKey(taskKey)] ?: false }

    suspend fun setEnabled(taskKey: String, enabled: Boolean) {
        context.schedulerDataStore.edit { it[enabledKey(taskKey)] = enabled }
    }

    fun nextRunAt(taskKey: String): Flow<Long> =
        context.schedulerDataStore.data.map { it[nextRunKey(taskKey)] ?: 0L }

    suspend fun setNextRunAt(taskKey: String, epochMillis: Long) {
        context.schedulerDataStore.edit { it[nextRunKey(taskKey)] = epochMillis }
    }

    fun lastResult(taskKey: String): Flow<String> =
        context.schedulerDataStore.data.map { it[lastResultKey(taskKey)] ?: "" }

    suspend fun setLastResult(taskKey: String, summary: String) {
        context.schedulerDataStore.edit { it[lastResultKey(taskKey)] = summary }
    }

    val engineRunning: Flow<Boolean> =
        context.schedulerDataStore.data.map { it[ENGINE_RUNNING_KEY] ?: false }

    suspend fun setEngineRunning(running: Boolean) {
        context.schedulerDataStore.edit { it[ENGINE_RUNNING_KEY] = running }
    }

    companion object {
        private val ENGINE_RUNNING_KEY = booleanPreferencesKey("engine_running")
        private fun enabledKey(taskKey: String) = booleanPreferencesKey("${taskKey}_enabled")
        private fun nextRunKey(taskKey: String) = longPreferencesKey("${taskKey}_next_run_at")
        private fun lastResultKey(taskKey: String) = stringPreferencesKey("${taskKey}_last_result")
    }
}
