package com.example.recipebox.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recipebox.RecipeBoxApplication
import com.example.recipebox.ui.components.LocalNavAnimatedVisibilityScope
import com.example.recipebox.ui.components.LocalSharedTransitionScope
import com.example.recipebox.ui.recipedetail.RecipeDetailScreen
import com.example.recipebox.ui.recipeedit.RecipeEditScreen
import com.example.recipebox.ui.recipelist.RecipeListScreen
import com.example.recipebox.ui.settings.SettingsScreen
import com.example.recipebox.ui.shoppinglist.ShoppingListScreen
import com.example.recipebox.ui.shoppinglist.ShoppingListViewModel
import com.example.recipebox.ui.theme.EmphasizedEasing
import com.example.recipebox.ui.theme.NavMotionDuration
import kotlinx.coroutines.flow.MutableSharedFlow

private const val RecipeListRoute = "recipe_list"
private const val ShoppingListRoute = "shopping_list"
internal val TabRoutes = setOf(RecipeListRoute, ShoppingListRoute)

@Composable
fun RecipeBoxNavGraph(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in TabRoutes

    // Activity-scoped, so the badge stays live on every tab
    val shoppingViewModel: ShoppingListViewModel = viewModel(
        factory = ShoppingListViewModel.factory(
            (LocalContext.current.applicationContext as RecipeBoxApplication).shoppingRepository,
        ),
    )
    val uncheckedCount by shoppingViewModel.uncheckedCount.collectAsState()

    // Tapping the tab you're already on sends one of these; the tab scrolls itself to the top.
    // Events rather than state, so a tab restored later doesn't replay an old request.
    val recipesScrollToTop = remember { MutableSharedFlow<Unit>(extraBufferCapacity = 1) }
    val shoppingScrollToTop = remember { MutableSharedFlow<Unit>(extraBufferCapacity = 1) }

    val animationsEnabled = rememberAnimationsEnabled()
    val slidePx = with(LocalDensity.current) { SharedAxisSlideDistance.roundToPx() }

    SharedTransitionLayout {
        Scaffold(
            bottomBar = {
                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = if (animationsEnabled) {
                        expandVertically(tween(NavMotionDuration, easing = EmphasizedEasing))
                    } else {
                        expandVertically(tween(0))
                    },
                    exit = if (animationsEnabled) {
                        shrinkVertically(tween(NavMotionDuration, easing = EmphasizedEasing))
                    } else {
                        shrinkVertically(tween(0))
                    },
                ) {
                    ShortNavigationBar {
                        ShortNavigationBarItem(
                            selected = currentRoute == RecipeListRoute,
                            onClick = {
                                if (currentRoute == RecipeListRoute) {
                                    recipesScrollToTop.tryEmit(Unit)
                                } else {
                                    navController.navigateToTab(RecipeListRoute)
                                }
                            },
                            icon = { Icon(Icons.Outlined.SoupKitchen, contentDescription = null) },
                            label = { Text("Recipes") },
                        )
                        ShortNavigationBarItem(
                            selected = currentRoute == ShoppingListRoute,
                            onClick = {
                                if (currentRoute == ShoppingListRoute) {
                                    shoppingScrollToTop.tryEmit(Unit)
                                } else {
                                    navController.navigateToTab(ShoppingListRoute)
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
            // With animations off there's no flight to share, so elements just appear in place
            CompositionLocalProvider(
                LocalSharedTransitionScope provides if (animationsEnabled) this@SharedTransitionLayout else null,
            ) {
                NavHost(
                    navController = navController,
                    startDestination = RecipeListRoute,
                    // Consume the bars' insets so screens with their own Scaffold don't pad for them twice
                    modifier = Modifier
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding),
                    enterTransition = navEnterTransition(slidePx, animationsEnabled, forward = true),
                    exitTransition = navExitTransition(slidePx, animationsEnabled, forward = true),
                    popEnterTransition = navEnterTransition(slidePx, animationsEnabled, forward = false),
                    popExitTransition = navExitTransition(slidePx, animationsEnabled, forward = false),
                ) {
                    composable(RecipeListRoute) { entry ->
                        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
                            RecipeListScreen(
                                onRecipeClick = { id -> navController.from(entry) { navigate("recipe_detail/$id") } },
                                onAddClick = { navController.from(entry) { navigate("recipe_edit") } },
                                onSettingsClick = { navController.from(entry) { navigate("settings") } },
                                scrollToTopRequests = recipesScrollToTop,
                            )
                        }
                    }
                    composable("settings") { entry ->
                        SettingsScreen(onBack = { navController.from(entry) { popBackStack() } })
                    }
                    composable(
                        route = "recipe_detail/{recipeId}",
                        arguments = listOf(navArgument("recipeId") { type = NavType.IntType }),
                    ) { entry ->
                        val recipeId = entry.arguments?.getInt("recipeId") ?: return@composable
                        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
                            RecipeDetailScreen(
                                onBack = { navController.from(entry) { popBackStack() } },
                                onEdit = { navController.from(entry) { navigate("recipe_edit?recipeId=$recipeId") } },
                            )
                        }
                    }
                    composable(
                        route = "recipe_edit?recipeId={recipeId}",
                        arguments = listOf(navArgument("recipeId") { type = NavType.IntType; defaultValue = -1 }),
                    ) { entry ->
                        RecipeEditScreen(
                            onBack = { navController.from(entry) { popBackStack() } },
                            onSaved = { navController.from(entry) { popBackStack() } },
                            onDeleted = { navController.from(entry) { popBackStack(RecipeListRoute, inclusive = false) } },
                        )
                    }
                    composable(ShoppingListRoute) {
                        ShoppingListScreen(scrollToTopRequests = shoppingScrollToTop)
                    }
                }
            }
        }
    }
}

/**
 * Runs [action] only while [entry] is still the top of the back stack. The first tap moves the top
 * on straight away, so a second quick tap on the screen that's leaving is ignored instead of
 * navigating (or popping) twice.
 */
private inline fun NavHostController.from(entry: NavBackStackEntry, action: NavHostController.() -> Unit) {
    if (currentBackStackEntry?.id == entry.id) action()
}

/** Switches tab, keeping each tab's own back stack and scroll position, never stacking a duplicate. */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
