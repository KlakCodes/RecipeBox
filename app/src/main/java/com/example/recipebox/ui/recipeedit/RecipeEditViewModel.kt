package com.example.recipebox.ui.recipeedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.recipebox.data.Ingredient
import com.example.recipebox.data.RecipeRepository
import com.example.recipebox.data.Step
import com.example.recipebox.util.formatEditableAmount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

data class IngredientDraft(
    val id: String = UUID.randomUUID().toString(),
    val amount: String = "",
    val unit: String = "",
    val name: String = "",
)

data class StepDraft(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
)

sealed class SaveState {
    object Idle : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
    object Deleted : SaveState()
}

class RecipeEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: RecipeRepository,
) : ViewModel() {

    val recipeId: Int = savedStateHandle["recipeId"] ?: -1
    val isEditing: Boolean = recipeId != -1

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val _servings = MutableStateFlow(1)
    val servings: StateFlow<Int> = _servings

    private val _ingredients = MutableStateFlow(listOf(IngredientDraft()))
    val ingredients: StateFlow<List<IngredientDraft>> = _ingredients

    private val _steps = MutableStateFlow(listOf(StepDraft()))
    val steps: StateFlow<List<StepDraft>> = _steps

    // RecipeIconCategory key, or null for Auto
    private val _icon = MutableStateFlow<String?>(null)
    val icon: StateFlow<String?> = _icon

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog

    init {
        if (isEditing) {
            viewModelScope.launch {
                repository.getById(recipeId).first()?.let { recipe ->
                    _title.value = recipe.title
                    _servings.value = recipe.servings
                    _icon.value = recipe.icon
                    _ingredients.value = recipe.ingredients.map { ing ->
                        IngredientDraft(amount = ing.amount?.let(::formatEditableAmount) ?: "", unit = ing.unit, name = ing.name)
                    }.ifEmpty { listOf(IngredientDraft()) }
                    _steps.value = recipe.steps.map { step ->
                        StepDraft(text = step.text)
                    }.ifEmpty { listOf(StepDraft()) }
                }
            }
        }
    }

    fun updateTitle(value: String) { _title.value = value }

    fun updateIcon(key: String?) { _icon.value = key }

    fun incrementServings() { _servings.value++ }
    fun decrementServings() { if (_servings.value > 1) _servings.value-- }

    fun updateIngredient(index: Int, draft: IngredientDraft) {
        _ingredients.value = _ingredients.value.toMutableList().also { it[index] = draft }
    }

    fun addIngredient() {
        _ingredients.value = _ingredients.value + IngredientDraft()
    }

    fun removeIngredient(index: Int) {
        if (_ingredients.value.size > 1) {
            _ingredients.value = _ingredients.value.toMutableList().also { it.removeAt(index) }
        }
    }

    fun updateStep(index: Int, draft: StepDraft) {
        _steps.value = _steps.value.toMutableList().also { it[index] = draft }
    }

    fun addStep() {
        _steps.value = _steps.value + StepDraft()
    }

    fun removeStep(index: Int) {
        if (_steps.value.size > 1) {
            _steps.value = _steps.value.toMutableList().also { it.removeAt(index) }
        }
    }

    fun save() {
        val title = _title.value.trim()
        if (title.isBlank()) {
            _saveState.value = SaveState.Error("Title is required")
            return
        }
        val ingredients = _ingredients.value.filter { it.name.isNotBlank() }
        if (ingredients.isEmpty()) {
            _saveState.value = SaveState.Error("Add at least one ingredient")
            return
        }
        val steps = _steps.value.filter { it.text.isNotBlank() }
        viewModelScope.launch {
            repository.saveRecipe(
                title = title,
                servings = _servings.value,
                ingredients = ingredients.map { Ingredient(it.amount.toDoubleOrNull(), it.unit, it.name) },
                steps = steps.mapIndexed { i, s -> Step(i + 1, s.text) },
                icon = _icon.value,
                recipeId = recipeId,
            )
            _saveState.value = SaveState.Success
        }
    }

    fun resetSaveState() { _saveState.value = SaveState.Idle }

    fun confirmDelete() { _showDeleteDialog.value = true }
    fun dismissDelete() { _showDeleteDialog.value = false }

    fun delete() {
        viewModelScope.launch {
            repository.deleteRecipe(recipeId)
            _saveState.value = SaveState.Deleted
        }
    }

    companion object {
        fun factory(repository: RecipeRepository) = viewModelFactory {
            initializer { RecipeEditViewModel(createSavedStateHandle(), repository) }
        }
    }
}
