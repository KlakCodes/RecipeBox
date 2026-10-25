package com.example.recipebox

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.recipebox.data.RecipeBoxDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

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
