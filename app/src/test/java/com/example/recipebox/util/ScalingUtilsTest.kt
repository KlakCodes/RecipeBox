package com.example.recipebox.util

import com.example.recipebox.data.Ingredient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScalingUtilsTest {

    // --- scaleAmount ---

    @Test
    fun `scaleAmount returns same value when servings unchanged`() {
        assertEquals(200.0, scaleAmount(200.0, 4, 4), 0.001)
    }

    @Test
    fun `scaleAmount doubles when servings double`() {
        assertEquals(400.0, scaleAmount(200.0, 4, 8), 0.001)
    }

    @Test
    fun `scaleAmount halves when servings halve`() {
        assertEquals(100.0, scaleAmount(200.0, 4, 2), 0.001)
    }

    @Test
    fun `scaleAmount scales by ratio`() {
        assertEquals(300.0, scaleAmount(200.0, 4, 6), 0.001)
    }

    @Test
    fun `scaleAmount handles fractional result`() {
        // 1 tbsp * 3/2 = 1.5 tbsp
        assertEquals(1.5, scaleAmount(1.0, 2, 3), 0.001)
    }

    // --- formatAmount: decimal units ---

    @Test
    fun `formatAmount shows integer for whole grams`() {
        assertEquals("300", formatAmount(300.0, "g"))
    }

    @Test
    fun `formatAmount shows integer for whole ml`() {
        assertEquals("200", formatAmount(200.0, "ml"))
    }

    @Test
    fun `formatAmount shows decimal for non-integer grams`() {
        assertEquals("1.5", formatAmount(1.5, "g"))
    }

    @Test
    fun `formatAmount hides float noise in grams`() {
        // 200 * 6/4 has no float noise, but check near-integer is clean
        assertEquals("300", formatAmount(300.0000001, "g"))
    }

    // --- formatAmount: cooking fraction units ---

    @Test
    fun `formatAmount shows half fraction for tsp`() {
        assertEquals("½", formatAmount(0.5, "tsp"))
    }

    @Test
    fun `formatAmount shows mixed number for tbsp`() {
        assertEquals("1½", formatAmount(1.5, "tbsp"))
    }

    @Test
    fun `formatAmount shows quarter fraction for cup`() {
        assertEquals("¼", formatAmount(0.25, "cup"))
    }

    @Test
    fun `formatAmount shows third fraction for tsp`() {
        assertEquals("⅓", formatAmount(1.0 / 3, "tsp"))
    }

    @Test
    fun `formatAmount shows two-thirds fraction for tbsp`() {
        assertEquals("2⅔", formatAmount(2.0 + 2.0 / 3, "tbsp"))
    }

    @Test
    fun `formatAmount shows three-quarters fraction for tsp`() {
        assertEquals("¾", formatAmount(0.75, "tsp"))
    }

    @Test
    fun `formatAmount shows whole number for integer tbsp`() {
        assertEquals("3", formatAmount(3.0, "tbsp"))
    }

    @Test
    fun `formatAmount shows one-third mixed number`() {
        assertEquals("1⅓", formatAmount(1.0 + 1.0 / 3, "tbsp"))
    }

    // --- formatIngredient ---

    @Test
    fun `formatIngredient scales and formats grams`() {
        assertEquals("300 g pasta", formatIngredient(Ingredient(200.0, "g", "pasta"), 4, 6))
    }

    @Test
    fun `formatIngredient returns name only when amount is null`() {
        assertEquals("salt to taste", formatIngredient(Ingredient(null, "", "salt to taste"), 4, 6))
    }

    @Test
    fun `formatIngredient uses fraction for cooking unit`() {
        // 1 tbsp * 3/2 = 1.5 tbsp → "1½ tbsp olive oil"
        assertEquals("1½ tbsp olive oil", formatIngredient(Ingredient(1.0, "tbsp", "olive oil"), 2, 3))
    }

    @Test
    fun `formatIngredient handles unit-less count ingredient`() {
        // 2 eggs * 6/4 = 3 eggs
        assertEquals("3 eggs", formatIngredient(Ingredient(2.0, "", "eggs"), 4, 6))
    }

    @Test
    fun `formatIngredient no scaling when servings unchanged`() {
        assertEquals("400 g chicken", formatIngredient(Ingredient(400.0, "g", "chicken"), 4, 4))
    }

    @Test
    fun `formatScaledAmount scales and keeps unit`() {
        assertEquals("300 g", formatScaledAmount(Ingredient(200.0, "g", "pasta"), 4, 6))
        assertEquals("1½ tbsp", formatScaledAmount(Ingredient(1.0, "tbsp", "oil"), 2, 3))
    }

    @Test
    fun `formatScaledAmount omits empty unit and returns null without amount`() {
        assertEquals("3", formatScaledAmount(Ingredient(3.0, "", "garlic cloves"), 4, 4))
        assertNull(formatScaledAmount(Ingredient(null, "", "salt"), 4, 4))
    }

    @Test
    fun `splitFreeTextNote moves trailing note into the pill`() {
        assertEquals("To taste" to "salt", splitFreeTextNote("salt to taste"))
        assertEquals("To serve" to "salsa", splitFreeTextNote("salsa, to serve"))
        assertEquals(null to "a handful of basil", splitFreeTextNote("a handful of basil"))
    }
}
