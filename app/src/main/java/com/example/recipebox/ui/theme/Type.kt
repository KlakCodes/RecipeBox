package com.example.recipebox.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.recipebox.R

// Roboto Flex is a variable font bundled in res/font (the app is offline, so no downloadable fonts).
// Headings use a slightly widened cut for the chunky Expressive look.
@OptIn(ExperimentalTextApi::class)
private fun robotoFlex(weight: FontWeight) = Font(
    R.font.roboto_flex,
    weight = weight,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight.weight),
        FontVariation.width(112f),
    ),
)

/** Between ExtraBold and Black, for section and card titles. */
val FontWeightHeavy = FontWeight(850)

val RobotoFlex = FontFamily(
    robotoFlex(FontWeight.Normal),
    robotoFlex(FontWeight.Medium),
    robotoFlex(FontWeight.SemiBold),
    robotoFlex(FontWeight.Bold),
    robotoFlex(FontWeight.ExtraBold),
    robotoFlex(FontWeightHeavy),
    robotoFlex(FontWeight.Black),
)

private fun flex(size: Int, weight: FontWeight, lineHeight: Int, letterSpacing: Double = 0.0) = TextStyle(
    fontFamily = RobotoFlex,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
)

private val Base = Typography()

val Typography = Typography(
    // Servings stepper number; tabular figures so the width doesn't jump between values
    displayLarge = flex(44, FontWeight.Black, 48, -1.0).copy(fontFeatureSettings = "tnum"),
    // Screen titles: "Recipe Box", "Shopping list"
    displayMedium = flex(40, FontWeight.Black, 44, -1.0),
    // Recipe detail title
    displaySmall = flex(34, FontWeight.Black, 38, -0.5),
    headlineLarge = flex(32, FontWeight.Black, 38, -0.5),
    // Hero card title
    headlineMedium = flex(26, FontWeightHeavy, 30, -0.25),
    // Section headings: "Recently added", "Ingredients", "To buy"
    headlineSmall = flex(20, FontWeightHeavy, 26),
    // Card titles, "Servings", big button labels
    titleLarge = flex(18, FontWeight.ExtraBold, 22),
    // List row names (ingredients, shopping items)
    titleMedium = flex(17, FontWeight.Bold, 22),
    // Amounts in pills and on shopping rows
    titleSmall = flex(16, FontWeight.ExtraBold, 20),
    labelLarge = flex(14, FontWeight.Bold, 20),
    // Card metadata: "Serves 2 · 9 items", "From 2 recipes"
    labelMedium = flex(13, FontWeight.SemiBold, 16),
    labelSmall = Base.labelSmall.copy(fontFamily = RobotoFlex, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
)
