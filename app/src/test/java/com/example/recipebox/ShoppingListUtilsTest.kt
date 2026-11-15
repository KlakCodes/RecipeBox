package com.example.recipebox

import com.example.recipebox.data.Ingredient
import com.example.recipebox.data.ShoppingItem
import com.example.recipebox.util.ShoppingEdit
import com.example.recipebox.util.addManualItem
import com.example.recipebox.util.editShoppingItem
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

    private val editList = listOf(
        ShoppingItem(id = 1, name = "milk", amount = 1.0, unit = "l", checked = true, sourceRecipeIds = setOf(3, 4)),
        ShoppingItem(id = 2, name = "Flour", amount = 200.0, unit = "g", checked = false, sourceRecipeIds = setOf(5)),
        ShoppingItem(id = 3, name = "salt", amount = null, unit = "", checked = true),
    )

    @Test
    fun `edit - changes name, amount and unit in place, keeping ticked state and sources`() {
        val edit = editShoppingItem(editList, id = 1, name = "  oat milk ", amountText = "1,5", unit = "l")
        assertTrue(edit is ShoppingEdit.Updated)
        val item = (edit as ShoppingEdit.Updated).item
        assertEquals(ShoppingItem(id = 1, name = "oat milk", amount = 1.5, unit = "l", checked = true, sourceRecipeIds = setOf(3, 4)), item)
    }

    @Test
    fun `edit - changing only the case doesn't merge the item with itself`() {
        val edit = editShoppingItem(editList, id = 2, name = "flour", amountText = "200", unit = "g")
        assertEquals(ShoppingEdit.Updated(editList[1].copy(name = "flour")), edit)
    }

    @Test
    fun `edit - blank or invalid amount means no amount`() {
        val blank = editShoppingItem(editList, id = 2, name = "Flour", amountText = " ", unit = "g") as ShoppingEdit.Updated
        assertNull(blank.item.amount)
        val junk = editShoppingItem(editList, id = 2, name = "Flour", amountText = "lots", unit = "g") as ShoppingEdit.Updated
        assertNull(junk.item.amount)
    }

    @Test
    fun `edit - blank name or missing item changes nothing`() {
        assertNull(editShoppingItem(editList, id = 1, name = "   ", amountText = "1", unit = "l"))
        assertNull(editShoppingItem(editList, id = 99, name = "eggs", amountText = "6", unit = ""))
    }

    @Test
    fun `edit into merge - same name ignoring case and unit adds the amounts and combines sources`() {
        val list = editList + ShoppingItem(id = 4, name = "plain flour", amount = 100.0, unit = "g", sourceRecipeIds = setOf(6))
        val edit = editShoppingItem(list, id = 4, name = "FLOUR", amountText = "100", unit = "g")
        assertEquals(
            ShoppingEdit.Merged(
                removedId = 4,
                into = ShoppingItem(id = 2, name = "Flour", amount = 300.0, unit = "g", checked = false, sourceRecipeIds = setOf(5, 6)),
            ),
            edit,
        )
    }

    @Test
    fun `edit into merge - different unit stays a separate item`() {
        val edit = editShoppingItem(editList, id = 1, name = "flour", amountText = "1", unit = "kg")
        assertTrue(edit is ShoppingEdit.Updated)
    }

    @Test
    fun `edit into merge - stays ticked only if both items were ticked`() {
        val ticked = editList.map { if (it.id == 2) it.copy(checked = true) else it } +
            ShoppingItem(id = 4, name = "bread flour", amount = 50.0, unit = "g", checked = true)
        val bothTicked = editShoppingItem(ticked, id = 4, name = "flour", amountText = "50", unit = "g") as ShoppingEdit.Merged
        assertTrue(bothTicked.into.checked)

        // milk (ticked) renamed into Flour (to buy): still something to buy
        val oneTicked = editShoppingItem(editList, id = 1, name = "flour", amountText = "100", unit = "g") as ShoppingEdit.Merged
        assertFalse(oneTicked.into.checked)
        assertEquals(300.0, oneTicked.into.amount!!, 0.001)
        assertEquals(setOf(3, 4, 5), oneTicked.into.sourceRecipeIds)
    }

    @Test
    fun `edit into merge - amount-less items merge on name, keeping any amount`() {
        // "Salt" with no amount matches "salt" by name alone, like adding does, so it's never listed twice
        val list = editList + ShoppingItem(id = 4, name = "sea salt", amount = null, unit = "")
        val noAmounts = editShoppingItem(list, id = 4, name = "Salt", amountText = "", unit = "") as ShoppingEdit.Merged
        assertEquals(3, noAmounts.into.id)
        assertNull(noAmounts.into.amount)

        val withAmount = editShoppingItem(
            editList + ShoppingItem(id = 4, name = "salt", amount = 5.0, unit = "g"),
            id = 3, name = "salt", amountText = "", unit = "",
        ) as ShoppingEdit.Merged
        assertEquals(4, withAmount.into.id)
        assertEquals(5.0, withAmount.into.amount!!, 0.001)
    }
}
