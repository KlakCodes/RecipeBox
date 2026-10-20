package com.example.recipebox.ui.recipedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.recipebox.data.Recipe
import com.example.recipebox.data.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class RecipeDetailViewModel(
    savedStateHandle: SavedStateHandle,
    repository: RecipeRepository,
) : ViewModel() {
    private val recipeId: Int = checkNotNull(savedStateHandle["recipeId"])

    val recipe: StateFlow<Recipe?> = repository.getById(recipeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // null means "not yet overridden — use recipe's base servings"
    private val _servingsOverride = MutableStateFlow<Int?>(null)

    val chosenServings: StateFlow<Int> = combine(recipe, _servingsOverride) { r, override ->
        override ?: r?.servings ?: 1
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 1)

    fun incrementServings() {
        _servingsOverride.value = chosenServings.value + 1
    }

    fun decrementServings() {
        val current = chosenServings.value
        if (current > 1) _servingsOverride.value = current - 1
    }

    companion object {
        fun factory(repository: RecipeRepository) = viewModelFactory {
            initializer { RecipeDetailViewModel(createSavedStateHandle(), repository) }
        }
    }
}
