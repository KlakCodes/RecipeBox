package com.example.recipebox.data

object RecipeRepository {
    fun getAll(): List<Recipe> = sampleRecipes
    fun getById(id: Int): Recipe? = sampleRecipes.find { it.id == id }
}

private val sampleRecipes = listOf(
    Recipe(
        id = 1,
        title = "Spaghetti Carbonara",
        servings = 4,
        ingredients = listOf(
            Ingredient(400.0, "g", "spaghetti"),
            Ingredient(200.0, "g", "pancetta, diced"),
            Ingredient(4.0, "", "eggs"),
            Ingredient(100.0, "g", "Parmesan, grated"),
            Ingredient(2.0, "", "garlic cloves"),
            Ingredient(null, "", "salt to taste"),
            Ingredient(null, "", "black pepper to taste"),
        ),
        steps = listOf(
            Step(1, "Bring a large pot of salted water to the boil and cook the spaghetti until al dente."),
            Step(2, "Fry the pancetta and garlic in a dry pan over medium heat until the pancetta is crisp."),
            Step(3, "Beat the eggs with the Parmesan and a generous pinch of black pepper."),
            Step(4, "Drain the pasta, reserving a cup of cooking water, then remove the pan from the heat."),
            Step(5, "Add the pasta to the pan, pour over the egg mixture, and toss quickly, adding a splash of pasta water to loosen."),
        ),
    ),
    Recipe(
        id = 2,
        title = "Chicken Tikka Masala",
        servings = 6,
        ingredients = listOf(
            Ingredient(800.0, "g", "chicken thighs, boneless and diced"),
            Ingredient(400.0, "g", "tinned tomatoes"),
            Ingredient(200.0, "ml", "double cream"),
            Ingredient(2.0, "", "onions, sliced"),
            Ingredient(4.0, "", "garlic cloves, minced"),
            Ingredient(3.0, "tbsp", "tikka masala paste"),
        ),
        steps = listOf(
            Step(1, "Fry the onions in oil over medium heat for 10 minutes until golden."),
            Step(2, "Add the garlic and tikka masala paste and cook for 2 minutes."),
            Step(3, "Add the chicken and cook until sealed on all sides."),
            Step(4, "Stir in the tomatoes and simmer for 20 minutes."),
            Step(5, "Stir in the cream and heat through for 5 minutes. Season to taste."),
        ),
    ),
    Recipe(
        id = 3,
        title = "Banana Bread",
        servings = 8,
        ingredients = listOf(
            Ingredient(3.0, "", "ripe bananas, mashed"),
            Ingredient(200.0, "g", "plain flour"),
            Ingredient(100.0, "g", "butter, melted"),
            Ingredient(150.0, "g", "caster sugar"),
            Ingredient(2.0, "", "eggs"),
            Ingredient(1.0, "tsp", "baking soda"),
        ),
        steps = listOf(
            Step(1, "Preheat oven to 175°C and grease a 9×5 inch loaf tin."),
            Step(2, "Mash the bananas in a large bowl and mix in the melted butter."),
            Step(3, "Beat in the sugar, eggs, and baking soda."),
            Step(4, "Fold in the flour until just combined — do not overmix."),
            Step(5, "Pour into the tin and bake for 55–65 minutes until a skewer comes out clean."),
        ),
    ),
    Recipe(
        id = 4,
        title = "Greek Salad",
        servings = 2,
        ingredients = listOf(
            Ingredient(2.0, "", "ripe tomatoes, cut into wedges"),
            Ingredient(0.5, "", "cucumber, thickly sliced"),
            Ingredient(100.0, "g", "feta, cut into chunks"),
            Ingredient(50.0, "g", "Kalamata olives"),
            Ingredient(2.0, "tbsp", "olive oil"),
            Ingredient(1.0, "tsp", "dried oregano"),
        ),
        steps = listOf(
            Step(1, "Arrange the tomatoes, cucumber, and olives in a serving bowl."),
            Step(2, "Add the feta on top."),
            Step(3, "Drizzle with olive oil and sprinkle with oregano. Serve immediately."),
        ),
    ),
    Recipe(
        id = 5,
        title = "Beef Tacos",
        servings = 4,
        ingredients = listOf(
            Ingredient(500.0, "g", "ground beef"),
            Ingredient(8.0, "", "taco shells"),
            Ingredient(1.0, "", "onion, finely diced"),
            Ingredient(2.0, "tbsp", "taco seasoning"),
            Ingredient(100.0, "g", "cheddar, grated"),
            Ingredient(null, "", "salsa, to serve"),
            Ingredient(null, "", "sour cream, to serve"),
        ),
        steps = listOf(
            Step(1, "Fry the onion in a pan over medium heat for 5 minutes until softened."),
            Step(2, "Add the ground beef and cook, breaking it up, until browned all over."),
            Step(3, "Stir in the taco seasoning with 3 tbsp of water and simmer for 3 minutes."),
            Step(4, "Warm the taco shells according to packet instructions."),
            Step(5, "Fill each shell with the beef mixture and top with cheese, salsa, and sour cream."),
        ),
    ),
)
