package com.example.recipebox.ui.shoppinglist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recipebox.RecipeBoxApplication
import com.example.recipebox.util.formatAmount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel = viewModel(
        factory = ShoppingListViewModel.factory(
            (LocalContext.current.applicationContext as RecipeBoxApplication).shoppingRepository,
        ),
    ),
) {
    val shoppingItems by viewModel.items.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping list") },
                actions = {
                    TextButton(onClick = { viewModel.clearChecked() }) { Text("Clear checked") }
                    TextButton(onClick = { viewModel.clearAll() }) { Text("Clear all") }
                },
            )
        },
    ) { innerPadding ->
        if (shoppingItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Your shopping list is empty",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(shoppingItems, key = { it.id }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = item.checked,
                            onCheckedChange = { viewModel.toggleChecked(item.id, it) },
                        )
                        val text = buildString {
                            if (item.amount != null) {
                                append(formatAmount(item.amount, item.unit))
                                if (item.unit.isNotEmpty()) append(" ${item.unit}")
                                append(" ")
                            }
                            append(item.name)
                        }
                        Text(
                            text = text,
                            style = if (item.checked) {
                                MaterialTheme.typography.bodyMedium.copy(
                                    textDecoration = TextDecoration.LineThrough,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            } else {
                                MaterialTheme.typography.bodyMedium
                            },
                        )
                    }
                }
            }
        }
    }
}
