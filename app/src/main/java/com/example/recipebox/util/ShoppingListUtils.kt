package com.example.recipebox.util

import com.example.recipebox.data.Ingredient
import com.example.recipebox.data.ShoppingItem

fun mergeIntoShoppingList(
    existing: List<ShoppingItem>,
    ingredients: List<Ingredient>,
    baseServings: Int,
    chosenServings: Int,
): List<ShoppingItem> {
    val result = existing.toMutableList()

    for (ingredient in ingredients) {
        val scaledAmount = ingredient.amount?.let { scaleAmount(it, baseServings, chosenServings) }

        if (scaledAmount == null) {
            val alreadyPresent = result.any { it.name.equals(ingredient.name, ignoreCase = true) }
            if (!alreadyPresent) {
                result.add(ShoppingItem(name = ingredient.name, amount = null, unit = ingredient.unit))
            }
        } else {
            val matchIndex = result.indexOfFirst {
                it.name.equals(ingredient.name, ignoreCase = true) && it.unit == ingredient.unit
            }
            if (matchIndex >= 0) {
                val existing = result[matchIndex]
                result[matchIndex] = existing.copy(amount = (existing.amount ?: 0.0) + scaledAmount)
            } else {
                result.add(ShoppingItem(name = ingredient.name, amount = scaledAmount, unit = ingredient.unit))
            }
        }
    }

    return result
}
