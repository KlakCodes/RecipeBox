package com.example.recipebox.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recipebox.ui.recipedetail.RecipeDetailScreen
import com.example.recipebox.ui.recipeedit.RecipeEditScreen
import com.example.recipebox.ui.recipelist.RecipeListScreen
import com.example.recipebox.ui.shoppinglist.ShoppingListScreen

@Composable
fun RecipeBoxNavGraph(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute == "recipe_list" || currentRoute == "shopping_list"

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == "recipe_list",
                        onClick = {
                            navController.navigate("recipe_list") {
                                popUpTo("recipe_list") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Recipes") },
                        label = { Text("Recipes") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == "shopping_list",
                        onClick = {
                            navController.navigate("shopping_list") {
                                popUpTo("recipe_list") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Shopping list") },
                        label = { Text("Shopping list") },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "recipe_list",
            modifier = Modifier.padding(innerPadding),
        ) {
            composable("recipe_list") {
                RecipeListScreen(
                    onRecipeClick = { id -> navController.navigate("recipe_detail/$id") },
                    onAddClick = { navController.navigate("recipe_edit") },
                )
            }
            composable(
                route = "recipe_detail/{recipeId}",
                arguments = listOf(navArgument("recipeId") { type = NavType.IntType }),
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getInt("recipeId") ?: return@composable
                RecipeDetailScreen(
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate("recipe_edit?recipeId=$recipeId") },
                )
            }
            composable(
                route = "recipe_edit?recipeId={recipeId}",
                arguments = listOf(navArgument("recipeId") { type = NavType.IntType; defaultValue = -1 }),
            ) {
                RecipeEditScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                    onDeleted = { navController.popBackStack("recipe_list", inclusive = false) },
                )
            }
            composable("shopping_list") {
                ShoppingListScreen()
            }
        }
    }
}
