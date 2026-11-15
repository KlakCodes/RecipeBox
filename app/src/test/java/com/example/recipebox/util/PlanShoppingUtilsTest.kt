package com.example.recipebox.util

import com.example.recipebox.data.Ingredient
import com.example.recipebox.data.PlannedMeal
import com.example.recipebox.data.Recipe
import com.example.recipebox.data.ShoppingItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PlanShoppingUtilsTest {
    private val monday = LocalDate.of(2026, 11, 9)

    private val pasta = Recipe(
        id = 1,
        title = "Pasta",
        servings = 4,
        ingredients = listOf(
            Ingredient(200.0, "g", "spaghetti"),
            Ingredient(2.0, "tbsp", "olive oil"),
            Ingredient(null, "", "salt"),
        ),
    )

    private val salad = Recipe(
        id = 2,
        title = "Salad",
        servings = 2,
        ingredients = listOf(
            Ingredient(1.0, "tbsp", "Olive oil"),
            Ingredient(100.0, "g", "feta"),
            Ingredient(null, "", "Salt"),
        ),
    )

    private fun meal(id: Int, recipe: Recipe, servings: Int, day: Long = 0) =
        PlannedMeal(id = id, date = monday.plusDays(day), servings = servings, position = 0, recipe = recipe)

    private fun List<ShoppingItem>.named(name: String) = single { it.name.equals(name, ignoreCase = true) }

    @Test
    fun `empty week leaves the list unchanged`() {
        val existing = listOf(ShoppingItem(id = 5, name = "milk", amount = 1.0, unit = "l"))
        assertEquals(existing, addMealsToShoppingList(existing, emptyList()))
    }

    @Test
    fun `each meal is scaled to its own servings`() {
        val result = addMealsToShoppingList(emptyList(), listOf(meal(1, pasta, servings = 6)))
        assertEquals(300.0, result.named("spaghetti").amount!!, 0.0001)
        assertEquals(3.0, result.named("olive oil").amount!!, 0.0001)
    }

    @Test
    fun `same recipe planned twice at different servings adds up`() {
        val result = addMealsToShoppingList(
            emptyList(),
            listOf(meal(1, pasta, servings = 4, day = 0), meal(2, pasta, servings = 2, day = 3)),
        )
        // 200 g for 4 + 100 g for 2
        assertEquals(300.0, result.named("spaghetti").amount!!, 0.0001)
        assertEquals(1, result.count { it.name.equals("spaghetti", ignoreCase = true) })
    }

    @Test
    fun `ingredient shared by two recipes merges ignoring case`() {
        val result = addMealsToShoppingList(emptyList(), listOf(meal(1, pasta, 4), meal(2, salad, 4)))
        // 2 tbsp (pasta at base) + 1 tbsp × 4/2 (salad doubled)
        val oil = result.named("olive oil")
        assertEquals(4.0, oil.amount!!, 0.0001)
        assertEquals(setOf(1, 2), oil.sourceRecipeIds)
    }

    @Test
    fun `amount-less items appear once however many meals use them`() {
        val result = addMealsToShoppingList(
            emptyList(),
            listOf(meal(1, pasta, 4), meal(2, salad, 2), meal(3, pasta, 8)),
        )
        val salt = result.named("salt")
        assertNull(salt.amount)
        assertEquals(setOf(1, 2), salt.sourceRecipeIds)
    }

    @Test
    fun `meals merge into items already on the list`() {
        val existing = listOf(
            ShoppingItem(id = 7, name = "Spaghetti", amount = 500.0, unit = "g"),
            ShoppingItem(id = 8, name = "milk", amount = 1.0, unit = "l"),
        )
        val result = addMealsToShoppingList(existing, listOf(meal(1, pasta, 4)))
        val spaghetti = result.single { it.id == 7 }
        assertEquals(700.0, spaghetti.amount!!, 0.0001)
        assertEquals(setOf(1), spaghetti.sourceRecipeIds)
        assertEquals(existing[1], result.single { it.id == 8 })
    }

    @Test
    fun `different units stay as separate items`() {
        val kgPasta = Recipe(id = 3, title = "Big pasta", servings = 1, ingredients = listOf(Ingredient(1.0, "kg", "spaghetti")))
        val result = addMealsToShoppingList(emptyList(), listOf(meal(1, pasta, 4), meal(2, kgPasta, 1)))
        assertEquals(2, result.count { it.name == "spaghetti" })
        assertTrue(result.any { it.unit == "kg" && it.amount == 1.0 })
    }
}
