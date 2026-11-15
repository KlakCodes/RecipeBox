package com.example.recipebox

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.recipebox.data.PlanRepository
import com.example.recipebox.data.RecipeBoxDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalDate

/** Builds old database versions from app/schemas and checks each migration keeps the data. */
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    // Its own file, so it never touches the app's real recipebox.db
    private val testDb = "migration-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        RecipeBoxDatabase::class.java,
    )

    @After
    fun deleteTestDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(testDb)
        // Room's lock file isn't removed by deleteDatabase
        File(context.getDatabasePath(testDb).path + ".lck").delete()
    }

    @Test
    fun migrate3To4_keepsRecipesAndAddsNullIcon() {
        helper.createDatabase(testDb, 3).use { db ->
            db.execSQL("INSERT INTO recipes (id, title, servings, notes, createdAt) VALUES (1, 'Paneer Curry', 2, NULL, 1000)")
            db.execSQL("INSERT INTO ingredients (id, recipeId, amount, unit, name, position) VALUES (1, 1, 250.0, 'g', 'paneer', 0)")
            db.execSQL("INSERT INTO steps (id, recipeId, text, position) VALUES (1, 1, 'Fry the paneer', 1)")
            db.execSQL("INSERT INTO shopping_items (id, name, amount, unit, checked, sourceRecipeIds) VALUES (1, 'milk', 1.0, 'l', 0, '')")
        }

        // Fails if the migrated schema doesn't match what Room expects for version 4
        helper.runMigrationsAndValidate(testDb, 4, true, RecipeBoxDatabase.MIGRATION_3_4).use { db ->
            db.query("SELECT title, servings, createdAt, icon FROM recipes WHERE id = 1").use { c ->
                assertTrue(c.moveToFirst())
                assertEquals("Paneer Curry", c.getString(0))
                assertEquals(2, c.getInt(1))
                assertEquals(1000L, c.getLong(2))
                assertTrue(c.isNull(3))
            }
            db.query("SELECT count(*) FROM ingredients WHERE recipeId = 1").use { c ->
                c.moveToFirst(); assertEquals(1, c.getInt(0))
            }
            db.query("SELECT count(*) FROM steps WHERE recipeId = 1").use { c ->
                c.moveToFirst(); assertEquals(1, c.getInt(0))
            }
            db.query("SELECT count(*) FROM shopping_items").use { c ->
                c.moveToFirst(); assertEquals(1, c.getInt(0))
            }
        }
    }

    @Test
    fun migrate4To5_keepsAllDataAndAddsEmptyPlannedMeals() {
        helper.createDatabase(testDb, 4).use { db ->
            db.execSQL("INSERT INTO recipes (id, title, servings, notes, createdAt, icon) VALUES (1, 'Paneer Curry', 2, 'Spicy', 1000, 'curry')")
            db.execSQL("INSERT INTO recipes (id, title, servings, notes, createdAt, icon) VALUES (2, 'Toast', 1, NULL, 2000, NULL)")
            db.execSQL("INSERT INTO ingredients (id, recipeId, amount, unit, name, position) VALUES (1, 1, 250.0, 'g', 'paneer', 0)")
            db.execSQL("INSERT INTO ingredients (id, recipeId, amount, unit, name, position) VALUES (2, 1, NULL, '', 'salt to taste', 1)")
            db.execSQL("INSERT INTO steps (id, recipeId, text, position) VALUES (1, 1, 'Fry the paneer', 1)")
            db.execSQL("INSERT INTO shopping_items (id, name, amount, unit, checked, sourceRecipeIds) VALUES (1, 'milk', 1.5, 'l', 1, '')")
            db.execSQL("INSERT INTO shopping_items (id, name, amount, unit, checked, sourceRecipeIds) VALUES (2, 'paneer', 250.0, 'g', 0, '1')")
        }

        // Fails if the migrated schema doesn't match what Room expects for version 5
        helper.runMigrationsAndValidate(testDb, 5, true, RecipeBoxDatabase.MIGRATION_4_5).use { db ->
            db.query("SELECT id, title, servings, notes, createdAt, icon FROM recipes ORDER BY id").use { c ->
                assertTrue(c.moveToFirst())
                assertEquals("Paneer Curry", c.getString(1))
                assertEquals(2, c.getInt(2))
                assertEquals("Spicy", c.getString(3))
                assertEquals(1000L, c.getLong(4))
                assertEquals("curry", c.getString(5))
                assertTrue(c.moveToNext())
                assertEquals("Toast", c.getString(1))
                assertTrue(c.isNull(5))
            }
            db.query("SELECT amount, name FROM ingredients WHERE recipeId = 1 ORDER BY position").use { c ->
                assertEquals(2, c.count)
                c.moveToFirst(); assertEquals(250.0, c.getDouble(0), 0.0)
                c.moveToNext(); assertTrue(c.isNull(0)); assertEquals("salt to taste", c.getString(1))
            }
            db.query("SELECT text FROM steps WHERE recipeId = 1").use { c ->
                c.moveToFirst(); assertEquals("Fry the paneer", c.getString(0))
            }
            db.query("SELECT name, amount, unit, checked, sourceRecipeIds FROM shopping_items ORDER BY id").use { c ->
                assertEquals(2, c.count)
                c.moveToFirst()
                assertEquals("milk", c.getString(0)); assertEquals(1.5, c.getDouble(1), 0.0); assertEquals(1, c.getInt(3))
                c.moveToNext()
                assertEquals("paneer", c.getString(0)); assertEquals("1", c.getString(4))
            }
            db.query("SELECT count(*) FROM planned_meals").use { c ->
                c.moveToFirst(); assertEquals(0, c.getInt(0))
            }

            // The new table works, and deleting a recipe deletes its planned meals
            db.execSQL("PRAGMA foreign_keys = ON")
            db.execSQL("INSERT INTO planned_meals (recipeId, date, servings, position) VALUES (1, 20403, 4, 0)")
            db.execSQL("INSERT INTO planned_meals (recipeId, date, servings, position) VALUES (2, 20403, 1, 1)")
            db.execSQL("DELETE FROM recipes WHERE id = 1")
            db.query("SELECT recipeId FROM planned_meals").use { c ->
                assertEquals(1, c.count)
                c.moveToFirst(); assertEquals(2, c.getInt(0))
            }
        }
    }

    @Test
    fun openingVersion4WithRoom_readsRecipesAndPlansMeals() {
        helper.createDatabase(testDb, 4).use { db ->
            db.execSQL("INSERT INTO recipes (id, title, servings, notes, createdAt, icon) VALUES (1, 'Burger', 4, NULL, 1000, NULL)")
            db.execSQL("INSERT INTO shopping_items (id, name, amount, unit, checked, sourceRecipeIds) VALUES (1, 'buns', 4.0, '', 0, '1')")
        }

        val room = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            RecipeBoxDatabase::class.java,
            testDb,
        ).addMigrations(*RecipeBoxDatabase.ALL_MIGRATIONS).build()
        try {
            runBlocking {
                assertEquals("Burger", room.recipeDao().getRecipeEntityById(1)?.title)
                assertEquals(listOf("buns"), room.shoppingDao().getAllOnce().map { it.name })

                val day = LocalDate.of(2026, 11, 10)
                PlanRepository(room.planDao()).addMeal(recipeId = 1, date = day, servings = 6)
                val week = PlanRepository(room.planDao()).getWeek(LocalDate.of(2026, 11, 9)).first()
                assertEquals(1, week.size)
                assertEquals("Burger", week[0].recipe.title)
                assertEquals(day, week[0].date)
                assertEquals(6, week[0].servings)
            }
        } finally {
            room.close()
        }
    }

    @Test
    fun openingMigratedDatabaseWithRoom_readsRecipe() {
        helper.createDatabase(testDb, 3).use { db ->
            db.execSQL("INSERT INTO recipes (id, title, servings, notes, createdAt) VALUES (1, 'Burger', 4, NULL, 1000)")
        }

        // Same migrations the app registers, and no destructive fallback
        val room = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            RecipeBoxDatabase::class.java,
            testDb,
        ).addMigrations(*RecipeBoxDatabase.ALL_MIGRATIONS).build()
        try {
            val recipe = runBlocking { room.recipeDao().getRecipeEntityById(1) }
            assertEquals("Burger", recipe?.title)
            assertNull(recipe?.icon)
        } finally {
            room.close()
        }
    }
}
