package com.example.recipebox.ui.recipelist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recipebox.RecipeBoxApplication
import com.example.recipebox.data.Recipe
import com.example.recipebox.ui.components.DecorShape
import com.example.recipebox.ui.components.EmptyState
import com.example.recipebox.ui.components.recipeIcon
import com.example.recipebox.ui.theme.RecipeBoxShapes
import com.example.recipebox.ui.theme.thumbnailShape
import com.example.recipebox.util.plural
import java.time.LocalTime

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RecipeListScreen(
    onRecipeClick: (Int) -> Unit = {},
    onAddClick: () -> Unit = {},
    viewModel: RecipeListViewModel = viewModel(
        factory = RecipeListViewModel.factory(
            (LocalContext.current.applicationContext as RecipeBoxApplication).repository,
        ),
    ),
) {
    val recipes by viewModel.recipes.collectAsState()
    val greeting = remember { greetingFor(LocalTime.now().hour) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 112.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "header", span = { GridItemSpan(maxLineSpan) }) {
                Header(greeting)
            }
            if (recipes.isNotEmpty()) {
                item(key = "recent", span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        "Recently added",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
                // Recipes are sorted newest first, so the first one is the hero
                val newest = recipes.first()
                item(key = newest.id, span = { GridItemSpan(maxLineSpan) }) {
                    HeroRecipeCard(newest, onClick = { onRecipeClick(newest.id) })
                }
                itemsIndexed(recipes.drop(1), key = { _, it -> it.id }) { index, recipe ->
                    RecipeCard(recipe, index, onClick = { onRecipeClick(recipe.id) })
                }
            } else {
                item(key = "empty", span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState(
                        icon = {
                            Icon(
                                Icons.Default.RestaurantMenu,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                        },
                        polygon = MaterialShapes.Cookie9Sided,
                        title = "No recipes yet",
                        hint = "Tap New recipe to add your first one",
                        modifier = Modifier.padding(top = 64.dp),
                    )
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 16.dp)
                .height(72.dp),
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(Modifier.size(12.dp))
            Text("New recipe", style = MaterialTheme.typography.titleLarge)
        }
    }
}

internal fun greetingFor(hour: Int): String = when (hour) {
    in 5..11 -> "Good morning"
    in 12..17 -> "Good afternoon"
    else -> "Good evening"
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Header(greeting: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                greeting,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text("Recipe Box", style = MaterialTheme.typography.displayMedium)
        }
        DecorShape(
            polygon = MaterialShapes.Sunny,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier.size(56.dp),
        ) {
            Icon(
                Icons.Outlined.Eco,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HeroRecipeCard(recipe: Recipe, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(176.dp),
        shape = RecipeBoxShapes.Hero,
        colors = CardDefaults.cardColors(
            containerColor = colors.primaryContainer,
            contentColor = colors.onPrimaryContainer,
        ),
    ) {
        Box(Modifier.fillMaxSize()) {
            DecorShape(
                polygon = MaterialShapes.Sunny,
                // A tint of primary over the container: a stronger tone of the same hue in light and dark
                color = colors.primary.copy(alpha = 0.22f),
                rotation = 12f,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 28.dp, y = (-24).dp)
                    .size(156.dp),
            ) {
                Icon(
                    recipeIcon(recipe.id),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = (-10).dp, y = 10.dp)
                        .size(56.dp),
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 22.dp, end = 120.dp, bottom = 20.dp),
            ) {
                Text(
                    recipe.title,
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "Serves ${recipe.servings} · ${plural(recipe.ingredients.size, "ingredient")}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun RecipeCard(recipe: Recipe, index: Int, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    // Neighbouring cards cycle container colours; the thumbnail uses the matching strong colour
    val palette = when (index % 4) {
        0 -> CardPalette(colors.tertiaryContainer, colors.onTertiaryContainer, colors.tertiary, colors.onTertiary)
        1 -> CardPalette(colors.secondaryContainer, colors.onSecondaryContainer, colors.secondary, colors.onSecondary)
        2 -> CardPalette(colors.primaryContainer, colors.onPrimaryContainer, colors.primary, colors.onPrimary)
        else -> CardPalette(colors.surfaceContainerHighest, colors.onSurface, colors.primary, colors.onPrimary)
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(152.dp),
        shape = RecipeBoxShapes.Card,
        colors = CardDefaults.cardColors(containerColor = palette.container, contentColor = palette.onContainer),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(palette.strong, thumbnailShape(index)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(recipeIcon(recipe.id), contentDescription = null, tint = palette.onStrong)
            }
            Column {
                Text(
                    recipe.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "Serves ${recipe.servings} · ${plural(recipe.ingredients.size, "item")}",
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

private data class CardPalette(val container: Color, val onContainer: Color, val strong: Color, val onStrong: Color)
