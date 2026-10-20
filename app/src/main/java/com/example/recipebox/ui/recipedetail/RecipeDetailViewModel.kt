package com.example.recipebox.ui.recipedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.recipebox.data.Recipe
import com.example.recipebox.data.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RecipeDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val recipeId: Int = checkNotNull(savedStateHandle["recipeId"])
    val recipe: StateFlow<Recipe?> = MutableStateFlow(RecipeRepository.getById(recipeId))
}
