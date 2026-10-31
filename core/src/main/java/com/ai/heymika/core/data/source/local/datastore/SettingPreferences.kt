package com.ai.heymika.core.data.source.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ailearn_settings")

/** User preferences persisted with DataStore Preferences. */
class SettingPreferences(
    private val context: Context,
) {
    fun getDarkMode(): Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    fun getDailyReminder(): Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[DAILY_REMINDER_KEY] ?: false
        }

    suspend fun setDailyReminder(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DAILY_REMINDER_KEY] = enabled
        }
    }

    private companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val DAILY_REMINDER_KEY = booleanPreferencesKey("daily_reminder")
    }
}
