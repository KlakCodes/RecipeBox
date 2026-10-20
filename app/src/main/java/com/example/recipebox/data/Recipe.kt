package com.example.recipebox.data

data class Ingredient(val amount: Double?, val unit: String, val name: String)

data class Step(val position: Int, val text: String)

data class Recipe(
    val id: Int,
    val title: String,
    val servings: Int,
    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<Step> = emptyList(),
)
