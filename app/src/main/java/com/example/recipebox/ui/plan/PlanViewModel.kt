package com.example.recipebox.ui.plan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.recipebox.data.PlanRepository
import com.example.recipebox.data.PlannedMeal
import com.example.recipebox.data.Recipe
import com.example.recipebox.data.RecipeRepository
import com.example.recipebox.data.ShoppingListChange
import com.example.recipebox.data.ShoppingRepository
import com.example.recipebox.util.startOfWeek
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Something the Plan screen shows a snackbar for. */
sealed interface PlanMessage {
    data class MealRemoved(val meal: PlannedMeal) : PlanMessage
    data class AddedToShoppingList(val change: ShoppingListChange) : PlanMessage
}

@OptIn(ExperimentalCoroutinesApi::class)
class PlanViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val planRepository: PlanRepository,
    recipeRepository: RecipeRepository,
    private val shoppingRepository: ShoppingRepository,
) : ViewModel() {
    // Saved as an epoch day so the viewed week survives rotation and process death
    private val weekStartDay: StateFlow<Long> =
        savedStateHandle.getStateFlow(WeekStartKey, startOfWeek(LocalDate.now()).toEpochDay())

    val weekStart: StateFlow<LocalDate> = weekStartDay
        .map { LocalDate.ofEpochDay(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, LocalDate.ofEpochDay(weekStartDay.value))

    /** The viewed week's meals in day order; null until the first load, so the empty state doesn't flash. */
    val meals: StateFlow<List<PlannedMeal>?> = weekStartDay
        .flatMapLatest { planRepository.getWeek(LocalDate.ofEpochDay(it)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val recipes: StateFlow<List<Recipe>> = recipeRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // One-off events rather than state, so clearing one can't cancel the snackbar showing it
    private val _messages = Channel<PlanMessage>(Channel.BUFFERED)
    val messages: Flow<PlanMessage> = _messages.receiveAsFlow()

    fun previousWeek() = showWeek(weekStart.value.minusWeeks(1))
    fun nextWeek() = showWeek(weekStart.value.plusWeeks(1))
    fun thisWeek(today: LocalDate) = showWeek(startOfWeek(today))

    private fun showWeek(start: LocalDate) {
        savedStateHandle[WeekStartKey] = start.toEpochDay()
    }

    /** Adds [recipe] to [date] at its base servings. */
    fun addMeal(recipe: Recipe, date: LocalDate) {
        viewModelScope.launch { planRepository.addMeal(recipe.id, date, recipe.servings) }
    }

    fun setServings(meal: PlannedMeal, servings: Int) {
        if (servings < 1) return
        viewModelScope.launch { planRepository.setServings(meal.id, servings) }
    }

    fun removeMeal(meal: PlannedMeal) {
        viewModelScope.launch {
            planRepository.removeMeal(meal.id)
            _messages.send(PlanMessage.MealRemoved(meal))
        }
    }

    fun restoreMeal(meal: PlannedMeal) {
        viewModelScope.launch { planRepository.restoreMeal(meal) }
    }

    fun addWeekToShoppingList() {
        val weekMeals = meals.value.orEmpty()
        if (weekMeals.isEmpty()) return
        viewModelScope.launch {
            _messages.send(PlanMessage.AddedToShoppingList(shoppingRepository.addPlannedMeals(weekMeals)))
        }
    }

    fun undoShoppingAdd(change: ShoppingListChange) {
        viewModelScope.launch { shoppingRepository.undoChange(change) }
    }

    companion object {
        private const val WeekStartKey = "weekStart"

        fun factory(
            planRepository: PlanRepository,
            recipeRepository: RecipeRepository,
            shoppingRepository: ShoppingRepository,
        ) = viewModelFactory {
            initializer {
                PlanViewModel(createSavedStateHandle(), planRepository, recipeRepository, shoppingRepository)
            }
        }
    }
}
