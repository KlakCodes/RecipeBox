package com.example.recipebox.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

enum class ColourSource { Wallpaper, Tangerine }

enum class ThemeMode { System, Light, Dark }

data class AppSettings(
    val colourSource: ColourSource = ColourSource.Wallpaper,
    val themeMode: ThemeMode = ThemeMode.System,
)

fun AppSettings.useDarkTheme(systemDark: Boolean): Boolean = when (themeMode) {
    ThemeMode.System -> systemDark
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
}

/** Wallpaper colours need Android 12+; otherwise the tangerine palette is used whatever the setting. */
fun AppSettings.useDynamicColor(dynamicColorSupported: Boolean): Boolean =
    colourSource == ColourSource.Wallpaper && dynamicColorSupported

/** Builds settings from the stored enum names; anything missing or unrecognised falls back to the default. */
fun parseSettings(colourSource: String?, themeMode: String?): AppSettings {
    val defaults = AppSettings()
    return AppSettings(
        colourSource = ColourSource.entries.firstOrNull { it.name == colourSource } ?: defaults.colourSource,
        themeMode = ThemeMode.entries.firstOrNull { it.name == themeMode } ?: defaults.themeMode,
    )
}

class SettingsRepository(private val dataStore: DataStore<Preferences>) {
    val settings: Flow<AppSettings> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> parseSettings(prefs[COLOUR_SOURCE], prefs[THEME_MODE]) }

    suspend fun setColourSource(source: ColourSource) {
        dataStore.edit { it[COLOUR_SOURCE] = source.name }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME_MODE] = mode.name }
    }

    private companion object {
        val COLOUR_SOURCE = stringPreferencesKey("colour_source")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
