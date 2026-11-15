package com.example.recipebox.ui.plan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recipebox.RecipeBoxApplication
import com.example.recipebox.data.PlannedMeal
import com.example.recipebox.data.Recipe
import com.example.recipebox.ui.components.EmptyState
import com.example.recipebox.ui.components.TonalIconButton
import com.example.recipebox.ui.components.recipeIcon
import com.example.recipebox.ui.theme.RecipeBoxShapes
import com.example.recipebox.ui.theme.RecipeBoxTheme
import com.example.recipebox.ui.theme.thumbnailShape
import com.example.recipebox.util.formatWeekRange
import com.example.recipebox.util.plural
import com.example.recipebox.util.startOfWeek
import com.example.recipebox.util.weekDays
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun PlanScreen(
    onRecipeClick: (Int) -> Unit,
    scrollToTopRequests: Flow<Unit> = emptyFlow(),
    viewModel: PlanViewModel = viewModel(
        factory = (LocalContext.current.applicationContext as RecipeBoxApplication).let { app ->
            PlanViewModel.factory(app.planRepository, app.repository, app.shoppingRepository)
        },
    ),
) {
    val weekStart by viewModel.weekStart.collectAsState()
    val meals by viewModel.meals.collectAsState()
    val recipes by viewModel.recipes.collectAsState()
    val today = rememberToday()
    // Epoch day of the card whose "Add meal" sheet is open
    var addingToDay by rememberSaveable { mutableStateOf<Long?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    LaunchedEffect(scrollToTopRequests) {
        scrollToTopRequests.collect { listState.animateScrollToItem(0) }
    }

    // collectLatest: a newer message replaces the snackbar showing instead of queueing behind it
    LaunchedEffect(viewModel) {
        viewModel.messages.collectLatest { m ->
            when (m) {
                is PlanMessage.MealRemoved -> {
                    val result = snackbarHostState.showSnackbar(
                        "Removed ${m.meal.recipe.title}",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.restoreMeal(m.meal)
                }
                is PlanMessage.AddedToShoppingList -> {
                    val result = snackbarHostState.showSnackbar(
                        "Added ${plural(m.change.mealCount, "meal")}",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.undoShoppingAdd(m.change)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PlanContent(
            weekStart = weekStart,
            today = today,
            meals = meals,
            listState = listState,
            onPreviousWeek = viewModel::previousWeek,
            onNextWeek = viewModel::nextWeek,
            onThisWeek = { viewModel.thisWeek(today) },
            onAddMeal = { day -> addingToDay = day.toEpochDay() },
            onRecipeClick = onRecipeClick,
            onServingsChange = viewModel::setServings,
            onRemove = viewModel::removeMeal,
            onAddToShoppingList = viewModel::addWeekToShoppingList,
        )

        // Sits above the pill button so it never covers it
        SnackbarHost(
            snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp),
        )
    }

    addingToDay?.let { epochDay ->
        val day = LocalDate.ofEpochDay(epochDay)
        AddMealSheet(
            title = "Add to ${day.format(DayNameFormat)}",
            recipes = recipes,
            onPick = { recipe -> viewModel.addMeal(recipe, day) },
            onDismiss = { addingToDay = null },
        )
    }
}

private val DayNameFormat = DateTimeFormatter.ofPattern("EEEE")
private val DayMonthFormat = DateTimeFormatter.ofPattern("d MMM")

/** Today's date, re-read on every resume so the highlight moves on after midnight. */
@Composable
private fun rememberToday(): LocalDate {
    var today by remember { mutableStateOf(LocalDate.now()) }
    LifecycleResumeEffect(Unit) {
        today = LocalDate.now()
        onPauseOrDispose { }
    }
    return today
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PlanContent(
    weekStart: LocalDate,
    today: LocalDate,
    meals: List<PlannedMeal>?,
    listState: LazyListState,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onThisWeek: () -> Unit,
    onAddMeal: (LocalDate) -> Unit,
    onRecipeClick: (Int) -> Unit,
    onServingsChange: (PlannedMeal, Int) -> Unit,
    onRemove: (PlannedMeal) -> Unit,
    onAddToShoppingList: () -> Unit,
) {
    val mealsByDay = remember(meals) { meals.orEmpty().groupBy { it.date } }
    val mealCount = meals?.size ?: 0

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            // Bottom padding clears the floating pill button (20 + 68 + 20), matching the other tabs
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "header") {
                Header(
                    weekStart = weekStart,
                    isThisWeek = weekStart == startOfWeek(today),
                    onPreviousWeek = onPreviousWeek,
                    onNextWeek = onNextWeek,
                    onThisWeek = onThisWeek,
                )
            }
            if (meals != null && meals.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        icon = {
                            Icon(
                                Icons.Outlined.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                        },
                        polygon = MaterialShapes.Sunny,
                        title = "Nothing planned yet",
                        hint = "Add a meal to any day and it'll show up here",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .animateItem(),
                    )
                }
            }
            items(weekDays(weekStart), key = { it.toEpochDay() }) { day ->
                DayCard(
                    day = day,
                    isToday = day == today,
                    meals = mealsByDay[day].orEmpty(),
                    onAddMeal = { onAddMeal(day) },
                    onRecipeClick = onRecipeClick,
                    onServingsChange = onServingsChange,
                    onRemove = onRemove,
                    modifier = Modifier.animateItem(),
                )
            }
        }

        AnimatedVisibility(
            visible = mealCount > 0,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Button(
                onClick = onAddToShoppingList,
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
                    .fillMaxWidth()
                    .height(68.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp),
            ) {
                Icon(Icons.Outlined.ShoppingCart, contentDescription = null)
                Spacer(Modifier.size(12.dp))
                Text(
                    "Add ${plural(mealCount, "meal")} to shopping list",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun Header(
    weekStart: LocalDate,
    isThisWeek: Boolean,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onThisWeek: () -> Unit,
) {
    Column {
        Text("Plan", style = MaterialTheme.typography.displayMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                formatWeekRange(weekStart),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!isThisWeek) {
                AssistChip(
                    onClick = onThisWeek,
                    label = { Text("This week") },
                    leadingIcon = { Icon(Icons.Outlined.Today, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    shape = CircleShape,
                )
            }
            Spacer(Modifier.weight(1f))
            TonalIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Previous week", onPreviousWeek)
            TonalIconButton(Icons.AutoMirrored.Filled.ArrowForward, "Next week", onNextWeek)
        }
    }
}

@Composable
private fun DayCard(
    day: LocalDate,
    isToday: Boolean,
    meals: List<PlannedMeal>,
    onAddMeal: () -> Unit,
    onRecipeClick: (Int) -> Unit,
    onServingsChange: (PlannedMeal, Int) -> Unit,
    onRemove: (PlannedMeal) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RecipeBoxShapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = if (isToday) colors.primaryContainer else colors.surfaceContainer,
            contentColor = if (isToday) colors.onPrimaryContainer else colors.onSurface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(day.format(DayNameFormat), style = MaterialTheme.typography.titleLarge)
                Text(
                    if (isToday) "Today · ${day.format(DayMonthFormat)}" else day.format(DayMonthFormat),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isToday) colors.onPrimaryContainer else colors.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp),
                )
            }
            meals.forEachIndexed { index, meal ->
                // Keyed so a swiped-away row's state doesn't pass to the row that moves up
                key(meal.id) {
                    MealRow(
                        meal = meal,
                        index = index,
                        onRecipeClick = { onRecipeClick(meal.recipe.id) },
                        onServingsChange = { onServingsChange(meal, it) },
                        onRemove = { onRemove(meal) },
                    )
                }
            }
            FilledTonalButton(
                onClick = onAddMeal,
                modifier = Modifier.heightIn(min = 44.dp),
                shape = CircleShape,
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Add meal", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun MealRow(
    meal: PlannedMeal,
    index: Int,
    onRecipeClick: () -> Unit,
    onServingsChange: (Int) -> Unit,
    onRemove: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(20.dp)
    val dismissState = rememberSwipeToDismissBoxState()

    SwipeToDismissBox(
        state = dismissState,
        onDismiss = { onRemove() },
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.errorContainer, shape)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null, tint = colors.onErrorContainer)
            }
        },
        enableDismissFromStartToEnd = false,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp),
            shape = shape,
            color = colors.surfaceContainerLowest,
            contentColor = colors.onSurface,
        ) {
            Row(
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    onClick = onRecipeClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(colors.secondaryContainer, thumbnailShape(index)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                recipeIcon(meal.recipe),
                                contentDescription = null,
                                tint = colors.onSecondaryContainer,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Text(
                            meal.recipe.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )
                    }
                }
                ServingsChip(meal.servings, onServingsChange)
            }
        }
    }
}

/** "4 servings" pill; tapping it opens a small −/+ stepper that saves as you go. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ServingsChip(servings: Int, onServingsChange: (Int) -> Unit) {
    val colors = MaterialTheme.colorScheme
    var open by remember { mutableStateOf(false) }

    Box {
        Surface(
            onClick = { open = true },
            shape = CircleShape,
            color = colors.tertiaryContainer,
            contentColor = colors.onTertiaryContainer,
        ) {
            Text(
                plural(servings, "serving"),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
        DropdownMenu(
            expanded = open,
            onDismissRequest = { open = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = colors.surfaceContainerHigh,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledIconButton(
                    onClick = { onServingsChange(servings - 1) },
                    enabled = servings > 1,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = colors.surface,
                        contentColor = colors.onSurface,
                    ),
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Fewer servings")
                }
                Text(
                    "$servings",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(min = 48.dp),
                )
                FilledIconButton(
                    onClick = { onServingsChange(servings + 1) },
                    shape = RoundedCornerShape(12.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = colors.tertiary,
                        contentColor = colors.onTertiary,
                    ),
                ) {
                    Icon(Icons.Default.Add, contentDescription = "More servings")
                }
            }
        }
    }
}

private val PreviewMonday = LocalDate.of(2026, 11, 9)

private fun previewMeal(id: Int, day: Long, title: String, servings: Int) = PlannedMeal(
    id = id,
    date = PreviewMonday.plusDays(day),
    servings = servings,
    position = id,
    recipe = Recipe(id = id, title = title, servings = 4),
)

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun PlanContentPreview() {
    RecipeBoxTheme(dynamicColor = false) {
        PlanContent(
            weekStart = PreviewMonday,
            today = PreviewMonday.plusDays(1),
            meals = listOf(
                previewMeal(1, 0, "Spaghetti Carbonara", 4),
                previewMeal(2, 1, "Chicken Tikka Masala", 6),
                previewMeal(3, 1, "Greek Salad", 2),
                previewMeal(4, 4, "Beef Tacos", 4),
            ),
            listState = rememberLazyListState(),
            onPreviousWeek = {}, onNextWeek = {}, onThisWeek = {}, onAddMeal = {}, onRecipeClick = {},
            onServingsChange = { _, _ -> }, onRemove = {}, onAddToShoppingList = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlanContentEmptyPreview() {
    RecipeBoxTheme(dynamicColor = false) {
        PlanContent(
            weekStart = PreviewMonday.plusWeeks(1),
            today = PreviewMonday,
            meals = emptyList(),
            listState = rememberLazyListState(),
            onPreviousWeek = {}, onNextWeek = {}, onThisWeek = {}, onAddMeal = {}, onRecipeClick = {},
            onServingsChange = { _, _ -> }, onRemove = {}, onAddToShoppingList = {},
        )
    }
}
