package com.example.recipebox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.recipebox.data.AppSettings
import com.example.recipebox.data.useDarkTheme
import com.example.recipebox.data.useDynamicColor
import com.example.recipebox.navigation.RecipeBoxNavGraph
import com.example.recipebox.ui.theme.RecipeBoxTheme
import com.example.recipebox.ui.theme.supportsDynamicColor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settingsFlow = (application as RecipeBoxApplication).settingsRepository.settings
        setContent {
            // Null until DataStore's first read (a few ms), so the wrong theme never flashes on launch
            val settings: AppSettings? by settingsFlow.collectAsState(initial = null)
            val current = settings ?: return@setContent
            RecipeBoxTheme(
                darkTheme = current.useDarkTheme(isSystemInDarkTheme()),
                dynamicColor = current.useDynamicColor(supportsDynamicColor),
            ) {
                RecipeBoxNavGraph()
            }
        }
    }
}
