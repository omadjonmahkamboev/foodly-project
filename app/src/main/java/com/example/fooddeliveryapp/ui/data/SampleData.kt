package com.example.fooddeliveryapp.ui.data

import androidx.compose.ui.graphics.Color
import com.example.fooddeliveryapp.BuildConfig
import com.example.fooddeliveryapp.ui.AppLanguage
import com.example.fooddeliveryapp.ui.theme.Gold
import com.example.fooddeliveryapp.ui.theme.Orange
import com.example.fooddeliveryapp.ui.theme.Rose
import com.example.fooddeliveryapp.ui.theme.Sky
import com.example.fooddeliveryapp.ui.theme.Success

object SampleData {
    fun drawableUri(resourceName: String): String =
        "android.resource://${BuildConfig.APPLICATION_ID}/drawable/$resourceName"

    val onboarding = listOf(
        OnboardingSlide(
            title = "Все любимые блюда в одном месте",
            description = "Фастфуд, плов, суши, шаурма, завтраки, супы, десерты и напитки рядом с тобой.",
            emoji = "🍔",
            accent = Orange,
        ),
        OnboardingSlide(
            title = "Открывай категорию и выбирай тип блюда",
            description = "Внутри каждой категории Foodly есть понятные разделы: пицца, бургеры, роллы, плов, боулы, кофе и многое другое.",
            emoji = "🍕",
            accent = Gold,
        ),
        OnboardingSlide(
            title = "Укажи точку доставки через Yandex карту",
            description = "Тапни по карте, подтверди адрес и получай точную доставку без звонков и путаницы.",
            emoji = "🛵",
            accent = Sky,
        ),
    )

    private data class DishSeed(
        val category: String,
        val title: String,
        val subtitle: String,
        val price: Int,
        val image: String,
        val ingredients: List<String>,
    )

    private data class RestaurantSeed(
        val id: String,
        val name: String,
        val subtitle: String,
        val description: String,
        val rating: Double,
        val deliveryTime: String,
        val deliveryFee: String,
        val accent: Color,
        val emoji: String,
        val tags: List<String>,
        val location: GeoPoint,
        val dishes: List<DishSeed>,
    )

    private fun dish(
        category: String,
        title: String,
        subtitle: String,
        price: Int,
        image: String,
        vararg ingredients: String,
    ) = DishSeed(category, title, subtitle, price, image, ingredients.toList())

    private data class RestaurantVisualSeed(
        val coverImage: String,
        val galleryImages: List<String> = emptyList(),
    )

    private fun visuals(
        coverImage: String,
        vararg galleryImages: String,
    ) = RestaurantVisualSeed(coverImage, galleryImages.toList())

    private val catalog = listOf(
        RestaurantSeed(
            id = "burger_bistro",
            name = "Burger Bistro",
            subtitle = "Smash burgers, crispy chicken, fries and signature combos",
            description = "Burger Bistro is the premium burger corner in Foodly with glossy buns, juicy patties, smoky sauces and crunchy sides.",
            rating = 4.9,
            deliveryTime = "14-20 min",
            deliveryFee = "Free",
            accent = Orange,
            emoji = "🍔",
            tags = listOf("Burger", "Smash", "Combo"),
            location = GeoPoint(41.314920, 69.258410),
            dishes = listOf(
                dish("bistro_burgers", "Bistro Classic", "Single smash patty, cheddar, tomato and house sauce", 590, "burger_classic_cutout", "bun", "beef", "cheddar", "tomato", "sauce"),
                dish("bistro_burgers", "Smoky Double", "Double beef, smoked cheddar, grilled onion and BBQ glaze", 760, "burger_smoky_double_cutout", "bun", "beef", "cheddar", "bbq", "onion"),
                dish("bistro_burgers", "Crispy Chicken", "Golden chicken fillet, slaw and creamy pepper sauce", 620, "burger_crispy_chicken_cutout", "bun", "chicken", "slaw", "sauce"),
                dish("bistro_sides", "Truffle Fries Box", "Crispy fries, parmesan dust and silky sauce", 340, "foodly_u_fries_2", "potato", "parmesan", "sauce"),
                dish("bistro_combo", "Bistro Combo", "Classic burger, fries and a cold drink", 890, "foodly_u_burger_combo_1", "bun", "beef", "potato", "drink"),
            ),
        ),
        RestaurantSeed(
            id = "fast_food",
            name = "Фастфуд",
            subtitle = "Бургеры, пицца, фри, хот-доги, наггетсы и комбо",
            description = "Большая категория Foodly для быстрого заказа: сочные бургеры, горячая пицца, фри, хот-доги, наггетсы и готовые комбо.",
            rating = 4.8,
            deliveryTime = "18-25 мин",
            deliveryFee = "Бесплатно",
            accent = Orange,
            emoji = "🍔",
            tags = listOf("Бургеры", "Пицца", "Комбо"),
            location = GeoPoint(41.319209, 69.247630),
            dishes = listOf(
                dish("fast_burgers", "Классический бургер", "Говядина, сыр, овощи и фирменный соус", 520, "foodly_u_burger_1", "bun", "beef", "cheese", "lettuce", "sauce"),
                dish("fast_burgers", "Двойной чизбургер", "Две котлеты, cheddar и маринованные огурцы", 690, "foodly_u_burger_2", "bun", "beef", "cheddar", "pickle", "sauce"),
                dish("fast_burgers", "Чикенбургер", "Хрустящая курица, slaw и сливочный соус", 560, "foodly_u_chicken_burger_1", "bun", "chicken", "lettuce", "sauce"),
                dish("fast_pizza", "Маргарита", "Томатный соус, mozzarella и базилик", 640, "foodly_u_pizza_1", "tomato", "mozzarella", "basil", "dough"),
                dish("fast_pizza", "Пепперони", "Пикантная pepperoni и тягучий сыр", 720, "foodly_u_pizza_2", "pepperoni", "cheese", "tomato", "dough"),
                dish("fast_pizza", "4 сыра", "Mozzarella, cheddar, dorblu и parmesan", 760, "foodly_u_pizza_3", "cheese", "mozzarella", "dough"),
                dish("fast_pizza", "Мясная пицца", "Говядина, курица, pepperoni и BBQ", 820, "foodly_u_pizza_4", "beef", "chicken", "pepperoni", "bbq"),
                dish("fast_fries", "Картошка фри", "Золотистая картошка с соусом", 230, "foodly_u_fries_1", "potato", "salt", "sauce"),
                dish("fast_fries", "Сырная фри", "Фри с сырным соусом и зеленью", 310, "foodly_u_fries_2", "potato", "cheese", "greens"),
                dish("fast_hotdogs", "Хот-дог классический", "Булочка, сосиска, горчица и лук", 320, "foodly_u_hotdog_1", "bun", "sausage", "mustard", "onion"),
                dish("fast_hotdogs", "Хот-дог BBQ", "Сосиска, BBQ-соус и жареный лук", 380, "foodly_u_hotdog_2", "bun", "sausage", "bbq", "onion"),
                dish("fast_hotdogs", "Хот-дог сырный", "Сосиска, cheddar и томатный relish", 360, "foodly_u_hotdog_3", "bun", "sausage", "cheddar", "tomato"),
                dish("fast_nuggets", "Куриные наггетсы", "Наггетсы с двумя соусами", 340, "foodly_u_nuggets_1", "chicken", "crumbs", "sauce"),
                dish("fast_nuggets", "Острые наггетсы", "Наггетсы с chili-соусом", 380, "foodly_u_nuggets_2", "chicken", "pepper", "sauce"),
                dish("fast_combo", "Бургер комбо", "Бургер, фри и напиток", 820, "foodly_u_burger_combo_1", "bun", "beef", "potato", "drink"),
                dish("fast_combo", "Пицца комбо", "Пицца и холодный напиток", 880, "foodly_u_pizza_combo_1", "pizza", "drink"),
            ),
        ),
        RestaurantSeed(
            id = "national_food",
            name = "Национальная еда",
            subtitle = "Плов, самса, манты, лагман, шашлык и казан-кебаб",
            description = "Любимые национальные блюда: насыщенный плов, сочная самса, манты, лагман, шашлык и казан-кебаб.",
            rating = 4.9,
            deliveryTime = "24-35 мин",
            deliveryFee = "Бесплатно",
            accent = Gold,
            emoji = "🍚",
            tags = listOf("Плов", "Самса", "Шашлык"),
            location = GeoPoint(41.303164, 69.241901),
            dishes = listOf(
                dish("national_plov", "Плов чайханский", "Рис, говядина, морковь и нут", 620, "foodly_u_plov_1", "rice", "beef", "carrot", "chickpea"),
                dish("national_plov", "Плов праздничный", "Большая порция с мясом и изюмом", 720, "foodly_u_plov_2", "rice", "beef", "carrot", "raisin"),
                dish("national_samsa", "Самса с мясом", "Слоеное тесто и сочная мясная начинка", 180, "foodly_u_samsa_1", "dough", "beef", "onion"),
                dish("national_samsa", "Самса с курицей", "Горячая самса с курицей и луком", 170, "foodly_u_samsa_2", "dough", "chicken", "onion"),
                dish("national_manti", "Манты с мясом", "Тесто, мясо и лук на пару", 480, "foodly_u_manti_1", "dough", "beef", "onion"),
                dish("national_manti", "Манты с тыквой", "Нежные манты с тыквой и специями", 420, "foodly_u_manti_2", "dough", "pumpkin", "onion"),
                dish("national_lagman", "Лагман", "Лапша, мясо и овощной соус", 560, "foodly_u_lagman_1", "noodles", "beef", "pepper", "tomato"),
                dish("national_shashlik", "Шашлык говяжий", "Маринованное мясо на углях", 260, "foodly_u_shashlik_1", "beef", "onion", "pepper"),
                dish("national_shashlik", "Шашлык куриный", "Курица на углях с луком", 230, "foodly_u_shashlik_2", "chicken", "onion", "pepper"),
                dish("national_kazan_kebab", "Казан-кебаб", "Картофель, мясо и специи из казана", 780, "foodly_u_kazan_kebab_1", "beef", "potato", "onion"),
            ),
        ),
        RestaurantSeed(
            id = "asian_food",
            name = "Азиатская еда",
            subtitle = "Суши, роллы, wok, рамен, лапша и рис с курицей",
            description = "Азиатские блюда Foodly: суши, роллы, wok, рамен, лапша и рис с курицей.",
            rating = 4.7,
            deliveryTime = "22-32 мин",
            deliveryFee = "8 000 сум",
            accent = Sky,
            emoji = "🍣",
            tags = listOf("Суши", "Wok", "Рамен"),
            location = GeoPoint(41.326660, 69.292110),
            dishes = listOf(
                dish("asian_sushi", "Суши с лососем", "Нежный лосось на рисе", 520, "foodly_u_sushi_1", "salmon", "rice", "wasabi"),
                dish("asian_sushi", "Суши сет", "Ассорти суши с рыбой", 980, "foodly_u_sushi_2", "fish", "rice", "nori"),
                dish("asian_rolls", "Филадельфия ролл", "Лосось, cream cheese и огурец", 780, "foodly_u_rolls_1", "salmon", "rice", "cheese", "cucumber"),
                dish("asian_rolls", "Калифорния ролл", "Краб, avocado, рис и икра", 720, "foodly_u_rolls_2", "crab", "rice", "avocado", "nori"),
                dish("asian_rolls", "Темпура ролл", "Горячий ролл с креветкой", 820, "foodly_u_rolls_3", "shrimp", "rice", "nori"),
                dish("asian_wok", "Wok с говядиной", "Лапша, говядина и соус", 690, "foodly_u_wok_1", "noodles", "beef", "sauce"),
                dish("asian_wok", "Овощной Wok", "Лапша, овощи и кунжут", 540, "foodly_u_wok_2", "noodles", "greens", "sesame"),
                dish("asian_ramen", "Рамен с яйцом", "Бульон, лапша, яйцо и зелень", 650, "foodly_u_ramen_1", "noodles", "egg", "broth"),
                dish("asian_ramen", "Spicy ramen", "Острый бульон, лапша и курица", 690, "foodly_u_ramen_2", "noodles", "chicken", "pepper"),
                dish("asian_noodles", "Лапша с овощами", "Пшеничная лапша и wok-овощи", 520, "foodly_u_noodles_1", "noodles", "pepper", "greens"),
                dish("asian_noodles", "Лапша teriyaki", "Лапша, курица и соус teriyaki", 610, "foodly_u_noodles_2", "noodles", "chicken", "teriyaki"),
                dish("asian_chicken_rice", "Рис с курицей", "Рис, курица и овощи", 560, "foodly_u_chicken_rice_1", "rice", "chicken", "carrot"),
                dish("asian_chicken_rice", "Курица teriyaki с рисом", "Курица, рис и сладкий соус", 680, "foodly_u_teriyaki_1", "chicken", "rice", "teriyaki"),
            ),
        ),
        RestaurantSeed(
            id = "shawarma_doner",
            name = "Шаурма и донер",
            subtitle = "Шаурма, донер, лаваш и гирос",
            description = "Горячие роллы и тарелки: шаурма, донер, лаваш, гирос, свежие овощи и плотные соусы.",
            rating = 4.7,
            deliveryTime = "16-24 мин",
            deliveryFee = "6 000 сум",
            accent = Orange,
            emoji = "🌯",
            tags = listOf("Шаурма", "Донер", "Лаваш"),
            location = GeoPoint(41.300800, 69.255100),
            dishes = listOf(
                dish("shawarma", "Шаурма с курицей", "Курица, овощи и чесночный соус", 420, "foodly_u_shawarma_1", "lavash", "chicken", "tomato", "sauce"),
                dish("shawarma", "Шаурма с говядиной", "Говядина, томаты и пикантный соус", 480, "foodly_u_shawarma_2", "lavash", "beef", "tomato", "sauce"),
                dish("shawarma", "Шаурма cheese", "Курица, сыр, овощи и соус", 460, "foodly_u_shawarma_3", "lavash", "chicken", "cheese"),
                dish("doner", "Донер classic", "Мясо, овощи и фирменный соус", 520, "foodly_u_doner_1", "beef", "tomato", "sauce"),
                dish("doner", "Донер тарелка", "Мясо, фри и салат", 680, "foodly_u_doner_2", "beef", "potato", "greens"),
                dish("lavash", "Лаваш с курицей", "Курица, овощи и чесночный соус", 440, "foodly_u_lavash_1", "lavash", "chicken", "tomato"),
                dish("lavash", "Лаваш с сыром", "Курица, сыр и овощи", 460, "foodly_u_lavash_2", "lavash", "chicken", "cheese"),
                dish("gyros", "Гирос classic", "Мясо, пита, овощи и соус", 540, "foodly_u_gyros_1", "bread", "beef", "greens"),
                dish("gyros", "Гирос chicken", "Курица, pita и свежие овощи", 520, "foodly_u_gyros_2", "bread", "chicken", "greens"),
            ),
        ),
        RestaurantSeed(
            id = "chicken",
            name = "Курица",
            subtitle = "Жареная курица, крылышки, chicken box и сеты",
            description = "Куриное меню с хрустящей корочкой: жареная курица, крылышки, chicken box и куриные сеты.",
            rating = 4.8,
            deliveryTime = "18-26 мин",
            deliveryFee = "Бесплатно",
            accent = Rose,
            emoji = "🍗",
            tags = listOf("Крылышки", "Chicken box", "Куриные сеты"),
            location = GeoPoint(41.314100, 69.300500),
            dishes = listOf(
                dish("fried_chicken", "Жареная курица", "Хрустящая курица с соусом", 620, "foodly_u_fried_chicken_1", "chicken", "crumbs", "sauce"),
                dish("fried_chicken", "Стрипсы", "Куриное филе в хрустящей панировке", 520, "foodly_u_fried_chicken_2", "chicken", "crumbs", "sauce"),
                dish("chicken_wings", "Острые крылышки", "Крылышки buffalo и ranch", 560, "foodly_u_wings_1", "chicken", "pepper", "sauce"),
                dish("chicken_wings", "Медовые крылышки", "Крылышки honey BBQ", 590, "foodly_u_wings_2", "chicken", "honey", "bbq"),
                dish("chicken_wings", "Крылышки classic", "Куриные крылышки с соусом", 540, "foodly_u_wings_3", "chicken", "sauce"),
                dish("chicken_box", "Chicken box classic", "Курица, фри и соусы", 860, "foodly_u_chicken_box_1", "chicken", "potato", "sauce"),
                dish("chicken_box", "Chicken box spicy", "Острая курица, фри и ranch", 910, "foodly_u_chicken_box_2", "chicken", "pepper", "potato"),
                dish("chicken_sets", "Куриный сет", "Крылышки, стрипсы и фри", 980, "foodly_u_chicken_set_1", "chicken", "potato", "sauce"),
                dish("chicken_sets", "Сет на двоих", "Курица, крылышки и напитки", 1420, "foodly_u_chicken_set_2", "chicken", "drink", "sauce"),
            ),
        ),
        RestaurantSeed(
            id = "healthy_salads",
            name = "Салаты и здоровая еда",
            subtitle = "Цезарь, греческий салат, боулы, фитнес и vegetarian",
            description = "Свежие салаты и легкие блюда: Цезарь, греческий салат, боулы, фитнес-блюда и вегетарианская еда.",
            rating = 4.8,
            deliveryTime = "15-24 мин",
            deliveryFee = "Бесплатно",
            accent = Success,
            emoji = "🥗",
            tags = listOf("Цезарь", "Боулы", "Фитнес"),
            location = GeoPoint(41.322500, 69.217900),
            dishes = listOf(
                dish("caesar_salad", "Цезарь с курицей", "Курица, romaine, parmesan и соус", 520, "foodly_u_caesar_1", "chicken", "greens", "cheese", "sauce"),
                dish("caesar_salad", "Цезарь с креветкой", "Креветки, листья салата и parmesan", 620, "foodly_u_caesar_2", "shrimp", "greens", "cheese"),
                dish("greek_salad", "Греческий салат", "Овощи, feta и оливки", 460, "foodly_u_greek_1", "tomato", "cucumber", "cheese", "greens"),
                dish("greek_salad", "Greek fresh", "Огурец, томаты, сыр и зелень", 430, "foodly_u_greek_2", "tomato", "cucumber", "cheese"),
                dish("bowls", "Боул с киноа", "Киноа, овощи и avocado", 560, "foodly_u_bowl_1", "rice", "avocado", "greens"),
                dish("bowls", "Chicken bowl", "Курица, рис и овощи", 620, "foodly_u_bowl_2", "chicken", "rice", "greens"),
                dish("bowls", "Salmon bowl", "Рис, лосось и avocado", 760, "foodly_u_bowl_3", "salmon", "rice", "avocado"),
                dish("fitness_food", "Фитнес-блюдо", "Курица, крупа и овощи", 620, "foodly_u_fitness_1", "chicken", "rice", "greens"),
                dish("fitness_food", "Белковый боул", "Яйцо, курица и овощи", 680, "foodly_u_fitness_2", "egg", "chicken", "greens"),
                dish("vegetarian_food", "Овощное блюдо", "Сезонные овощи и зелень", 430, "foodly_u_vegetarian_1", "greens", "tomato", "pepper"),
                dish("vegetarian_food", "Вегетарианская тарелка", "Овощи, соус и травы", 470, "foodly_u_vegetarian_2", "greens", "sauce", "herbs"),
                dish("vegetarian_food", "Vegetarian wok", "Лапша и овощи в соусе", 520, "foodly_u_vegetarian_3", "noodles", "pepper", "sauce"),
            ),
        ),
        RestaurantSeed(
            id = "breakfasts",
            name = "Завтраки",
            subtitle = "Омлет, блины, каша, сэндвичи и сырники",
            description = "Завтраки на каждый день: омлеты, блины, каша, сэндвичи и сырники.",
            rating = 4.8,
            deliveryTime = "15-23 мин",
            deliveryFee = "5 000 сум",
            accent = Gold,
            emoji = "🍳",
            tags = listOf("Омлет", "Блины", "Сырники"),
            location = GeoPoint(41.307455, 69.279728),
            dishes = listOf(
                dish("omelet", "Омлет с сыром", "Яйца, сыр и зелень", 360, "foodly_u_omelet_1", "egg", "cheese", "greens"),
                dish("omelet", "Омлет с овощами", "Яйца, томаты и перец", 380, "foodly_u_omelet_2", "egg", "tomato", "pepper"),
                dish("pancakes", "Блины с медом", "Тонкие блины с медом", 340, "foodly_u_pancakes_1", "dough", "honey", "berry"),
                dish("pancakes", "Блины с ягодами", "Блины, ягоды и сливочный соус", 390, "foodly_u_pancakes_2", "dough", "berry", "cream"),
                dish("porridge", "Овсяная каша", "Овсянка, ягоды и мед", 310, "foodly_u_porridge_1", "oats", "berry", "honey"),
                dish("porridge", "Каша с бананом", "Овсянка, банан и орехи", 330, "foodly_u_porridge_2", "oats", "banana", "honey"),
                dish("sandwiches", "Сэндвич с курицей", "Тост, курица, сыр и зелень", 420, "foodly_u_sandwich_1", "bread", "chicken", "cheese"),
                dish("sandwiches", "Сэндвич с сыром", "Тост, сыр и томаты", 360, "foodly_u_sandwich_2", "bread", "cheese", "tomato"),
                dish("syrniki", "Сырники classic", "Творожные сырники со сметаной", 390, "foodly_u_syrniki_1", "cheese", "cream", "berry"),
                dish("syrniki", "Сырники berry", "Сырники с ягодным соусом", 430, "foodly_u_syrniki_2", "cheese", "berry", "cream"),
            ),
        ),
        RestaurantSeed(
            id = "soups",
            name = "Супы",
            subtitle = "Куриный, чечевичный, мастава, крем-суп и борщ",
            description = "Горячие супы для спокойного обеда: куриный суп, чечевичный суп, мастава, крем-суп и борщ.",
            rating = 4.6,
            deliveryTime = "19-27 мин",
            deliveryFee = "5 000 сум",
            accent = Sky,
            emoji = "🍲",
            tags = listOf("Куриный суп", "Борщ", "Крем-суп"),
            location = GeoPoint(41.290900, 69.230400),
            dishes = listOf(
                dish("chicken_soup", "Куриный суп", "Курица, лапша и зелень", 390, "foodly_u_chicken_soup_1", "chicken", "noodles", "herbs"),
                dish("chicken_soup", "Куриный суп с рисом", "Курица, рис и овощи", 410, "foodly_u_chicken_soup_2", "chicken", "rice", "carrot"),
                dish("lentil_soup", "Чечевичный суп", "Чечевица, специи и лимон", 360, "foodly_u_lentil_soup_1", "lentil", "lemon", "herbs"),
                dish("lentil_soup", "Чечевичный spicy", "Пряный чечевичный суп", 380, "foodly_u_lentil_soup_2", "lentil", "pepper", "lemon"),
                dish("mastava_soup", "Мастава", "Рис, овощи и мясной бульон", 420, "foodly_u_mastava_1", "rice", "beef", "tomato"),
                dish("cream_soup", "Крем-суп грибной", "Сливочный суп и грибы", 430, "foodly_u_cream_soup_1", "mushroom", "cream", "herbs"),
                dish("cream_soup", "Крем-суп сырный", "Сливочный суп с сыром", 440, "foodly_u_cream_soup_2", "cheese", "cream", "herbs"),
                dish("borscht", "Борщ", "Свекла, говядина и сметана", 430, "foodly_u_borscht_1", "beef", "tomato", "greens"),
                dish("borscht", "Борщ домашний", "Горячий борщ с зеленью", 410, "foodly_u_borscht_2", "tomato", "greens", "cream"),
            ),
        ),
        RestaurantSeed(
            id = "desserts_bakery",
            name = "Десерты и выпечка",
            subtitle = "Торты, чизкейк, донаты, мороженое, самса и круассаны",
            description = "Сладкие десерты и свежая выпечка: торты, чизкейк, донаты, мороженое, самса, круассаны и булочки.",
            rating = 4.8,
            deliveryTime = "16-24 мин",
            deliveryFee = "6 000 сум",
            accent = Rose,
            emoji = "🍰",
            tags = listOf("Чизкейк", "Донаты", "Круассаны"),
            location = GeoPoint(41.333300, 69.290100),
            dishes = listOf(
                dish("cakes", "Шоколадный торт", "Бисквит и шоколадный крем", 520, "foodly_u_cake_1", "chocolate", "cream", "sugar"),
                dish("cakes", "Фруктовый торт", "Бисквит, крем и ягоды", 540, "foodly_u_cake_2", "cream", "berry", "sugar"),
                dish("cheesecake", "Чизкейк classic", "Cream cheese и ягодный соус", 490, "foodly_u_cheesecake_1", "cheese", "cream", "berry"),
                dish("cheesecake", "Чизкейк caramel", "Сливочный чизкейк и карамель", 520, "foodly_u_cheesecake_2", "cheese", "cream", "sugar"),
                dish("donuts", "Донаты", "Глазурь, мягкое тесто и начинка", 330, "foodly_u_donuts_1", "dough", "sugar", "cream"),
                dish("donuts", "Donut box", "Ассорти донатов", 680, "foodly_u_donuts_2", "dough", "sugar", "cream"),
                dish("icecream", "Мороженое", "Пломбир с ягодами", 310, "foodly_u_icecream_1", "cream", "berry", "sugar"),
                dish("icecream", "Ice cream cup", "Мороженое с сиропом", 330, "foodly_u_icecream_2", "cream", "sugar"),
                dish("bakery_samsa", "Самса", "Слоеная самса с мясом", 180, "foodly_u_samsa_1", "dough", "beef", "onion"),
                dish("croissants", "Круассан classic", "Слоеное тесто и сливочное масло", 280, "foodly_u_croissant_1", "dough", "cream"),
                dish("croissants", "Круассан chocolate", "Слоеный круассан с шоколадом", 320, "foodly_u_croissant_2", "dough", "chocolate"),
                dish("buns", "Булочки", "Мягкие булочки из печи", 140, "foodly_u_buns_1", "dough", "sugar"),
                dish("buns", "Булочка cinnamon", "Булочка с корицей", 220, "foodly_u_buns_2", "dough", "sugar"),
            ),
        ),
        RestaurantSeed(
            id = "drinks",
            name = "Напитки",
            subtitle = "Газировка, соки, вода, лимонады, чай, кофе и смузи",
            description = "Холодные и горячие напитки: газировка, соки, вода, лимонады, чай, кофе, молочные коктейли и смузи.",
            rating = 4.6,
            deliveryTime = "10-18 мин",
            deliveryFee = "Бесплатно",
            accent = Sky,
            emoji = "🥤",
            tags = listOf("Лимонады", "Кофе", "Смузи"),
            location = GeoPoint(41.286200, 69.264800),
            dishes = listOf(
                dish("soda", "Газировка cola", "Холодная газировка 0.5 л", 180, "foodly_u_soda_1", "soda", "ice"),
                dish("soda", "Газировка lemon", "Освежающая газировка", 180, "foodly_u_soda_2", "soda", "lemon", "ice"),
                dish("juices", "Апельсиновый сок", "Свежий апельсиновый сок", 260, "foodly_u_juice_1", "orange", "ice"),
                dish("juices", "Яблочный сок", "Холодный яблочный сок", 220, "foodly_u_juice_2", "apple", "ice"),
                dish("water", "Вода", "Негазированная вода", 90, "foodly_u_water_1", "water"),
                dish("lemonades", "Лимонад classic", "Лимон, мята и лед", 240, "foodly_u_lemonade_1", "lemon", "mint", "ice"),
                dish("lemonades", "Ягодный лимонад", "Ягоды, лимон и лед", 280, "foodly_u_lemonade_2", "berry", "lemon", "ice"),
                dish("tea", "Чай", "Горячий чай с лимоном", 120, "foodly_u_tea_1", "tea", "lemon"),
                dish("coffee", "Кофе latte", "Кофе с молоком", 220, "foodly_u_coffee_1", "coffee", "milk"),
                dish("coffee", "Кофе cappuccino", "Кофе и молочная пена", 240, "foodly_u_coffee_2", "coffee", "milk"),
                dish("milkshakes", "Молочный коктейль vanilla", "Молоко, мороженое и сироп", 320, "foodly_u_milkshake_1", "milk", "cream", "sugar"),
                dish("milkshakes", "Молочный коктейль berry", "Молоко, ягоды и мороженое", 340, "foodly_u_milkshake_2", "milk", "berry", "cream"),
                dish("smoothies", "Манго смузи", "Манго, банан и лед", 340, "foodly_u_smoothie_1", "mango", "banana", "ice"),
                dish("smoothies", "Green smoothie", "Зелень, банан и йогурт", 350, "foodly_u_smoothie_2", "greens", "banana", "milk"),
            ),
        ),
    )

    private val restaurantVisuals = mapOf(
        "burger_bistro" to visuals(
            "restaurant_urban_burger",
            "restaurant_burger",
            "burger_classic_cutout",
            "burger_smoky_double_cutout",
            "burger_crispy_chicken_cutout",
        ),
        "fast_food" to visuals(
            "home_snack",
            "restaurant_pizza_republic",
            "restaurant_pizza",
            "home_fries",
            "home_hotdog",
            "home_pizza",
        ),
        "national_food" to visuals("restaurant_milliy"),
        "asian_food" to visuals(
            "restaurant_sakura_box",
            "restaurant_sakura",
        ),
        "shawarma_doner" to visuals(
            "restaurant_shawarma_house",
            "restaurant_shawarma",
            "home_shawarma",
        ),
        "chicken" to visuals(
            "restaurant_crispy_wings",
            "restaurant_wings",
            "home_wings",
        ),
        "healthy_salads" to visuals(
            "restaurant_green_bowl",
            "restaurant_green",
        ),
        "desserts_bakery" to visuals("home_dessert"),
    )

    val categories = catalog.map { seed ->
        AppCategory(seed.id, seed.name, seed.emoji, seed.accent)
    }

    val restaurants = catalog.map { seed ->
        val restaurantVisual = restaurantVisuals[seed.id]
        val galleryImages = buildList {
            restaurantVisual?.let { visuals ->
                add(drawableUri(visuals.coverImage))
                addAll(visuals.galleryImages.map(::drawableUri))
            }
            addAll(seed.dishes.map { dish -> drawableUri(dish.image) })
        }
            .distinct()
            .take(8)
        Restaurant(
            id = seed.id,
            name = seed.name,
            subtitle = seed.subtitle,
            description = seed.description,
            rating = seed.rating,
            deliveryTime = seed.deliveryTime,
            deliveryFee = seed.deliveryFee,
            accent = seed.accent,
            emoji = seed.emoji,
            tags = seed.tags,
            location = seed.location,
            menu = seed.dishes.mapIndexed { index, dish ->
                MenuItem(
                    id = "${seed.id}_${dish.category}_${index + 1}",
                    restaurantId = seed.id,
                    title = dish.title,
                    subtitle = dish.subtitle,
                    price = dish.price,
                    emoji = seed.emoji,
                    accent = seed.accent,
                    category = dish.category,
                    imageUrl = drawableUri(dish.image),
                    ingredients = dish.ingredients,
                    details = "${dish.title}: ${dish.subtitle}. Готовим свежее блюдо для доставки Foodly и аккуратно упаковываем перед отправкой.",
                )
            },
            imageUrl = galleryImages.firstOrNull() ?: drawableUri(seed.dishes.first().image),
            imageUrls = galleryImages,
        )
    }

    val promos = listOf(
        PromoOffer(
            title = "Скидка 25% на первый заказ",
            description = "Активируй купон `FOODLY25` и получи яркий старт в приложении.",
            badge = "Сегодня",
            accent = Orange,
        ),
        PromoOffer(
            title = "Бесплатная доставка после 20:00",
            description = "Вечерние заказы по городу без доплаты за courier delivery.",
            badge = "Выгодно",
            accent = Sky,
        ),
        PromoOffer(
            title = "2 десерта по цене 1",
            description = "Забери сладкое к заказу и закрой день красиво.",
            badge = "Sweet deal",
            accent = Rose,
        ),
    )

    private val mapAreas = listOf(
        MapArea("Дом", "Tashkent City, блок B", "Улица Ислама Каримова", GeoPoint(41.316807, 69.248598)),
        MapArea("Офис", "Ц-1, Мирабад", "Улица Амира Темура", GeoPoint(41.307455, 69.279728)),
        MapArea("Студия", "Юнусабад, 8 квартал", "Улица Шахрисабз", GeoPoint(41.360653, 69.287608)),
        MapArea("Гости", "Чиланзар, 19 квартал", "Улица Бунёдкор", GeoPoint(41.275242, 69.200478)),
        MapArea("Встреча", "ТТЗ, Tech Park", "Улица Махтумкули", GeoPoint(41.350967, 69.363640)),
    )

    val defaultAddress: DeliveryAddress = addressFromPoint(mapAreas.first().point)

    val initialOngoingOrders = emptyList<OrderSummary>()

    val initialHistoryOrders = emptyList<OrderSummary>()

    fun addressFromPoint(point: GeoPoint): DeliveryAddress {
        val nearest = nearestMapArea(point)
        return DeliveryAddress(
            label = nearest.label,
            title = nearest.subtitle,
            subtitle = "Точка: %.5f, %.5f".format(point.latitude, point.longitude),
            point = point,
        )
    }

    fun streetNameForPoint(point: GeoPoint): String =
        streetNameForPoint(point, AppLanguage.Russian)

    fun streetNameForPoint(point: GeoPoint, language: AppLanguage): String =
        nearestMapArea(point).streetName(language)

    private fun nearestMapArea(point: GeoPoint): MapArea =
        mapAreas.minByOrNull { area ->
            val latDelta = area.point.latitude - point.latitude
            val lonDelta = area.point.longitude - point.longitude
            latDelta * latDelta + lonDelta * lonDelta
        } ?: mapAreas.first()

    private data class MapArea(
        val label: String,
        val subtitle: String,
        val street: String,
        val point: GeoPoint,
    ) {
        fun streetName(language: AppLanguage): String = when (street) {
            "Улица Ислама Каримова" -> when (language) {
                AppLanguage.English -> "Islam Karimov Street"
                AppLanguage.Russian -> street
                AppLanguage.Uzbek -> "Islom Karimov ko'chasi"
            }
            "Улица Амира Темура" -> when (language) {
                AppLanguage.English -> "Amir Temur Street"
                AppLanguage.Russian -> street
                AppLanguage.Uzbek -> "Amir Temur ko'chasi"
            }
            "Улица Шахрисабз" -> when (language) {
                AppLanguage.English -> "Shakhrisabz Street"
                AppLanguage.Russian -> street
                AppLanguage.Uzbek -> "Shahrisabz ko'chasi"
            }
            "Улица Бунёдкор" -> when (language) {
                AppLanguage.English -> "Bunyodkor Street"
                AppLanguage.Russian -> street
                AppLanguage.Uzbek -> "Bunyodkor ko'chasi"
            }
            "Улица Махтумкули" -> when (language) {
                AppLanguage.English -> "Makhtumkuli Street"
                AppLanguage.Russian -> street
                AppLanguage.Uzbek -> "Maxtumquli ko'chasi"
            }
            else -> street
        }
    }
}
