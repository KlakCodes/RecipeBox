package com.example.recipebox.ui.theme

import androidx.compose.animation.core.CubicBezierEasing

/** Screen transitions and the shared elements that ride on them all take this long. */
const val NavMotionDuration = 300

/** In a fade-through the outgoing content is gone this early; the incoming fades in over the rest. */
const val FadeThroughOutDuration = 90

/** M3 emphasized easing: quick start, long gentle settle. */
val EmphasizedEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
