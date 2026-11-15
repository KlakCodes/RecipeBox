package com.example.recipebox.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {
    /** Meals from [startDay] to [endDay] inclusive (epoch days), in day order. */
    @Transaction
    @Query("SELECT * FROM planned_meals WHERE date BETWEEN :startDay AND :endDay ORDER BY date ASC, position ASC, id ASC")
    fun getBetween(startDay: Long, endDay: Long): Flow<List<PlannedMealWithRecipe>>

    @Query("SELECT COALESCE(MAX(position) + 1, 0) FROM planned_meals WHERE date = :day")
    suspend fun nextPosition(day: Long): Int

    /** Inserting with an existing id puts a removed meal back exactly where it was. */
    @Insert
    suspend fun insert(meal: PlannedMealEntity): Long

    @Query("UPDATE planned_meals SET servings = :servings WHERE id = :id")
    suspend fun setServings(id: Int, servings: Int)

    @Query("DELETE FROM planned_meals WHERE id = :id")
    suspend fun deleteById(id: Int)
}
