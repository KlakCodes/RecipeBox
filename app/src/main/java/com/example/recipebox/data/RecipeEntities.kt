package com.example.recipebox.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val servings: Int,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    // RecipeIconCategory key chosen by hand; null means pick automatically
    val icon: String? = null,
)

@Entity(
    tableName = "ingredients",
    foreignKeys = [ForeignKey(
        entity = RecipeEntity::class,
        parentColumns = ["id"],
        childColumns = ["recipeId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("recipeId")],
)
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recipeId: Int,
    val amount: Double?,
    val unit: String,
    val name: String,
    val position: Int,
)

@Entity(
    tableName = "steps",
    foreignKeys = [ForeignKey(
        entity = RecipeEntity::class,
        parentColumns = ["id"],
        childColumns = ["recipeId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("recipeId")],
)
data class StepEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recipeId: Int,
    val text: String,
    val position: Int,
)

data class RecipeWithDetails(
    @Embedded val recipe: RecipeEntity,
    @Relation(parentColumn = "id", entityColumn = "recipeId")
    val ingredients: List<IngredientEntity>,
    @Relation(parentColumn = "id", entityColumn = "recipeId")
    val steps: List<StepEntity>,
)

@Entity(
    tableName = "planned_meals",
    foreignKeys = [ForeignKey(
        entity = RecipeEntity::class,
        parentColumns = ["id"],
        childColumns = ["recipeId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("recipeId"), Index("date")],
)
data class PlannedMealEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recipeId: Int,
    // LocalDate.toEpochDay()
    val date: Long,
    val servings: Int,
    // Order within the day
    val position: Int,
)

data class PlannedMealWithRecipe(
    @Embedded val meal: PlannedMealEntity,
    @Relation(entity = RecipeEntity::class, parentColumn = "recipeId", entityColumn = "id")
    val recipe: RecipeWithDetails,
)

@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double?,
    val unit: String,
    val checked: Boolean = false,
    // Comma-separated recipe ids; see ShoppingItem.sourceRecipeIds
    @ColumnInfo(defaultValue = "") val sourceRecipeIds: String = "",
)
