package com.example.recipebox.ui.shoppinglist

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recipebox.RecipeBoxApplication
import com.example.recipebox.data.ManualAddResult
import com.example.recipebox.data.ShoppingItem
import com.example.recipebox.ui.components.EmptyState
import com.example.recipebox.util.formatAmount

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel = viewModel(
        factory = ShoppingListViewModel.factory(
            (LocalContext.current.applicationContext as RecipeBoxApplication).shoppingRepository,
        ),
    ),
) {
    val shoppingItems by viewModel.items.collectAsState()
    val lastManualAdd by viewModel.lastManualAdd.collectAsState()
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(lastManualAdd) {
        val result = lastManualAdd ?: return@LaunchedEffect
        viewModel.resetLastManualAdd()
        val name = result.name.replaceFirstChar { it.uppercase() }
        if (result is ManualAddResult.AlreadyListed) {
            snackbarHostState.showSnackbar("$name is already on the list")
        } else {
            val action = snackbarHostState.showSnackbar("Added $name", actionLabel = "Undo", duration = SnackbarDuration.Short)
            if (action == SnackbarResult.ActionPerformed) viewModel.undoAdd(result)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ShoppingListContent(shoppingItems, viewModel)

        ExtendedFloatingActionButton(
            onClick = { showAddSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 16.dp)
                .height(72.dp),
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(Modifier.size(12.dp))
            Text("Add item", style = MaterialTheme.typography.titleLarge)
        }

        // Sits above the FAB so it never covers it
        SnackbarHost(
            snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp),
        )
    }

    if (showAddSheet) {
        AddItemSheet(
            onAdd = { name, amount, unit -> viewModel.addItem(name, amount, unit) },
            onDismiss = { showAddSheet = false },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ShoppingListContent(shoppingItems: List<ShoppingItem>, viewModel: ShoppingListViewModel) {
    val (ticked, toBuy) = shoppingItems.partition { it.checked }

    if (shoppingItems.isEmpty()) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text("Shopping list", style = MaterialTheme.typography.displayMedium)
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    },
                    polygon = MaterialShapes.Sunny,
                    title = "All done",
                    hint = "Add a recipe from its page, or tap Add item",
                )
            }
        }
        return
    }

    LazyColumn(
        // Bottom padding clears the Add item FAB, matching the Recipe list
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "header") {
            Header(
                tickedCount = ticked.size,
                total = shoppingItems.size,
                onClearTicked = { viewModel.clearChecked() },
                onClearAll = { viewModel.clearAll() },
            )
        }
        if (toBuy.isNotEmpty()) {
            item(key = "toBuyHeader") { SectionHeader("To buy", Modifier.animateItem()) }
            items(toBuy, key = { it.id }) { item ->
                ShoppingRow(item, onToggle = { viewModel.toggleChecked(item.id, it) }, modifier = Modifier.animateItem())
            }
        }
        if (ticked.isNotEmpty()) {
            item(key = "tickedHeader") { SectionHeader("Ticked", Modifier.animateItem()) }
            items(ticked, key = { it.id }) { item ->
                ShoppingRow(item, onToggle = { viewModel.toggleChecked(item.id, it) }, modifier = Modifier.animateItem())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Header(tickedCount: Int, total: Int, onClearTicked: () -> Unit, onClearAll: () -> Unit) {
    val progress by animateFloatAsState(tickedCount.toFloat() / total, label = "progress")
    val stroke = with(LocalDensity.current) { Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round) }

    Column {
        Text("Shopping list", style = MaterialTheme.typography.displayMedium)
        Text(
            "$tickedCount of $total ticked",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
        )
        LinearWavyProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp),
            color = MaterialTheme.colorScheme.tertiary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            stroke = stroke,
            trackStroke = stroke,
            stopSize = 0.dp,
        )
        Row(
            modifier = Modifier.padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilledTonalButton(
                onClick = onClearTicked,
                enabled = tickedCount > 0,
                modifier = Modifier.height(44.dp),
                shape = CircleShape,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            ) {
                Text("Clear ticked", style = MaterialTheme.typography.labelLarge)
            }
            OutlinedButton(
                onClick = onClearAll,
                modifier = Modifier.height(44.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Clear all", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.headlineSmall,
        modifier = modifier.padding(start = 4.dp, top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun ShoppingRow(item: ShoppingItem, onToggle: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    // Ticked rows drop their background so the to-buy rows stand out
    val container by animateColorAsState(
        if (item.checked) Color.Transparent else colors.surfaceContainerLow,
        label = "rowContainer",
    )
    val amount = item.amount?.let { a ->
        listOfNotNull(formatAmount(a, item.unit), item.unit.ifEmpty { null }).joinToString(" ")
    }

    Surface(
        checked = item.checked,
        onCheckedChange = onToggle,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp),
        shape = RoundedCornerShape(22.dp),
        color = container,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TickBox(item.checked)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp),
            ) {
                Text(
                    item.name.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (item.checked) colors.onSurfaceVariant else colors.onSurface,
                    textDecoration = if (item.checked) TextDecoration.LineThrough else null,
                )
                if (!item.checked && item.sourceRecipeIds.size > 1) {
                    Text(
                        "From ${item.sourceRecipeIds.size} recipes",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.tertiary,
                    )
                }
            }
            if (amount != null) {
                Text(
                    amount,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (item.checked) colors.onSurfaceVariant else colors.primary,
                    textDecoration = if (item.checked) TextDecoration.LineThrough else null,
                )
            }
        }
    }
}

/** 28dp rounded-square box when unticked; a filled tertiary circle with a check when ticked. */
@Composable
private fun TickBox(checked: Boolean) {
    val colors = MaterialTheme.colorScheme
    if (checked) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(colors.tertiary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = colors.onTertiary, modifier = Modifier.size(18.dp))
        }
    } else {
        Box(
            modifier = Modifier
                .size(28.dp)
                .border(2.dp, colors.onSurfaceVariant, RoundedCornerShape(8.dp)),
        )
    }
}
