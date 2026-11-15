package com.example.recipebox.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        RecipeEntity::class,
        IngredientEntity::class,
        StepEntity::class,
        ShoppingItemEntity::class,
        PlannedMealEntity::class,
    ],
    version = 5,
    exportSchema = true,
)
abstract class RecipeBoxDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun shoppingDao(): ShoppingDao
    abstract fun planDao(): PlanDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE shopping_items ADD COLUMN sourceRecipeIds TEXT NOT NULL DEFAULT ''")
            }
        }

        // Existing recipes get NULL, which means Auto
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN icon TEXT")
            }
        }

        // Only adds the new table; existing tables are untouched. SQL matches schemas/.../5.json
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `planned_meals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`recipeId` INTEGER NOT NULL, `date` INTEGER NOT NULL, `servings` INTEGER NOT NULL, " +
                        "`position` INTEGER NOT NULL, FOREIGN KEY(`recipeId`) REFERENCES `recipes`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE )",
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_planned_meals_recipeId` ON `planned_meals` (`recipeId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_planned_meals_date` ON `planned_meals` (`date`)")
            }
        }

        val ALL_MIGRATIONS = arrayOf(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)

        @Volatile private var instance: RecipeBoxDatabase? = null

        fun getInstance(context: Context): RecipeBoxDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    RecipeBoxDatabase::class.java,
                    "recipebox.db",
                )
                    // No destructive fallback: a missing migration should crash, never silently wipe recipes
                    .addMigrations(*ALL_MIGRATIONS)
                    .build().also { instance = it }
            }
    }
}
