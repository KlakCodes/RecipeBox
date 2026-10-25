package com.example.recipebox.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.SoupKitchen
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recipebox.RecipeBoxApplication
import com.example.recipebox.ui.recipedetail.RecipeDetailScreen
import com.example.recipebox.ui.recipeedit.RecipeEditScreen
import com.example.recipebox.ui.recipelist.RecipeListScreen
import com.example.recipebox.ui.settings.SettingsScreen
import com.example.recipebox.ui.shoppinglist.ShoppingListScreen
import com.example.recipebox.ui.shoppinglist.ShoppingListViewModel

@Composable
fun RecipeBoxNavGraph(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute == "recipe_list" || currentRoute == "shopping_list"

    // Activity-scoped, so the badge stays live on every tab
    val shoppingViewModel: ShoppingListViewModel = viewModel(
        factory = ShoppingListViewModel.factory(
            (LocalContext.current.applicationContext as RecipeBoxApplication).shoppingRepository,
        ),
    )
    val uncheckedCount by shoppingViewModel.uncheckedCount.collectAsState()

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                ShortNavigationBar {
                    ShortNavigationBarItem(
                        selected = currentRoute == "recipe_list",
                        onClick = {
                            navController.navigate("recipe_list") {
                                popUpTo("recipe_list") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Outlined.SoupKitchen, contentDescription = null) },
                        label = { Text("Recipes") },
                    )
                    ShortNavigationBarItem(
                        selected = currentRoute == "shopping_list",
                        onClick = {
                            navController.navigate("shopping_list") {
                                popUpTo("recipe_list") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (uncheckedCount > 0) Badge { Text("$uncheckedCount") }
                                },
                            ) {
                                Icon(Icons.Outlined.ShoppingCart, contentDescription = null)
                            }
                        },
                        label = { Text("Shopping list") },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "recipe_list",
            // Consume the bars' insets so screens with their own Scaffold don't pad for them twice
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            composable("recipe_list") {
                RecipeListScreen(
                    onRecipeClick = { id -> navController.navigate("recipe_detail/$id") },
                    onAddClick = { navController.navigate("recipe_edit") },
                    onSettingsClick = { navController.navigate("settings") },
                )
            }
            composable("settings") {
                SettingsScreen(onBack = { navController.popBackStack() })
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
