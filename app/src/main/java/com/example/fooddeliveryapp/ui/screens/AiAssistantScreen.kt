package com.example.fooddeliveryapp.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.HeadsetMic
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.fooddeliveryapp.ui.AppLanguage
import com.example.fooddeliveryapp.ui.FoodAppState
import com.example.fooddeliveryapp.ui.LocalAppLanguage
import com.example.fooddeliveryapp.ui.data.MenuItem
import com.example.fooddeliveryapp.ui.data.Restaurant
import com.example.fooddeliveryapp.ui.formatCurrency
import com.example.fooddeliveryapp.ui.theme.CardWhite
import com.example.fooddeliveryapp.ui.theme.Gold
import com.example.fooddeliveryapp.ui.theme.Orange
import com.example.fooddeliveryapp.ui.theme.Rose
import com.example.fooddeliveryapp.ui.theme.Sky
import com.example.fooddeliveryapp.ui.theme.Success
import java.util.Locale

@Composable
fun AiAssistantScreen(
    appState: FoodAppState,
    onBack: () -> Unit,
    onOpenCart: () -> Unit,
) {

    val language = LocalAppLanguage.current
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val messages = remember(language) {
        mutableStateListOf(
            AiAssistantMessage(
                id = "welcome",
                fromUser = false,
                text = language.aiWelcomeMessage(),
            ),
        )
    }
    var draft by rememberSaveable { mutableStateOf("") }
    var voiceError by rememberSaveable { mutableStateOf<String?>(null) }
    var messageSeed by rememberSaveable { mutableStateOf(0) }
    var pendingSpeechIntent by remember { mutableStateOf<Intent?>(null) }
    var pendingSelection by remember { mutableStateOf<AiPendingSelection?>(null) }

    LaunchedEffect(language) {
        pendingSelection = null
        pendingSpeechIntent = null
        voiceError = null
    }

    fun nextMessageId(): String {
        messageSeed += 1
        return "ai_message_$messageSeed"
    }

    fun submitPrompt(rawPrompt: String) {
        val prompt = rawPrompt.trim()
        if (prompt.isBlank()) return

        messages.add(AiAssistantMessage(nextMessageId(), fromUser = true, text = prompt))
        val reply = appState.handleAiPrompt(prompt, language, pendingSelection)
        pendingSelection = reply.pendingSelection
        messages.add(AiAssistantMessage(nextMessageId(), fromUser = false, text = reply.text))
        draft = ""
        voiceError = null
    }

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val spokenText = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
            ?.trim()
        if (spokenText.isNullOrBlank()) {
            voiceError = language.aiVoiceNotRecognized()
        } else {
            submitPrompt(spokenText)
        }
    }

    val microphonePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val intent = pendingSpeechIntent
        pendingSpeechIntent = null
        if (granted && intent != null) {
            speechLauncher.launch(intent)
        } else {
            voiceError = language.aiVoicePermissionDenied()
        }
    }

    fun startVoiceInput() {
        voiceError = null
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.aiSpeechLocale())
            putExtra(RecognizerIntent.EXTRA_PROMPT, language.aiSpeechPrompt())
        }
        if (intent.resolveActivity(context.packageManager) == null) {
            voiceError = language.aiVoiceUnavailable()
            return
        }
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            speechLauncher.launch(intent)
        } else {
            pendingSpeechIntent = intent
            microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AiTopBar(
            cartCount = appState.cartCount,
            onBack = onBack,
            onOpenCart = onOpenCart,
            language = language,
        )
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(language.aiSuggestions(), key = { it }) { suggestion ->
                        AiSuggestionChip(
                            text = suggestion,
                            onClick = { submitPrompt(suggestion) },
                        )
                    }
                }
            }
            items(messages, key = { it.id }) { message ->
                AiMessageBubble(message = message)
            }
        }
        voiceError?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = Rose,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
        if (appState.cartCount > 0) {
            Button(
                onClick = onOpenCart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Success, contentColor = CardWhite),
            ) {
                Icon(Icons.Outlined.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(language.aiOpenCart(appState.cartCount))
            }
        }
        AiInputBar(
            value = draft,
            onValueChange = { draft = it },
            onSend = { submitPrompt(draft) },
            onVoice = ::startVoiceInput,
            language = language,
        )
    }
}

@Composable
private fun AiTopBar(
    cartCount: Int,
    onBack: () -> Unit,
    onOpenCart: () -> Unit,
    language: AppLanguage,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Foodly AI",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = language.aiStatusLine(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (cartCount > 0) {
            IconButton(
                onClick = onOpenCart,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Orange.copy(alpha = 0.12f)),
            ) {
                Icon(Icons.Outlined.ShoppingBag, contentDescription = null, tint = Orange)
            }
        }
    }
}

@Composable
private fun AiMessageBubble(message: AiAssistantMessage) {
    val bubbleColor = if (message.fromUser) Orange else MaterialTheme.colorScheme.surface
    val textColor = if (message.fromUser) CardWhite else MaterialTheme.colorScheme.onSurface
    val alignment = if (message.fromUser) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.86f),
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (message.fromUser) 14.dp else 4.dp,
                bottomEnd = if (message.fromUser) 4.dp else 14.dp,
            ),
            color = bubbleColor,
            tonalElevation = if (message.fromUser) 0.dp else 2.dp,
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
            )
        }
    }
}

@Composable
private fun AiSuggestionChip(
    text: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Sky.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

@Composable
private fun AiInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onVoice: () -> Unit,
    language: AppLanguage,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(language.aiInputPlaceholder()) },
            maxLines = 4,
            shape = RoundedCornerShape(10.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
        )
        IconButton(
            onClick = onVoice,
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Gold.copy(alpha = 0.18f)),
        ) {
            Icon(Icons.Outlined.HeadsetMic, contentDescription = null, tint = Gold)
        }
        IconButton(
            onClick = onSend,
            enabled = value.isNotBlank(),
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (value.isBlank()) MaterialTheme.colorScheme.surfaceVariant else Orange),
        ) {
            Icon(
                Icons.Default.Send,
                contentDescription = null,
                tint = if (value.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else CardWhite,
            )
        }
    }
}

private data class AiAssistantMessage(
    val id: String,
    val fromUser: Boolean,
    val text: String,
)

private data class AiAssistantReply(
    val text: String,
    val pendingSelection: AiPendingSelection? = null,
)

private data class AiPendingSelection(
    val options: List<MenuItem>,
    val mode: AiPendingMode,
)

private enum class AiPendingMode {
    Add,
    Details,
    Remove,
}

private data class AiMenuCategory(
    val id: String,
    val menuCategories: Set<String>,
    val tokens: List<String>,
)

private val AiMenuCategories = listOf(
    AiMenuCategory("fast_food", setOf("fast_burgers", "fast_pizza", "fast_fries", "fast_hotdogs", "fast_nuggets", "fast_combo"), listOf("fast food", "burger", "burgers", "hamburger", "hot dog", "fries", "nuggets", "combo", "фастфуд", "бургер", "бургеры", "хот-дог", "фри", "наггет", "комбо")),
    AiMenuCategory("national_food", setOf("national_plov", "national_samsa", "national_manti", "national_lagman", "national_shashlik", "national_kazan_kebab"), listOf("plov", "samsa", "manti", "lagman", "shashlik", "kazan", "плов", "самса", "манты", "лагман", "шашлык", "казан")),
    AiMenuCategory("asian_food", setOf("asian_sushi", "asian_rolls", "asian_wok", "asian_ramen", "asian_noodles", "asian_chicken_rice"), listOf("asian", "sushi", "roll", "rolls", "wok", "ramen", "noodles", "rice with chicken", "азиат", "суши", "ролл", "роллы", "вок", "рамен", "лапша", "рис с курицей")),
    AiMenuCategory("shawarma_doner", setOf("shawarma", "doner", "lavash", "gyros"), listOf("shawarma", "doner", "lavash", "gyros", "шаурм", "донер", "лаваш", "гирос")),
    AiMenuCategory("chicken", setOf("fried_chicken", "chicken_wings", "chicken_box", "chicken_sets"), listOf("chicken", "wings", "fried chicken", "chicken box", "куриц", "крыл", "жареная курица", "куриные сеты")),
    AiMenuCategory("healthy_salads", setOf("caesar_salad", "greek_salad", "bowls", "fitness_food", "vegetarian_food"), listOf("healthy", "salad", "salads", "caesar", "greek", "bowl", "fitness", "vegetarian", "салат", "салаты", "цезарь", "греческий", "боул", "фитнес", "вегетариан")),
    AiMenuCategory("breakfasts", setOf("omelet", "pancakes", "porridge", "sandwiches", "syrniki"), listOf("breakfast", "omelet", "pancake", "porridge", "sandwich", "syrniki", "завтрак", "омлет", "блин", "каша", "сэндвич", "сырники")),
    AiMenuCategory("soups", setOf("chicken_soup", "lentil_soup", "mastava_soup", "cream_soup", "borscht"), listOf("soup", "soups", "chicken soup", "lentil", "mastava", "cream soup", "borscht", "суп", "супы", "куриный суп", "чечевич", "мастава", "крем-суп", "борщ")),
    AiMenuCategory("desserts_bakery", setOf("cakes", "cheesecake", "donuts", "icecream", "bakery_samsa", "croissants", "buns"), listOf("dessert", "desserts", "bakery", "cake", "cheesecake", "donut", "ice cream", "samsa", "croissant", "bun", "десерт", "выпеч", "торт", "чизкейк", "донат", "морожен", "самса", "круассан", "булоч")),
    AiMenuCategory("drinks", setOf("soda", "juices", "water", "lemonades", "tea", "coffee", "milkshakes", "smoothies"), listOf("drink", "drinks", "soda", "juice", "water", "lemonade", "tea", "coffee", "milkshake", "smoothie", "напит", "газиров", "сок", "вода", "лимонад", "чай", "кофе", "коктейль", "смузи")),
)

private val SpecificBurgerCategoryIds = emptySet<String>()

private fun FoodAppState.handleAiPrompt(
    prompt: String,
    language: AppLanguage,
    pendingSelection: AiPendingSelection?,
): AiAssistantReply {
    val cleanPrompt = prompt.cleanForAi()

    if (cleanPrompt.isGreeting() && hasFoodlyIntent(cleanPrompt).not()) {
        return AiAssistantReply(language.aiGreetingReply())
    }

    if (pendingSelection != null) {
        val selectedItems = pendingSelection.selectItems(cleanPrompt)
        if (cleanPrompt.isIngredientQuestion() || cleanPrompt.isDetailsQuestion()) {
            val detailsItems = selectedItems.ifEmpty {
                pendingSelection.options.takeIf { it.size == 1 }.orEmpty()
            }
            return if (detailsItems.isNotEmpty()) {
                AiAssistantReply(
                    text = language.aiItemDetailsReply(detailsItems),
                    pendingSelection = pendingSelection,
                )
            } else {
                AiAssistantReply(
                    text = language.aiClarifyDetailsReply(pendingSelection.options),
                    pendingSelection = pendingSelection,
                )
            }
        }

        if (selectedItems.isNotEmpty()) {
            return when {
                pendingSelection.mode == AiPendingMode.Add || cleanPrompt.hasAddIntent() || cleanPrompt.isPositiveSelection() -> {
                    selectedItems.forEach(::addToCart)
                    AiAssistantReply(language.aiAddedItemsReply(selectedItems, cartCount))
                }
                pendingSelection.mode == AiPendingMode.Remove || cleanPrompt.hasRemoveIntent() -> {
                    selectedItems.forEach { item -> removeFromCart(item.id) }
                    AiAssistantReply(language.aiRemovedItemsReply(selectedItems, cartCount))
                }
                else -> {
                    AiAssistantReply(
                        text = language.aiItemDetailsReply(selectedItems),
                        pendingSelection = pendingSelection.copy(options = selectedItems),
                    )
                }
            }
        }

        if (cleanPrompt.isNegativeSelection()) {
            return AiAssistantReply(language.aiSelectionCancelledReply())
        }

        return AiAssistantReply(
            text = language.aiRepeatSelectionReply(pendingSelection.options, pendingSelection.mode),
            pendingSelection = pendingSelection,
        )
    }

    if (cleanPrompt.isCartQuestion() && cleanPrompt.hasAddIntent().not() && cleanPrompt.hasRemoveIntent().not()) {
        return AiAssistantReply(aiCartSummaryReply(language))
    }

    if (cleanPrompt.isOrderStatusQuestion() && cleanPrompt.hasAddIntent().not() && cleanPrompt.hasRemoveIntent().not()) {
        return AiAssistantReply(aiOrderStatusReply(language))
    }

    if (cleanPrompt.isIngredientQuestion() || cleanPrompt.isDetailsQuestion()) {
        val directMatches = findDirectMenuMatches(cleanPrompt)
        if (directMatches.size == 1) {
            return AiAssistantReply(language.aiItemDetailsReply(directMatches))
        }

        val detailOptions = directMatches.takeIf { it.size > 1 }
            ?: findClarificationOptions(cleanPrompt, maxOptions = 5)
        if (detailOptions.isNotEmpty()) {
            val pending = AiPendingSelection(detailOptions, AiPendingMode.Details)
            return AiAssistantReply(
                text = language.aiAskDetailsChoiceReply(detailOptions),
                pendingSelection = pending,
            )
        }
    }

    if (cleanPrompt.isCookingQuestion()) {
        return AiAssistantReply(language.aiCookingReply(cleanPrompt))
    }

    if (cleanPrompt.isRecommendationQuestion()) {
        val recommendations = recommendMenuItems(cleanPrompt)
        if (recommendations.isNotEmpty()) {
            val pending = AiPendingSelection(recommendations, AiPendingMode.Add)
            return AiAssistantReply(
                text = language.aiRecommendationReply(recommendations),
                pendingSelection = pending,
            )
        }
    }

    answerKnowledgeQuestion(cleanPrompt, language)?.let { answer ->
        return AiAssistantReply(answer)
    }

    if (shouldHandleCartRemoval(cleanPrompt)) {
        if (cartItems.isEmpty()) {
            return AiAssistantReply(language.aiEmptyCartRemoveReply())
        }

        val exactMatches = findCartExactMatches(cleanPrompt)
        if (exactMatches.size == 1) {
            exactMatches.forEach { item -> removeFromCart(item.id) }
            return AiAssistantReply(language.aiRemovedItemsReply(exactMatches, cartCount))
        }

        val directMatches = findCartDirectMatches(cleanPrompt)
        if (directMatches.size == 1) {
            directMatches.forEach { item -> removeFromCart(item.id) }
            return AiAssistantReply(language.aiRemovedItemsReply(directMatches, cartCount))
        }

        val options = findCartRemovalOptions(cleanPrompt, maxOptions = 7)
        if (options.isNotEmpty()) {
            val pending = AiPendingSelection(options, AiPendingMode.Remove)
            return AiAssistantReply(
                text = language.aiAskRemoveChoiceReply(options),
                pendingSelection = pending,
            )
        }
    }

    if (cleanPrompt.hasAddIntent()) {
        val requestedCategories = requestedMenuCategories(cleanPrompt)
        val exactMatches = findExactTitleMatches(cleanPrompt)
        if (exactMatches.isNotEmpty()) {
            exactMatches.forEach(::addToCart)
            return AiAssistantReply(language.aiAddedItemsReply(exactMatches, cartCount))
        }

        val directMatches = findDirectMenuMatches(cleanPrompt)
        if (directMatches.isNotEmpty() && requestedCategories.isEmpty()) {
            directMatches.forEach(::addToCart)
            return AiAssistantReply(language.aiAddedItemsReply(directMatches, cartCount))
        }

        val options = findClarificationOptions(cleanPrompt, maxOptions = 7)
        if (options.size > 1) {
            val pending = AiPendingSelection(options, AiPendingMode.Add)
            return AiAssistantReply(
                text = language.aiAskAddChoiceReply(options, requestedCategories),
                pendingSelection = pending,
            )
        }
        if (options.size == 1) {
            options.forEach(::addToCart)
            return AiAssistantReply(language.aiAddedItemsReply(options, cartCount))
        }
    }

    if (hasFoodlyIntent(cleanPrompt).not()) {
        return AiAssistantReply(language.aiOutOfScopeReply())
    }

    val scopedRestaurants = mentionedRestaurants(cleanPrompt)
    val scopedMenu = if (scopedRestaurants.isNotEmpty()) {
        scopedRestaurants.flatMap { it.menu }
    } else {
        selectedRestaurant.menu
    }
    val recommendations = scopedMenu
        .filterNot { it.isAiDrink() }
        .take(2)
        .joinToString { it.title }
        .ifBlank { selectedRestaurant.menu.filterNot { it.isAiDrink() }.take(2).joinToString { it.title } }
    val restaurantName = scopedRestaurants.firstOrNull()?.name ?: selectedRestaurant.name
    return AiAssistantReply(language.aiGeneralReply(recommendations, restaurantName))
}

private fun FoodAppState.findClarificationOptions(
    cleanPrompt: String,
    maxOptions: Int,
): List<MenuItem> {
    val categories = requestedMenuCategories(cleanPrompt)
    if (categories.isNotEmpty()) {
        return optionsForCategories(categories, cleanPrompt, maxOptions)
    }

    if (cleanPrompt.wantsSomeDishes()) {
        val scopedRestaurants = mentionedRestaurants(cleanPrompt)
        val primaryMenu = if (scopedRestaurants.isNotEmpty()) {
            scopedRestaurants.flatMap { it.menu }
        } else {
            selectedRestaurant.menu
        }
        val currentFood = primaryMenu
            .asSequence()
            .filterNot { it.isAiDrink() }
            .distinctBy { it.category }
            .take(maxOptions - 1)
            .toList()
        val drink = if (cleanPrompt.wantsDrink()) {
            primaryMenu.firstOrNull { it.isAiDrink() }
                ?: restaurants.asSequence().flatMap { it.menu.asSequence() }.firstOrNull { it.isAiDrink() }
        } else {
            null
        }
        return (currentFood + listOfNotNull(drink)).distinctBy { it.id }.take(maxOptions)
    }

    return emptyList()
}

private fun FoodAppState.findCartRemovalOptions(
    cleanPrompt: String,
    maxOptions: Int,
): List<MenuItem> {
    val cartMenu = cartItems.map { it.item }
    val categories = requestedMenuCategories(cleanPrompt)
    if (categories.isNotEmpty()) {
        return cartMenu
            .filter { item -> categories.any { category -> item.category in category.menuCategories } }
            .distinctBy { it.id }
            .take(maxOptions)
    }

    val directMatches = findCartDirectMatches(cleanPrompt)
    if (directMatches.isNotEmpty()) {
        return directMatches.take(maxOptions)
    }

    return cartMenu
        .distinctBy { it.id }
        .take(maxOptions)
}

private fun FoodAppState.recommendMenuItems(cleanPrompt: String): List<MenuItem> {
    val scopedRestaurants = mentionedRestaurants(cleanPrompt)
    val menu = when {
        scopedRestaurants.isNotEmpty() -> scopedRestaurants.flatMap { it.menu }
        selectedRestaurant.menu.isNotEmpty() -> selectedRestaurant.menu
        else -> restaurants.flatMap { it.menu }
    }
    val ranked = menu
        .map { item -> item to item.recommendationScore(cleanPrompt) }
        .filter { (_, score) -> score > 0 }
        .sortedWith(compareByDescending<Pair<MenuItem, Int>> { it.second }.thenBy { it.first.price })
        .map { it.first }
        .distinctBy { it.id }
        .take(5)

    return ranked.ifEmpty {
        menu
            .filterNot { it.isAiDrink() }
            .distinctBy { it.category }
            .take(4)
    }
}

private fun MenuItem.isAiDrink(): Boolean =
    category in setOf("drinks", "soda", "juices", "water", "lemonades", "tea", "coffee", "milkshakes", "smoothies")

private fun MenuItem.recommendationScore(cleanPrompt: String): Int {
    var score = 0
    if (isAiDrink() && cleanPrompt.wantsDrink()) score += 8
    if (category in setOf("caesar_salad", "greek_salad", "bowls", "fitness_food", "vegetarian_food", "soda", "juices", "water", "lemonades", "tea", "coffee", "smoothies") && cleanPrompt.wantsLightFood()) score += 5
    if (category in setOf("fast_burgers", "fast_pizza", "fast_combo", "national_plov", "national_manti", "national_lagman", "national_shashlik", "national_kazan_kebab", "shawarma", "doner", "lavash", "gyros", "fried_chicken", "chicken_wings", "chicken_box", "chicken_sets") && cleanPrompt.wantsFillingFood()) score += 5
    if (category in setOf("fast_pizza", "fast_combo", "asian_rolls", "chicken_box", "chicken_sets", "donuts", "cakes") && cleanPrompt.wantsForCompany()) score += 5
    if (category in setOf("fried_chicken", "chicken_wings", "chicken_box", "chicken_sets", "shawarma", "lavash", "asian_chicken_rice", "fast_burgers", "fast_nuggets") && cleanPrompt.wantsChicken()) score += 5
    if (category in setOf("caesar_salad", "greek_salad", "bowls", "fitness_food", "vegetarian_food") && cleanPrompt.wantsHealthyFood()) score += 8
    if (category in setOf("cakes", "cheesecake", "donuts", "icecream", "croissants", "buns") && cleanPrompt.wantsSweetFood()) score += 8
    if (price <= 250 && cleanPrompt.wantsBudgetFood()) score += 6
    if (ingredients.any { cleanPrompt.contains(it.cleanForAi()) }) score += 4
    if (cleanPrompt.contains(category.cleanForAi())) score += 6
    if (score == 0 && cleanPrompt.isRecommendationQuestion()) score = 1
    return score
}

private fun FoodAppState.optionsForCategories(
    categories: List<AiMenuCategory>,
    cleanPrompt: String,
    maxOptions: Int,
): List<MenuItem> {
    val scopedRestaurants = mentionedRestaurants(cleanPrompt)
    val allItems = if (scopedRestaurants.isNotEmpty()) {
        scopedRestaurants.flatMap { it.menu }
    } else {
        restaurants.flatMap { it.menu }
    }
    val currentMenu = if (scopedRestaurants.isNotEmpty()) allItems else selectedRestaurant.menu
    return categories
        .flatMap { category ->
            val current = currentMenu.filter { it.category in category.menuCategories }
            val other = allItems.filter { item ->
                item.category in category.menuCategories && current.none { it.id == item.id }
            }
            (current + other).take(if (categories.size == 1) maxOptions else 3)
        }
        .distinctBy { it.id }
        .take(maxOptions)
}

private fun FoodAppState.requestedMenuCategories(cleanPrompt: String): List<AiMenuCategory> {
    val matches = AiMenuCategories
        .mapNotNull { category ->
            category.firstMentionIndex(cleanPrompt)?.let { index -> category to index }
        }
        .sortedBy { it.second }
        .map { it.first }
    val specificBurgerMatches = matches.filter { it.id in SpecificBurgerCategoryIds }
    return specificBurgerMatches.ifEmpty { matches }
}

private fun AiMenuCategory.firstMentionIndex(cleanPrompt: String): Int? =
    tokens
        .map { token -> cleanPrompt.indexOf(token.cleanForAi()) }
        .filter { index -> index >= 0 }
        .minOrNull()

private fun FoodAppState.findDirectMenuMatches(cleanPrompt: String): List<MenuItem> =
    searchableMenu(cleanPrompt)
        .mapNotNull { item -> item.directMatchScore(cleanPrompt)?.let { score -> item to score } }
        .sortedByDescending { it.second }
        .map { it.first }
        .distinctBy { it.id }
        .take(3)

private fun FoodAppState.findExactTitleMatches(cleanPrompt: String): List<MenuItem> =
    searchableMenu(cleanPrompt)
        .filter { item ->
            val title = item.title.cleanForAi()
            title.length > 4 && cleanPrompt.contains(title)
        }
        .distinctBy { it.id }
        .take(3)

private fun FoodAppState.findCartDirectMatches(cleanPrompt: String): List<MenuItem> =
    cartItems
        .map { it.item }
        .mapNotNull { item -> item.directMatchScore(cleanPrompt)?.let { score -> item to score } }
        .sortedByDescending { it.second }
        .map { it.first }
        .distinctBy { it.id }
        .take(3)

private fun FoodAppState.findCartExactMatches(cleanPrompt: String): List<MenuItem> =
    cartItems
        .map { it.item }
        .filter { item ->
            val title = item.title.cleanForAi()
            title.length > 4 && cleanPrompt.contains(title)
        }
        .distinctBy { it.id }
        .take(3)

private fun FoodAppState.searchableMenu(cleanPrompt: String): List<MenuItem> {
    val scopedRestaurants = mentionedRestaurants(cleanPrompt)
    return if (scopedRestaurants.isNotEmpty()) {
        scopedRestaurants.flatMap { it.menu }
    } else {
        restaurants.flatMap { it.menu }
    }
}

private fun FoodAppState.mentionedRestaurants(cleanPrompt: String): List<Restaurant> =
    restaurants.filter { restaurant -> restaurant.matchesAiMention(cleanPrompt) }

private fun Restaurant.matchesAiMention(cleanPrompt: String): Boolean {
    val names = listOf(
        name.cleanForAi(),
        id.replace("_", " ").cleanForAi(),
    )
    return names.any { nameToken ->
        nameToken.length > 2 && cleanPrompt.contains(nameToken)
    }
}

private fun FoodAppState.hasFoodlyIntent(cleanPrompt: String): Boolean =
    cleanPrompt.isCartQuestion() ||
        cleanPrompt.isOrderStatusQuestion() ||
        cleanPrompt.isCookingQuestion() ||
        cleanPrompt.isRecommendationQuestion() ||
        cleanPrompt.isIngredientQuestion() ||
        cleanPrompt.isDetailsQuestion() ||
        cleanPrompt.hasAddIntent() ||
        cleanPrompt.hasRemoveIntent() ||
        cleanPrompt.wantsSomeDishes() ||
        requestedMenuCategories(cleanPrompt).isNotEmpty() ||
        mentionedRestaurants(cleanPrompt).isNotEmpty() ||
        findDirectMenuMatches(cleanPrompt).isNotEmpty() ||
        findCartDirectMatches(cleanPrompt).isNotEmpty() ||
        AiKnowledgeBase.any { knowledge -> knowledge.tokens.any { token -> cleanPrompt.contains(token.cleanForAi()) } }

private fun MenuItem.directMatchScore(cleanPrompt: String): Int? {
    val title = title.cleanForAi()
    if (title.isNotBlank() && cleanPrompt.contains(title)) return 10

    val titleWords = title
        .split(" ")
        .filter { it.length > 2 }
    if (titleWords.size < 2) return null

    val matchedTitleWords = titleWords.count { cleanPrompt.contains(it) }
    if (matchedTitleWords >= 2) return 6 + matchedTitleWords

    val ingredientScore = ingredients
        .map { it.cleanForAi() }
        .filter { it.length > 3 }
        .count { cleanPrompt.contains(it) }
    return if (ingredientScore >= 2) ingredientScore else null
}

private fun AiPendingSelection.selectItems(cleanPrompt: String): List<MenuItem> {
    if (cleanPrompt.selectsAllOptions()) return options

    val selectedByIndex = cleanPrompt.selectionIndexes(options.size)
        .mapNotNull { index -> options.getOrNull(index) }
    val selectedByName = options
        .mapNotNull { item -> item.selectionMatchScore(cleanPrompt)?.let { score -> item to score } }
        .sortedByDescending { it.second }
        .map { it.first }

    return (selectedByIndex + selectedByName)
        .distinctBy { it.id }
        .take(4)
}

private fun MenuItem.selectionMatchScore(cleanPrompt: String): Int? {
    val title = title.cleanForAi()
    if (title.isNotBlank() && cleanPrompt.contains(title)) return 20
    val matchedWords = title
        .split(" ")
        .filter { it.length > 2 }
        .count { cleanPrompt.contains(it) }
    return when {
        matchedWords >= 2 -> 12 + matchedWords
        matchedWords == 1 && title.split(" ").any { it.length >= 5 && cleanPrompt.contains(it) } -> 8
        else -> null
    }
}

private fun String.hasAddIntent(): Boolean =
    listOf(
        "add",
        "order",
        "pick",
        "choose",
        "grab",
        "get",
        "bring",
        "give",
        "put",
        "want",
        "send",
        "show",
        "добав",
        "дай",
        "дайте",
        "возьми",
        "выбер",
        "покаж",
        "подбер",
        "полож",
        "закаж",
        "куп",
        "хочу",
        "хотел",
        "можно",
        "qosh",
        "qo sh",
        "buyurt",
        "olib ber",
        "tanla",
    ).any { contains(it) }

private fun String.hasRemoveIntent(): Boolean =
    listOf(
        "delete",
        "remove",
        "clear",
        "удал",
        "убери",
        "очист",
        "ochir",
        "o'chir",
        "olib tashla",
    ).any { contains(it) }

private fun FoodAppState.shouldHandleCartRemoval(cleanPrompt: String): Boolean =
    cleanPrompt.hasRemoveIntent() &&
        (
            cleanPrompt.isGenericRemoveCommand() ||
                cleanPrompt.isCartQuestion() ||
                requestedMenuCategories(cleanPrompt).isNotEmpty() ||
                findCartExactMatches(cleanPrompt).isNotEmpty() ||
                findCartDirectMatches(cleanPrompt).isNotEmpty()
        )

private fun String.isGenericRemoveCommand(): Boolean =
    hasRemoveIntent() && split(" ").count { it.isNotBlank() } == 1

private fun String.isPositiveSelection(): Boolean =
    listOf("да", "ага", "ок", "okay", "yes", "добавь", "выбираю", "беру", "tanla", "ha")
        .any { contains(it) }

private fun String.isNegativeSelection(): Boolean =
    listOf("нет", "отмена", "не надо", "cancel", "no", "yoq", "yo'q")
        .any { contains(it) }

private fun String.isGreeting(): Boolean {
    val greetings = listOf("привет", "здравствуй", "здравствуйте", "салом", "salom", "hello", "hi", "hey")
    val words = split(" ").filter { it.isNotBlank() }
    return words.size <= 4 && greetings.any { greeting -> words.any { it == greeting } }
}

private fun String.isCartQuestion(): Boolean =
    listOf("cart", "basket", "корзин", "savat")
        .any { contains(it) }

private fun String.isOrderStatusQuestion(): Boolean {
    val orderTokens = listOf("order", "заказ", "buyurt")
    val statusTokens = listOf("status", "статус", "track", "отслед", "где", "eta", "курьер", "достав", "yetkaz")
    return orderTokens.any { contains(it) } && statusTokens.any { contains(it) }
}

private fun String.wantsDrink(): Boolean =
    AiMenuCategories.first { it.id == "drinks" }
        .tokens
        .any { token -> contains(token.cleanForAi()) }

private fun String.wantsSomeDishes(): Boolean =
    listOf("food", "meal", "dish", "блюд", "еду", "еда", "что нибудь", "something", "taom", "ovqat")
        .any { contains(it) }

private fun String.isCookingQuestion(): Boolean =
    listOf("cook", "recipe", "готов", "рецеп", "приготов", "как сделать", "pishir", "tayyor")
        .any { contains(it) }

private fun String.isRecommendationQuestion(): Boolean =
    listOf("recommend", "suggest", "совет", "посовет", "что взять", "что выбрать", "что заказать", "что поесть", "tavsiya", "nima yey")
        .any { contains(it) }

private fun String.isIngredientQuestion(): Boolean =
    listOf("ingredient", "ingredients", "состав", "ингреди", "из чего", "что внутри", "tarkib")
        .any { contains(it) }

private fun String.isDetailsQuestion(): Boolean =
    listOf("detail", "details", "подроб", "детал", "опис", "как сделано", "как готов", "tell me about", "malumot")
        .any { contains(it) }

private fun String.selectsAllOptions(): Boolean =
    listOf("все", "оба", "all", "hammasi", "barchasi")
        .any { contains(it) }

private fun String.wantsLightFood(): Boolean =
    listOf("light", "легк", "легкое", "не тяжел", "oson", "yengil").any { contains(it) }

private fun String.wantsFillingFood(): Boolean =
    listOf("сыт", "голод", "filling", "hungry", "och", "to'yimli").any { contains(it) }

private fun String.wantsForCompany(): Boolean =
    listOf("company", "friends", "party", "компан", "друз", "вечерин", "двоих", "семь", "mehmon", "dost").any { contains(it) }

private fun String.wantsChicken(): Boolean =
    listOf("chicken", "куриц", "tovuq").any { contains(it) }

private fun String.wantsHealthyFood(): Boolean =
    listOf("healthy", "полез", "диет", "salad", "салат", "avocado", "soglom", "foydali").any { contains(it) }

private fun String.wantsSweetFood(): Boolean =
    listOf("sweet", "dessert", "слад", "десерт", "shirin").any { contains(it) }

private fun String.wantsBudgetFood(): Boolean =
    listOf("cheap", "budget", "дешев", "недорог", "бюджет", "arzon").any { contains(it) }

private fun String.selectionIndexes(maxSize: Int): List<Int> {
    val indexes = mutableSetOf<Int>()
    Regex("""\b\d+\b""")
        .findAll(this)
        .mapNotNull { it.value.toIntOrNull()?.minus(1) }
        .filter { it in 0 until maxSize }
        .forEach(indexes::add)

    val words = listOf(
        0 to listOf("перв", "first", "one", "birinchi"),
        1 to listOf("втор", "second", "two", "ikkinchi"),
        2 to listOf("трет", "third", "three", "uchinchi"),
        3 to listOf("четвер", "fourth", "four", "tortinchi", "to'rtinchi"),
        4 to listOf("пят", "fifth", "five", "beshinchi"),
        5 to listOf("шест", "sixth", "six", "oltinchi"),
        6 to listOf("седьм", "seventh", "seven", "yettinchi"),
    )
    words
        .filter { (index, tokens) -> index < maxSize && tokens.any { token -> contains(token) } }
        .forEach { (index, _) -> indexes.add(index) }

    if (contains("последн") || contains("last") || contains("oxirgi")) {
        indexes.add(maxSize - 1)
    }
    return indexes.sorted()
}

private fun String.cleanForAi(): String =
    lowercase(Locale.ROOT)
        .replace('ё', 'е')
        .replace(Regex("[^\\p{L}\\p{N}\\s]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()

private fun AppLanguage.aiWelcomeMessage(): String = when (this) {
    AppLanguage.English -> "Hi, welcome to Foodly. I am Foodly AI: I help only with food, orders, the cart, menu ingredients and cooking."
    AppLanguage.Russian -> "Привет, добро пожаловать в Foodly. Я Foodly AI: помогаю только с едой, заказами, корзиной, составом блюд и готовкой."
    AppLanguage.Uzbek -> "Salom, Foodly'ga xush kelibsiz. Men Foodly AI: faqat taom, buyurtma, savat, menyu tarkibi va pishirish bo'yicha yordam beraman."
}

private fun AppLanguage.aiStatusLine(): String = when (this) {
    AppLanguage.English -> "Food, cart and cooking assistant"
    AppLanguage.Russian -> "Помощник по еде, корзине и готовке"
    AppLanguage.Uzbek -> "Taom, savat va pishirish yordamchisi"
}

private fun AppLanguage.aiInputPlaceholder(): String = when (this) {
    AppLanguage.English -> "Ask Foodly AI"
    AppLanguage.Russian -> "Напиши Foodly AI"
    AppLanguage.Uzbek -> "Foodly AI ga yozing"
}

private fun AppLanguage.aiSuggestions(): List<String> = when (this) {
    AppLanguage.English -> listOf(
        "Suggest something filling",
        "Add plov",
        "What sauce fits chicken?",
        "How do I reheat pizza?",
    )
    AppLanguage.Russian -> listOf(
        "Посоветуй что-то сытное",
        "Добавь плов",
        "Какой соус к курице?",
        "Как разогреть пиццу?",
    )
    AppLanguage.Uzbek -> listOf(
        "To'yimli taom tavsiya qil",
        "Osh qo'sh",
        "Tovuqqa qaysi sous?",
        "Pitsani qanday qizdiraman?",
    )
}

private fun AppLanguage.aiOpenCart(count: Int): String = when (this) {
    AppLanguage.English -> "Open cart ($count)"
    AppLanguage.Russian -> "Открыть корзину ($count)"
    AppLanguage.Uzbek -> "Savatni ochish ($count)"
}

private fun AppLanguage.aiSpeechLocale(): String = when (this) {
    AppLanguage.English -> "en-US"
    AppLanguage.Russian -> "ru-RU"
    AppLanguage.Uzbek -> "uz-UZ"
}

private fun AppLanguage.aiSpeechPrompt(): String = when (this) {
    AppLanguage.English -> "Tell Foodly AI what to do"
    AppLanguage.Russian -> "Скажите Foodly AI, что сделать"
    AppLanguage.Uzbek -> "Foodly AI ga nima qilishni ayting"
}

private fun AppLanguage.aiVoiceUnavailable(): String = when (this) {
    AppLanguage.English -> "Voice input is not available on this device."
    AppLanguage.Russian -> "На этом устройстве голосовой ввод недоступен."
    AppLanguage.Uzbek -> "Bu qurilmada ovozli kiritish mavjud emas."
}

private fun AppLanguage.aiVoicePermissionDenied(): String = when (this) {
    AppLanguage.English -> "Microphone permission is needed for voice input."
    AppLanguage.Russian -> "Для голосового ввода нужен доступ к микрофону."
    AppLanguage.Uzbek -> "Ovozli kiritish uchun mikrofon ruxsati kerak."
}

private fun AppLanguage.aiVoiceNotRecognized(): String = when (this) {
    AppLanguage.English -> "I could not recognize the voice command."
    AppLanguage.Russian -> "Не получилось распознать голосовую команду."
    AppLanguage.Uzbek -> "Ovozli buyruqni aniqlab bo'lmadi."
}

private fun AppLanguage.aiGreetingReply(): String = when (this) {
    AppLanguage.English -> "Hi! Welcome to Foodly. I can help with orders, the cart, menu ingredients and cooking."
    AppLanguage.Russian -> "Привет! Добро пожаловать в Foodly. Помогу с заказами, корзиной, составом блюд и готовкой."
    AppLanguage.Uzbek -> "Salom! Foodly'ga xush kelibsiz. Buyurtma, savat, taom tarkibi va pishirish bo'yicha yordam beraman."
}

private fun AppLanguage.aiOutOfScopeReply(): String = when (this) {
    AppLanguage.English -> "I am Foodly AI, so I stay with food, orders, the cart and cooking. Ask me to choose a dish, add it to cart, explain ingredients or give cooking advice."
    AppLanguage.Russian -> "Я Foodly AI, поэтому держусь тем еды, заказов, корзины и готовки. Могу выбрать блюдо, добавить его в корзину, рассказать состав или подсказать по готовке."
    AppLanguage.Uzbek -> "Men Foodly AI man, shuning uchun taom, buyurtma, savat va pishirish mavzularida yordam beraman. Taom tanlash, savatga qo'shish, tarkibini aytish yoki pishirish maslahatini so'rang."
}

private fun FoodAppState.aiCartSummaryReply(language: AppLanguage): String {
    if (cartItems.isEmpty()) {
        return when (language) {
            AppLanguage.English -> "Your cart is empty. Tell me what food you want, and I will suggest options from Foodly."
            AppLanguage.Russian -> "Корзина пока пустая. Напиши, что хочется поесть, и я предложу варианты из Foodly."
            AppLanguage.Uzbek -> "Savat hozircha bo'sh. Qanday taom xohlayotganingizni yozing, Foodly'dan variantlar taklif qilaman."
        }
    }

    val lines = cartItems.joinToString("\n") { line ->
        val lineTotal = formatCurrency(line.item.price * line.quantity, language)
        "${line.quantity}x ${line.item.title} - $lineTotal"
    }
    val totalLabel = formatCurrency(total, language)
    return when (language) {
        AppLanguage.English -> "In your cart:\n$lines\n\nTotal: $totalLabel."
        AppLanguage.Russian -> "В корзине:\n$lines\n\nИтого: $totalLabel."
        AppLanguage.Uzbek -> "Savatda:\n$lines\n\nJami: $totalLabel."
    }
}

private fun FoodAppState.aiOrderStatusReply(language: AppLanguage): String {
    if (ongoingOrders.isEmpty()) {
        return when (language) {
            AppLanguage.English -> "You do not have active orders right now. I can help build a new order."
            AppLanguage.Russian -> "Сейчас активных заказов нет. Могу помочь собрать новый заказ."
            AppLanguage.Uzbek -> "Hozir faol buyurtmalar yo'q. Yangi buyurtma yig'ishga yordam beraman."
        }
    }

    val lines = ongoingOrders.take(3).joinToString("\n") { order ->
        "${order.restaurantName}: ${order.itemsLabel}. ${order.eta}"
    }
    return when (language) {
        AppLanguage.English -> "Active orders:\n$lines"
        AppLanguage.Russian -> "Активные заказы:\n$lines"
        AppLanguage.Uzbek -> "Faol buyurtmalar:\n$lines"
    }
}

private fun AppLanguage.aiAddedItemsReply(
    items: List<MenuItem>,
    cartCount: Int,
): String {
    val names = items.joinToString { it.title }
    return when (this) {
        AppLanguage.English -> "Added to cart: $names. Your cart now has $cartCount items."
        AppLanguage.Russian -> "Добавил в корзину: $names. Сейчас в корзине позиций: $cartCount."
        AppLanguage.Uzbek -> "Savatga qo'shildi: $names. Savatda hozir $cartCount ta mahsulot bor."
    }
}

private fun AppLanguage.aiRemovedItemsReply(
    items: List<MenuItem>,
    cartCount: Int,
): String {
    val names = items.joinToString { it.title }
    return when (this) {
        AppLanguage.English -> "Removed from cart: $names. Your cart now has $cartCount items."
        AppLanguage.Russian -> "Убрал из корзины: $names. Сейчас в корзине позиций: $cartCount."
        AppLanguage.Uzbek -> "Savatdan olib tashlandi: $names. Savatda hozir $cartCount ta mahsulot bor."
    }
}

private fun AppLanguage.aiAskAddChoiceReply(
    options: List<MenuItem>,
    categories: List<AiMenuCategory> = emptyList(),
): String {
    val body = if (categories.size > 1) {
        options.aiGroupedOptionsText(categories, this)
    } else {
        options.aiOptionsText(this)
    }
    return when (this) {
        AppLanguage.English -> "I found a few good matches. Which one should I add? Reply with a number or name.\n\n$body"
        AppLanguage.Russian -> "Нашёл несколько вариантов. Какой именно добавить? Напиши номер или название.\n\n$body"
        AppLanguage.Uzbek -> "Bir nechta variant topdim. Qaysi birini qo'shay? Raqam yoki nomini yozing.\n\n$body"
    }
}

private fun AppLanguage.aiAskRemoveChoiceReply(options: List<MenuItem>): String = when (this) {
    AppLanguage.English -> "Which item should I remove from the cart? Reply with a number or name.\n\n${options.aiOptionsText(this)}"
    AppLanguage.Russian -> "Какое блюдо убрать из корзины? Напиши номер или название.\n\n${options.aiOptionsText(this)}"
    AppLanguage.Uzbek -> "Savatdan qaysi taomni olib tashlay? Raqam yoki nomini yozing.\n\n${options.aiOptionsText(this)}"
}

private fun AppLanguage.aiEmptyCartRemoveReply(): String = when (this) {
    AppLanguage.English -> "Your cart is already empty, so there is nothing to remove."
    AppLanguage.Russian -> "Корзина уже пустая, удалять пока нечего."
    AppLanguage.Uzbek -> "Savat allaqachon bo'sh, olib tashlaydigan narsa yo'q."
}

private fun AppLanguage.aiAskDetailsChoiceReply(options: List<MenuItem>): String = when (this) {
    AppLanguage.English -> "About which item should I tell you more?\n\n${options.aiOptionsText(this)}"
    AppLanguage.Russian -> "Про какое блюдо рассказать подробнее?\n\n${options.aiOptionsText(this)}"
    AppLanguage.Uzbek -> "Qaysi taom haqida batafsil aytay?\n\n${options.aiOptionsText(this)}"
}

private fun AppLanguage.aiRepeatSelectionReply(
    options: List<MenuItem>,
    mode: AiPendingMode,
): String = when (mode) {
    AiPendingMode.Add -> when (this) {
        AppLanguage.English -> "I am still waiting for your choice. Send a number, a name, or ask about ingredients.\n\n${options.aiOptionsText(this)}"
        AppLanguage.Russian -> "Я жду выбор: напиши номер, название или спроси про ингредиенты.\n\n${options.aiOptionsText(this)}"
        AppLanguage.Uzbek -> "Tanlovingizni kutyapman: raqam, nom yoki ingredientlar haqida savol yozing.\n\n${options.aiOptionsText(this)}"
    }
    AiPendingMode.Details -> when (this) {
        AppLanguage.English -> "Choose the item you want details for.\n\n${options.aiOptionsText(this)}"
        AppLanguage.Russian -> "Выбери блюдо, про которое нужны детали.\n\n${options.aiOptionsText(this)}"
        AppLanguage.Uzbek -> "Batafsil ma'lumot kerak bo'lgan taomni tanlang.\n\n${options.aiOptionsText(this)}"
    }
    AiPendingMode.Remove -> when (this) {
        AppLanguage.English -> "Tell me which cart item to remove. Send a number or a name.\n\n${options.aiOptionsText(this)}"
        AppLanguage.Russian -> "Напиши, какое блюдо убрать из корзины. Можно номер или название.\n\n${options.aiOptionsText(this)}"
        AppLanguage.Uzbek -> "Savatdan qaysi taomni olib tashlashni yozing. Raqam yoki nom bo'lishi mumkin.\n\n${options.aiOptionsText(this)}"
    }
}

private fun AppLanguage.aiClarifyDetailsReply(options: List<MenuItem>): String = when (this) {
    AppLanguage.English -> "I can explain ingredients and preparation, but choose one item first.\n\n${options.aiOptionsText(this)}"
    AppLanguage.Russian -> "Могу рассказать состав и как сделано, но сначала выбери одно блюдо.\n\n${options.aiOptionsText(this)}"
    AppLanguage.Uzbek -> "Tarkibi va tayyorlanishini aytaman, lekin avval bitta taomni tanlang.\n\n${options.aiOptionsText(this)}"
}

private fun AppLanguage.aiItemDetailsReply(items: List<MenuItem>): String =
    items.joinToString(separator = "\n\n") { item ->
        val restaurantName = item.restaurantId.aiRestaurantLabel()
        when (this) {
            AppLanguage.English -> "${item.title} from $restaurantName: ${item.subtitle}. Ingredients: ${item.ingredients.aiIngredientText()}. ${item.details.ifBlank { "Prepared fresh by the restaurant." }}"
            AppLanguage.Russian -> "${item.title} из $restaurantName: ${item.subtitle}. Ингредиенты: ${item.ingredients.aiIngredientText()}. ${item.details.ifBlank { "Готовится свежим на кухне ресторана." }}"
            AppLanguage.Uzbek -> "$restaurantName dan ${item.title}: ${item.subtitle}. Tarkibi: ${item.ingredients.aiIngredientText()}. ${item.details.ifBlank { "Restoran oshxonasida yangi tayyorlanadi." }}"
        }
    }

private fun AppLanguage.aiSelectionCancelledReply(): String = when (this) {
    AppLanguage.English -> "Okay, I cancelled that choice. You can ask for another dish or cooking advice."
    AppLanguage.Russian -> "Хорошо, отменил этот выбор. Можешь попросить другое блюдо или спросить про готовку."
    AppLanguage.Uzbek -> "Yaxshi, tanlov bekor qilindi. Boshqa taom yoki pishirish haqida so'rashingiz mumkin."
}

private fun AppLanguage.aiRecommendationReply(items: List<MenuItem>): String = when (this) {
    AppLanguage.English -> "I would pick these from the current menu. Reply with a number/name and I will add it, or ask about ingredients.\n\n${items.aiOptionsText(this)}"
    AppLanguage.Russian -> "Я бы выбрал вот это из текущего меню. Напиши номер/название - добавлю, или спроси про состав.\n\n${items.aiOptionsText(this)}"
    AppLanguage.Uzbek -> "Hozirgi menyudan shularni tanlagan bo'lardim. Raqam/nomini yozing - qo'shaman, yoki tarkibini so'rang.\n\n${items.aiOptionsText(this)}"
}

private fun List<MenuItem>.aiOptionsText(language: AppLanguage): String =
    mapIndexed { index, item -> item.aiOptionLine(index + 1, language) }
        .joinToString("\n")

private fun List<MenuItem>.aiGroupedOptionsText(
    categories: List<AiMenuCategory>,
    language: AppLanguage,
): String {
    var number = 1
    val usedIds = mutableSetOf<String>()
    val sections = categories.mapNotNull { category ->
        val sectionItems = filter { item ->
            item.id !in usedIds && item.category in category.menuCategories
        }
        if (sectionItems.isEmpty()) {
            null
        } else {
            usedIds += sectionItems.map { it.id }
            buildString {
                append(category.aiLabel(language))
                append(":\n")
                append(sectionItems.joinToString("\n") { item ->
                    item.aiOptionLine(number++, language)
                })
            }
        }
    }.toMutableList()

    val remaining = filter { it.id !in usedIds }
    if (remaining.isNotEmpty()) {
        sections += buildString {
            append(
                when (language) {
                    AppLanguage.English -> "Other matches"
                    AppLanguage.Russian -> "Другие варианты"
                    AppLanguage.Uzbek -> "Boshqa variantlar"
                },
            )
            append(":\n")
            append(remaining.joinToString("\n") { item ->
                item.aiOptionLine(number++, language)
            })
        }
    }
    return sections.joinToString("\n\n")
}

private fun AiMenuCategory.aiLabel(language: AppLanguage): String = when (id) {
    "fast_food" -> when (language) {
        AppLanguage.English -> "Fast food"
        AppLanguage.Russian -> "Фастфуд"
        AppLanguage.Uzbek -> "Fastfud"
    }
    "national_food" -> when (language) {
        AppLanguage.English -> "National food"
        AppLanguage.Russian -> "Национальная еда"
        AppLanguage.Uzbek -> "Milliy oshxona"
    }
    "asian_food" -> when (language) {
        AppLanguage.English -> "Asian food"
        AppLanguage.Russian -> "Азиатская еда"
        AppLanguage.Uzbek -> "Osiyo taomlari"
    }
    "shawarma_doner" -> when (language) {
        AppLanguage.English -> "Shawarma and doner"
        AppLanguage.Russian -> "Шаурма и донер"
        AppLanguage.Uzbek -> "Shaurma va doner"
    }
    "chicken" -> when (language) {
        AppLanguage.English -> "Chicken"
        AppLanguage.Russian -> "Курица"
        AppLanguage.Uzbek -> "Tovuq"
    }
    "healthy_salads" -> when (language) {
        AppLanguage.English -> "Salads and healthy food"
        AppLanguage.Russian -> "Салаты и здоровая еда"
        AppLanguage.Uzbek -> "Salatlar va foydali taom"
    }
    "breakfasts" -> when (language) {
        AppLanguage.English -> "Breakfasts"
        AppLanguage.Russian -> "Завтраки"
        AppLanguage.Uzbek -> "Nonushtalar"
    }
    "soups" -> when (language) {
        AppLanguage.English -> "Soups"
        AppLanguage.Russian -> "Супы"
        AppLanguage.Uzbek -> "Sho'rvalar"
    }
    "desserts_bakery" -> when (language) {
        AppLanguage.English -> "Desserts and bakery"
        AppLanguage.Russian -> "Десерты и выпечка"
        AppLanguage.Uzbek -> "Desertlar va pishiriqlar"
    }
    "drinks" -> when (language) {
        AppLanguage.English -> "Drinks"
        AppLanguage.Russian -> "Напитки"
        AppLanguage.Uzbek -> "Ichimliklar"
    }
    else -> id.replace("_", " ").replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase(Locale.ROOT) else char.toString()
    }
}

private fun MenuItem.aiOptionLine(
    number: Int,
    language: AppLanguage,
): String {
    val price = formatCurrency(price, language)
    val ingredients = ingredients.take(4).aiIngredientText()
    val restaurantName = restaurantId.aiRestaurantLabel()
    return when (language) {
        AppLanguage.English -> "$number. $title from $restaurantName - $subtitle, $price. Ingredients: $ingredients"
        AppLanguage.Russian -> "$number. $title из $restaurantName - $subtitle, $price. Состав: $ingredients"
        AppLanguage.Uzbek -> "$number. $restaurantName dan $title - $subtitle, $price. Tarkibi: $ingredients"
    }
}

private fun String.aiRestaurantLabel(): String =
    split("_")
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.ROOT) else char.toString()
            }
        }

private fun List<String>.aiIngredientText(): String =
    if (isEmpty()) {
        "chef sauce"
    } else {
        joinToString(", ")
    }

private fun FoodAppState.answerKnowledgeQuestion(
    cleanPrompt: String,
    language: AppLanguage,
): String? {
    val entry = AiKnowledgeBase.firstOrNull { knowledge ->
        knowledge.tokens.any { token -> cleanPrompt.contains(token.cleanForAi()) }
    } ?: return null

    val currentMenuHint = selectedRestaurant.menu
        .filter { it.category in entry.relatedCategories }
        .take(3)
        .takeIf { it.isNotEmpty() }
        ?.joinToString { it.title }

    return entry.answer(language, currentMenuHint)
}

private data class AiKnowledgeEntry(
    val tokens: List<String>,
    val relatedCategories: Set<String> = emptySet(),
    val english: String,
    val russian: String,
    val uzbek: String,
) {
    fun answer(
        language: AppLanguage,
        currentMenuHint: String?,
    ): String {
        val base = when (language) {
            AppLanguage.English -> english
            AppLanguage.Russian -> russian
            AppLanguage.Uzbek -> uzbek
        }
        if (currentMenuHint.isNullOrBlank()) return base
        return when (language) {
            AppLanguage.English -> "$base\n\nFrom the current menu, I can connect this with: $currentMenuHint."
            AppLanguage.Russian -> "$base\n\nИз текущего меню сюда хорошо подходит: $currentMenuHint."
            AppLanguage.Uzbek -> "$base\n\nHozirgi menyudan bunga mos variantlar: $currentMenuHint."
        }
    }
}

private val AiKnowledgeBase = listOf(
    AiKnowledgeEntry(
        tokens = listOf("medium rare", "прожарк", "стейк", "steak"),
        english = "For steak, dry the meat, salt it before cooking, sear on high heat, then rest it 5-7 minutes. Resting keeps the juice inside.",
        russian = "Для стейка обсуши мясо, посоли перед жаркой, быстро обжарь на сильном огне и дай отдохнуть 5-7 минут. Так сок останется внутри.",
        uzbek = "Steyk uchun go'shtni quriting, pishirishdan oldin tuzlang, baland olovda qovuring va 5-7 daqiqa tindiring. Shunda suvi ichida qoladi.",
    ),
    AiKnowledgeEntry(
        tokens = listOf("соус", "sauce", "чесноч", "garlic", "bbq"),
        relatedCategories = setOf("fast_burgers", "fast_fries", "fast_hotdogs", "fried_chicken", "chicken_wings", "shawarma", "doner", "lavash"),
        english = "A good sauce needs balance: fat for body, acid for brightness, salt for flavor, and a little sweetness or spice. Garlic sauce works with shawarma and fries; BBQ works with beef, wings and smoky burgers.",
        russian = "Хороший соус держится на балансе: жирность дает тело, кислота освежает, соль раскрывает вкус, сладость или острота добавляют характер. Чесночный соус подходит к шаурме и фри, BBQ - к говядине, крылышкам и smoky-бургерам.",
        uzbek = "Yaxshi sous balansdan iborat: yog' to'qlik beradi, nordonlik tetik qiladi, tuz ta'mni ochadi, shirinlik yoki achchiqlik xarakter qo'shadi. Sarimsoqli sous lavash va fri bilan, BBQ esa mol go'shti, qanotchalar va smoky burger bilan yaxshi.",
    ),
    AiKnowledgeEntry(
        tokens = listOf("калори", "calorie", "пп", "диет", "полез", "healthy", "protein", "белок"),
        relatedCategories = setOf("caesar_salad", "greek_salad", "bowls", "fitness_food", "vegetarian_food", "asian_sushi", "asian_chicken_rice"),
        english = "For a lighter order, choose grilled chicken, bowls, sushi-style rice dishes, yogurt desserts or smoothies. Add sauce separately and avoid pairing fries with a sweet drink if you want a lighter meal.",
        russian = "Для более легкого заказа выбирай курицу, боулы, рисовые блюда в стиле суши, йогуртовые десерты или смузи. Соус лучше отдельно, а фри со сладким напитком не сочетать, если хочешь легче.",
        uzbek = "Yengilroq buyurtma uchun tovuq, bowl, guruchli sushi uslubidagi taomlar, yogurt desertlari yoki smoothie tanlang. Sousni alohida oling, yengil ovqat xohlasangiz fri va shirin ichimlikni birga olmaganingiz yaxshi.",
    ),
    AiKnowledgeEntry(
        tokens = listOf("как хран", "storage", "хранить", "разогреть", "reheat", "qizdir"),
        relatedCategories = setOf("fast_pizza", "fast_burgers", "fast_fries", "fried_chicken", "chicken_wings", "croissants", "buns"),
        english = "Pizza reheats best in a dry pan on low heat with a lid for 3-4 minutes. Burgers are better warmed open: patty separately, bun separately. Fries need dry heat, not microwave, or they become soft.",
        russian = "Пиццу лучше разогревать на сухой сковороде под крышкой 3-4 минуты. Бургер лучше греть разобранным: котлету отдельно, булочку отдельно. Фри нужен сухой жар, не микроволновка, иначе станет мягким.",
        uzbek = "Pitsani quruq tovadan past olovda qopqoq bilan 3-4 daqiqa qizdirish yaxshi. Burgerni ajratib qizdiring: kotlet alohida, bulochka alohida. Fri uchun mikroto'lqin emas, quruq issiqlik kerak, aks holda yumshab qoladi.",
    ),
    AiKnowledgeEntry(
        tokens = listOf("замен", "substitute", "без", "allergy", "аллерг", "no onion", "без лука", "piyozsiz"),
        english = "If you need substitutions, choose a dish close to your taste and write the preference in chat/order comment when available: no onion, sauce separately, less spicy, no cheese. For allergies, avoid the dish if the ingredient can be hidden in sauce or breading.",
        russian = "Если нужна замена, выбирай близкое блюдо и указывай пожелание в чате/комментарии к заказу, когда доступно: без лука, соус отдельно, менее остро, без сыра. При аллергии лучше избегать блюда, если ингредиент может быть скрыт в соусе или панировке.",
        uzbek = "Almashtirish kerak bo'lsa, didingizga yaqin taom tanlang va imkon bo'lsa chat/izohda yozing: piyozsiz, sous alohida, kamroq achchiq, pishloqsiz. Allergiya bo'lsa, ingredient sous yoki panirovkada yashirin bo'lishi mumkin bo'lgan taomdan saqlaning.",
    ),
    AiKnowledgeEntry(
        tokens = listOf("остр", "spicy", "achchiq", "перец", "чили"),
        relatedCategories = setOf("fried_chicken", "chicken_wings", "fast_pizza", "asian_wok", "asian_ramen", "shawarma"),
        english = "Spicy food feels better when there is fat or dairy nearby: cheese, mayo, yogurt sauce. If you want heat without heaviness, pick chicken, wings or Asian bowls instead of a double beef burger.",
        russian = "Острое лучше ощущается, когда рядом есть жирность или молочная основа: сыр, майо, йогуртовый соус. Если хочешь остроту без тяжести, бери курицу, крылышки или азиатские боулы вместо двойного говяжьего бургера.",
        uzbek = "Achchiq taom yog'li yoki sutli asos bilan yaxshiroq chiqadi: pishloq, mayo, yogurt sous. Og'ir bo'lmagan achchiq taom xohlasangiz, double beef burger o'rniga tovuq, qanotchalar yoki Asian bowl oling.",
    ),
)

private fun AppLanguage.aiCookingReply(cleanPrompt: String): String = when {
    cleanPrompt.contains("pizza") || cleanPrompt.contains("пицц") -> when (this) {
        AppLanguage.English -> "For pizza, heat the oven well, keep the dough thin, add sauce lightly, then bake fast until the edge is crisp."
        AppLanguage.Russian -> "Для пиццы хорошо разогрей духовку, раскатай тонкое тесто, не перегружай соусом и выпекай быстро до хрустящего края."
        AppLanguage.Uzbek -> "Pitsa uchun pechni yaxshilab qizdiring, xamirni yupqa qiling, sousni ozroq qo'shing va cheti qarsildoq bo'lguncha pishiring."
    }
    cleanPrompt.contains("chicken") || cleanPrompt.contains("куриц") || cleanPrompt.contains("tovuq") -> when (this) {
        AppLanguage.English -> "For juicy chicken, salt it first, cook on medium heat, and let it rest a few minutes before cutting."
        AppLanguage.Russian -> "Чтобы курица была сочной, сначала посоли ее, готовь на среднем огне и дай отдохнуть пару минут перед нарезкой."
        AppLanguage.Uzbek -> "Tovuq suvli chiqishi uchun avval tuzlang, o'rtacha olovda pishiring va kesishdan oldin biroz tindiring."
    }
    cleanPrompt.contains("burger") || cleanPrompt.contains("бургер") -> when (this) {
        AppLanguage.English -> "For a better burger, toast the bun, season the patty right before frying, and add sauce after the meat rests."
        AppLanguage.Russian -> "Для хорошего бургера поджарь булочку, посоли котлету прямо перед жаркой и добавь соус после короткого отдыха мяса."
        AppLanguage.Uzbek -> "Burger uchun bulochkani qizartiring, kotletni qovurishdan oldin tuzlang va go'sht tindirilgach sous qo'shing."
    }
    else -> when (this) {
        AppLanguage.English -> "A simple rule: balance salt, acid, fat and heat. Taste at the end and adjust one thing at a time."
        AppLanguage.Russian -> "Простое правило: держи баланс соли, кислоты, жира и температуры. В конце пробуй и меняй по одному вкусу за раз."
        AppLanguage.Uzbek -> "Oddiy qoida: tuz, nordonlik, yog' va issiqlik balansini saqlang. Oxirida tatib ko'rib, bittadan tuzating."
    }
}

private fun AppLanguage.aiGeneralReply(
    recommendations: String,
    restaurantName: String,
): String = when (this) {
    AppLanguage.English -> "I can answer in the current app language and use Foodly menu data. Try asking: “what is lighter?”, “what sauce fits chicken?”, “what is inside plov?”, or “add the second pizza”. From $restaurantName I would start with: $recommendations."
    AppLanguage.Russian -> "Я отвечаю на текущем языке приложения и использую данные меню Foodly. Можешь спросить: «что полегче?», «какой соус к курице?», «что внутри плова?» или «добавь вторую пиццу». Из $restaurantName я бы начал с: $recommendations."
    AppLanguage.Uzbek -> "Men ilovaning hozirgi tilida javob beraman va Foodly menyu ma'lumotlaridan foydalanaman. Masalan: “yengilroq nima?”, “tovuqqa qaysi sous?”, “osh tarkibi nima?” yoki “ikkinchi pitsani qo'sh”. $restaurantName dan boshlash uchun: $recommendations."
}
