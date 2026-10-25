package com.example.recipebox.navigation

import android.content.ContentResolver
import android.provider.Settings
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavBackStackEntry
import com.example.recipebox.ui.theme.EmphasizedEasing
import com.example.recipebox.ui.theme.FadeThroughOutDuration
import com.example.recipebox.ui.theme.NavMotionDuration

/** How far a screen travels in the shared-axis transition. */
internal val SharedAxisSlideDistance = 30.dp

// M3 fade-through and shared-axis timings: the old screen is gone in the first 90ms,
// the new one fades in over the remaining 210ms
private const val FadeInDuration = NavMotionDuration - FadeThroughOutDuration

private typealias NavEnter = AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition
private typealias NavExit = AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isTabSwitch() =
    initialState.destination.route in TabRoutes && targetState.destination.route in TabRoutes

/**
 * Transitions for the whole graph: fade-through between bottom-nav tabs, shared-axis X everywhere
 * else. [forward] is false for the pop pair, which reverses the slide (and is what predictive back
 * scrubs through). With [enabled] false every transition is instant.
 */
internal fun navEnterTransition(slidePx: Int, enabled: Boolean, forward: Boolean): NavEnter = {
    when {
        !enabled -> EnterTransition.None
        isTabSwitch() -> fadeIn(tween(FadeInDuration, delayMillis = FadeThroughOutDuration, easing = LinearEasing)) +
            scaleIn(tween(FadeInDuration, delayMillis = FadeThroughOutDuration, easing = EmphasizedEasing), initialScale = 0.92f)
        else -> slideInHorizontally(tween(NavMotionDuration, easing = EmphasizedEasing)) {
            if (forward) slidePx else -slidePx
        } + fadeIn(tween(FadeInDuration, delayMillis = FadeThroughOutDuration, easing = LinearEasing))
    }
}

internal fun navExitTransition(slidePx: Int, enabled: Boolean, forward: Boolean): NavExit = {
    when {
        !enabled -> ExitTransition.None
        isTabSwitch() -> fadeOut(tween(FadeThroughOutDuration, easing = LinearEasing))
        else -> slideOutHorizontally(tween(NavMotionDuration, easing = EmphasizedEasing)) {
            if (forward) -slidePx else slidePx
        } + fadeOut(tween(FadeThroughOutDuration, easing = LinearEasing))
    }
}

/**
 * False when the system "Remove animations" setting (animator duration scale 0) is on. Re-read on
 * every resume so changing the setting while the app is in the background takes effect.
 */
@Composable
internal fun rememberAnimationsEnabled(): Boolean {
    val resolver = LocalContext.current.contentResolver
    var enabled by remember { mutableStateOf(animationsEnabled(resolver)) }
    LifecycleResumeEffect(resolver) {
        enabled = animationsEnabled(resolver)
        onPauseOrDispose { }
    }
    return enabled
}

private fun animationsEnabled(resolver: ContentResolver) =
    Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
