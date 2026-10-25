package com.example.recipebox.ui.theme

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TangerinePrimaryDark,
    onPrimary = TangerineOnPrimaryDark,
    primaryContainer = TangerinePrimaryContainerDark,
    onPrimaryContainer = TangerineOnPrimaryContainerDark,
    inversePrimary = InversePrimaryDark,
    secondary = SaffronSecondaryDark,
    onSecondary = SaffronOnSecondaryDark,
    secondaryContainer = SaffronSecondaryContainerDark,
    onSecondaryContainer = SaffronOnSecondaryContainerDark,
    tertiary = HerbTertiaryDark,
    onTertiary = HerbOnTertiaryDark,
    tertiaryContainer = HerbTertiaryContainerDark,
    onTertiaryContainer = HerbOnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceTint = TangerinePrimaryDark,
    inverseSurface = InverseSurfaceDark,
    inverseOnSurface = InverseOnSurfaceDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    surfaceBright = SurfaceBrightDark,
    surfaceDim = SurfaceDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
)

private val LightColorScheme = lightColorScheme(
    primary = TangerinePrimaryLight,
    onPrimary = TangerineOnPrimaryLight,
    primaryContainer = TangerinePrimaryContainerLight,
    onPrimaryContainer = TangerineOnPrimaryContainerLight,
    inversePrimary = InversePrimaryLight,
    secondary = SaffronSecondaryLight,
    onSecondary = SaffronOnSecondaryLight,
    secondaryContainer = SaffronSecondaryContainerLight,
    onSecondaryContainer = SaffronOnSecondaryContainerLight,
    tertiary = HerbTertiaryLight,
    onTertiary = HerbOnTertiaryLight,
    tertiaryContainer = HerbTertiaryContainerLight,
    onTertiaryContainer = HerbOnTertiaryContainerLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceTint = TangerinePrimaryLight,
    inverseSurface = InverseSurfaceLight,
    inverseOnSurface = InverseOnSurfaceLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    surfaceBright = SurfaceLight,
    surfaceDim = SurfaceDimLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
)

/** Wallpaper colours arrived in Android 12. Always true while minSdk is 31; kept in case minSdk drops. */
@SuppressLint("ObsoleteSdkInt")
val supportsDynamicColor: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/** The wallpaper or tangerine scheme. Also used by Settings to preview each palette's swatches. */
@SuppressLint("NewApi")
fun recipeBoxColorScheme(dynamic: Boolean, dark: Boolean, context: Context): ColorScheme = when {
    dynamic && supportsDynamicColor -> if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    dark -> DarkColorScheme
    else -> LightColorScheme
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RecipeBoxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Wallpaper colours. Pass false (or run on Android 11 or older) to show the tangerine palette.
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = recipeBoxColorScheme(dynamicColor, darkTheme, LocalContext.current)

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        shapes = Shapes,
        typography = Typography,
        content = content
    )
}
