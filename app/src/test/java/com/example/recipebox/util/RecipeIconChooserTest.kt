package com.example.recipebox.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecipeIconChooserTest {

    @Test
    fun `tomato and basil pasta - pasta`() {
        assertEquals(
            RecipeIconCategory.Pasta,
            chooseIcon("Tomato & basil pasta", listOf("spaghetti", "tomatoes", "basil", "olive oil")),
        )
    }

    @Test
    fun `chocolate cake with eggs - cake beats egg ingredient`() {
        assertEquals(
            RecipeIconCategory.Cake,
            chooseIcon("Chocolate cake", listOf("eggs", "flour", "sugar", "butter", "cocoa powder")),
        )
    }

    @Test
    fun `chicken curry - two title matches with equal scores go to the later keyword`() {
        assertEquals(
            RecipeIconCategory.Curry,
            chooseIcon("Chicken curry", listOf("chicken thighs", "curry paste", "onion", "rice")),
        )
    }

    @Test
    fun `mystery dish - fallback plate`() {
        assertEquals(RecipeIconCategory.Plate, chooseIcon("Mystery dish", listOf("love", "secrets")))
    }

    @Test
    fun `empty recipe - fallback plate`() {
        assertEquals(RecipeIconCategory.Plate, chooseIcon("", emptyList()))
    }

    @Test
    fun `whole words only - eggplant is not egg and licorice is not rice`() {
        assertEquals(RecipeIconCategory.Plate, chooseIcon("Eggplant parmigiana", listOf("licorice")))
    }

    @Test
    fun `phrases match across punctuation and case - Stir-Fry`() {
        assertEquals(RecipeIconCategory.StirFry, chooseIcon("Veggie STIR-FRY", emptyList()))
    }

    @Test
    fun `cream alone is not ice cream`() {
        assertEquals(RecipeIconCategory.Plate, chooseIcon("Peaches and cream", listOf("double cream")))
        assertEquals(RecipeIconCategory.Dessert, chooseIcon("Vanilla ice cream", emptyList()))
    }

    @Test
    fun `plurals listed as keywords match`() {
        assertEquals(RecipeIconCategory.Breakfast, chooseIcon("Fluffy pancakes", emptyList()))
        assertEquals(RecipeIconCategory.Cookie, chooseIcon("Oat biscuits", emptyList()))
    }

    @Test
    fun `ingredients alone can choose the icon`() {
        assertEquals(RecipeIconCategory.Fish, chooseIcon("Friday dinner", listOf("salmon fillets", "lemon", "dill")))
    }

    @Test
    fun `tie between title and ingredients goes to the title`() {
        // Meat scores 3 from three ingredients, Soup scores 3 from the title
        assertEquals(
            RecipeIconCategory.Soup,
            chooseIcon("Winter soup", listOf("beef", "bacon", "ham hock")),
        )
    }

    @Test
    fun `more ingredient points can outscore a title match`() {
        // Meat: 4 ingredients = 4 points beats Soup's 3 from the title
        assertEquals(
            RecipeIconCategory.Meat,
            chooseIcon("Winter soup", listOf("beef", "bacon", "ham hock", "pork belly")),
        )
    }

    @Test
    fun `equal ingredient-only scores go to category order`() {
        // Pasta comes before Pizza in the enum
        assertEquals(RecipeIconCategory.Pasta, chooseIcon("Leftovers", listOf("penne", "pizza dough")))
    }

    @Test
    fun `keys are unique and round-trip`() {
        val keys = RecipeIconCategory.entries.map { it.key }
        assertEquals(keys.size, keys.toSet().size)
        RecipeIconCategory.entries.forEach { assertEquals(it, RecipeIconCategory.fromKey(it.key)) }
    }

    @Test
    fun `unknown or null key means auto`() {
        assertNull(RecipeIconCategory.fromKey(null))
        assertNull(RecipeIconCategory.fromKey("spaceship"))
    }
}
