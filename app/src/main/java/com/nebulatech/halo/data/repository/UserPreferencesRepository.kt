package com.nebulatech.halo.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val THEME_KEY = stringPreferencesKey("app_theme")
    private val ALARM_SOUND_URI_KEY = stringPreferencesKey("alarm_sound_uri")
    private val ALARM_SOUND_TITLE_KEY = stringPreferencesKey("alarm_sound_title")
    private val IS_FIRST_LAUNCH_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("is_first_launch")
    private val LAST_PROMPTED_VERSION_KEY = androidx.datastore.preferences.core.intPreferencesKey("last_prompted_version")
    private val DYNAMIC_COLOR_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("is_dynamic_color")
    private val SNOOZE_DURATION_KEY = androidx.datastore.preferences.core.intPreferencesKey("snooze_duration")

    val appTheme: Flow<AppTheme> = context.dataStore.data
        .map { preferences ->
            val themeName = preferences[THEME_KEY] ?: AppTheme.SYSTEM.name
            try {
                AppTheme.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                AppTheme.SYSTEM
            }
        }

    val dynamicColor: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DYNAMIC_COLOR_KEY] ?: false
        }

    val snoozeDuration: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[SNOOZE_DURATION_KEY] ?: 5
        }

    val alarmSound: Flow<Pair<String, String>> = context.dataStore.data
        .map { preferences ->
            val uri = preferences[ALARM_SOUND_URI_KEY] ?: ""
            val title = preferences[ALARM_SOUND_TITLE_KEY] ?: "Default"
            uri to title
        }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_FIRST_LAUNCH_KEY] ?: true
        }

    val lastPromptedVersionCode: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[LAST_PROMPTED_VERSION_KEY] ?: 0
        }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }

    suspend fun setAlarmSound(uri: String, title: String) {
        context.dataStore.edit { preferences ->
            preferences[ALARM_SOUND_URI_KEY] = uri
            preferences[ALARM_SOUND_TITLE_KEY] = title
        }
    }

    suspend fun completeOnboarding() {
        context.dataStore.edit { preferences ->
            preferences[IS_FIRST_LAUNCH_KEY] = false
        }
    }

    suspend fun setLastPromptedVersion(version: Int) {
        context.dataStore.edit { preferences ->
            preferences[LAST_PROMPTED_VERSION_KEY] = version
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DYNAMIC_COLOR_KEY] = enabled
        }
    }

    suspend fun setSnoozeDuration(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[SNOOZE_DURATION_KEY] = minutes
        }
    }
}

enum class AppTheme {
    LIGHT,
    DARK,
    SYSTEM
}
