package com.example.recipebox.ui.recipedetail

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recipebox.RecipeBoxApplication
import com.example.recipebox.data.Ingredient
import com.example.recipebox.data.Recipe
import com.example.recipebox.data.Step
import com.example.recipebox.ui.components.DecorShape
import com.example.recipebox.ui.components.ServingsCard
import com.example.recipebox.ui.components.TonalIconButton
import com.example.recipebox.ui.components.recipeIcon
import com.example.recipebox.ui.theme.RecipeBoxShapes
import com.example.recipebox.util.formatScaledAmount
import com.example.recipebox.util.plural
import com.example.recipebox.util.splitFreeTextNote

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RecipeDetailScreen(
    onBack: () -> Unit,
    onEdit: (Int) -> Unit = {},
    viewModel: RecipeDetailViewModel = viewModel(
        factory = RecipeDetailViewModel.factory(
            recipeRepository = (LocalContext.current.applicationContext as RecipeBoxApplication).repository,
            shoppingRepository = (LocalContext.current.applicationContext as RecipeBoxApplication).shoppingRepository,
        ),
    ),
) {
    val recipe by viewModel.recipe.collectAsState()
    val chosenServings by viewModel.chosenServings.collectAsState()
    val addedToList by viewModel.addedToList.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(addedToList) {
        if (addedToList) {
            snackbarHostState.showSnackbar("Added to shopping list")
            viewModel.resetAddedToList()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val r = recipe
        if (r == null) {
            LoadingIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                // Bottom padding clears the floating add button (20 + 68 + 20)
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 112.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TonalIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", onBack)
                        TonalIconButton(
                            Icons.Outlined.Edit,
                            "Edit recipe",
                            onClick = { onEdit(r.id) },
                            shape = RecipeBoxShapes.Squircle,
                        )
                    }
                }

                item { HeroPanel(r, modifier = Modifier.padding(top = 8.dp)) }

                item {
                    ServingsCard(
                        servings = chosenServings,
                        onDecrement = { viewModel.decrementServings() },
                        onIncrement = { viewModel.incrementServings() },
                        caption = if (chosenServings != r.servings) "Scaled from ${r.servings}" else "Original recipe",
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

                item { SectionHeader("Ingredients") }

                if (r.ingredients.isNotEmpty()) {
                    items(r.ingredients) { ingredient ->
                        IngredientRow(ingredient, r.servings, chosenServings)
                    }
                } else {
                    item { EmptyNote("No ingredients added") }
                }

                item { SectionHeader("Method") }

                if (r.steps.isNotEmpty()) {
                    items(r.steps) { step -> StepRow(step) }
                } else {
                    item { EmptyNote("No method steps added") }
                }
            }

            Button(
                onClick = { viewModel.addToShoppingList() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
                    .fillMaxWidth()
                    .height(68.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp),
            ) {
                Icon(Icons.Outlined.ShoppingCart, contentDescription = null)
                Spacer(Modifier.size(12.dp))
                Text(
                    "Add ${plural(chosenServings, "serving")} to list",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }

        SnackbarHost(
            snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HeroPanel(recipe: Recipe, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(196.dp)
            .clip(RecipeBoxShapes.HeroLarge)
            .background(colors.primaryContainer),
    ) {
        DecorShape(
            polygon = MaterialShapes.Sunny,
            color = colors.primary.copy(alpha = 0.22f),
            rotation = 12f,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 36.dp, y = (-32).dp)
                .size(184.dp),
        ) {
            Icon(
                recipeIcon(recipe.id),
                contentDescription = null,
                tint = colors.onPrimaryContainer,
                modifier = Modifier
                    .offset(x = (-14).dp, y = 14.dp)
                    .size(64.dp),
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, end = 24.dp, bottom = 22.dp),
        ) {
            Text(recipe.title, style = MaterialTheme.typography.displaySmall, color = colors.onPrimaryContainer)
            Text(
                "${plural(recipe.ingredients.size, "ingredient")} · ${plural(recipe.steps.size, "step")}",
                style = MaterialTheme.typography.labelLarge,
                color = colors.onPrimaryContainer,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun IngredientRow(ingredient: Ingredient, baseServings: Int, chosenServings: Int) {
    val scaled = formatScaledAmount(ingredient, baseServings, chosenServings)
    // Amount-less items like "salt to taste" put the note in the pill instead
    val (pill, name) = if (scaled != null) scaled to ingredient.name else splitFreeTextNote(ingredient.name)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .widthIn(min = 92.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    pill ?: "—",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                name.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                modifier = Modifier.padding(start = 14.dp, end = 8.dp),
            )
        }
    }
}

@Composable
private fun StepRow(step: Step) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "${step.position}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
        Text(
            step.text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1f)
                .padding(top = 6.dp),
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun EmptyNote(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp),
    )
}
