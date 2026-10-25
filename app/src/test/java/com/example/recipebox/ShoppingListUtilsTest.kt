package com.example.recipebox

import com.example.recipebox.data.Ingredient
import com.example.recipebox.data.ShoppingItem
import com.example.recipebox.util.addManualItem
import com.example.recipebox.util.mergeIntoShoppingList
import com.example.recipebox.util.parseAmount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShoppingListUtilsTest {

    @Test
    fun `empty list - all ingredients added at scaled amounts`() {
        val result = mergeIntoShoppingList(
            existing = emptyList(),
            ingredients = listOf(
                Ingredient(200.0, "g", "pasta"),
                Ingredient(100.0, "ml", "cream"),
            ),
            baseServings = 4,
            chosenServings = 4,
        )
        assertEquals(2, result.size)
        assertEquals(200.0, result[0].amount!!, 0.001)
        assertEquals("pasta", result[0].name)
        assertEquals("g", result[0].unit)
        assertEquals(100.0, result[1].amount!!, 0.001)
    }

    @Test
    fun `same name and unit - amounts are summed`() {
        val existing = listOf(ShoppingItem(id = 1, name = "pasta", amount = 200.0, unit = "g"))
        val result = mergeIntoShoppingList(
            existing = existing,
            ingredients = listOf(Ingredient(200.0, "g", "pasta")),
            baseServings = 4,
            chosenServings = 4,
        )
        assertEquals(1, result.size)
        assertEquals(400.0, result[0].amount!!, 0.001)
        assertEquals(1, result[0].id)
    }

    @Test
    fun `same name but different unit - separate items`() {
        val existing = listOf(ShoppingItem(id = 1, name = "cream", amount = 200.0, unit = "ml"))
        val result = mergeIntoShoppingList(
            existing = existing,
            ingredients = listOf(Ingredient(1.0, "cup", "cream")),
            baseServings = 4,
            chosenServings = 4,
        )
        assertEquals(2, result.size)
        assertEquals("ml", result[0].unit)
        assertEquals("cup", result[1].unit)
    }

    @Test
    fun `null amount ingredient - added once`() {
        val result = mergeIntoShoppingList(
            existing = emptyList(),
            ingredients = listOf(Ingredient(null, "", "salt to taste")),
            baseServings = 4,
            chosenServings = 4,
        )
        assertEquals(1, result.size)
        assertNull(result[0].amount)
        assertEquals("salt to taste", result[0].name)
    }

    @Test
    fun `null amount ingredient already present - not duplicated`() {
        val existing = listOf(ShoppingItem(id = 1, name = "salt to taste", amount = null, unit = ""))
        val result = mergeIntoShoppingList(
            existing = existing,
            ingredients = listOf(Ingredient(null, "", "salt to taste")),
            baseServings = 4,
            chosenServings = 4,
        )
        assertEquals(1, result.size)
    }

    @Test
    fun `null amount name match is case insensitive`() {
        val existing = listOf(ShoppingItem(id = 1, name = "Salt To Taste", amount = null, unit = ""))
        val result = mergeIntoShoppingList(
            existing = existing,
            ingredients = listOf(Ingredient(null, "", "salt to taste")),
            baseServings = 4,
            chosenServings = 4,
        )
        assertEquals(1, result.size)
    }

    @Test
    fun `scaling applied correctly`() {
        val result = mergeIntoShoppingList(
            existing = emptyList(),
            ingredients = listOf(Ingredient(200.0, "g", "pasta")),
            baseServings = 4,
            chosenServings = 6,
        )
        assertEquals(300.0, result[0].amount!!, 0.001)
    }

    @Test
    fun `merge amount match is case insensitive`() {
        val existing = listOf(ShoppingItem(id = 1, name = "Parmesan", amount = 100.0, unit = "g"))
        val result = mergeIntoShoppingList(
            existing = existing,
            ingredients = listOf(Ingredient(100.0, "g", "parmesan")),
            baseServings = 4,
            chosenServings = 4,
        )
        assertEquals(1, result.size)
        assertEquals(200.0, result[0].amount!!, 0.001)
    }

    @Test
    fun `new item id is zero`() {
        val result = mergeIntoShoppingList(
            existing = emptyList(),
            ingredients = listOf(Ingredient(100.0, "g", "flour")),
            baseServings = 2,
            chosenServings = 2,
        )
        assertEquals(0, result[0].id)
    }

    @Test
    fun `existing item with no amount treated as zero when merging`() {
        val existing = listOf(ShoppingItem(id = 1, name = "butter", amount = null, unit = "g"))
        val result = mergeIntoShoppingList(
            existing = existing,
            ingredients = listOf(Ingredient(50.0, "g", "butter")),
            baseServings = 2,
            chosenServings = 2,
        )
        assertEquals(1, result.size)
        assertEquals(50.0, result[0].amount!!, 0.001)
    }

    @Test
    fun `checked status preserved on merged item`() {
        val existing = listOf(ShoppingItem(id = 1, name = "pasta", amount = 200.0, unit = "g", checked = true))
        val result = mergeIntoShoppingList(
            existing = existing,
            ingredients = listOf(Ingredient(200.0, "g", "pasta")),
            baseServings = 4,
            chosenServings = 4,
        )
        assertTrue(result[0].checked)
    }

    @Test
    fun `merging from a second recipe records both sources`() {
        val existing = listOf(ShoppingItem(id = 1, name = "onions", amount = 1.0, unit = "", sourceRecipeIds = setOf(7)))
        val result = mergeIntoShoppingList(
            existing = existing,
            ingredients = listOf(Ingredient(1.0, "", "Onions"), Ingredient(null, "", "salt")),
            baseServings = 2,
            chosenServings = 2,
            recipeId = 9,
        )
        assertEquals(setOf(7, 9), result[0].sourceRecipeIds)
        assertEquals(setOf(9), result[1].sourceRecipeIds)
    }

    @Test
    fun `adding the same recipe twice keeps one source`() {
        val first = mergeIntoShoppingList(emptyList(), listOf(Ingredient(null, "", "salt")), 2, 2, recipeId = 7)
        val second = mergeIntoShoppingList(first, listOf(Ingredient(null, "", "salt")), 2, 2, recipeId = 7)
        assertEquals(1, second.size)
        assertEquals(setOf(7), second[0].sourceRecipeIds)
    }

    @Test
    fun `manual item - added to empty list with trimmed name`() {
        val result = addManualItem(emptyList(), "  Milk ", "2", "l")
        assertEquals(1, result.size)
        assertEquals("Milk", result[0].name)
        assertEquals(2.0, result[0].amount!!, 0.001)
        assertEquals("l", result[0].unit)
        assertTrue(result[0].sourceRecipeIds.isEmpty())
    }

    @Test
    fun `manual item - same name ignoring case and same unit merges into existing`() {
        val existing = listOf(ShoppingItem(id = 3, name = "pasta", amount = 200.0, unit = "g", sourceRecipeIds = setOf(1)))
        val result = addManualItem(existing, " PASTA", "150", "g")
        assertEquals(1, result.size)
        assertEquals(3, result[0].id)
        assertEquals(350.0, result[0].amount!!, 0.001)
        assertEquals(setOf(1), result[0].sourceRecipeIds)
    }

    @Test
    fun `manual item - same name but different unit is a separate item`() {
        val existing = listOf(ShoppingItem(id = 3, name = "milk", amount = 500.0, unit = "ml"))
        val result = addManualItem(existing, "milk", "1", "l")
        assertEquals(2, result.size)
        assertEquals(500.0, result[0].amount!!, 0.001)
        assertEquals("l", result[1].unit)
    }

    @Test
    fun `manual item - merging into a ticked item unticks it`() {
        val existing = listOf(ShoppingItem(id = 3, name = "eggs", amount = 6.0, unit = "item", checked = true))
        val result = addManualItem(existing, "Eggs", "6", "item")
        assertEquals(12.0, result[0].amount!!, 0.001)
        assertFalse(result[0].checked)
    }

    @Test
    fun `manual item - no amount is added with null amount`() {
        val result = addManualItem(emptyList(), "Salt", "", "")
        assertEquals(1, result.size)
        assertNull(result[0].amount)
    }

    @Test
    fun `manual item - invalid amount is treated as no amount`() {
        val result = addManualItem(emptyList(), "Bread", "a loaf", "item")
        assertNull(result[0].amount)
    }

    @Test
    fun `manual item - no amount already on list is not duplicated`() {
        val existing = listOf(ShoppingItem(id = 3, name = "salt", amount = null, unit = ""))
        val result = addManualItem(existing, "Salt", "", "")
        assertEquals(existing, result)
    }

    @Test
    fun `manual item - ticked no-amount item already on list is unticked, not duplicated`() {
        val existing = listOf(ShoppingItem(id = 3, name = "salt", amount = null, unit = "", checked = true))
        val result = addManualItem(existing, "salt", "", "")
        assertEquals(1, result.size)
        assertFalse(result[0].checked)
    }

    @Test
    fun `parseAmount - accepts dot or comma decimals and rejects blank, junk, zero and negatives`() {
        assertEquals(1.5, parseAmount(" 1.5 ")!!, 0.001)
        assertEquals(1.5, parseAmount("1,5")!!, 0.001)
        assertNull(parseAmount(""))
        assertNull(parseAmount("   "))
        assertNull(parseAmount("abc"))
        assertNull(parseAmount("0"))
        assertNull(parseAmount("-2"))
        assertNull(parseAmount("NaN"))
    }
}
