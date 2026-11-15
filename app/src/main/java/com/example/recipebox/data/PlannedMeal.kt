package com.example.recipebox.data

import java.time.LocalDate

/** A recipe planned for a day, at its own servings (independent of the recipe's base servings). */
data class PlannedMeal(
    val id: Int,
    val date: LocalDate,
    val servings: Int,
    val position: Int,
    val recipe: Recipe,
)
