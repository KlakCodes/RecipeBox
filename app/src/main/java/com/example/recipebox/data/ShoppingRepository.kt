package com.example.recipebox.data

import com.example.recipebox.util.addManualItem
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

    /** Adds a hand-typed item, merging it like a recipe ingredient. The result carries what Undo needs. */
    suspend fun addManual(name: String, amountText: String, unit: String): ManualAddResult {
        val existing = dao.getAllOnce().map { it.toDomain() }
        val updated = addManualItem(existing, name, amountText, unit)
        val inserted = updated.firstOrNull { it.id == 0 }
        if (inserted != null) {
            val id = dao.upsertItem(inserted.toEntity()).toInt()
            return ManualAddResult.Inserted(id, inserted.name)
        }
        val changed = updated.firstOrNull { it !in existing } ?: return ManualAddResult.AlreadyListed(name.trim())
        dao.upsertItem(changed.toEntity())
        return ManualAddResult.Merged(previous = existing.first { it.id == changed.id })
    }

    suspend fun undoManualAdd(result: ManualAddResult) {
        when (result) {
            is ManualAddResult.Inserted -> dao.deleteById(result.id)
            is ManualAddResult.Merged -> dao.upsertItem(result.previous.toEntity())
            is ManualAddResult.AlreadyListed -> Unit
        }
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
