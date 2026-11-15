package com.example.recipebox.ui.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.recipebox.data.ItemEditResult
import com.example.recipebox.data.ManualAddResult
import com.example.recipebox.data.ShoppingItem
import com.example.recipebox.data.ShoppingRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Something the Shopping list shows a snackbar with Undo for. */
sealed interface ShoppingMessage {
    data class Deleted(val item: ShoppingItem) : ShoppingMessage
    data class Merged(val result: ItemEditResult.Merged) : ShoppingMessage
}

class ShoppingListViewModel(private val repository: ShoppingRepository) : ViewModel() {
    val items: StateFlow<List<ShoppingItem>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Items still to buy, for the nav bar badge. */
    val uncheckedCount: StateFlow<Int> = repository.getAll()
        .map { list -> list.count { !it.checked } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _lastManualAdd = MutableStateFlow<ManualAddResult?>(null)
    /** Set after adding an item by hand, so the screen can show its snackbar. */
    val lastManualAdd: StateFlow<ManualAddResult?> = _lastManualAdd

    fun addItem(name: String, amountText: String, unit: String) {
        if (name.isBlank()) return
        viewModelScope.launch { _lastManualAdd.value = repository.addManual(name, amountText, unit) }
    }

    fun undoAdd(result: ManualAddResult) {
        viewModelScope.launch { repository.undoManualAdd(result) }
    }

    fun resetLastManualAdd() {
        _lastManualAdd.value = null
    }

    // One-off events rather than state, so clearing one can't cancel the snackbar showing it
    private val _messages = Channel<ShoppingMessage>(Channel.BUFFERED)
    val messages: Flow<ShoppingMessage> = _messages.receiveAsFlow()

    fun editItem(id: Int, name: String, amountText: String, unit: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val result = repository.editItem(id, name, amountText, unit)
            if (result is ItemEditResult.Merged) _messages.send(ShoppingMessage.Merged(result))
        }
    }

    fun undoEdit(result: ItemEditResult) {
        viewModelScope.launch { repository.undoEdit(result) }
    }

    fun deleteItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
            _messages.send(ShoppingMessage.Deleted(item))
        }
    }

    fun restoreItem(item: ShoppingItem) {
        viewModelScope.launch { repository.restoreItem(item) }
    }

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
