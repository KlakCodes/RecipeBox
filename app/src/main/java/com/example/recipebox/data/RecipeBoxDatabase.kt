package com.example.recipebox.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RecipeEntity::class, IngredientEntity::class, StepEntity::class], version = 1, exportSchema = false)
abstract class RecipeBoxDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile private var instance: RecipeBoxDatabase? = null

        fun getInstance(context: Context): RecipeBoxDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    RecipeBoxDatabase::class.java,
                    "recipebox.db",
                ).build().also { instance = it }
            }
    }
}
