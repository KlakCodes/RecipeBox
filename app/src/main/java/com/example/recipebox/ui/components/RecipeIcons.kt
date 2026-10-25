package com.example.recipebox.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.example.recipebox.R
import com.example.recipebox.data.Recipe
import com.example.recipebox.data.displayIcon
import com.example.recipebox.util.RecipeIconCategory

/** Material Symbols Rounded drawable for each category (res/drawable/ic_symbol_*). */
@DrawableRes
private fun RecipeIconCategory.drawable(): Int = when (this) {
    RecipeIconCategory.Pasta -> R.drawable.ic_symbol_dinner_dining
    RecipeIconCategory.Pizza -> R.drawable.ic_symbol_local_pizza
    RecipeIconCategory.Burger -> R.drawable.ic_symbol_lunch_dining
    RecipeIconCategory.Curry -> R.drawable.ic_symbol_stockpot
    RecipeIconCategory.Soup -> R.drawable.ic_symbol_soup_kitchen
    RecipeIconCategory.Rice -> R.drawable.ic_symbol_rice_bowl
    RecipeIconCategory.Salad -> R.drawable.ic_symbol_eco
    RecipeIconCategory.Fish -> R.drawable.ic_symbol_set_meal
    RecipeIconCategory.Meat -> R.drawable.ic_symbol_outdoor_grill
    RecipeIconCategory.Chicken -> R.drawable.ic_symbol_kebab_dining
    RecipeIconCategory.Egg -> R.drawable.ic_symbol_egg
    RecipeIconCategory.Breakfast -> R.drawable.ic_symbol_breakfast_dining
    RecipeIconCategory.Bread -> R.drawable.ic_symbol_bakery_dining
    RecipeIconCategory.Cake -> R.drawable.ic_symbol_cake
    RecipeIconCategory.Cookie -> R.drawable.ic_symbol_cookie
    RecipeIconCategory.Dessert -> R.drawable.ic_symbol_icecream
    RecipeIconCategory.Drink -> R.drawable.ic_symbol_local_cafe
    RecipeIconCategory.Cocktail -> R.drawable.ic_symbol_local_bar
    RecipeIconCategory.StirFry -> R.drawable.ic_symbol_skillet
    RecipeIconCategory.Fruit -> R.drawable.ic_symbol_nutrition
    RecipeIconCategory.Plate -> R.drawable.ic_symbol_restaurant
}

@Composable
fun RecipeIconCategory.vector(): ImageVector = ImageVector.vectorResource(drawable())

/** A recipe's icon: the one chosen on the edit screen, otherwise the automatic pick. */
@Composable
fun recipeIcon(recipe: Recipe): ImageVector = remember(recipe) { recipe.displayIcon() }.vector()
