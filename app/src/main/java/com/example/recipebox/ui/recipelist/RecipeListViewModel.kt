package com.example.recipebox.ui.recipelist

import androidx.lifecycle.ViewModel
import com.example.recipebox.data.Recipe
import com.example.recipebox.data.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RecipeListViewModel : ViewModel() {
    private val _recipes = MutableStateFlow(RecipeRepository.getAll())
    val recipes: StateFlow<List<Recipe>> = _recipes
}
