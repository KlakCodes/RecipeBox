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

        val matchIndex = result.matchIndex(ingredient.name, ingredient.unit, hasAmount = scaledAmount != null)
        if (scaledAmount == null) {
            if (matchIndex >= 0) {
                val match = result[matchIndex]
                result[matchIndex] = match.copy(sourceRecipeIds = match.sourceRecipeIds + sources)
            } else {
                result.add(ShoppingItem(name = ingredient.name, amount = null, unit = ingredient.unit, sourceRecipeIds = sources))
            }
        } else {
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

/**
 * The item an ingredient merges into: same name (ignoring case) and, when it has an amount, the same unit.
 * Items with no amount match on name alone so they're never duplicated.
 */
private fun List<ShoppingItem>.matchIndex(name: String, unit: String, hasAmount: Boolean): Int = indexOfFirst {
    it.name.equals(name, ignoreCase = true) && (!hasAmount || it.unit == unit)
}

/** What editing an item does to the list. */
sealed interface ShoppingEdit {
    /** The item changed in place, keeping its id, ticked state and sources. */
    data class Updated(val item: ShoppingItem) : ShoppingEdit

    /** The edit matched another item: [removedId] goes, and [into] (that other item) takes its amount and sources. */
    data class Merged(val removedId: Int, val into: ShoppingItem) : ShoppingEdit
}

/**
 * Edits item [id] using the same rules as adding by hand: the name is trimmed and required, and a blank
 * or invalid amount means no amount. If the result matches another item (see [matchIndex]) the two
 * merge: amounts add up, sources combine, and it stays ticked only if both were ticked.
 * Returns null when the name is blank or the item is gone.
 */
fun editShoppingItem(existing: List<ShoppingItem>, id: Int, name: String, amountText: String, unit: String): ShoppingEdit? {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return null
    val original = existing.firstOrNull { it.id == id } ?: return null
    val edited = original.copy(name = trimmed, amount = parseAmount(amountText), unit = unit)

    val others = existing.filter { it.id != id }
    val matchIndex = others.matchIndex(edited.name, edited.unit, hasAmount = edited.amount != null)
    if (matchIndex < 0) return ShoppingEdit.Updated(edited)

    val target = others[matchIndex]
    val amount = if (target.amount == null && edited.amount == null) null else (target.amount ?: 0.0) + (edited.amount ?: 0.0)
    return ShoppingEdit.Merged(
        removedId = id,
        into = target.copy(
            amount = amount,
            checked = target.checked && edited.checked,
            sourceRecipeIds = target.sourceRecipeIds + edited.sourceRecipeIds,
        ),
    )
}

/** An amount typed by hand. Blank, unparseable, zero or negative means no amount. "1,5" works as well as "1.5". */
fun parseAmount(text: String): Double? =
    text.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() && it > 0 }

/**
 * Adds an item typed on the Shopping list, using the same merge rules as recipes. A ticked match is
 * unticked, since adding it again means you need to buy it.
 */
fun addManualItem(existing: List<ShoppingItem>, name: String, amountText: String, unit: String): List<ShoppingItem> {
    val ingredient = Ingredient(parseAmount(amountText), unit, name.trim())
    val matchIndex = existing.matchIndex(ingredient.name, ingredient.unit, hasAmount = ingredient.amount != null)
    val merged = mergeIntoShoppingList(existing, listOf(ingredient), baseServings = 1, chosenServings = 1)
    if (matchIndex < 0) return merged
    return merged.toMutableList().also { it[matchIndex] = it[matchIndex].copy(checked = false) }
}
