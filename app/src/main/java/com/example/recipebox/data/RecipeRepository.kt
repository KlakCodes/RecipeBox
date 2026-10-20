package com.example.recipebox.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecipeRepository(private val dao: RecipeDao) {
    fun getAll(): Flow<List<Recipe>> =
        dao.getAllWithDetails().map { list -> list.map { it.toDomain() } }

    fun getById(id: Int): Flow<Recipe?> =
        dao.getWithDetails(id).map { it?.toDomain() }
}

private fun RecipeWithDetails.toDomain(): Recipe = Recipe(
    id = recipe.id,
    title = recipe.title,
    servings = recipe.servings,
    ingredients = ingredients.sortedBy { it.position }.map { Ingredient(it.amount, it.unit, it.name) },
    steps = steps.sortedBy { it.position }.map { Step(it.position, it.text) },
)
