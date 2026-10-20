package com.example.recipebox.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Transaction
    @Query("SELECT * FROM recipes ORDER BY createdAt DESC")
    fun getAllWithDetails(): Flow<List<RecipeWithDetails>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    fun getWithDetails(id: Int): Flow<RecipeWithDetails?>

    @Insert
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Insert
    suspend fun insertIngredients(ingredients: List<IngredientEntity>)

    @Insert
    suspend fun insertSteps(steps: List<StepEntity>)

    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun count(): Int
}
