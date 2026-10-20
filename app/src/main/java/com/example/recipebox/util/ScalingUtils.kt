package com.example.recipebox.util

import com.example.recipebox.data.Ingredient
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

fun scaleAmount(amount: Double, baseServings: Int, chosenServings: Int): Double =
    amount * chosenServings.toDouble() / baseServings.toDouble()

private val cookingFractionUnits = setOf("tsp", "tbsp", "cup")

private val fractionSymbols = listOf(
    1.0 / 4 to "¼",
    1.0 / 3 to "⅓",
    1.0 / 2 to "½",
    2.0 / 3 to "⅔",
    3.0 / 4 to "¾",
)

fun formatAmount(amount: Double, unit: String): String =
    if (unit in cookingFractionUnits) formatWithFractions(amount) else formatDecimal(amount)

private fun formatDecimal(amount: Double): String {
    val rounded = (amount * 100).roundToInt() / 100.0
    return if (rounded == floor(rounded)) rounded.toLong().toString()
    else "%.2f".format(rounded).trimEnd('0').trimEnd('.')
}

private fun formatWithFractions(amount: Double): String {
    val rounded = (amount * 100).roundToInt() / 100.0
    val whole = floor(rounded).toInt()
    val frac = rounded - whole
    val (closestValue, closestSymbol) = fractionSymbols.minByOrNull { (v, _) -> abs(v - frac) }!!
    return when {
        frac < 0.07 -> whole.toString()
        abs(closestValue - frac) > 0.07 -> formatDecimal(rounded)
        whole == 0 -> closestSymbol
        else -> "$whole$closestSymbol"
    }
}

fun formatIngredient(ingredient: Ingredient, baseServings: Int, chosenServings: Int): String {
    val amount = ingredient.amount ?: return ingredient.name
    val scaled = scaleAmount(amount, baseServings, chosenServings)
    val amountStr = formatAmount(scaled, ingredient.unit)
    return listOfNotNull(amountStr, ingredient.unit.ifEmpty { null }, ingredient.name)
        .joinToString(" ")
}
