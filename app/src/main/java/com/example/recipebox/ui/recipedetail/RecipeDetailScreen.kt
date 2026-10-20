package com.example.recipebox.ui.recipedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recipebox.RecipeBoxApplication
import com.example.recipebox.util.formatIngredient

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe?.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    recipe?.let { r ->
                        IconButton(onClick = { onEdit(r.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit recipe")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (recipe != null) {
                ExtendedFloatingActionButton(
                    text = { Text("Add to list") },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                    onClick = { viewModel.addToShoppingList() },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        recipe?.let { r ->
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.decrementServings() },
                            enabled = chosenServings > 1,
                        ) {
                            Text("−", style = MaterialTheme.typography.titleLarge)
                        }
                        Text(
                            "$chosenServings servings",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.widthIn(min = 96.dp),
                        )
                        IconButton(onClick = { viewModel.incrementServings() }) {
                            Text("+", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }

                if (r.ingredients.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))
                        Text("Ingredients", style = MaterialTheme.typography.titleMedium)
                    }
                    items(r.ingredients) { ingredient ->
                        Text(
                            formatIngredient(ingredient, r.servings, chosenServings),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                if (r.steps.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))
                        Text("Method", style = MaterialTheme.typography.titleMedium)
                    }
                    items(r.steps) { step ->
                        Text(
                            "${step.position}. ${step.text}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}
