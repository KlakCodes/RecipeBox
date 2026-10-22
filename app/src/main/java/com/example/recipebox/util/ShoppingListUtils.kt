package com.example.recipebox.util

import com.example.recipebox.data.Ingredient
import com.example.recipebox.data.ShoppingItem

/**
 * Merges a recipe's ingredients (scaled to [chosenServings]) into the list. [recipeId] is recorded on
 * every item the recipe contributes to, so the UI can show how many recipes an item came from.
 */
fun mergeIntoShoppingList(
    existing: List<ShoppingItem>,
    ingredients: List<Ingredient>,
    baseServings: Int,
    chosenServings: Int,
    recipeId: Int? = null,
): List<ShoppingItem> {
    val result = existing.toMutableList()
    val sources = setOfNotNull(recipeId)

    for (ingredient in ingredients) {
        val scaledAmount = ingredient.amount?.let { scaleAmount(it, baseServings, chosenServings) }

        if (scaledAmount == null) {
            val matchIndex = result.indexOfFirst { it.name.equals(ingredient.name, ignoreCase = true) }
            if (matchIndex >= 0) {
                val match = result[matchIndex]
                result[matchIndex] = match.copy(sourceRecipeIds = match.sourceRecipeIds + sources)
            } else {
                result.add(ShoppingItem(name = ingredient.name, amount = null, unit = ingredient.unit, sourceRecipeIds = sources))
            }
        } else {
            val matchIndex = result.indexOfFirst {
                it.name.equals(ingredient.name, ignoreCase = true) && it.unit == ingredient.unit
            }
            if (matchIndex >= 0) {
                val match = result[matchIndex]
                result[matchIndex] = match.copy(
                    amount = (match.amount ?: 0.0) + scaledAmount,
                    sourceRecipeIds = match.sourceRecipeIds + sources,
                )
            } else {
                result.add(ShoppingItem(name = ingredient.name, amount = scaledAmount, unit = ingredient.unit, sourceRecipeIds = sources))
            }
        }
    }

    return result
}
