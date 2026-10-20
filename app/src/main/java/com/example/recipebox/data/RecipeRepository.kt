package com.example.recipebox.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecipeRepository(private val dao: RecipeDao) {
    fun getAll(): Flow<List<Recipe>> =
        dao.getAllWithDetails().map { list -> list.map { it.toDomain() } }

    fun getById(id: Int): Flow<Recipe?> =
        dao.getWithDetails(id).map { it?.toDomain() }

    suspend fun saveRecipe(
        title: String,
        servings: Int,
        ingredients: List<Ingredient>,
        steps: List<Step>,
        recipeId: Int = -1,
    ) {
        if (recipeId == -1) {
            val id = dao.insertRecipe(RecipeEntity(title = title, servings = servings)).toInt()
            dao.insertIngredients(ingredients.mapIndexed { i, it ->
                IngredientEntity(recipeId = id, amount = it.amount, unit = it.unit, name = it.name, position = i)
            })
            dao.insertSteps(steps.mapIndexed { i, it ->
                StepEntity(recipeId = id, text = it.text, position = i + 1)
            })
        } else {
            val existing = dao.getRecipeEntityById(recipeId) ?: return
            dao.updateRecipe(existing.copy(title = title, servings = servings))
            dao.deleteIngredientsByRecipeId(recipeId)
            dao.deleteStepsByRecipeId(recipeId)
            dao.insertIngredients(ingredients.mapIndexed { i, it ->
                IngredientEntity(recipeId = recipeId, amount = it.amount, unit = it.unit, name = it.name, position = i)
            })
            dao.insertSteps(steps.mapIndexed { i, it ->
                StepEntity(recipeId = recipeId, text = it.text, position = i + 1)
            })
        }
    }

    suspend fun deleteRecipe(id: Int) {
        dao.deleteRecipeById(id)
    }
}

private fun RecipeWithDetails.toDomain(): Recipe = Recipe(
    id = recipe.id,
    title = recipe.title,
    servings = recipe.servings,
    ingredients = ingredients.sortedBy { it.position }.map { Ingredient(it.amount, it.unit, it.name) },
    steps = steps.sortedBy { it.position }.map { Step(it.position, it.text) },
)
