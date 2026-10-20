package com.example.recipebox.ui.recipelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.recipebox.data.Recipe
import com.example.recipebox.data.RecipeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class RecipeListViewModel(private val repository: RecipeRepository) : ViewModel() {
    val recipes: StateFlow<List<Recipe>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    companion object {
        fun factory(repository: RecipeRepository) = viewModelFactory {
            initializer { RecipeListViewModel(repository) }
        }
    }
}
