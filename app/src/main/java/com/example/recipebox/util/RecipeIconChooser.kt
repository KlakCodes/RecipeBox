package com.example.recipebox.util

/**
 * Food icon categories. [key] is what gets saved when an icon is chosen by hand, so never rename one.
 * Keywords are matched as whole words or phrases, so list plurals explicitly. Order breaks the last ties.
 */
enum class RecipeIconCategory(val key: String, val label: String, val keywords: List<String>) {
    Pasta(
        "pasta", "Pasta",
        listOf(
            "pasta", "spaghetti", "penne", "lasagne", "lasagna", "macaroni", "mac and cheese", "fusilli",
            "linguine", "tagliatelle", "fettuccine", "rigatoni", "ravioli", "tortellini", "gnocchi", "orzo",
            "carbonara", "bolognese", "noodle", "noodles", "ramen", "udon", "soba", "pad thai", "chow mein",
        ),
    ),
    Pizza("pizza", "Pizza", listOf("pizza", "pizzas", "calzone", "calzones", "margherita")),
    Burger(
        "burger", "Burger",
        listOf("burger", "burgers", "hamburger", "cheeseburger", "sandwich", "sandwiches", "sliders", "hot dog", "hot dogs"),
    ),
    Curry(
        "curry", "Curry",
        listOf(
            "curry", "curries", "stew", "stews", "chilli", "chili", "casserole", "tagine", "dal", "dhal",
            "korma", "masala", "tikka", "jalfrezi", "vindaloo", "goulash",
        ),
    ),
    Soup("soup", "Soup", listOf("soup", "soups", "broth", "chowder", "bisque", "gazpacho", "minestrone")),
    Rice(
        "rice", "Rice bowl",
        listOf("rice", "risotto", "paella", "biryani", "pilaf", "pilau", "poke", "bibimbap", "congee"),
    ),
    Salad(
        "salad", "Salad",
        listOf("salad", "salads", "lettuce", "spinach", "kale", "rocket", "arugula", "coleslaw", "slaw", "greens"),
    ),
    Fish(
        "fish", "Fish",
        listOf(
            "fish", "salmon", "tuna", "cod", "haddock", "mackerel", "sardine", "sardines", "trout", "prawn",
            "prawns", "shrimp", "crab", "mussels", "seafood", "sushi",
        ),
    ),
    Meat(
        "meat", "Meat & grill",
        listOf(
            "steak", "steaks", "beef", "pork", "lamb", "sausage", "sausages", "bbq", "barbecue", "ribs",
            "bacon", "pancetta", "ham", "mince", "meatballs",
        ),
    ),
    Chicken(
        "chicken", "Chicken",
        listOf("chicken", "kebab", "kebabs", "skewer", "skewers", "souvlaki", "shawarma", "wings", "drumsticks", "turkey", "satay"),
    ),
    Egg("egg", "Egg", listOf("egg", "eggs", "omelette", "omelet", "frittata", "quiche", "shakshuka")),
    Breakfast(
        "breakfast", "Breakfast",
        listOf(
            "breakfast", "brunch", "pancake", "pancakes", "waffle", "waffles", "porridge", "oats", "oatmeal",
            "granola", "muesli", "crepe", "crepes", "toast",
        ),
    ),
    Bread(
        "bread", "Bread",
        listOf(
            "bread", "breads", "loaf", "loaves", "baguette", "sourdough", "focaccia", "bun", "buns", "rolls",
            "croissant", "croissants", "bagel", "bagels", "scone", "scones",
        ),
    ),
    Cake(
        "cake", "Cake",
        listOf("cake", "cakes", "cupcake", "cupcakes", "cheesecake", "brownie", "brownies", "muffin", "muffins", "gateau"),
    ),
    Cookie("cookie", "Cookie", listOf("cookie", "cookies", "biscuit", "biscuits", "shortbread", "flapjack", "flapjacks")),
    Dessert(
        "dessert", "Dessert",
        listOf("ice cream", "gelato", "sorbet", "dessert", "desserts", "pudding", "puddings", "mousse", "trifle", "sundae"),
    ),
    Drink(
        "drink", "Drink",
        listOf(
            "coffee", "espresso", "latte", "cappuccino", "tea", "hot chocolate", "smoothie", "smoothies",
            "milkshake", "milkshakes", "lemonade",
        ),
    ),
    Cocktail("cocktail", "Cocktail", listOf("cocktail", "cocktails", "mojito", "sangria", "punch", "spritz")),
    StirFry("stir_fry", "Stir-fry", listOf("stir fry", "stir fried", "wok", "fajita", "fajitas", "fried")),
    Fruit(
        "fruit", "Fruit",
        listOf("fruit", "fruits", "apple", "apples", "banana", "bananas", "berries", "strawberries", "mango"),
    ),

    /** Shown when nothing matches. */
    Plate("plate", "Plate", emptyList());

    companion object {
        /** The category saved under [key], or null for Auto or a key that no longer exists. */
        fun fromKey(key: String?): RecipeIconCategory? = entries.firstOrNull { it.key == key }
    }
}

/**
 * Picks an icon from the recipe's words. A keyword in the title scores 3, and each ingredient with a
 * keyword scores 1. Ties go to a title match, then to the keyword later in the title (English dish names
 * end with the dish: "Chicken curry"), then to category order. No match gives [RecipeIconCategory.Plate].
 */
fun chooseIcon(title: String, ingredientNames: List<String>): RecipeIconCategory {
    val normalisedTitle = normalise(title)
    val normalisedIngredients = ingredientNames.map(::normalise)

    data class Score(val category: RecipeIconCategory, val points: Int, val titlePosition: Int)

    val best = RecipeIconCategory.entries
        .map { category ->
            // Plate has no keywords, so it scores 0 and is only used as the fallback
            val keywords = normalisedKeywords.getValue(category)
            val titlePosition = keywords.maxOfOrNull { normalisedTitle.lastIndexOf(it) } ?: -1
            val titlePoints = if (titlePosition >= 0) 3 else 0
            val ingredientPoints = normalisedIngredients.count { ingredient ->
                keywords.any { ingredient.contains(it) }
            }
            Score(category, titlePoints + ingredientPoints, titlePosition)
        }
        .filter { it.points > 0 }
        // maxWith keeps the first of equal elements, so category order settles any remaining tie
        .maxWithOrNull(compareBy<Score> { it.points }.thenBy { it.titlePosition >= 0 }.thenBy { it.titlePosition })

    return best?.category ?: RecipeIconCategory.Plate
}

private val NonWordChars = Regex("[^\\p{L}\\p{N}]+")

// Normalised once: this runs for every recipe card, so per-call regex work made scrolling and navigation stutter
private val normalisedKeywords: Map<RecipeIconCategory, List<String>> =
    RecipeIconCategory.entries.associateWith { category -> category.keywords.map(::normalise) }

/** Lowercase words separated by single spaces and padded with one at each end, so " egg " never matches "eggplant". */
private fun normalise(text: String): String =
    " " + text.lowercase().split(NonWordChars).filter { it.isNotEmpty() }.joinToString(" ") + " "
