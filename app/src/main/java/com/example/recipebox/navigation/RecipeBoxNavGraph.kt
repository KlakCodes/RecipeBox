package com.example.recipebox.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recipebox.ui.recipedetail.RecipeDetailScreen
import com.example.recipebox.ui.recipeedit.RecipeEditScreen
import com.example.recipebox.ui.recipelist.RecipeListScreen

@Composable
fun RecipeBoxNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = "recipe_list") {
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
    }
}
