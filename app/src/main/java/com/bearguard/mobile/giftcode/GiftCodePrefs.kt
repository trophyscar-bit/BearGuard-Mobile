package com.bearguard.mobile.giftcode

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.giftCodeDataStore by preferencesDataStore(name = "giftcode_prefs")

/**
 * matt/2026-08-15: profiles.character_id / character_server are empty in Bearguard-Win's own
 * database.db too -- this was never set up on the Windows side either, so there's nothing to
 * import. Stored locally per-device instead; whoever runs BearGuard Mobile enters it once.
 */
class GiftCodePrefs(private val context: Context) {
    val playerId: Flow<String> = context.giftCodeDataStore.data.map { it[PLAYER_ID_KEY] ?: "" }
    val region: Flow<String> = context.giftCodeDataStore.data.map { it[REGION_KEY] ?: "" }

    suspend fun save(playerId: String, region: String) {
        context.giftCodeDataStore.edit { prefs ->
            prefs[PLAYER_ID_KEY] = playerId
            prefs[REGION_KEY] = region
        }
    }

    companion object {
        private val PLAYER_ID_KEY = stringPreferencesKey("player_id")
        private val REGION_KEY = stringPreferencesKey("region")
    }
}
