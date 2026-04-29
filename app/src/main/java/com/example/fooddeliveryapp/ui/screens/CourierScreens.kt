package com.example.fooddeliveryapp.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.core.content.ContextCompat
import com.example.fooddeliveryapp.ui.AppLanguage
import com.example.fooddeliveryapp.ui.AppThemeMode
import com.example.fooddeliveryapp.ui.FoodAppState
import com.example.fooddeliveryapp.ui.LocalAppLanguage
import com.example.fooddeliveryapp.ui.UzbekistanPhoneMask
import com.example.fooddeliveryapp.ui.components.asPrice
import com.example.fooddeliveryapp.ui.formatUzbekPhoneInput
import com.example.fooddeliveryapp.ui.data.ChatAuthor
import com.example.fooddeliveryapp.ui.data.CourierDeliveryOrder
import com.example.fooddeliveryapp.ui.data.CourierOrderStatus
import com.example.fooddeliveryapp.ui.data.CourierProfileDetails
import com.example.fooddeliveryapp.ui.data.GeoPoint
import com.example.fooddeliveryapp.ui.data.OrderReview
import com.example.fooddeliveryapp.ui.data.PaymentCard
import com.example.fooddeliveryapp.ui.data.cardBrandName
import com.example.fooddeliveryapp.ui.data.detectPaymentMethod
import com.example.fooddeliveryapp.ui.map.YandexRouteMap
import com.example.fooddeliveryapp.ui.theme.CardWhite
import com.example.fooddeliveryapp.ui.theme.Gold
import com.example.fooddeliveryapp.ui.theme.InkSoft
import com.example.fooddeliveryapp.ui.theme.Orange
import com.example.fooddeliveryapp.ui.theme.OrangeSoft
import com.example.fooddeliveryapp.ui.theme.Rose
import com.example.fooddeliveryapp.ui.theme.Success
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@Composable
fun CourierAuthScreen(
    onLogin: suspend (email: String, password: String) -> String?,
    onRegister: suspend (name: String, email: String, phone: String, password: String) -> String?,
    onAuthenticated: () -> Unit,
    onBack: () -> Unit,
) {
    val text = courierText()
    val scope = rememberCoroutineScope()
    var isRegister by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("Robert Fox") }
    var email by rememberSaveable { mutableStateOf("courier@foodly.app") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("1234") }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    fun submit() {
        scope.launch {
            isLoading = true
            error = null
            val result = if (isRegister) {
                onRegister(name, email, phone, password)
            } else {
                onLogin(email, password)
            }
            isLoading = false
            if (result == null) onAuthenticated() else error = result
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CourierTopBar(title = if (isRegister) text.signUpTitle else text.loginTitle, onBack = onBack)
        CourierInfoCard(
            title = text.workspaceTitle,
            text = text.workspaceText,
            icon = Icons.Default.LocalShipping,
        )
        if (isRegister) {
            CourierTextField(text.fullName, name, { name = it })
        }
        CourierTextField(text.email, email, { email = it }, KeyboardType.Email)
        if (isRegister) {
            CourierTextField(
                label = text.phone,
                value = phone,
                onValueChange = { phone = it.formatUzbekPhoneInput() },
                keyboardType = KeyboardType.Phone,
                placeholder = UzbekistanPhoneMask,
            )
        }
        CourierTextField(text.password, password, { password = it }, KeyboardType.Password)
        CourierPrimaryButton(
            text = if (isLoading) text.pleaseWait else if (isRegister) text.createCourierAccount else text.logIn,
            enabled = !isLoading,
            onClick = ::submit,
        )
        error?.let {
            Text(it, color = Rose, style = MaterialTheme.typography.bodyMedium)
        }
        TextButton(
            onClick = {
                isRegister = !isRegister
                error = null
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(
                text = if (isRegister) text.alreadyRegistered else text.newCourier,
                color = Orange,
            )
        }
    }
}

@Composable
fun CourierHomeScreen(
    appState: FoodAppState,
    onOpenOrder: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    onOpenProfile: () -> Unit,
) {
    val text = courierText()
    val courier = appState.currentCourier ?: appState.courierProfileDetails
    val currentOrders = appState.activeCourierOrders
    val availableOrders = appState.availableCourierOrders
    CourierLocationSync(appState = appState)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            CourierHomeHeader(
                courier = courier,
                isOnline = appState.courierIsOnline,
                onToggleOnline = appState::toggleCourierOnline,
                onOpenProfile = onOpenProfile,
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CourierStatCard(text.balance, appState.courierWalletBalance.asPrice(), Icons.Default.CreditCard, Modifier.weight(1f))
                CourierStatCard(text.rating, appState.courierRatingLabel, Icons.Default.Star, Modifier.weight(1f))
                CourierStatCard(text.done, appState.courierDeliveredCount.toString(), Icons.Default.History, Modifier.weight(1f))
            }
        }
        if (appState.courierIsBlocked) {
            item {
                CourierEmptyCard("Rating block", appState.courierBlockMessage)
            }
        } else if (appState.courierRatingCount > 0 && appState.courierRating < 4.5) {
            item {
                CourierEmptyCard("Low rating", "Fewer new orders are shown until the courier rating returns to 4.5 or higher.")
            }
        }
        item {
            CourierOrdersWorkspace(
                appState = appState,
                currentOrders = currentOrders,
                availableOrders = availableOrders,
                historyOrders = appState.courierHistoryOrders,
                onOpenOrder = onOpenOrder,
                onOpenChat = onOpenChat,
            )
        }
    }
}

@Composable
private fun CourierOrdersWorkspace(
    appState: FoodAppState,
    currentOrders: List<CourierDeliveryOrder>,
    availableOrders: List<CourierDeliveryOrder>,
    historyOrders: List<CourierDeliveryOrder>,
    onOpenOrder: (String) -> Unit,
    onOpenChat: (String) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val showTwoColumns = maxWidth >= 620.dp
        if (showTwoColumns) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                CourierPrimaryOrdersColumn(
                    appState = appState,
                    currentOrders = currentOrders,
                    availableOrders = availableOrders,
                    onOpenOrder = onOpenOrder,
                    onOpenChat = onOpenChat,
                    modifier = Modifier.weight(1f),
                )
                CourierHistoryOrdersColumn(
                    appState = appState,
                    orders = historyOrders,
                    onOpenOrder = onOpenOrder,
                    onOpenChat = onOpenChat,
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                CourierPrimaryOrdersColumn(
                    appState = appState,
                    currentOrders = currentOrders,
                    availableOrders = availableOrders,
                    onOpenOrder = onOpenOrder,
                    onOpenChat = onOpenChat,
                )
                CourierHistoryOrdersColumn(
                    appState = appState,
                    orders = historyOrders,
                    onOpenOrder = onOpenOrder,
                    onOpenChat = onOpenChat,
                )
            }
        }
    }
}

@Composable
private fun CourierPrimaryOrdersColumn(
    appState: FoodAppState,
    currentOrders: List<CourierDeliveryOrder>,
    availableOrders: List<CourierDeliveryOrder>,
    onOpenOrder: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val text = courierText()
    val showingCurrentOrders = currentOrders.isNotEmpty()

    CourierWorkspacePanel(
        title = if (showingCurrentOrders) text.currentOrders else text.availableOrders,
        count = if (showingCurrentOrders) currentOrders.size else availableOrders.size,
        modifier = modifier,
    ) {
        when {
            showingCurrentOrders -> currentOrders.forEach { order ->
                CourierOrderCard(
                    order = order,
                    primaryAction = text.open,
                    onOpen = {
                        appState.selectCourierOrder(order.id)
                        onOpenOrder(order.id)
                    },
                    onPrimary = {
                        appState.selectCourierOrder(order.id)
                        onOpenOrder(order.id)
                    },
                    onChat = { onOpenChat(order.id) },
                )
            }
            !appState.courierIsOnline -> CourierEmptyCard(text.youAreOffline, text.turnOnlineHint)
            availableOrders.isEmpty() -> CourierEmptyCard(text.noNewOrders, text.newOrdersHint)
            else -> {
                CourierNoticeCard(count = availableOrders.size)
                availableOrders.forEach { order ->
                    CourierOrderCard(
                        order = order,
                        primaryAction = text.accept,
                        onOpen = {
                            appState.selectCourierOrder(order.id)
                            onOpenOrder(order.id)
                        },
                        onPrimary = {
                            appState.acceptCourierOrder(order.id)
                            onOpenOrder(order.id)
                        },
                        onChat = { onOpenChat(order.id) },
                        showChatAction = false,
                    )
                }
            }
        }
    }
}

@Composable
private fun CourierHistoryOrdersColumn(
    appState: FoodAppState,
    orders: List<CourierDeliveryOrder>,
    onOpenOrder: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val text = courierText()
    CourierWorkspacePanel(
        title = text.deliveryHistory,
        count = orders.size,
        modifier = modifier,
    ) {
        if (orders.isEmpty()) {
            CourierEmptyCard(text.historyEmpty, text.historyHint)
        } else {
            orders.forEach { order ->
                CourierOrderCard(
                    order = order,
                    primaryAction = text.details,
                    onOpen = {
                        appState.selectCourierOrder(order.id)
                        onOpenOrder(order.id)
                    },
                    onPrimary = {
                        appState.selectCourierOrder(order.id)
                        onOpenOrder(order.id)
                    },
                    onChat = { onOpenChat(order.id) },
                    showChatAction = false,
                )
            }
        }
    }
}

@Composable
@SuppressLint("MissingPermission")
private fun CourierLocationSync(appState: FoodAppState) {
    val context = LocalContext.current
    val hasActiveOrders = appState.activeCourierOrders.isNotEmpty()
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {}

    LaunchedEffect(appState.courierIsOnline, hasActiveOrders, appState.currentCourier?.id) {
        if (
            appState.courierIsOnline &&
            hasActiveOrders &&
            context.hasCourierLocationPermission().not()
        ) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    DisposableEffect(context, appState.courierIsOnline, hasActiveOrders, appState.currentCourier?.id) {
        if (
            appState.courierIsOnline.not() ||
            hasActiveOrders.not() ||
            context.hasCourierLocationPermission().not()
        ) {
            return@DisposableEffect onDispose {}
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { provider -> runCatching { locationManager.isProviderEnabled(provider) }.getOrDefault(false) }
        if (providers.isEmpty()) {
            return@DisposableEffect onDispose {}
        }

        val listener = LocationListener { location ->
            appState.updateCourierLocationForActiveOrders(location.toGeoPoint())
        }
        providers
            .mapNotNull { provider -> runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull(Location::getTime)
            ?.let { location -> appState.updateCourierLocationForActiveOrders(location.toGeoPoint()) }
        providers.forEach { provider ->
            runCatching {
                locationManager.requestLocationUpdates(
                    provider,
                    CourierLocationUpdateMs,
                    CourierLocationMinMeters,
                    listener,
                )
            }
        }

        onDispose {
            runCatching { locationManager.removeUpdates(listener) }
        }
    }
}

private fun Context.hasCourierLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

private fun Location.toGeoPoint(): GeoPoint =
    GeoPoint(latitude = latitude, longitude = longitude)

private const val CourierLocationUpdateMs = 10_000L
private const val CourierLocationMinMeters = 15f

@Composable
private fun CourierLocationRequiredCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OrangeSoft.copy(alpha = 0.38f))
                .padding(22.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(CardWhite.copy(alpha = 0.86f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Orange,
                        modifier = Modifier.size(30.dp),
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun CourierMapExpandButton(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
            contentDescription = null,
            tint = Orange,
            modifier = Modifier.size(26.dp),
        )
    }
}

@Composable
fun CourierOrderDetailsScreen(
    appState: FoodAppState,
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit,
) {
    val text = courierText()
    val order = appState.selectedCourierOrder ?: appState.activeCourierOrder
    if (order == null) {
        CourierCenteredState(text.noOrderSelected, onBack)
        return
    }
    CourierLocationSync(appState = appState)
    val nextAction = order.nextActionLabel(text)
    var mapExpanded by rememberSaveable(order.id) { mutableStateOf(false) }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        val sheetScrollState = rememberScrollState()
        val expandedSheetTop = (maxHeight - 160.dp).coerceAtLeast(340.dp)
        val collapsedSheetTop = (maxHeight * 0.46f)
            .coerceIn(280.dp, (maxHeight - 220.dp).coerceAtLeast(280.dp))
        val sheetTop by animateDpAsState(
            targetValue = if (mapExpanded) expandedSheetTop else collapsedSheetTop,
            animationSpec = tween(durationMillis = 340),
            label = "courierSheetTop",
        )
        val mapHeight by animateDpAsState(
            targetValue = if (mapExpanded) maxHeight else (collapsedSheetTop + 28.dp).coerceAtMost(maxHeight),
            animationSpec = tween(durationMillis = 340),
            label = "courierMapHeight",
        )
        val toggleTop by animateDpAsState(
            targetValue = (sheetTop - 24.dp).coerceAtLeast(96.dp),
            animationSpec = tween(durationMillis = 340),
            label = "courierToggleTop",
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(mapHeight)
                .align(Alignment.TopCenter),
        ) {
            if (order.courierPoint != null) {
                YandexRouteMap(
                    startPoint = order.courierPoint,
                    endPoint = order.customerAddress.point,
                    startLabel = text.mapSelfLabel(),
                    endLabel = text.mapCustomerLabel,
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = 0.dp,
                )
            } else {
                CourierLocationRequiredCard(
                    title = text.locationRequiredTitle,
                    message = text.locationRequiredText,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        CourierTopBar(
            title = text.orderDetails,
            onBack = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .zIndex(3f),
        )

        CourierMapExpandButton(
            expanded = mapExpanded,
            onClick = { mapExpanded = !mapExpanded },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = toggleTop)
                .zIndex(4f),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = sheetTop)
                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(sheetScrollState)
                .padding(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 28.dp)
                .zIndex(2f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 68.dp, height = 5.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.34f)),
            )
            CourierOrderStatusCard(order)
            CourierAddressCard(order)
            CourierInfoCard(
                title = text.items,
                text = order.itemsLabel,
                icon = Icons.Default.LocalShipping,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CourierStatCard(text.total, order.total.asPrice(), Icons.Default.CreditCard, Modifier.weight(1f))
                CourierStatCard(text.earning, order.earning.asPrice(), Icons.Default.Star, Modifier.weight(1f))
            }
            if (nextAction != null) {
                CourierPrimaryButton(
                    text = nextAction,
                    onClick = {
                        if (order.status == CourierOrderStatus.Available) {
                            appState.acceptCourierOrder(order.id)
                        } else {
                            appState.updateCourierOrderStatus(order.id, order.nextStatus())
                        }
                    },
                )
            }
            if (order.status != CourierOrderStatus.Available) {
                Button(
                    onClick = { onOpenChat(order.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text.chatWithCustomer)
                }
            }
        }
    }
}

@Composable
fun CourierProfileScreen(
    appState: FoodAppState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogOut: () -> Unit,
) {
    val text = courierText()
    val profile = appState.currentCourier ?: appState.courierProfileDetails
    var withdrawMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var withdrawMessageIsError by rememberSaveable { mutableStateOf(false) }
    var showWithdrawSuccess by rememberSaveable { mutableStateOf(false) }
    var showLogOutDialog by rememberSaveable { mutableStateOf(false) }
    var showPayoutCardDialog by rememberSaveable { mutableStateOf(false) }
    var payoutHolderName by rememberSaveable(profile.name) { mutableStateOf(profile.name) }
    var payoutCardNumber by rememberSaveable { mutableStateOf("") }
    var payoutExpiry by rememberSaveable { mutableStateOf("") }
    var payoutCardError by rememberSaveable { mutableStateOf<String?>(null) }

    fun requestWithdraw() {
        val withdrawError = appState.withdrawCourierBalance()
        withdrawMessageIsError = withdrawError != null
        withdrawMessage = withdrawError ?: text.withdrawalCreatedToCard(appState.courierPayoutCard?.last4.orEmpty())
        showWithdrawSuccess = withdrawError == null
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 118.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            CourierProfileBalanceHeader(
                title = text.courierProfile,
                balance = appState.courierWalletBalance.asPrice(),
                onBack = onBack,
                onWithdraw = ::requestWithdraw,
            )
        }
        if (withdrawMessage != null && withdrawMessageIsError) {
            item {
                Text(
                    text = withdrawMessage.orEmpty(),
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = Rose,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
        item {
            CourierProfileMenuGroup {
                CourierProfileMenuRow(Icons.Default.Person, "Personal Info", profile.name, onClick = onEdit)
                CourierProfileMenuRow(
                    icon = Icons.Default.CreditCard,
                    title = text.payoutCard,
                    value = appState.courierPayoutCard?.let { "${it.brand} ${it.last4}" } ?: text.addPayoutCard,
                    onClick = { showPayoutCardDialog = true },
                )
                CourierProfileMenuRow(Icons.Default.Settings, text.settings, appState.language.courierLanguageLabel(), onClick = onOpenSettings)
            }
        }
        item {
            CourierProfileMenuGroup {
                CourierProfileMenuRow(Icons.Default.ReceiptLong, "Withdrawal History", appState.courierLastWithdrawalLabel)
                CourierProfileMenuRow(Icons.Default.LocalShipping, "Number of Orders", appState.courierDeliveredCount.toString())
                CourierProfileMenuRow(Icons.Default.Star, "User Reviews", appState.courierRatingLabel)
            }
        }
        item {
            CourierProfileMenuGroup {
                CourierProfileMenuRow(
                    icon = Icons.Default.Logout,
                    title = text.logOut,
                    value = "",
                    iconTint = Rose,
                    onClick = { showLogOutDialog = true },
                )
            }
        }
    }

    if (showWithdrawSuccess) {
        WithdrawSuccessDialog(onDismiss = { showWithdrawSuccess = false })
    }

    if (showPayoutCardDialog) {
        AlertDialog(
            onDismissRequest = {
                showPayoutCardDialog = false
                payoutCardError = null
            },
            title = { Text(text.payoutCard) },
            text = {
                CourierPayoutCard(
                    card = appState.courierPayoutCard,
                    showForm = true,
                    holderName = payoutHolderName,
                    cardNumber = payoutCardNumber,
                    expiry = payoutExpiry,
                    error = payoutCardError,
                    onToggleForm = {
                        showPayoutCardDialog = false
                        payoutCardError = null
                    },
                    onHolderNameChange = {
                        payoutHolderName = it
                        payoutCardError = null
                    },
                    onCardNumberChange = {
                        payoutCardNumber = it.formatCourierCardNumberInput()
                        payoutCardError = null
                    },
                    onExpiryChange = {
                        payoutExpiry = it.formatCourierExpiryInput()
                        payoutCardError = null
                    },
                    onSave = {
                        payoutCardError = appState.saveCourierPayoutCard(
                            number = payoutCardNumber,
                            holderName = payoutHolderName,
                            expiry = payoutExpiry,
                        )
                        if (payoutCardError == null) {
                            showPayoutCardDialog = false
                            payoutCardNumber = ""
                            payoutExpiry = ""
                        }
                    },
                )
            },
            confirmButton = {},
        )
    }

    if (showLogOutDialog) {
        AlertDialog(
            onDismissRequest = { showLogOutDialog = false },
            title = { Text(text.logOut) },
            text = {
                Text(
                    text = text.logOutQuestion,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogOutDialog = false
                        onLogOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose, contentColor = CardWhite),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(text.logOut)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogOutDialog = false }) {
                    Text(text.cancel, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
        )
    }
}

@Composable
fun CourierSettingsScreen(
    appState: FoodAppState,
    onBack: () -> Unit,
) {
    val text = courierText()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 118.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { CourierTopBar(title = text.settings, onBack = onBack) }
        item {
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                CourierSettingsCard(
                    currentTheme = appState.themeMode,
                    currentLanguage = appState.language,
                    onThemeChange = appState::updateThemeMode,
                    onLanguageChange = appState::updateLanguage,
                )
            }
        }
    }
}

@Composable
private fun CourierProfileBalanceHeader(
    title: String,
    balance: String,
    onBack: () -> Unit,
    onWithdraw: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
            .background(Orange)
            .statusBarsPadding()
            .padding(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 26.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CardWhite),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                }
                Text(title, style = MaterialTheme.typography.titleMedium, color = CardWhite)
            }
            Text("Available Balance", style = MaterialTheme.typography.bodyMedium, color = CardWhite.copy(alpha = 0.92f))
            Text(
                text = balance,
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                color = CardWhite,
            )
            Button(
                onClick = onWithdraw,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = CardWhite),
                modifier = Modifier.border(1.dp, CardWhite, RoundedCornerShape(8.dp)),
            ) {
                Text("Withdraw")
            }
        }
    }
}

@Composable
private fun CourierProfileMenuGroup(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.54f)),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun CourierProfileMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    iconTint: Color = Orange,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (value.isNotBlank()) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (onClick != null) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CourierProfileMapSection(order: CourierDeliveryOrder?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Map, contentDescription = null, tint = Orange)
                Text("Map", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            }
            if (order == null) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "No active delivery route",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                YandexRouteMap(
                    startPoint = order.restaurantPoint,
                    endPoint = order.customerAddress.point,
                    courierPoint = order.courierPoint,
                    restaurantPoint = order.restaurantPoint,
                    startLabel = "R",
                    endLabel = "C",
                    cornerRadius = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(176.dp),
                )
            }
        }
    }
}

@Composable
private fun WithdrawSuccessDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .clip(CircleShape)
                    .background(Orange),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CardWhite, modifier = Modifier.size(46.dp))
            }
        },
        title = {
            Text(
                text = "Withdraw Successful",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {},
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = CardWhite),
            ) {
                Text("OK")
            }
        },
    )
}

@Composable
private fun CourierPayoutCard(
    card: PaymentCard?,
    showForm: Boolean,
    holderName: String,
    cardNumber: String,
    expiry: String,
    error: String?,
    onToggleForm: () -> Unit,
    onHolderNameChange: (String) -> Unit,
    onCardNumberChange: (String) -> Unit,
    onExpiryChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    val text = courierText()
    val detectedMethod = detectPaymentMethod(cardNumber)
    val cardNumberValue = remember(cardNumber) {
        TextFieldValue(text = cardNumber, selection = TextRange(cardNumber.length))
    }
    val expiryValue = remember(expiry) {
        TextFieldValue(text = expiry, selection = TextRange(expiry.length))
    }
    val canSave = holderName.trim().length >= 2 &&
        cardNumber.cardDigits().length == 16 &&
        detectedMethod != null &&
        expiry.isValidCourierExpiryInput()

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text.payoutCard, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(text.payoutCardDescription, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (card != null) {
                    TextButton(onClick = onToggleForm) {
                        Text(if (showForm) text.cancel else text.changePayoutCard, color = Orange)
                    }
                }
            }

            if (card != null) {
                CourierPayoutCardPreview(card = card)
            }

            if (showForm) {
                CourierTextField(
                    label = text.cardHolderName,
                    value = holderName,
                    onValueChange = onHolderNameChange,
                )
                CourierPayoutTextField(
                    label = text.cardNumber,
                    value = cardNumberValue,
                    onValueChange = { next -> onCardNumberChange(next.text) },
                    keyboardType = KeyboardType.Number,
                    placeholder = "8600 0000 0000 0000",
                )
                detectedMethod?.let { method ->
                    Text(
                        text = text.detectedCard(method.cardBrandName()),
                        style = MaterialTheme.typography.bodySmall,
                        color = Success,
                    )
                }
                CourierPayoutTextField(
                    label = text.expiry,
                    value = expiryValue,
                    onValueChange = { next -> onExpiryChange(next.text) },
                    keyboardType = KeyboardType.Text,
                    placeholder = "MM/YY",
                )
                Text(
                    text = text.payoutCardSecurity,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                error?.let { message ->
                    Text(message, style = MaterialTheme.typography.bodyMedium, color = Rose)
                }
                CourierPrimaryButton(
                    text = if (card == null) text.addPayoutCard else text.savePayoutCard,
                    enabled = canSave,
                    onClick = onSave,
                )
            }
        }
    }
}

@Composable
private fun CourierPayoutTextField(
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    keyboardType: KeyboardType,
    placeholder: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = value,
            onValueChange = { next ->
                val formatted = when (keyboardType) {
                    KeyboardType.Number -> next.text.formatCourierCardNumberInput()
                    else -> next.text.formatCourierExpiryInput()
                }
                onValueChange(TextFieldValue(text = formatted, selection = TextRange(formatted.length)))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            placeholder = {
                Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = Orange,
                unfocusedBorderColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun CourierPayoutCardPreview(card: PaymentCard) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(card.brand, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Icon(Icons.Default.CreditCard, contentDescription = null, tint = Orange)
        }
        Text(
            text = "•••• ${card.last4}",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = card.holderName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(card.expiry, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CourierSettingsCard(
    currentTheme: AppThemeMode,
    currentLanguage: AppLanguage,
    onThemeChange: (AppThemeMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
) {
    val text = courierText()
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = Orange)
                Text(text.settings, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            }
            Text("Theme", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CourierThemeChoice(
                    label = "Light",
                    selected = currentTheme == AppThemeMode.Light,
                    icon = Icons.Default.LightMode,
                    onClick = { onThemeChange(AppThemeMode.Light) },
                    modifier = Modifier.weight(1f),
                )
                CourierThemeChoice(
                    label = "Dark",
                    selected = currentTheme == AppThemeMode.Dark,
                    icon = Icons.Default.DarkMode,
                    onClick = { onThemeChange(AppThemeMode.Dark) },
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Language, contentDescription = null, tint = Orange, modifier = Modifier.size(18.dp))
                Text(
                    text = text.languageDescription,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(text.languageTitle, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(AppLanguage.Russian, AppLanguage.Uzbek, AppLanguage.English).forEach { option ->
                    val selected = option == currentLanguage
                    Button(
                        onClick = { onLanguageChange(option) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) Orange else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selected) CardWhite else MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Text(
                            text = option.courierLanguageLabel(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CourierThemeChoice(
    label: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Orange else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) CardWhite else MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun CourierEditProfileScreen(
    appState: FoodAppState,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val text = courierText()
    val current = appState.currentCourier ?: appState.courierProfileDetails
    var name by rememberSaveable { mutableStateOf(current.name) }
    var email by rememberSaveable { mutableStateOf(current.email) }
    var phone by rememberSaveable { mutableStateOf(current.phone.takeIf { it.isNotBlank() }?.formatUzbekPhoneInput().orEmpty()) }
    var vehicle by rememberSaveable { mutableStateOf(current.vehicle) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CourierTopBar(title = text.editCourier, onBack = onBack)
        CourierTextField(text.name, name, { name = it })
        CourierTextField(text.email, email, { email = it }, KeyboardType.Email)
        CourierTextField(
            label = text.phone,
            value = phone,
            onValueChange = { phone = it.formatUzbekPhoneInput() },
            keyboardType = KeyboardType.Phone,
            placeholder = UzbekistanPhoneMask,
        )
        CourierTextField(text.vehicle, vehicle, { vehicle = it })
        CourierPrimaryButton(
            text = text.save,
            onClick = {
                appState.updateCourierProfile(
                    CourierProfileDetails(
                        id = current.id,
                        name = name,
                        email = email,
                        phone = phone,
                        vehicle = vehicle,
                        photoUrl = current.photoUrl,
                    ),
                )
                onSaved()
            },
        )
    }
}

@Composable
fun CourierChatScreen(
    appState: FoodAppState,
    onBack: () -> Unit,
) {
    val text = courierText()
    val order = appState.selectedCourierOrder ?: appState.activeCourierOrder
    if (order == null) {
        CourierCenteredState(text.noChatSelected, onBack)
        return
    }
    if (order.status == CourierOrderStatus.Delivered) {
        CourierCenteredState(text.chatClosed, onBack)
        return
    }
    var draft by rememberSaveable { mutableStateOf("") }
    val messages = appState.chatMessagesForOrder(order.id)
    LaunchedEffect(order.id) {
        appState.markCourierChatRead(order.id)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding(),
    ) {
        CourierChatHeader(order = order, onBack = onBack)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(messages, key = { it.id }) { message ->
                CourierChatBubble(
                    text = message.text,
                    time = message.timeLabel,
                    mine = message.author == ChatAuthor.Courier,
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(text.writeSomething) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
            )
            IconButton(
                onClick = {
                    appState.sendCourierMessage(order.id, draft)
                    draft = ""
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(CardWhite),
            ) {
                Icon(Icons.Default.Send, contentDescription = null, tint = Orange)
            }
        }
    }
}

@Composable
fun CourierMessagesScreen(
    appState: FoodAppState,
    onOpenChat: (String) -> Unit,
) {
    val text = courierText()
    val inboxOrders = appState.courierChatThreads

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, top = 32.dp, end = 20.dp, bottom = 118.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = text.messages,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (inboxOrders.isEmpty()) {
            item { CourierEmptyCard(text.noActiveChats, text.noActiveChatsHint) }
        } else {
            items(inboxOrders, key = { it.id }) { order ->
                val lastMessage = appState.chatMessagesForOrder(order.id).lastOrNull()
                CourierInboxRow(
                    order = order,
                    preview = lastMessage?.text ?: order.itemsLabel,
                    time = lastMessage?.timeLabel ?: order.status.label(text),
                    unread = appState.isCourierChatUnread(order.id),
                    onClick = { onOpenChat(order.id) },
                )
            }
        }
    }
}

@Composable
fun CourierReviewsScreen(appState: FoodAppState) {
    val text = courierText()
    val reviews = appState.courierReviews

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, top = 32.dp, end = 20.dp, bottom = 118.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                text = text.reviews,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (reviews.isEmpty()) {
            item { CourierEmptyCard(text.noReviewsYet, text.noReviewsHint) }
        } else {
            items(reviews, key = { it.orderId }) { review ->
                CourierReviewRow(
                    review = review,
                    customerName = appState.courierOrderForOrder(review.orderId)?.customerName ?: "Customer",
                )
            }
        }
    }
}

@Composable
private fun CourierInboxRow(
    order: CourierDeliveryOrder,
    preview: String,
    time: String,
    unread: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(OrangeSoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = Orange)
            if (unread) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Success),
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = order.customerName.ifBlank { order.restaurantName },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = preview,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (unread) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Orange),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("1", style = MaterialTheme.typography.bodySmall, color = CardWhite)
                }
            }
        }
    }
}

@Composable
private fun CourierReviewRow(
    review: OrderReview,
    customerName: String,
) {
    val text = courierText()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = Orange)
        }
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)),
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = review.createdAtMillis.toCourierReviewDate(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    text = customerName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = text.orderNumber(review.orderId.takeLast(6).uppercase()),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < review.courierRating) Orange else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(15.dp),
                        )
                    }
                }
                Text(
                    text = review.comment.ifBlank { "Great food and service." },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CourierHomeHeader(
    courier: CourierProfileDetails,
    isOnline: Boolean,
    onToggleOnline: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    val text = courierText()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(OrangeSoft)
                .clickable(onClick = onOpenProfile),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = Orange)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(courier.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = if (isOnline) text.onlineReceivingOrders else text.offline,
                style = MaterialTheme.typography.bodySmall,
                color = if (isOnline) Success else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Button(
            onClick = onToggleOnline,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isOnline) Success else Orange,
                contentColor = CardWhite,
            ),
        ) {
            Text(if (isOnline) text.online else text.goOnline)
        }
    }
}

@Composable
private fun CourierOrderCard(
    order: CourierDeliveryOrder,
    primaryAction: String,
    onOpen: () -> Unit,
    onPrimary: () -> Unit,
    onChat: () -> Unit,
    showChatAction: Boolean = true,
) {
    val text = courierText()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Orange.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = Orange)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(order.restaurantName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(order.status.label(text), style = MaterialTheme.typography.bodySmall, color = order.status.statusColor())
                }
                Text(order.earning.asPrice(), style = MaterialTheme.typography.titleMedium, color = Success)
            }
            Text(order.itemsLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Orange, modifier = Modifier.size(18.dp))
                Text(order.customerAddress.title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onPrimary,
                    modifier = if (showChatAction) Modifier.weight(1f) else Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = CardWhite),
                ) {
                    Text(primaryAction)
                }
                if (showChatAction) {
                    Button(
                        onClick = onChat,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Text(text.chat)
                    }
                }
            }
        }
    }
}

@Composable
private fun CourierOrderStatusCard(order: CourierDeliveryOrder) {
    val text = courierText()
    CourierInfoCard(
        title = order.status.label(text),
        text = "${order.customerName} - ${order.customerPhone}",
        icon = Icons.Default.CheckCircle,
    )
}

@Composable
private fun CourierAddressCard(order: CourierDeliveryOrder) {
    val text = courierText()
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CourierAddressLine(text.pickup, order.restaurantAddress)
            CourierAddressLine(text.dropOff, "${order.customerAddress.title}, ${order.customerAddress.subtitle}")
        }
    }
}

@Composable
private fun CourierAddressLine(label: String, address: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Orange, modifier = Modifier.size(20.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Text(address, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CourierWorkspacePanel(
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(OrangeSoft.copy(alpha = 0.8f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = count.toString().padStart(2, '0'),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Orange,
                        )
                    }
                }
                content()
            },
        )
    }
}

@Composable
private fun CourierNoticeCard(count: Int) {
    val text = courierText()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Orange.copy(alpha = 0.12f))
            .border(1.dp, Orange.copy(alpha = 0.24f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = Orange)
        Column {
            Text(text.newOrderNotification, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(text.availableOrdersWaiting(count), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CourierInfoCard(
    title: String,
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Orange.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = Orange)
            }
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CourierStatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(icon, contentDescription = null, tint = Orange, modifier = Modifier.size(20.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CourierEmptyCard(title: String, text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CourierTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
        }
        Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun CourierTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String = "",
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            placeholder = placeholder.takeIf { it.isNotBlank() }?.let { hint ->
                { Text(hint, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = Orange,
                unfocusedBorderColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun CourierPrimaryButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = CardWhite),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun CourierChatHeader(order: CourierDeliveryOrder, onBack: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
            Text(order.customerName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun CourierChatBubble(
    text: String,
    time: String,
    mine: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (mine) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(time, style = MaterialTheme.typography.bodySmall, color = InkSoft)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (mine) Orange else MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (mine) CardWhite else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun CourierCenteredState(text: String, onBack: () -> Unit) {
    val labels = courierText()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(16.dp))
        CourierPrimaryButton(text = labels.back, onClick = onBack)
    }
}

@Composable
private fun courierText(): CourierText = CourierText(LocalAppLanguage.current)

private val CourierReviewDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun Long.toCourierReviewDate(): String =
    Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .format(CourierReviewDateFormatter)

@Composable
private fun CourierText.mapSelfLabel(): String = when (LocalAppLanguage.current) {
    AppLanguage.English -> "Me"
    AppLanguage.Russian -> "Я"
    AppLanguage.Uzbek -> "Men"
}

private fun String.cardDigits(): String = filter(Char::isDigit)

private fun String.formatCourierCardNumberInput(): String =
    cardDigits()
        .take(16)
        .chunked(4)
        .joinToString(" ")

private fun String.formatCourierExpiryInput(): String {
    val digits = cardDigits().take(4)
    return if (digits.length <= 2) {
        digits
    } else {
        digits.take(2) + "/" + digits.drop(2)
    }
}

private fun String.isValidCourierExpiryInput(): Boolean =
    matches(Regex("""\d{2}/\d{2}"""))

private fun AppLanguage.courierLanguageLabel(): String = when (this) {
    AppLanguage.English -> "English"
    AppLanguage.Russian -> "Русский"
    AppLanguage.Uzbek -> "O'zbek"
}

private class CourierText(private val language: AppLanguage) {
    val signUpTitle: String get() = when (language) {
        AppLanguage.English -> "Courier Sign Up"
        AppLanguage.Russian -> "Регистрация курьера"
        AppLanguage.Uzbek -> "Kuryer ro'yxatdan o'tishi"
    }
    val loginTitle: String get() = when (language) {
        AppLanguage.English -> "Courier Log In"
        AppLanguage.Russian -> "Вход курьера"
        AppLanguage.Uzbek -> "Kuryer kirishi"
    }
    val workspaceTitle: String get() = when (language) {
        AppLanguage.English -> "Delivery workspace"
        AppLanguage.Russian -> "Рабочее место доставки"
        AppLanguage.Uzbek -> "Yetkazish ish joyi"
    }
    val workspaceText: String get() = when (language) {
        AppLanguage.English -> "Go online, accept orders, chat with customers and track delivery earnings."
        AppLanguage.Russian -> "Выходи онлайн, принимай заказы, пиши клиентам и следи за заработком."
        AppLanguage.Uzbek -> "Onlayn bo'ling, buyurtmalarni oling, mijozlar bilan yozishing va daromadni kuzating."
    }
    val fullName: String get() = when (language) {
        AppLanguage.English -> "Full name"
        AppLanguage.Russian -> "Полное имя"
        AppLanguage.Uzbek -> "To'liq ism"
    }
    val email: String get() = "Email"
    val phone: String get() = when (language) {
        AppLanguage.English -> "Phone"
        AppLanguage.Russian -> "Телефон"
        AppLanguage.Uzbek -> "Telefon"
    }
    val mapCourierLabel: String get() = when (language) {
        AppLanguage.English -> "Courier"
        AppLanguage.Russian -> "Курьер"
        AppLanguage.Uzbek -> "Kuryer"
    }
    val mapCustomerLabel: String get() = when (language) {
        AppLanguage.English -> "Customer"
        AppLanguage.Russian -> "Заказчик"
        AppLanguage.Uzbek -> "Mijoz"
    }
    val password: String get() = when (language) {
        AppLanguage.English -> "Password"
        AppLanguage.Russian -> "Пароль"
        AppLanguage.Uzbek -> "Parol"
    }
    val pleaseWait: String get() = when (language) {
        AppLanguage.English -> "Please wait..."
        AppLanguage.Russian -> "Подождите..."
        AppLanguage.Uzbek -> "Kuting..."
    }
    val createCourierAccount: String get() = when (language) {
        AppLanguage.English -> "Create courier account"
        AppLanguage.Russian -> "Создать аккаунт курьера"
        AppLanguage.Uzbek -> "Kuryer akkauntini yaratish"
    }
    val logIn: String get() = when (language) {
        AppLanguage.English -> "Log in"
        AppLanguage.Russian -> "Войти"
        AppLanguage.Uzbek -> "Kirish"
    }
    val alreadyRegistered: String get() = when (language) {
        AppLanguage.English -> "Already registered? Log in"
        AppLanguage.Russian -> "Уже зарегистрированы? Войти"
        AppLanguage.Uzbek -> "Ro'yxatdan o'tganmisiz? Kirish"
    }
    val newCourier: String get() = when (language) {
        AppLanguage.English -> "New courier? Sign up"
        AppLanguage.Russian -> "Новый курьер? Зарегистрироваться"
        AppLanguage.Uzbek -> "Yangi kuryermisiz? Ro'yxatdan o'tish"
    }
    val balance: String get() = when (language) {
        AppLanguage.English -> "Balance"
        AppLanguage.Russian -> "Баланс"
        AppLanguage.Uzbek -> "Balans"
    }
    val rating: String get() = when (language) {
        AppLanguage.English -> "Rating"
        AppLanguage.Russian -> "Рейтинг"
        AppLanguage.Uzbek -> "Reyting"
    }
    val done: String get() = when (language) {
        AppLanguage.English -> "Done"
        AppLanguage.Russian -> "Готово"
        AppLanguage.Uzbek -> "Bajarildi"
    }
    val currentOrders: String get() = when (language) {
        AppLanguage.English -> "Current orders"
        AppLanguage.Russian -> "Текущие заказы"
        AppLanguage.Uzbek -> "Joriy buyurtmalar"
    }
    val availableOrders: String get() = when (language) {
        AppLanguage.English -> "Available orders"
        AppLanguage.Russian -> "Доступные заказы"
        AppLanguage.Uzbek -> "Mavjud buyurtmalar"
    }
    val deliveryHistory: String get() = when (language) {
        AppLanguage.English -> "Delivery history"
        AppLanguage.Russian -> "История доставок"
        AppLanguage.Uzbek -> "Yetkazish tarixi"
    }
    val youAreOffline: String get() = when (language) {
        AppLanguage.English -> "You are offline"
        AppLanguage.Russian -> "Вы не в сети"
        AppLanguage.Uzbek -> "Siz oflaynsiz"
    }
    val turnOnlineHint: String get() = when (language) {
        AppLanguage.English -> "Turn online mode on to receive new order notifications."
        AppLanguage.Russian -> "Включите онлайн-режим, чтобы получать новые заказы."
        AppLanguage.Uzbek -> "Yangi buyurtmalarni olish uchun onlayn rejimni yoqing."
    }
    val noNewOrders: String get() = when (language) {
        AppLanguage.English -> "No new orders"
        AppLanguage.Russian -> "Новых заказов нет"
        AppLanguage.Uzbek -> "Yangi buyurtmalar yo'q"
    }
    val newOrdersHint: String get() = when (language) {
        AppLanguage.English -> "New customer orders will appear here automatically."
        AppLanguage.Russian -> "Новые заказы клиентов появятся здесь автоматически."
        AppLanguage.Uzbek -> "Mijozlarning yangi buyurtmalari shu yerda avtomatik ko'rinadi."
    }
    val historyEmpty: String get() = when (language) {
        AppLanguage.English -> "History is empty"
        AppLanguage.Russian -> "История пустая"
        AppLanguage.Uzbek -> "Tarix bo'sh"
    }
    val historyHint: String get() = when (language) {
        AppLanguage.English -> "Completed deliveries will be saved here."
        AppLanguage.Russian -> "Завершенные доставки будут сохраняться здесь."
        AppLanguage.Uzbek -> "Yakunlangan yetkazishlar shu yerda saqlanadi."
    }
    val open: String get() = when (language) {
        AppLanguage.English -> "Open"
        AppLanguage.Russian -> "Открыть"
        AppLanguage.Uzbek -> "Ochish"
    }
    val accept: String get() = when (language) {
        AppLanguage.English -> "Accept"
        AppLanguage.Russian -> "Принять"
        AppLanguage.Uzbek -> "Qabul qilish"
    }
    val details: String get() = when (language) {
        AppLanguage.English -> "Details"
        AppLanguage.Russian -> "Детали"
        AppLanguage.Uzbek -> "Tafsilotlar"
    }
    val orderDetails: String get() = when (language) {
        AppLanguage.English -> "Order details"
        AppLanguage.Russian -> "Детали заказа"
        AppLanguage.Uzbek -> "Buyurtma tafsilotlari"
    }
    val noOrderSelected: String get() = when (language) {
        AppLanguage.English -> "No order selected"
        AppLanguage.Russian -> "Заказ не выбран"
        AppLanguage.Uzbek -> "Buyurtma tanlanmagan"
    }
    val locationRequiredTitle: String get() = when (language) {
        AppLanguage.English -> "Turn on courier location"
        AppLanguage.Russian -> "Включите геолокацию курьера"
        AppLanguage.Uzbek -> "Kuryer geolokatsiyasini yoqing"
    }
    val locationRequiredText: String get() = when (language) {
        AppLanguage.English -> "The route to the customer appears after the app receives your real GPS point."
        AppLanguage.Russian -> "Маршрут к клиенту появится после получения вашей реальной GPS-точки."
        AppLanguage.Uzbek -> "Mijozgacha marshrut ilova haqiqiy GPS nuqtangizni olgandan keyin chiqadi."
    }
    val items: String get() = when (language) {
        AppLanguage.English -> "Items"
        AppLanguage.Russian -> "Позиции"
        AppLanguage.Uzbek -> "Mahsulotlar"
    }
    val total: String get() = when (language) {
        AppLanguage.English -> "Total"
        AppLanguage.Russian -> "Итого"
        AppLanguage.Uzbek -> "Jami"
    }
    val earning: String get() = when (language) {
        AppLanguage.English -> "Earning"
        AppLanguage.Russian -> "Доход"
        AppLanguage.Uzbek -> "Daromad"
    }
    val chatWithCustomer: String get() = when (language) {
        AppLanguage.English -> "Chat with customer"
        AppLanguage.Russian -> "Чат с клиентом"
        AppLanguage.Uzbek -> "Mijoz bilan chat"
    }
    val courierProfile: String get() = when (language) {
        AppLanguage.English -> "Courier profile"
        AppLanguage.Russian -> "Профиль курьера"
        AppLanguage.Uzbek -> "Kuryer profili"
    }
    val editProfile: String get() = when (language) {
        AppLanguage.English -> "Edit profile"
        AppLanguage.Russian -> "Изменить профиль"
        AppLanguage.Uzbek -> "Profilni o'zgartirish"
    }
    val settings: String get() = when (language) {
        AppLanguage.English -> "Settings"
        AppLanguage.Russian -> "Настройки"
        AppLanguage.Uzbek -> "Sozlamalar"
    }
    val languageTitle: String get() = when (language) {
        AppLanguage.English -> "Language"
        AppLanguage.Russian -> "Язык"
        AppLanguage.Uzbek -> "Til"
    }
    val languageDescription: String get() = when (language) {
        AppLanguage.English -> "Choose the courier app language."
        AppLanguage.Russian -> "Выберите язык приложения курьера."
        AppLanguage.Uzbek -> "Kuryer ilovasi tilini tanlang."
    }
    val deliveries: String get() = when (language) {
        AppLanguage.English -> "Deliveries"
        AppLanguage.Russian -> "Доставки"
        AppLanguage.Uzbek -> "Yetkazishlar"
    }
    val wallet: String get() = when (language) {
        AppLanguage.English -> "Wallet"
        AppLanguage.Russian -> "Кошелек"
        AppLanguage.Uzbek -> "Hamyon"
    }
    val payoutCard: String get() = when (language) {
        AppLanguage.English -> "Payout card"
        AppLanguage.Russian -> "Карта для вывода"
        AppLanguage.Uzbek -> "Pul yechish kartasi"
    }
    val payoutCardDescription: String get() = when (language) {
        AppLanguage.English -> "Withdraw courier earnings to this saved card."
        AppLanguage.Russian -> "Выводите заработок курьера на эту сохраненную карту."
        AppLanguage.Uzbek -> "Kuryer daromadini shu saqlangan kartaga yeching."
    }
    val addPayoutCard: String get() = when (language) {
        AppLanguage.English -> "Add payout card"
        AppLanguage.Russian -> "Добавить карту"
        AppLanguage.Uzbek -> "Karta qo'shish"
    }
    val changePayoutCard: String get() = when (language) {
        AppLanguage.English -> "Change"
        AppLanguage.Russian -> "Изменить"
        AppLanguage.Uzbek -> "O'zgartirish"
    }
    val savePayoutCard: String get() = when (language) {
        AppLanguage.English -> "Save card"
        AppLanguage.Russian -> "Сохранить карту"
        AppLanguage.Uzbek -> "Kartani saqlash"
    }
    val cardHolderName: String get() = when (language) {
        AppLanguage.English -> "Card holder"
        AppLanguage.Russian -> "Владелец карты"
        AppLanguage.Uzbek -> "Karta egasi"
    }
    val cardNumber: String get() = when (language) {
        AppLanguage.English -> "Card number"
        AppLanguage.Russian -> "Номер карты"
        AppLanguage.Uzbek -> "Karta raqami"
    }
    val expiry: String get() = when (language) {
        AppLanguage.English -> "Expiry"
        AppLanguage.Russian -> "Срок"
        AppLanguage.Uzbek -> "Muddat"
    }
    val payoutCardSecurity: String get() = when (language) {
        AppLanguage.English -> "Only brand and last 4 digits are saved for display."
        AppLanguage.Russian -> "Для отображения сохраняются только бренд и последние 4 цифры."
        AppLanguage.Uzbek -> "Ko'rsatish uchun faqat brend va oxirgi 4 raqam saqlanadi."
    }
    fun detectedCard(brand: String): String = when (language) {
        AppLanguage.English -> "Detected: $brand"
        AppLanguage.Russian -> "Определено: $brand"
        AppLanguage.Uzbek -> "Aniqlandi: $brand"
    }
    fun availableBalance(value: String): String = when (language) {
        AppLanguage.English -> "Available: $value"
        AppLanguage.Russian -> "Доступно: $value"
        AppLanguage.Uzbek -> "Mavjud: $value"
    }
    val withdrawBalance: String get() = when (language) {
        AppLanguage.English -> "Withdraw balance"
        AppLanguage.Russian -> "Вывести баланс"
        AppLanguage.Uzbek -> "Balansni yechish"
    }
    val withdrawalCreated: String get() = when (language) {
        AppLanguage.English -> "Withdrawal request created"
        AppLanguage.Russian -> "Заявка на вывод создана"
        AppLanguage.Uzbek -> "Pul yechish so'rovi yaratildi"
    }
    fun withdrawalCreatedToCard(last4: String): String = when (language) {
        AppLanguage.English -> "Withdrawal sent to •••• $last4"
        AppLanguage.Russian -> "Вывод отправлен на •••• $last4"
        AppLanguage.Uzbek -> "Pul •••• $last4 kartasiga yuborildi"
    }
    val noMoneyToWithdraw: String get() = when (language) {
        AppLanguage.English -> "No money to withdraw yet"
        AppLanguage.Russian -> "Пока нечего выводить"
        AppLanguage.Uzbek -> "Hozircha yechish uchun pul yo'q"
    }
    val logOut: String get() = when (language) {
        AppLanguage.English -> "Log out"
        AppLanguage.Russian -> "Выйти"
        AppLanguage.Uzbek -> "Chiqish"
    }
    val logOutQuestion: String get() = when (language) {
        AppLanguage.English -> "Do you want to leave this courier account? Your courier data will stay saved locally."
        AppLanguage.Russian -> "Выйти из аккаунта курьера? Данные курьера останутся сохраненными на устройстве."
        AppLanguage.Uzbek -> "Kuryer akkauntidan chiqasizmi? Kuryer ma'lumotlari qurilmada saqlanadi."
    }
    val cancel: String get() = when (language) {
        AppLanguage.English -> "Cancel"
        AppLanguage.Russian -> "Отмена"
        AppLanguage.Uzbek -> "Bekor qilish"
    }
    val editCourier: String get() = when (language) {
        AppLanguage.English -> "Edit courier"
        AppLanguage.Russian -> "Изменить курьера"
        AppLanguage.Uzbek -> "Kuryerni o'zgartirish"
    }
    val name: String get() = when (language) {
        AppLanguage.English -> "Name"
        AppLanguage.Russian -> "Имя"
        AppLanguage.Uzbek -> "Ism"
    }
    val vehicle: String get() = when (language) {
        AppLanguage.English -> "Vehicle"
        AppLanguage.Russian -> "Транспорт"
        AppLanguage.Uzbek -> "Transport"
    }
    val save: String get() = when (language) {
        AppLanguage.English -> "Save"
        AppLanguage.Russian -> "Сохранить"
        AppLanguage.Uzbek -> "Saqlash"
    }
    val noChatSelected: String get() = when (language) {
        AppLanguage.English -> "No chat selected"
        AppLanguage.Russian -> "Чат не выбран"
        AppLanguage.Uzbek -> "Chat tanlanmagan"
    }
    val messages: String get() = when (language) {
        AppLanguage.English -> "Messages"
        AppLanguage.Russian -> "Сообщения"
        AppLanguage.Uzbek -> "Xabarlar"
    }
    val reviews: String get() = when (language) {
        AppLanguage.English -> "Reviews"
        AppLanguage.Russian -> "Отзывы"
        AppLanguage.Uzbek -> "Sharhlar"
    }
    val noActiveChats: String get() = when (language) {
        AppLanguage.English -> "No active chats"
        AppLanguage.Russian -> "Активных чатов нет"
        AppLanguage.Uzbek -> "Faol chatlar yo'q"
    }
    val noActiveChatsHint: String get() = when (language) {
        AppLanguage.English -> "A chat opens while a customer has an active order and closes after delivery."
        AppLanguage.Russian -> "Чат открывается, пока у клиента есть активный заказ, и закрывается после доставки."
        AppLanguage.Uzbek -> "Mijozda faol buyurtma bo'lsa chat ochiladi, yetkazilgandan keyin yopiladi."
    }
    val chatClosed: String get() = when (language) {
        AppLanguage.English -> "This chat is closed after delivery"
        AppLanguage.Russian -> "Этот чат закрыт после доставки"
        AppLanguage.Uzbek -> "Bu chat yetkazilgandan keyin yopilgan"
    }
    val noReviewsYet: String get() = when (language) {
        AppLanguage.English -> "No reviews yet"
        AppLanguage.Russian -> "Отзывов пока нет"
        AppLanguage.Uzbek -> "Hozircha sharhlar yo'q"
    }
    val noReviewsHint: String get() = when (language) {
        AppLanguage.English -> "Customer ratings, stars, order numbers and comments will appear here."
        AppLanguage.Russian -> "Здесь появятся оценки клиентов, звезды, номера заказов и комментарии."
        AppLanguage.Uzbek -> "Bu yerda mijoz baholari, yulduzlar, buyurtma raqamlari va izohlar ko'rinadi."
    }
    fun orderNumber(value: String): String = when (language) {
        AppLanguage.English -> "Order #$value"
        AppLanguage.Russian -> "Заказ #$value"
        AppLanguage.Uzbek -> "Buyurtma #$value"
    }
    val writeSomething: String get() = when (language) {
        AppLanguage.English -> "Write something"
        AppLanguage.Russian -> "Напишите сообщение"
        AppLanguage.Uzbek -> "Xabar yozing"
    }
    val onlineReceivingOrders: String get() = when (language) {
        AppLanguage.English -> "Online and receiving orders"
        AppLanguage.Russian -> "В сети и принимает заказы"
        AppLanguage.Uzbek -> "Onlayn va buyurtmalarni qabul qilmoqda"
    }
    val offline: String get() = when (language) {
        AppLanguage.English -> "Offline"
        AppLanguage.Russian -> "Не в сети"
        AppLanguage.Uzbek -> "Oflayn"
    }
    val online: String get() = when (language) {
        AppLanguage.English -> "Online"
        AppLanguage.Russian -> "В сети"
        AppLanguage.Uzbek -> "Onlayn"
    }
    val goOnline: String get() = when (language) {
        AppLanguage.English -> "Go online"
        AppLanguage.Russian -> "Выйти онлайн"
        AppLanguage.Uzbek -> "Onlayn bo'lish"
    }
    val chat: String get() = when (language) {
        AppLanguage.English -> "Chat"
        AppLanguage.Russian -> "Чат"
        AppLanguage.Uzbek -> "Chat"
    }
    val pickup: String get() = when (language) {
        AppLanguage.English -> "Pickup"
        AppLanguage.Russian -> "Забрать"
        AppLanguage.Uzbek -> "Olib ketish"
    }
    val dropOff: String get() = when (language) {
        AppLanguage.English -> "Drop off"
        AppLanguage.Russian -> "Доставить"
        AppLanguage.Uzbek -> "Yetkazish"
    }
    val newOrderNotification: String get() = when (language) {
        AppLanguage.English -> "New order notification"
        AppLanguage.Russian -> "Уведомление о новом заказе"
        AppLanguage.Uzbek -> "Yangi buyurtma xabari"
    }
    fun availableOrdersWaiting(count: Int): String = when (language) {
        AppLanguage.English -> "$count available order(s) waiting for courier"
        AppLanguage.Russian -> "$count заказ(ов) ждут курьера"
        AppLanguage.Uzbek -> "$count ta buyurtma kuryerni kutmoqda"
    }
    val back: String get() = when (language) {
        AppLanguage.English -> "Back"
        AppLanguage.Russian -> "Назад"
        AppLanguage.Uzbek -> "Orqaga"
    }

    fun status(status: CourierOrderStatus): String = when (status) {
        CourierOrderStatus.Available -> when (language) {
            AppLanguage.English -> "Available"
            AppLanguage.Russian -> "Доступен"
            AppLanguage.Uzbek -> "Mavjud"
        }
        CourierOrderStatus.Accepted -> when (language) {
            AppLanguage.English -> "Accepted"
            AppLanguage.Russian -> "Принят"
            AppLanguage.Uzbek -> "Qabul qilindi"
        }
        CourierOrderStatus.ArrivedAtRestaurant -> when (language) {
            AppLanguage.English -> "At restaurant"
            AppLanguage.Russian -> "У ресторана"
            AppLanguage.Uzbek -> "Restoranda"
        }
        CourierOrderStatus.PickedUp -> when (language) {
            AppLanguage.English -> "Picked up"
            AppLanguage.Russian -> "Заказ забран"
            AppLanguage.Uzbek -> "Buyurtma olindi"
        }
        CourierOrderStatus.OnTheWay -> when (language) {
            AppLanguage.English -> "On the way"
            AppLanguage.Russian -> "В пути"
            AppLanguage.Uzbek -> "Yo'lda"
        }
        CourierOrderStatus.Delivered -> when (language) {
            AppLanguage.English -> "Delivered"
            AppLanguage.Russian -> "Доставлен"
            AppLanguage.Uzbek -> "Yetkazildi"
        }
    }

    fun nextAction(status: CourierOrderStatus): String? = when (status) {
        CourierOrderStatus.Available -> accept
        CourierOrderStatus.Accepted -> when (language) {
            AppLanguage.English -> "I arrived at restaurant"
            AppLanguage.Russian -> "Я у ресторана"
            AppLanguage.Uzbek -> "Restoranga yetdim"
        }
        CourierOrderStatus.ArrivedAtRestaurant -> when (language) {
            AppLanguage.English -> "Picked up order"
            AppLanguage.Russian -> "Забрал заказ"
            AppLanguage.Uzbek -> "Buyurtmani oldim"
        }
        CourierOrderStatus.PickedUp -> when (language) {
            AppLanguage.English -> "Start delivery"
            AppLanguage.Russian -> "Начать доставку"
            AppLanguage.Uzbek -> "Yetkazishni boshlash"
        }
        CourierOrderStatus.OnTheWay -> when (language) {
            AppLanguage.English -> "Delivered"
            AppLanguage.Russian -> "Доставлено"
            AppLanguage.Uzbek -> "Yetkazildi"
        }
        CourierOrderStatus.Delivered -> null
    }
}

private fun CourierOrderStatus.label(text: CourierText): String = text.status(this)

@Composable
private fun CourierOrderStatus.statusColor(): Color = when (this) {
    CourierOrderStatus.Available -> Orange
    CourierOrderStatus.Accepted -> Gold
    CourierOrderStatus.ArrivedAtRestaurant -> Gold
    CourierOrderStatus.PickedUp -> Orange
    CourierOrderStatus.OnTheWay -> Orange
    CourierOrderStatus.Delivered -> Success
}

private fun CourierDeliveryOrder.nextActionLabel(text: CourierText): String? = text.nextAction(status)

private fun CourierDeliveryOrder.nextStatus(): CourierOrderStatus = when (status) {
    CourierOrderStatus.Available -> CourierOrderStatus.Accepted
    CourierOrderStatus.Accepted -> CourierOrderStatus.ArrivedAtRestaurant
    CourierOrderStatus.ArrivedAtRestaurant -> CourierOrderStatus.PickedUp
    CourierOrderStatus.PickedUp -> CourierOrderStatus.OnTheWay
    CourierOrderStatus.OnTheWay -> CourierOrderStatus.Delivered
    CourierOrderStatus.Delivered -> CourierOrderStatus.Delivered
}
