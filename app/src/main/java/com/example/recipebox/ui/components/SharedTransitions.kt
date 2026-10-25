package com.example.recipebox.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.RemeasureToBounds
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.scaleToBounds
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import com.example.recipebox.ui.theme.EmphasizedEasing
import com.example.recipebox.ui.theme.FadeThroughOutDuration
import com.example.recipebox.ui.theme.NavMotionDuration

/** Null outside the nav graph (previews) or when animations are off; shared modifiers then do nothing. */
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

/** The current destination's enter/exit scope, provided by each nav destination that shares elements. */
val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/** The parts of a recipe that travel from its list card into the detail hero and back. */
enum class RecipeSharedPart { Container, Shape, Icon, Title }

private data class RecipeSharedKey(val recipeId: Int, val part: RecipeSharedPart)

private val RecipeBoundsTransform = BoundsTransform { _, _ -> tween(NavMotionDuration, easing = EmphasizedEasing) }
private val FadeThroughIn = fadeIn(tween(NavMotionDuration - FadeThroughOutDuration, delayMillis = FadeThroughOutDuration))
private val FadeThroughOut = fadeOut(tween(FadeThroughOutDuration))

/**
 * Tags this layout as [part] of recipe [recipeId] so it morphs between the list card and the detail
 * hero. [clipShape] is the container's shape; the parts inside it are clipped to it in flight.
 */
@Composable
fun Modifier.sharedRecipePart(recipeId: Int, part: RecipeSharedPart, clipShape: Shape = RectangleShape): Modifier {
    val sharedScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalNavAnimatedVisibilityScope.current ?: return this
    return with(sharedScope) {
        val state = rememberSharedContentState(RecipeSharedKey(recipeId, part))
        when (part) {
            // Container and shape fade through between the two looks while their bounds morph, so the
            // card's stretched content never overlaps the hero's
            RecipeSharedPart.Container -> sharedBounds(
                state,
                visibilityScope,
                enter = FadeThroughIn,
                exit = FadeThroughOut,
                boundsTransform = RecipeBoundsTransform,
                resizeMode = RemeasureToBounds,
                clipInOverlayDuringTransition = OverlayClip(clipShape),
            )
            RecipeSharedPart.Shape -> sharedBounds(
                state,
                visibilityScope,
                enter = FadeThroughIn,
                exit = FadeThroughOut,
                boundsTransform = RecipeBoundsTransform,
                resizeMode = RemeasureToBounds,
                zIndexInOverlay = 1f,
            )
            // The same glyph on both screens, so it moves and grows rather than cross-fading
            RecipeSharedPart.Icon -> sharedElement(
                state,
                visibilityScope,
                boundsTransform = RecipeBoundsTransform,
                zIndexInOverlay = 2f,
            )
            // Scaled rather than remeasured so the title never re-wraps mid-flight
            RecipeSharedPart.Title -> sharedBounds(
                state,
                visibilityScope,
                boundsTransform = RecipeBoundsTransform,
                resizeMode = scaleToBounds(),
                zIndexInOverlay = 2f,
            )
        }
    }
}
