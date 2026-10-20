package com.example.recipebox.ui.recipelist

import androidx.lifecycle.ViewModel
import com.example.recipebox.data.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RecipeListViewModel : ViewModel() {
    private val _recipes = MutableStateFlow(sampleRecipes)
    val recipes: StateFlow<List<Recipe>> = _recipes
}

private val sampleRecipes = listOf(
    Recipe(1, "Spaghetti Carbonara", 4),
    Recipe(2, "Chicken Tikka Masala", 6),
    Recipe(3, "Banana Bread", 8),
    Recipe(4, "Greek Salad", 2),
    Recipe(5, "Beef Tacos", 4),
)
