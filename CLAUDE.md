# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests (JVM, no device needed)
./gradlew test

# Run a single unit test class
./gradlew test --tests "com.example.recipebox.ExampleUnitTest"

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Lint
./gradlew lint

# Clean build
./gradlew clean assembleDebug
```

On Windows use `gradlew.bat` instead of `./gradlew`.

## Tech Stack

- **Language**: Kotlin 2.2.10
- **UI**: Jetpack Compose with Material3
- **Min SDK**: 31 (Android 12) — dynamic color (`dynamicColorScheme`) is always available
- **Target SDK**: 37
- **Build system**: Gradle with Kotlin DSL (`build.gradle.kts`); version catalog at `gradle/libs.versions.toml`

## Architecture

Currently a single-module app (`app/`) at the scaffold stage. The entry point is `MainActivity` (single-activity pattern). All UI is built with Compose — there are no XML layouts.

```
app/src/main/java/com/example/recipebox/
  MainActivity.kt          # Only activity; sets Compose content
  ui/theme/
    Theme.kt               # RecipeBoxTheme — supports dynamic color, dark/light
    Color.kt               # Static color tokens (fallback for pre-S devices)
    Type.kt                # Typography scale
```

The app uses a single-activity pattern. New screens should be added as composable functions wired into a nav graph rather than new activities.

## Key Conventions

- `RecipeBoxTheme` wraps all composable trees; always use it in `@Preview` functions.
- The app uses Material 3 Expressive (`MaterialExpressiveTheme`, material3 `1.5.0-beta02` pinned in the catalog). Dynamic (wallpaper) color is on by default (`dynamicColor = true`); the tangerine palette in `Color.kt` is the fallback and shows with `dynamicColor = false`. Never hardcode colors in screens — use `MaterialTheme.colorScheme` roles only, so wallpaper colors and dark mode work everywhere.
- Headings (display/headline/title/label) use bundled Roboto Flex (`res/font/roboto_flex.ttf`, variable weights up to 900); body text uses the system font. The typography slots are mapped to the mockup sizes in `Type.kt` (e.g. `displayMedium` = 40sp/900 screen titles). Shapes (`RecipeBoxShapes`, `thumbnailShape`) and badge polygons live in `ui/theme/Shape.kt`; shared Expressive widgets (`ServingsCard`, `DecorShape`, `TonalIconButton`, `EmptyState`, `recipeIcon`) in `ui/components/`.
- Dependency versions are centralized in `gradle/libs.versions.toml` — add new dependencies there, not inline in `build.gradle.kts`.

## App Plan

### Overview

Recipe Box is an offline Android app for saving your recipes, rescaling them to any number of servings, and turning them into one combined shopping list.
In version 1:
• Add, edit and delete recipes: title, servings, ingredients (amount, unit, name) and method steps
• Change the servings on a recipe and see every ingredient amount update
• Add a recipe (at its chosen servings) to a shopping list that merges duplicates and lets you tick items off
Not in version 1: photos, tags and search, importing recipes from websites, cloud sync and sharing. These go in the v2 list at the end.
Tech stack
Use Google's current recommended setup: Kotlin, Jetpack Compose and a Room database, all inside Android Studio. Everything is free and works offline.
Piece
What it's for
Android Studio
The IDE: writes, builds and runs the app on an emulator or your phone
Kotlin
The programming language
Jetpack Compose
Builds the screens in code (no XML layouts)
Room
Saves recipes and the shopping list in a local SQLite database
ViewModel + StateFlow
Holds each screen's data and keeps it when the phone rotates
Navigation Compose
Moves between screens
Material 3
Ready-made buttons, cards, text fields and theming
App structure: UI (Compose screens) → ViewModels → Repository → Room database. Each layer only talks to the one below it, which keeps the code easy to test and change.
Screens
Version 1 needs four screens. The recipe detail screen is the heart of the app, because that's where scaling happens.
A bottom bar with two tabs, Recipes and Shopping list, also lets you jump to the list from anywhere.

### Data model

Four Room tables cover v1. A recipe has many ingredients and many steps; the shopping list is its own table so it survives recipes being edited.
Table
Fields
Notes
Recipe
id, title, servings (Int), notes, createdAt
servings is the base the amounts were written for
Ingredient
id, recipeId, amount (Double?), unit, name, position
amount is empty for things like "salt to taste"
Step
id, recipeId, text, position
position keeps the method in order
ShoppingItem
id, name, amount (Double?), unit, checked (Boolean)
One row per merged ingredient
Store amounts as numbers and units as a fixed list (g, kg, ml, l, tsp, tbsp, cup, item). Free-text amounts like "a handful" can't be scaled, so keep them in name with no amount.

### Key logic

Scaling servings. Multiply every amount by the ratio of new servings to the recipe's base servings. Scaling is shown on screen only; the saved recipe never changes.
\text{scaled amount} = \text{amount} \times \frac{\text{chosen servings}}{\text{base servings}}
Example: a 4-serving recipe with 200 g pasta, scaled to 6 servings, shows 200 × 6/4 = 300 g. Round for display (300 g, not 300.0000001 g) and show cooking fractions for spoons and cups (1½ tbsp).
Building the shopping list. When you add a recipe at its chosen servings:
1. Scale each ingredient as above.
2. Look for an existing list item with the same name (ignoring case) and same unit.
3. If one exists, add the amounts together; otherwise insert a new item.
4. Items with no amount ("salt") are added once and never duplicated.
Keep this logic in plain Kotlin functions with no Android code in them, so you can write quick unit tests for it.
