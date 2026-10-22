package com.example.recipebox

import com.example.recipebox.data.Ingredient
import com.example.recipebox.data.ShoppingItem
import com.example.recipebox.util.mergeIntoShoppingList
import org.junit.Assert.assertEquals
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
}
