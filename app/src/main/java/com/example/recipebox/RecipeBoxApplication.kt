package com.example.recipebox

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.recipebox.data.IngredientEntity
import com.example.recipebox.data.RecipeBoxDatabase
import com.example.recipebox.data.RecipeDao
import com.example.recipebox.data.RecipeEntity
import com.example.recipebox.data.RecipeRepository
import com.example.recipebox.data.SettingsRepository
import com.example.recipebox.data.ShoppingRepository
import com.example.recipebox.data.StepEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class RecipeBoxApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { RecipeBoxDatabase.getInstance(this) }
    val repository by lazy { RecipeRepository(database.recipeDao()) }
    val shoppingRepository by lazy { ShoppingRepository(database.shoppingDao()) }
    val settingsRepository by lazy { SettingsRepository(settingsDataStore) }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            if (database.recipeDao().count() == 0) seedSampleRecipes(database.recipeDao())
        }
    }
}

private suspend fun seedSampleRecipes(dao: RecipeDao) {
    data class IngredientSeed(val amount: Double?, val unit: String, val name: String)
    data class RecipeSeed(
        val title: String,
        val servings: Int,
        val ingredients: List<IngredientSeed>,
        val steps: List<String>,
    )

    val samples = listOf(
        RecipeSeed(
            title = "Spaghetti Carbonara",
            servings = 4,
            ingredients = listOf(
                IngredientSeed(400.0, "g", "spaghetti"),
                IngredientSeed(200.0, "g", "pancetta, diced"),
                IngredientSeed(4.0, "", "eggs"),
                IngredientSeed(100.0, "g", "Parmesan, grated"),
                IngredientSeed(2.0, "", "garlic cloves"),
                IngredientSeed(null, "", "salt to taste"),
                IngredientSeed(null, "", "black pepper to taste"),
            ),
            steps = listOf(
                "Bring a large pot of salted water to the boil and cook the spaghetti until al dente.",
                "Fry the pancetta and garlic in a dry pan over medium heat until the pancetta is crisp.",
                "Beat the eggs with the Parmesan and a generous pinch of black pepper.",
                "Drain the pasta, reserving a cup of cooking water, then remove the pan from the heat.",
                "Add the pasta to the pan, pour over the egg mixture, and toss quickly, adding a splash of pasta water to loosen.",
            ),
        ),
        RecipeSeed(
            title = "Chicken Tikka Masala",
            servings = 6,
            ingredients = listOf(
                IngredientSeed(800.0, "g", "chicken thighs, boneless and diced"),
                IngredientSeed(400.0, "g", "tinned tomatoes"),
                IngredientSeed(200.0, "ml", "double cream"),
                IngredientSeed(2.0, "", "onions, sliced"),
                IngredientSeed(4.0, "", "garlic cloves, minced"),
                IngredientSeed(3.0, "tbsp", "tikka masala paste"),
            ),
            steps = listOf(
                "Fry the onions in oil over medium heat for 10 minutes until golden.",
                "Add the garlic and tikka masala paste and cook for 2 minutes.",
                "Add the chicken and cook until sealed on all sides.",
                "Stir in the tomatoes and simmer for 20 minutes.",
                "Stir in the cream and heat through for 5 minutes. Season to taste.",
            ),
        ),
        RecipeSeed(
            title = "Banana Bread",
            servings = 8,
            ingredients = listOf(
                IngredientSeed(3.0, "", "ripe bananas, mashed"),
                IngredientSeed(200.0, "g", "plain flour"),
                IngredientSeed(100.0, "g", "butter, melted"),
                IngredientSeed(150.0, "g", "caster sugar"),
                IngredientSeed(2.0, "", "eggs"),
                IngredientSeed(1.0, "tsp", "baking soda"),
            ),
            steps = listOf(
                "Preheat oven to 175°C and grease a 9×5 inch loaf tin.",
                "Mash the bananas in a large bowl and mix in the melted butter.",
                "Beat in the sugar, eggs, and baking soda.",
                "Fold in the flour until just combined — do not overmix.",
                "Pour into the tin and bake for 55–65 minutes until a skewer comes out clean.",
            ),
        ),
        RecipeSeed(
            title = "Greek Salad",
            servings = 2,
            ingredients = listOf(
                IngredientSeed(2.0, "", "ripe tomatoes, cut into wedges"),
                IngredientSeed(0.5, "", "cucumber, thickly sliced"),
                IngredientSeed(100.0, "g", "feta, cut into chunks"),
                IngredientSeed(50.0, "g", "Kalamata olives"),
                IngredientSeed(2.0, "tbsp", "olive oil"),
                IngredientSeed(1.0, "tsp", "dried oregano"),
            ),
            steps = listOf(
                "Arrange the tomatoes, cucumber, and olives in a serving bowl.",
                "Add the feta on top.",
                "Drizzle with olive oil and sprinkle with oregano. Serve immediately.",
            ),
        ),
        RecipeSeed(
            title = "Beef Tacos",
            servings = 4,
            ingredients = listOf(
                IngredientSeed(500.0, "g", "ground beef"),
                IngredientSeed(8.0, "", "taco shells"),
                IngredientSeed(1.0, "", "onion, finely diced"),
                IngredientSeed(2.0, "tbsp", "taco seasoning"),
                IngredientSeed(100.0, "g", "cheddar, grated"),
                IngredientSeed(null, "", "salsa, to serve"),
                IngredientSeed(null, "", "sour cream, to serve"),
            ),
            steps = listOf(
                "Fry the onion in a pan over medium heat for 5 minutes until softened.",
                "Add the ground beef and cook, breaking it up, until browned all over.",
                "Stir in the taco seasoning with 3 tbsp of water and simmer for 3 minutes.",
                "Warm the taco shells according to packet instructions.",
                "Fill each shell with the beef mixture and top with cheese, salsa, and sour cream.",
            ),
        ),
    )

    samples.forEach { seed ->
        val recipeId = dao.insertRecipe(RecipeEntity(title = seed.title, servings = seed.servings)).toInt()
        dao.insertIngredients(
            seed.ingredients.mapIndexed { index, ing ->
                IngredientEntity(recipeId = recipeId, amount = ing.amount, unit = ing.unit, name = ing.name, position = index)
            },
        )
        dao.insertSteps(
            seed.steps.mapIndexed { index, text ->
                StepEntity(recipeId = recipeId, text = text, position = index + 1)
            },
        )
    }
}
