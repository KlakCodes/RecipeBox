package com.example.recipebox.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class PlanRepository(private val dao: PlanDao) {
    /** The seven days starting at [weekStart]. */
    fun getWeek(weekStart: LocalDate): Flow<List<PlannedMeal>> =
        dao.getBetween(weekStart.toEpochDay(), weekStart.plusDays(6).toEpochDay())
            .map { list -> list.map { it.toDomain() } }

    /** Adds [recipeId] at the end of [date]'s meals. */
    suspend fun addMeal(recipeId: Int, date: LocalDate, servings: Int) {
        val day = date.toEpochDay()
        dao.insert(
            PlannedMealEntity(recipeId = recipeId, date = day, servings = servings.coerceAtLeast(1), position = dao.nextPosition(day)),
        )
    }

    suspend fun setServings(id: Int, servings: Int) = dao.setServings(id, servings.coerceAtLeast(1))

    suspend fun removeMeal(id: Int) = dao.deleteById(id)

    /** Undo for [removeMeal]: same id and position, so it reappears in place. */
    suspend fun restoreMeal(meal: PlannedMeal) {
        dao.insert(
            PlannedMealEntity(
                id = meal.id,
                recipeId = meal.recipe.id,
                date = meal.date.toEpochDay(),
                servings = meal.servings,
                position = meal.position,
            ),
        )
    }
}

private fun PlannedMealWithRecipe.toDomain() = PlannedMeal(
    id = meal.id,
    date = LocalDate.ofEpochDay(meal.date),
    servings = meal.servings,
    position = meal.position,
    recipe = recipe.toDomain(),
)
