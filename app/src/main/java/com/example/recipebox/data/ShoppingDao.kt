package com.example.recipebox.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingDao {
    @Query("SELECT * FROM shopping_items ORDER BY id ASC")
    fun getAll(): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM shopping_items ORDER BY id ASC")
    suspend fun getAllOnce(): List<ShoppingItemEntity>

    @Upsert
    suspend fun upsertItems(items: List<ShoppingItemEntity>)

    @Query("UPDATE shopping_items SET checked = :checked WHERE id = :id")
    suspend fun setChecked(id: Int, checked: Boolean)

    @Query("DELETE FROM shopping_items WHERE checked = 1")
    suspend fun deleteChecked()

    @Query("DELETE FROM shopping_items")
    suspend fun deleteAll()
}
