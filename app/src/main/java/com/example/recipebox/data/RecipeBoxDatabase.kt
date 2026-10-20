package com.example.recipebox.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [RecipeEntity::class, IngredientEntity::class, StepEntity::class, ShoppingItemEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class RecipeBoxDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun shoppingDao(): ShoppingDao

    companion object {
        @Volatile private var instance: RecipeBoxDatabase? = null

        fun getInstance(context: Context): RecipeBoxDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    RecipeBoxDatabase::class.java,
                    "recipebox.db",
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
    }
}
