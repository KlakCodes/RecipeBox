package com.example.recipebox.data

import com.example.recipebox.util.addManualItem
import com.example.recipebox.util.ShoppingEdit
import com.example.recipebox.util.addMealsToShoppingList
import com.example.recipebox.util.editShoppingItem
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

    /** Adds every meal at its planned servings, merging like [addFromRecipe]. The result carries what Undo needs. */
    suspend fun addPlannedMeals(meals: List<PlannedMeal>): ShoppingListChange {
        val existing = dao.getAllOnce().map { it.toDomain() }
        val merged = addMealsToShoppingList(existing, meals)
        val toUpsert = merged.filter { it !in existing }
        val ids = dao.upsertItems(toUpsert.map { it.toEntity() })
        return ShoppingListChange(
            mealCount = meals.size,
            previous = existing.filter { old -> toUpsert.any { it.id == old.id } },
            insertedIds = ids.filter { it > 0 }.map { it.toInt() },
        )
    }

    suspend fun undoChange(change: ShoppingListChange) {
        dao.deleteByIds(change.insertedIds)
        dao.upsertItems(change.previous.map { it.toEntity() })
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

    /** Edits item [id], merging it into a matching item if there is one. Null means nothing changed. */
    suspend fun editItem(id: Int, name: String, amountText: String, unit: String): ItemEditResult? {
        val existing = dao.getAllOnce().map { it.toDomain() }
        val previous = existing.firstOrNull { it.id == id } ?: return null
        return when (val edit = editShoppingItem(existing, id, name, amountText, unit)) {
            null -> null
            is ShoppingEdit.Updated -> {
                if (edit.item == previous) return null
                dao.upsertItem(edit.item.toEntity())
                ItemEditResult.Updated(previous)
            }
            is ShoppingEdit.Merged -> {
                val target = existing.first { it.id == edit.into.id }
                dao.upsertItem(edit.into.toEntity())
                dao.deleteById(edit.removedId)
                ItemEditResult.Merged(previous, target)
            }
        }
    }

    suspend fun undoEdit(result: ItemEditResult) {
        when (result) {
            is ItemEditResult.Updated -> dao.upsertItem(result.previous.toEntity())
            is ItemEditResult.Merged -> dao.upsertItems(listOf(result.previous.toEntity(), result.target.toEntity()))
        }
    }

    suspend fun deleteItem(item: ShoppingItem) = dao.deleteById(item.id)

    /** Undo for [deleteItem]: the same id, so it comes back in its old place. */
    suspend fun restoreItem(item: ShoppingItem) {
        dao.upsertItem(item.toEntity())
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
