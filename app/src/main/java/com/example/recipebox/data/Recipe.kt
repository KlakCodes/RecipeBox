package com.example.recipebox.data

import com.example.recipebox.util.RecipeIconCategory
import com.example.recipebox.util.chooseIcon

data class Ingredient(val amount: Double?, val unit: String, val name: String)

data class Step(val position: Int, val text: String)

data class Recipe(
    val id: Int,
    val title: String,
    val servings: Int,
    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<Step> = emptyList(),
    /** RecipeIconCategory key chosen by hand, or null for Auto. */
    val icon: String? = null,
)

/** The chosen icon if there is one (and it still exists), otherwise the automatic pick. */
fun Recipe.displayIcon(): RecipeIconCategory =
    RecipeIconCategory.fromKey(icon) ?: chooseIcon(title, ingredients.map { it.name })
