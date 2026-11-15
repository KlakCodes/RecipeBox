package com.example.recipebox

import com.example.recipebox.data.ItemEditResult
import com.example.recipebox.data.ShoppingDao
import com.example.recipebox.data.ShoppingItem
import com.example.recipebox.data.ShoppingItemEntity
import com.example.recipebox.data.ShoppingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Edit, merge, delete and their Undos against an in-memory DAO, checking the rows that end up stored. */
class ShoppingRepositoryTest {

    private class FakeShoppingDao(initial: List<ShoppingItemEntity>) : ShoppingDao {
        val rows = MutableStateFlow(initial.sortedBy { it.id })
        private var nextId = (initial.maxOfOrNull { it.id } ?: 0) + 1

        override fun getAll(): Flow<List<ShoppingItemEntity>> = rows
        override suspend fun getAllOnce() = rows.value

        override suspend fun upsertItems(items: List<ShoppingItemEntity>) = items.map { upsertItem(it) }

        override suspend fun upsertItem(item: ShoppingItemEntity): Long {
            if (item.id != 0 && rows.value.any { it.id == item.id }) {
                rows.value = rows.value.map { if (it.id == item.id) item else it }
                return -1
            }
            val stored = if (item.id == 0) item.copy(id = nextId++) else item
            rows.value = (rows.value + stored).sortedBy { it.id }
            return stored.id.toLong()
        }

        override suspend fun deleteById(id: Int) {
            rows.value = rows.value.filter { it.id != id }
        }

        override suspend fun deleteByIds(ids: List<Int>) {
            rows.value = rows.value.filter { it.id !in ids }
        }

        override suspend fun setChecked(id: Int, checked: Boolean) {
            rows.value = rows.value.map { if (it.id == id) it.copy(checked = checked) else it }
        }

        override suspend fun deleteChecked() {
            rows.value = rows.value.filter { !it.checked }
        }

        override suspend fun deleteAll() {
            rows.value = emptyList()
        }
    }

    private val start = listOf(
        ShoppingItemEntity(id = 1, name = "milk", amount = 1.0, unit = "l", checked = true, sourceRecipeIds = "3,4"),
        ShoppingItemEntity(id = 2, name = "Flour", amount = 200.0, unit = "g", checked = false, sourceRecipeIds = "5"),
        ShoppingItemEntity(id = 3, name = "plain flour", amount = 100.0, unit = "g", checked = false, sourceRecipeIds = "6"),
    )

    private fun items(repository: ShoppingRepository): List<ShoppingItem> = runBlocking { repository.getAll().first() }

    @Test
    fun `edit updates the row in place and Undo restores it`() = runBlocking {
        val dao = FakeShoppingDao(start)
        val repository = ShoppingRepository(dao)

        val result = repository.editItem(id = 1, name = "oat milk", amountText = "2", unit = "l")

        assertTrue(result is ItemEditResult.Updated)
        val edited = items(repository).single { it.id == 1 }
        assertEquals("oat milk", edited.name)
        assertEquals(2.0, edited.amount!!, 0.001)
        assertTrue(edited.checked)
        assertEquals(setOf(3, 4), edited.sourceRecipeIds)

        repository.undoEdit(result!!)
        assertEquals(start, dao.rows.value)
    }

    @Test
    fun `edit with nothing changed or a blank name returns null and stores nothing`() = runBlocking {
        val dao = FakeShoppingDao(start)
        val repository = ShoppingRepository(dao)

        assertNull(repository.editItem(id = 2, name = "Flour", amountText = "200", unit = "g"))
        assertNull(repository.editItem(id = 2, name = "  ", amountText = "200", unit = "g"))
        assertEquals(start, dao.rows.value)
    }

    @Test
    fun `edit into merge removes the edited row, updates the other, and Undo restores both`() = runBlocking {
        val dao = FakeShoppingDao(start)
        val repository = ShoppingRepository(dao)

        val result = repository.editItem(id = 3, name = "flour", amountText = "100", unit = "g")

        assertTrue(result is ItemEditResult.Merged)
        assertEquals("Flour", (result as ItemEditResult.Merged).target.name)
        val after = items(repository)
        assertEquals(listOf(1, 2), after.map { it.id })
        val flour = after.single { it.id == 2 }
        assertEquals(300.0, flour.amount!!, 0.001)
        assertEquals(setOf(5, 6), flour.sourceRecipeIds)

        repository.undoEdit(result)
        assertEquals(start, dao.rows.value)
    }

    @Test
    fun `delete removes the row and restore brings it back with the same id, tick and sources`() = runBlocking {
        val dao = FakeShoppingDao(start)
        val repository = ShoppingRepository(dao)
        val milk = items(repository).single { it.id == 1 }

        repository.deleteItem(milk)
        assertEquals(listOf(2, 3), dao.rows.value.map { it.id })

        repository.restoreItem(milk)
        assertEquals(start, dao.rows.value)
    }
}
