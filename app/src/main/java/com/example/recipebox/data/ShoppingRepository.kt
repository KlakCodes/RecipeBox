package com.example.recipebox.data

import com.example.recipebox.util.mergeIntoShoppingList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ShoppingRepository(private val dao: ShoppingDao) {
    fun getAll(): Flow<List<ShoppingItem>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    suspend fun addFromRecipe(recipe: Recipe, baseServings: Int, chosenServings: Int) {
        val existing = dao.getAllOnce().map { it.toDomain() }
        val merged = mergeIntoShoppingList(existing, recipe.ingredients, baseServings, chosenServings, recipe.id)
        val toUpsert = merged.filter { mergedItem ->
            val original = existing.find { it.id == mergedItem.id }
            original == null || original != mergedItem
        }
        dao.upsertItems(toUpsert.map { it.toEntity() })
    }

    suspend fun toggleChecked(id: Int, checked: Boolean) = dao.setChecked(id, checked)
    suspend fun clearChecked() = dao.deleteChecked()
    suspend fun clearAll() = dao.deleteAll()
}

private fun ShoppingItemEntity.toDomain() = ShoppingItem(
    id = id, name = name, amount = amount, unit = unit, checked = checked,
    sourceRecipeIds = sourceRecipeIds.split(',').mapNotNull { it.toIntOrNull() }.toSet(),
)

private fun ShoppingItem.toEntity() = ShoppingItemEntity(
    id = id, name = name, amount = amount, unit = unit, checked = checked,
    sourceRecipeIds = sourceRecipeIds.joinToString(","),
)
