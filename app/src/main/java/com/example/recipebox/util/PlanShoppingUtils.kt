package com.example.recipebox.util

import com.example.recipebox.data.PlannedMeal
import com.example.recipebox.data.ShoppingItem

/**
 * Merges every planned meal into the list, each scaled from its recipe's base servings to the
 * meal's servings. Uses the same rules as adding one recipe, so a recipe planned twice adds up.
 */
fun addMealsToShoppingList(existing: List<ShoppingItem>, meals: List<PlannedMeal>): List<ShoppingItem> =
    meals.fold(existing) { list, meal ->
        mergeIntoShoppingList(list, meal.recipe.ingredients, meal.recipe.servings, meal.servings, meal.recipe.id)
    }
