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

/** Scaled amount and unit for the amount pill, e.g. "300 g" or "1½ tbsp"; null when the ingredient has no amount. */
fun formatScaledAmount(ingredient: Ingredient, baseServings: Int, chosenServings: Int): String? {
    val amount = ingredient.amount ?: return null
    val amountStr = formatAmount(scaleAmount(amount, baseServings, chosenServings), ingredient.unit)
    return listOfNotNull(amountStr, ingredient.unit.ifEmpty { null }).joinToString(" ")
}

private val freeTextNote = Regex("""^(.*?),?\s+(to taste|to serve)$""", RegexOption.IGNORE_CASE)

/**
 * Splits a trailing "to taste" / "to serve" off an amount-less ingredient name so it can sit in
 * the amount pill: "salt to taste" → ("To taste", "salt"). Returns (null, name) when there's no note.
 */
fun splitFreeTextNote(name: String): Pair<String?, String> {
    val match = freeTextNote.matchEntire(name.trim()) ?: return null to name
    val (rest, note) = match.destructured
    return note.replaceFirstChar { it.uppercase() } to rest
}
