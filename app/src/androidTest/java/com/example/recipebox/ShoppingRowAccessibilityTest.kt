package com.example.recipebox

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.recipebox.data.RecipeBoxDatabase
import com.example.recipebox.data.ShoppingItemEntity
import com.example.recipebox.data.ShoppingRepository
import com.example.recipebox.ui.shoppinglist.ShoppingListScreen
import com.example.recipebox.ui.shoppinglist.ShoppingListViewModel
import com.example.recipebox.ui.theme.RecipeBoxTheme
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** TalkBack can't long-press or swipe a row, so Edit and Delete must be custom actions on it. */
@RunWith(AndroidJUnit4::class)
class ShoppingRowAccessibilityTest {
    @get:Rule
    val compose = createComposeRule()

    private lateinit var db: RecipeBoxDatabase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            RecipeBoxDatabase::class.java,
        ).build()
        runBlocking {
            db.shoppingDao().upsertItem(ShoppingItemEntity(id = 1, name = "milk", amount = 1.0, unit = "l"))
        }
        val viewModel = ShoppingListViewModel(ShoppingRepository(db.shoppingDao()))
        compose.setContent { RecipeBoxTheme { ShoppingListScreen(viewModel = viewModel) } }
        compose.waitUntil(5_000) { rowExists() }
    }

    @After
    fun tearDown() = db.close()

    private fun rowExists() =
        compose.onAllNodes(hasText("Milk") and hasClickAction()).fetchSemanticsNodes().isNotEmpty()

    private fun row(): SemanticsNodeInteraction = compose.onNode(hasText("Milk") and hasClickAction())

    private fun runCustomAction(label: String) {
        val actions = row().fetchSemanticsNode().config[SemanticsActions.CustomActions]
        compose.runOnIdle { actions.single { it.label == label }.action() }
    }

    @Test
    fun row_isACheckboxWithEditAndDeleteActions() {
        row().assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox))
        row().assert(SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, ToggleableState.Off))
        val labels = row().fetchSemanticsNode().config[SemanticsActions.CustomActions].map { it.label }
        assertEquals(listOf("Edit", "Delete"), labels)
    }

    @Test
    fun editAction_opensTheSheetInEditMode() {
        runCustomAction("Edit")
        compose.onNodeWithText("Edit item").assertExists()
        compose.onNodeWithText("Save changes").assertExists()
        compose.onNodeWithText("Delete").assertExists()
    }

    @Test
    fun deleteAction_removesTheItemAndOffersUndo() {
        runCustomAction("Delete")
        compose.waitUntil(5_000) { !rowExists() }
        compose.onNodeWithText("Deleted Milk").assertExists()
        assertEquals(0, runBlocking { db.shoppingDao().getAllOnce().size })
    }
}
