package com.example.recipebox.ui.recipeedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recipebox.RecipeBoxApplication
import com.example.recipebox.data.UNITS
import com.example.recipebox.ui.components.ServingsCard
import com.example.recipebox.ui.components.ShapeBadge
import com.example.recipebox.ui.theme.BadgePolygons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: RecipeEditViewModel = viewModel(
        factory = RecipeEditViewModel.factory(
            (LocalContext.current.applicationContext as RecipeBoxApplication).repository,
        ),
    ),
) {
    val title by viewModel.title.collectAsState()
    val servings by viewModel.servings.collectAsState()
    val ingredients by viewModel.ingredients.collectAsState()
    val steps by viewModel.steps.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var titleError by remember { mutableStateOf(false) }

    LaunchedEffect(saveState) {
        when (val s = saveState) {
            is SaveState.Success -> onSaved()
            is SaveState.Deleted -> onDeleted()
            is SaveState.Error -> {
                if (s.message.contains("Title", ignoreCase = true)) titleError = true
                snackbarHostState.showSnackbar(s.message)
                viewModel.resetSaveState()
            }
            else -> Unit
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDelete() },
            title = { Text("Delete recipe?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete() }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDelete() }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditing) "Edit Recipe" else "New Recipe") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (viewModel.isEditing) {
                        IconButton(onClick = { viewModel.confirmDelete() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete recipe")
                        }
                    }
                    IconButton(onClick = { viewModel.save() }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        viewModel.updateTitle(it)
                        if (titleError) titleError = false
                    },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    textStyle = MaterialTheme.typography.titleLarge,
                    singleLine = true,
                    isError = titleError,
                    supportingText = if (titleError) {
                        { Text("Title is required") }
                    } else null,
                )
            }

            item {
                ServingsCard(
                    servings = servings,
                    onDecrement = { viewModel.decrementServings() },
                    onIncrement = { viewModel.incrementServings() },
                )
            }

            item { SectionHeader("Ingredients") }

            itemsIndexed(ingredients, key = { _, it -> it.id }) { index, draft ->
                IngredientRow(
                    draft = draft,
                    onUpdate = { viewModel.updateIngredient(index, it) },
                    onRemove = { viewModel.removeIngredient(index) },
                    removable = ingredients.size > 1,
                )
            }

            item {
                AddButton("Add ingredient", onClick = { viewModel.addIngredient() })
            }

            item { SectionHeader("Method") }

            itemsIndexed(steps, key = { _, it -> it.id }) { index, draft ->
                StepRow(
                    number = index + 1,
                    draft = draft,
                    onUpdate = { viewModel.updateStep(index, it) },
                    onRemove = { viewModel.removeStep(index) },
                    removable = steps.size > 1,
                )
            }

            item {
                AddButton("Add step", onClick = { viewModel.addStep() })
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 4.dp),
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AddButton(text: String, onClick: () -> Unit) {
    FilledTonalButton(onClick = onClick, shapes = ButtonDefaults.shapes()) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        Text(text)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientRow(
    draft: IngredientDraft,
    onUpdate: (IngredientDraft) -> Unit,
    onRemove: () -> Unit,
    removable: Boolean,
) {
    var unitExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = draft.amount,
            onValueChange = { onUpdate(draft.copy(amount = it)) },
            label = { Text("Amt") },
            modifier = Modifier.width(80.dp),
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )

        ExposedDropdownMenuBox(
            expanded = unitExpanded,
            onExpandedChange = { unitExpanded = it },
            modifier = Modifier.width(96.dp),
        ) {
            OutlinedTextField(
                value = draft.unit.ifEmpty { "—" },
                onValueChange = {},
                readOnly = true,
                label = { Text("Unit") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(unitExpanded) },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                shape = MaterialTheme.shapes.medium,
                singleLine = true,
            )
            ExposedDropdownMenu(
                expanded = unitExpanded,
                onDismissRequest = { unitExpanded = false },
            ) {
                UNITS.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(unit.ifEmpty { "—" }) },
                        onClick = {
                            onUpdate(draft.copy(unit = unit))
                            unitExpanded = false
                        },
                    )
                }
            }
        }

        OutlinedTextField(
            value = draft.name,
            onValueChange = { onUpdate(draft.copy(name = it)) },
            label = { Text("Name") },
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
        )

        IconButton(onClick = onRemove, enabled = removable) {
            Icon(Icons.Default.Close, contentDescription = "Remove ingredient")
        }
    }
}

@Composable
private fun StepRow(
    number: Int,
    draft: StepDraft,
    onUpdate: (StepDraft) -> Unit,
    onRemove: () -> Unit,
    removable: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        ShapeBadge(
            text = "$number",
            polygon = BadgePolygons[(number - 1) % BadgePolygons.size],
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            size = 40.dp,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 12.dp, end = 4.dp),
        )

        OutlinedTextField(
            value = draft.text,
            onValueChange = { onUpdate(draft.copy(text = it)) },
            label = { Text("Step") },
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium,
            minLines = 2,
        )

        IconButton(onClick = onRemove, enabled = removable) {
            Icon(Icons.Default.Close, contentDescription = "Remove step")
        }
    }
}
