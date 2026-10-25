package com.example.recipebox.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recipebox.BuildConfig
import com.example.recipebox.RecipeBoxApplication
import com.example.recipebox.data.ColourSource
import com.example.recipebox.data.ThemeMode
import com.example.recipebox.data.useDarkTheme
import com.example.recipebox.ui.components.TonalIconButton
import com.example.recipebox.ui.theme.RecipeBoxShapes
import com.example.recipebox.ui.theme.RecipeBoxTheme
import com.example.recipebox.ui.theme.recipeBoxColorScheme
import com.example.recipebox.ui.theme.supportsDynamicColor

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.factory(
            (LocalContext.current.applicationContext as RecipeBoxApplication).settingsRepository,
        ),
    ),
) {
    val settings by viewModel.settings.collectAsState()
    SettingsContent(
        // Without wallpaper colour support the app runs on tangerine, so show that as chosen
        colourSource = if (supportsDynamicColor) settings.colourSource else ColourSource.Tangerine,
        themeMode = settings.themeMode,
        darkTheme = settings.useDarkTheme(isSystemInDarkTheme()),
        onBack = onBack,
        onColourSourceChange = viewModel::setColourSource,
        onThemeModeChange = viewModel::setThemeMode,
    )
}

@Composable
private fun SettingsContent(
    colourSource: ColourSource,
    themeMode: ThemeMode,
    darkTheme: Boolean,
    onBack: () -> Unit,
    onColourSourceChange: (ColourSource) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { TonalIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", onBack) }
        item {
            Text(
                "Settings",
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
            )
        }

        item { SectionHeading("Appearance") }
        item { SubLabel("Colour") }
        item {
            ColourCards(
                selected = colourSource,
                darkTheme = darkTheme,
                onSelect = onColourSourceChange,
            )
        }
        item { SubLabel("Theme", modifier = Modifier.padding(top = 12.dp)) }
        item { ThemeModeButtons(selected = themeMode, onSelect = onThemeModeChange) }

        item { SectionHeading("About", modifier = Modifier.padding(top = 24.dp)) }
        item { AboutRow() }
    }
}

@Composable
private fun SectionHeading(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.headlineSmall, modifier = modifier.padding(top = 8.dp))
}

@Composable
private fun SubLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(top = 4.dp),
    )
}

@Composable
private fun ColourCards(
    selected: ColourSource,
    darkTheme: Boolean,
    onSelect: (ColourSource) -> Unit,
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    // Each card previews its own palette in the current light/dark mode
    val wallpaperSwatches = if (supportsDynamicColor) {
        recipeBoxColorScheme(dynamic = true, dark = darkTheme, context = context).swatches()
    } else {
        List(3) { colors.outlineVariant }
    }
    val tangerineSwatches = recipeBoxColorScheme(dynamic = false, dark = darkTheme, context = context).swatches()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            ColourCard(
                label = "Wallpaper colours",
                swatches = wallpaperSwatches,
                selected = selected == ColourSource.Wallpaper,
                enabled = supportsDynamicColor,
                onClick = { onSelect(ColourSource.Wallpaper) },
            )
            if (!supportsDynamicColor) {
                Text(
                    "Needs Android 12 or newer",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, top = 6.dp),
                )
            }
        }
        ColourCard(
            label = "Tangerine",
            swatches = tangerineSwatches,
            selected = selected == ColourSource.Tangerine,
            enabled = true,
            onClick = { onSelect(ColourSource.Tangerine) },
            modifier = Modifier.weight(1f),
        )
    }
}

private fun ColorScheme.swatches() = listOf(primary, secondary, tertiary)

@Composable
private fun ColourCard(
    label: String,
    swatches: List<Color>,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RecipeBoxShapes.Card)
            .background(colors.surfaceContainerLow)
            .then(if (selected) Modifier.border(3.dp, colors.primary, RecipeBoxShapes.Card) else Modifier)
            // The whole card is the touch target; role + selected make TalkBack read it as a radio option
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .alpha(if (enabled) 1f else 0.38f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                swatches.forEach { swatch ->
                    Box(
                        Modifier
                            .size(28.dp)
                            .background(swatch, CircleShape),
                    )
                }
            }
            Text(label, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(24.dp)
                    .background(colors.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = colors.onPrimary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThemeModeButtons(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    val modes = ThemeMode.entries
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        modes.forEachIndexed { index, mode ->
            ToggleButton(
                checked = mode == selected,
                onCheckedChange = { onSelect(mode) },
                modifier = Modifier
                    .weight(1f)
                    .semantics { role = Role.RadioButton },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    modes.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
            ) {
                Text(mode.name)
            }
        }
    }
}

@Composable
private fun AboutRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RecipeBoxShapes.Card)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "Recipe Box",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        Text(
            "Version ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsPreview() {
    RecipeBoxTheme(dynamicColor = false) {
        SettingsContent(
            colourSource = ColourSource.Tangerine,
            themeMode = ThemeMode.System,
            darkTheme = false,
            onBack = {},
            onColourSourceChange = {},
            onThemeModeChange = {},
        )
    }
}
