package com.example.recipebox.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Shapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

/** Fixed shapes from the Expressive mockups, beyond the five [Shapes] slots. */
object RecipeBoxShapes {
    val Card = RoundedCornerShape(28.dp)
    val Hero = RoundedCornerShape(36.dp)
    val HeroLarge = RoundedCornerShape(40.dp)
    val Squircle = RoundedCornerShape(16.dp)
    val Leaf = RoundedCornerShape(topStart = 30.dp, topEnd = 10.dp, bottomEnd = 30.dp, bottomStart = 10.dp)
}

/** Recipe card thumbnails cycle circle → leaf → cookie → rounded square. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun thumbnailShape(index: Int): Shape = when (index % 4) {
    0 -> CircleShape
    1 -> RecipeBoxShapes.Leaf
    2 -> MaterialShapes.Cookie9Sided.toShape()
    else -> RoundedCornerShape(20.dp)
}

// Expressive polygon shapes for badges (step numbers in the editor).
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val BadgePolygons = listOf(
    MaterialShapes.Cookie9Sided,
    MaterialShapes.Sunny,
    MaterialShapes.Clover4Leaf,
    MaterialShapes.Cookie6Sided,
    MaterialShapes.SoftBurst,
    MaterialShapes.Pentagon,
)
