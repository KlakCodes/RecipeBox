package com.example.recipebox.ui.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.recipebox.data.ShoppingItem
import com.example.recipebox.data.ShoppingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShoppingListViewModel(private val repository: ShoppingRepository) : ViewModel() {
    val items: StateFlow<List<ShoppingItem>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Items still to buy, for the nav bar badge. */
    val uncheckedCount: StateFlow<Int> = repository.getAll()
        .map { list -> list.count { !it.checked } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun toggleChecked(id: Int, checked: Boolean) {
        viewModelScope.launch { repository.toggleChecked(id, checked) }
    }

    fun clearChecked() {
        viewModelScope.launch { repository.clearChecked() }
    }

    fun clearAll() {
        viewModelScope.launch { repository.clearAll() }
    }

    companion object {
        fun factory(repository: ShoppingRepository) = viewModelFactory {
            initializer { ShoppingListViewModel(repository) }
        }
    }
}
