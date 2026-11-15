package com.example.recipebox.data

data class ShoppingItem(
    val id: Int = 0,
    val name: String,
    val amount: Double?,
    val unit: String,
    val checked: Boolean = false,
    // Recipes this item was merged from, for the "From 2 recipes" note
    val sourceRecipeIds: Set<Int> = emptySet(),
)

/** What adding a week's meals did to the list: the rows as they were before, and the new rows, so Undo can put it back. */
data class ShoppingListChange(
    val mealCount: Int,
    val previous: List<ShoppingItem>,
    val insertedIds: List<Int>,
)

/** What editing an item did, holding the rows as they were so Undo can put them back. */
sealed interface ItemEditResult {
    data class Updated(val previous: ShoppingItem) : ItemEditResult

    /** [previous] was merged into [target], shown as it was before the merge. */
    data class Merged(val previous: ShoppingItem, val target: ShoppingItem) : ItemEditResult
}

/** What adding an item by hand did to the list, so the snackbar can describe it and Undo can reverse it. */
sealed interface ManualAddResult {
    val name: String

    data class Inserted(val id: Int, override val name: String) : ManualAddResult

    data class Merged(val previous: ShoppingItem) : ManualAddResult {
        override val name: String get() = previous.name
    }

    /** A no-amount item that was already on the to-buy list, so nothing changed. */
    data class AlreadyListed(override val name: String) : ManualAddResult
}
