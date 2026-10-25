package com.example.recipebox

import com.example.recipebox.data.AppSettings
import com.example.recipebox.data.ColourSource
import com.example.recipebox.data.ThemeMode
import com.example.recipebox.data.parseSettings
import com.example.recipebox.data.useDarkTheme
import com.example.recipebox.data.useDynamicColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsTest {
    @Test
    fun missingPreferencesGiveDefaults() {
        assertEquals(AppSettings(ColourSource.Wallpaper, ThemeMode.System), parseSettings(null, null))
    }

    @Test
    fun storedValuesRoundTripInBothDirections() {
        assertEquals(
            AppSettings(ColourSource.Tangerine, ThemeMode.Dark),
            parseSettings("Tangerine", "Dark"),
        )
        assertEquals(
            AppSettings(ColourSource.Wallpaper, ThemeMode.Light),
            parseSettings("Wallpaper", "Light"),
        )
    }

    @Test
    fun unknownValuesFallBackToDefaults() {
        assertEquals(AppSettings(), parseSettings("Neon", "Sepia"))
    }

    @Test
    fun themeModeControlsDarkTheme() {
        assertTrue(AppSettings(themeMode = ThemeMode.System).useDarkTheme(systemDark = true))
        assertFalse(AppSettings(themeMode = ThemeMode.System).useDarkTheme(systemDark = false))
        assertFalse(AppSettings(themeMode = ThemeMode.Light).useDarkTheme(systemDark = true))
        assertTrue(AppSettings(themeMode = ThemeMode.Dark).useDarkTheme(systemDark = false))
    }

    @Test
    fun wallpaperColourNeedsDynamicColourSupport() {
        assertTrue(AppSettings(colourSource = ColourSource.Wallpaper).useDynamicColor(dynamicColorSupported = true))
        assertFalse(AppSettings(colourSource = ColourSource.Wallpaper).useDynamicColor(dynamicColorSupported = false))
        assertFalse(AppSettings(colourSource = ColourSource.Tangerine).useDynamicColor(dynamicColorSupported = true))
    }
}
