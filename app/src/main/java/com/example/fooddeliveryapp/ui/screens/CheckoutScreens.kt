package com.example.fooddeliveryapp.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.fooddeliveryapp.ui.components.FoodlyImage
import androidx.core.content.ContextCompat
import com.example.fooddeliveryapp.backend.BackendResult
import com.example.fooddeliveryapp.backend.YandexGeocodingApi
import com.example.fooddeliveryapp.backend.YandexRoutingApi
import com.example.fooddeliveryapp.ui.AddressLabelOther
import com.example.fooddeliveryapp.ui.AppStrings
import com.example.fooddeliveryapp.ui.AppLanguage
import com.example.fooddeliveryapp.ui.displayLabel
import com.example.fooddeliveryapp.ui.displayTitle
import com.example.fooddeliveryapp.ui.FoodAppState
import com.example.fooddeliveryapp.ui.LocalAppLanguage
import com.example.fooddeliveryapp.ui.LocalAppStrings
import com.example.fooddeliveryapp.ui.yandexGeocoderLanguage
import com.example.fooddeliveryapp.ui.components.AppTextField
import com.example.fooddeliveryapp.ui.components.BreakdownRow
import com.example.fooddeliveryapp.ui.components.CircleIconSurface
import com.example.fooddeliveryapp.ui.components.EmptyOrdersCard
import com.example.fooddeliveryapp.ui.components.EmptyStateScreen
import com.example.fooddeliveryapp.ui.components.PaymentBreakdownCard
import com.example.fooddeliveryapp.ui.components.PrimaryButton
import com.example.fooddeliveryapp.ui.components.QtyButton
import com.example.fooddeliveryapp.ui.components.SecondaryButton
import com.example.fooddeliveryapp.ui.components.StatusChip
import com.example.fooddeliveryapp.ui.components.SummaryCard
import com.example.fooddeliveryapp.ui.components.TopBar
import com.example.fooddeliveryapp.ui.components.asPrice
import com.example.fooddeliveryapp.ui.data.ChatAuthor
import com.example.fooddeliveryapp.ui.data.CourierDeliveryOrder
import com.example.fooddeliveryapp.ui.data.CourierOrderStatus
import com.example.fooddeliveryapp.ui.data.DeliveryAddress
import com.example.fooddeliveryapp.ui.data.DiscountCoupon
import com.example.fooddeliveryapp.ui.data.GeoPoint
import com.example.fooddeliveryapp.ui.data.OrderStatus
import com.example.fooddeliveryapp.ui.data.OrderReview
import com.example.fooddeliveryapp.ui.data.OrderSummary
import com.example.fooddeliveryapp.ui.data.PaymentCard
import com.example.fooddeliveryapp.ui.data.PaymentMethod
import com.example.fooddeliveryapp.ui.data.Restaurant
import com.example.fooddeliveryapp.ui.data.detectPaymentMethod
import com.example.fooddeliveryapp.ui.map.YandexRouteMap
import com.example.fooddeliveryapp.ui.theme.CardWhite
import com.example.fooddeliveryapp.ui.theme.Cream
import com.example.fooddeliveryapp.ui.theme.Gold
import com.example.fooddeliveryapp.ui.theme.Ink
import com.example.fooddeliveryapp.ui.theme.InkSoft
import com.example.fooddeliveryapp.ui.theme.Night
import com.example.fooddeliveryapp.ui.theme.Orange
import com.example.fooddeliveryapp.ui.theme.OrangeDeep
import com.example.fooddeliveryapp.ui.theme.OrangeSoft
import com.example.fooddeliveryapp.ui.theme.Rose
import com.example.fooddeliveryapp.ui.theme.Success
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CartScreen(
    appState: FoodAppState,
    onBack: () -> Unit,
    onEditAddress: () -> Unit,
    onCheckout: () -> Unit,
    onContinueShopping: () -> Unit,
) {
    val strings = LocalAppStrings.current
    LaunchedEffect(appState.selectedAddress, appState.cartCount, appState.cartRestaurant.id) {
        appState.refreshDeliveryQuote()
    }

    if (appState.cartItems.isEmpty()) {
        EmptyStateScreen(
            title = strings.emptyCartTitle,
            description = strings.emptyCartDescription,
            actionLabel = strings.goHome,
            onAction = onContinueShopping,
            onBack = onBack,
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { TopBar(title = strings.cart, onBack = onBack) }
        item {
            Card(shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = Night)) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    appState.cartItems.forEach { line ->
                        CartItemRow(
                            line = line,
                            onMinus = { appState.changeQuantity(line.item.id, -1) },
                            onPlus = { appState.changeQuantity(line.item.id, 1) },
                            onRemove = { appState.removeFromCart(line.item.id) },
                        )
                    }
                }
            }
        }
        item {
            CartDeliveryAddressSection(
                appState = appState,
                onAddAddress = onEditAddress,
            )
        }
        item {
            PaymentBreakdownCard(
                subtotal = appState.subtotal,
                delivery = appState.deliveryFee,
                service = appState.serviceFee,
                total = appState.total,
                discount = appState.discountAmount,
            )
        }
        item {
            PrimaryButton(
                text = strings.checkoutToPay(appState.total.asPrice()),
                onClick = onCheckout,
            )
        }
    }
}

@Composable
fun PaymentScreen(
    appState: FoodAppState,
    checkoutMode: Boolean = false,
    onBack: () -> Unit,
    onAddCard: () -> Unit,
    onPay: () -> Unit,
) {
    val strings = LocalAppStrings.current
    var expandedCardId by rememberSaveable { mutableStateOf<String?>(null) }
    var couponInput by rememberSaveable { mutableStateOf(appState.appliedCoupon?.code.orEmpty()) }
    var couponMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var couponMessageIsError by rememberSaveable { mutableStateOf(false) }
    val methodCards = appState.savedPaymentCards.filter { it.matches(appState.selectedPaymentMethod) }

    LaunchedEffect(checkoutMode, appState.selectedAddress, appState.cartCount, appState.cartRestaurant.id) {
        appState.pruneExpiredCoupons()
        if (checkoutMode) {
            appState.refreshDeliveryQuote()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        PaymentHeader(title = strings.payment, onBack = onBack)
        PaymentMethodSelector(
            selectedMethod = appState.selectedPaymentMethod,
            onSelect = { method ->
                appState.selectPaymentMethod(method)
                expandedCardId = null
            },
        )
        PaymentSavedArea(
            method = appState.selectedPaymentMethod,
            cards = methodCards,
            selectedCardId = appState.selectedPaymentCardId,
            expandedCardId = expandedCardId,
            checkoutMode = checkoutMode,
            onSelectCard = { card ->
                appState.selectPaymentCard(card.id)
                expandedCardId = if (expandedCardId == card.id) null else card.id
            },
            onDeleteCard = { card ->
                appState.deletePaymentCard(card.id)
                if (expandedCardId == card.id) expandedCardId = null
            },
        )
        if (!checkoutMode) {
            AddNewPaymentButton(onClick = onAddCard)
        }
        if (checkoutMode) {
            PromoCodeCard(
                code = couponInput,
                appliedCoupon = appState.appliedCoupon,
                discountAmount = appState.discountAmount,
                message = couponMessage,
                messageIsError = couponMessageIsError,
                onCodeChange = {
                    couponInput = it.uppercase(Locale.US)
                    couponMessage = null
                    couponMessageIsError = false
                },
                onApply = {
                    val error = appState.applyCouponCode(couponInput)
                    couponMessage = error ?: strings.couponApplied
                    couponMessageIsError = error != null
                    if (error == null) {
                        couponInput = appState.appliedCoupon?.code.orEmpty()
                    }
                },
                onClear = {
                    appState.clearAppliedCoupon()
                    couponInput = ""
                    couponMessage = null
                    couponMessageIsError = false
                },
            )
            PaymentBreakdownCard(
                subtotal = appState.subtotal,
                delivery = appState.deliveryFee,
                service = appState.serviceFee,
                total = appState.total,
                discount = appState.discountAmount,
            )
        }
        appState.lastBackendError?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Rose,
                textAlign = TextAlign.Center,
            )
        }
        if (checkoutMode) {
            PaymentBottomBar(
                total = appState.total,
                isPlacingOrder = appState.isPlacingOrder,
                onPay = onPay,
            )
        }
    }
}

@Composable
private fun CartDeliveryAddressSection(
    appState: FoodAppState,
    onAddAddress: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val language = appState.language
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLocating by rememberSaveable { mutableStateOf(false) }
    var locationMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var locationMessageIsError by rememberSaveable { mutableStateOf(false) }
    var requestCurrentAddress: () -> Unit = {}

    fun handleLocationError(message: String) {
        isLocating = false
        locationMessage = message
        locationMessageIsError = true
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            requestCurrentAddress()
        } else {
            handleLocationError(language.cartLocationPermissionDenied())
        }
    }

    val locationSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        requestCurrentAddress()
    }

    requestCurrentAddress = {
        locationMessage = null
        locationMessageIsError = false
        when {
            hasCartLocationPermission(context).not() -> {
                locationMessage = language.cartLocationPermissionHint()
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ),
                )
            }

            isCartLocationEnabled(context).not() -> {
                locationMessage = language.cartLocationDisabled()
                locationSettingsLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }

            else -> {
                isLocating = true
                requestCurrentCartGeoPoint(
                    context = context,
                    language = language,
                    onPoint = { point ->
                        scope.launch {
                            val resolvedTitle = when (val result = YandexGeocodingApi.reverseGeocode(point, language.yandexGeocoderLanguage())) {
                                is BackendResult.Success -> result.data.formattedAddress
                                is BackendResult.Error -> null
                            }
                            appState.saveCurrentLocationAddress(
                                point.asCartCurrentLocationAddress(strings, resolvedTitle),
                            )
                            isLocating = false
                            locationMessage = language.currentAddressSaved()
                            locationMessageIsError = false
                        }
                    },
                    onError = ::handleLocationError,
                )
            }
        }
    }

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        text = strings.whereDeliver,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = strings.chooseDeliveryAddress,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(onClick = onAddAddress) {
                    Text(strings.addAddress, color = Orange)
                }
                TextButton(onClick = requestCurrentAddress, enabled = !isLocating) {
                    Text(
                        text = if (isLocating) language.currentAddressLoading() else language.currentAddressAction(),
                        color = Orange,
                    )
                }
            }
            appState.savedAddresses.forEachIndexed { index, address ->
                DeliveryAddressChoiceRow(
                    address = address,
                    selected = address == appState.selectedAddress,
                    onClick = { appState.selectSavedAddress(index) },
                )
            }
            locationMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (locationMessageIsError) Rose else Orange,
                )
            }
        }
    }
}

@Composable
private fun DeliveryAddressChoiceRow(
    address: DeliveryAddress,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val strings = LocalAppStrings.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Orange.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = if (selected) Orange else MaterialTheme.colorScheme.outline.copy(alpha = 0.28f),
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (selected) Orange else MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.LocalShipping,
                contentDescription = null,
                tint = if (selected) CardWhite else Orange,
                modifier = Modifier.size(22.dp),
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = address.displayLabel(strings),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = address.displayTitle(strings),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = address.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (selected) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Orange)
        } else {
            Text(text = LocalAppStrings.current.choose, style = MaterialTheme.typography.labelLarge, color = Orange)
        }
    }
}

@Composable
fun AddPaymentCardScreen(
    appState: FoodAppState,
    onClose: () -> Unit,
    onSaved: () -> Unit,
) {
    val strings = LocalAppStrings.current
    var holderName by rememberSaveable { mutableStateOf(appState.currentUser?.name ?: appState.profileDetails.fullName) }
    var cardNumber by rememberSaveable { mutableStateOf("") }
    var expiry by rememberSaveable { mutableStateOf("") }
    var cvv by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var isSaving by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val digits = cardNumber.cardDigits()
    val detectedMethod = detectPaymentMethod(cardNumber)
    val cardNumberValue = remember(cardNumber) {
        TextFieldValue(text = cardNumber, selection = TextRange(cardNumber.length))
    }
    val expiryValue = remember(expiry) {
        TextFieldValue(text = expiry, selection = TextRange(expiry.length))
    }
    val isHumo = detectedMethod == PaymentMethod.HumoCard
    val canSave = holderName.trim().length >= 2 &&
        detectedMethod != null &&
        digits.length == 16 &&
        expiry.isValidExpiryInput() &&
        (isHumo || cvv.cardDigits().length in 3..4)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 20.dp, top = 36.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            }
            Text(strings.addCard, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
        }

        AppTextField(
            value = holderName,
            onValueChange = { holderName = it },
            label = strings.cardHolderName,
            placeholder = "Vishal Khadok",
        )
        AddCardNumberField(
            value = cardNumberValue,
            detectedLabel = detectedMethod?.let(strings::paymentMethodTitle),
            onValueChange = { next -> cardNumber = next.text.formatCardNumberInput() },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            AddExpiryField(
                value = expiryValue,
                onValueChange = { next -> expiry = next.text.formatExpiryInput() },
                modifier = Modifier.weight(1f),
            )
            if (!isHumo) {
                AppTextField(
                    value = cvv,
                    onValueChange = { next -> cvv = next.cardDigits().take(4) },
                    label = "CVV",
                    placeholder = "***",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    isPassword = true,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        if (isHumo) {
            Text(
                text = "HumoCard works without CVV.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        error?.let { message ->
            Text(message, style = MaterialTheme.typography.bodyMedium, color = Rose)
        }
        Spacer(modifier = Modifier.weight(1f))
        PrimaryButton(
            text = if (isSaving) strings.saving else strings.addMakePayment,
            enabled = canSave && !isSaving,
            onClick = {
                scope.launch {
                    isSaving = true
                    error = appState.addPaymentCard(
                        number = cardNumber,
                        holderName = holderName,
                        expiry = expiry,
                        cvv = if (isHumo) "" else cvv,
                    )
                    isSaving = false
                    if (error == null) {
                        onSaved()
                    }
                }
            },
        )
    }
}

@Composable
private fun AddCardNumberField(
    value: TextFieldValue,
    detectedLabel: String?,
    onValueChange: (TextFieldValue) -> Unit,
) {
    val strings = LocalAppStrings.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = strings.cardNumber,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = value,
            onValueChange = { raw ->
                val formatted = raw.text.formatCardNumberInput()
                onValueChange(TextFieldValue(text = formatted, selection = TextRange(formatted.length)))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = {
                Text(
                    text = "2134 1234 1234 1234",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Orange.copy(alpha = 0.42f),
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
        )
        detectedLabel?.let { label ->
            Text(
                text = strings.detected(label),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = Orange,
            )
        }
    }
}

@Composable
private fun AddExpiryField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = strings.expireDate,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = value,
            onValueChange = { raw ->
                val formatted = raw.text.formatExpiryInput()
                onValueChange(TextFieldValue(text = formatted, selection = TextRange(formatted.length)))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = {
                Text(
                    text = "mm/yyyy",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Orange.copy(alpha = 0.42f),
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
        )
    }
}

@Composable
fun SuccessScreen(
    order: OrderSummary,
    onTrack: () -> Unit,
    onGoHome: () -> Unit,
) {
    val strings = LocalAppStrings.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(RoundedCornerShape(48.dp))
                .background(Brush.linearGradient(listOf(OrangeSoft, CardWhite, Gold.copy(alpha = 0.22f)))),
            contentAlignment = Alignment.Center,
        ) {
            Text("🎉", style = MaterialTheme.typography.displayLarge.copy(fontSize = MaterialTheme.typography.displayLarge.fontSize * 2.4))
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(strings.successTitle, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "${order.restaurantName}\n${order.itemsLabel}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(28.dp))
        PrimaryButton(text = strings.trackOrder, onClick = onTrack)
        Spacer(modifier = Modifier.height(12.dp))
        SecondaryButton(text = strings.goHome, onClick = onGoHome, accent = Orange)
    }
}

@Composable
fun OrdersScreen(
    appState: FoodAppState,
    onBack: () -> Unit,
    onTrack: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    onReorder: (OrderSummary) -> Unit,
) {
    val strings = LocalAppStrings.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var reviewOrderId by rememberSaveable { mutableStateOf<String?>(null) }
    var recentRefundOrderId by rememberSaveable { mutableStateOf<String?>(null) }
    var recentRefundUntilMillis by rememberSaveable { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000L)
            nowMillis = System.currentTimeMillis()
        }
    }
    val activeOrders = appState.ongoingOrders.filter { it.status != OrderStatus.Delivered && it.status != OrderStatus.Cancelled }
    val historyOrders = appState.historyOrders.filter { it.status == OrderStatus.Delivered || it.status == OrderStatus.Cancelled }
    val reviewOrder = reviewOrderId?.let { id -> historyOrders.firstOrNull { it.id == id } }

    if (reviewOrder != null) {
        OrderReviewDialog(
            order = reviewOrder,
            existingReview = appState.reviewForOrder(reviewOrder.id),
            onDismiss = { reviewOrderId = null },
            onSubmit = { courierRating, orderRating, comment ->
                appState.rateOrder(reviewOrder.id, courierRating, orderRating, comment)
                reviewOrderId = null
            },
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 22.dp, top = 28.dp, end = 22.dp, bottom = 118.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { OrdersHeader(title = strings.orders, onBack = onBack) }
        item {
            OrdersTabs(
                selectedTab = selectedTab,
                ongoingLabel = strings.ongoing,
                historyLabel = strings.history,
                onSelect = { selectedTab = it },
            )
        }
        if (appState.refundBalance > 0 && nowMillis < recentRefundUntilMillis) {
            item { RefundBalanceCard(balance = appState.refundBalance) }
        }
        if (appState.lastBackendError != null && selectedTab == 0) {
            item {
                Text(
                    text = appState.lastBackendError.orEmpty(),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Rose,
                )
            }
        }
        if (selectedTab == 0) {
            if (activeOrders.isEmpty()) {
                item {
                    EmptyOrdersCard(
                        title = strings.noActiveOrdersTitle,
                        description = strings.noActiveOrdersDescription,
                    )
                }
            } else {
                items(activeOrders, key = { it.id }) { order ->
                    val restaurant = appState.restaurantForOrder(order)
                    ActiveOrderCard(
                        order = order,
                        restaurant = restaurant,
                        canCancel = appState.canCancelOrder(order, nowMillis),
                        cancelTimeLeftMillis = appState.cancellationTimeLeftMillis(order, nowMillis),
                        courierStatus = appState.courierOrderForOrder(order.id)?.status?.customerLabel(strings),
                        chatUnread = appState.isCustomerChatUnread(order.id),
                        onTrack = { onTrack(order.id) },
                        onOpenChat = { onOpenChat(order.id) },
                        onCancel = {
                            if (appState.cancelOrder(order.id) == null) {
                                recentRefundOrderId = order.id
                                recentRefundUntilMillis = System.currentTimeMillis() + RefundNoticeDurationMillis
                            }
                        },
                    )
                }
            }
        } else {
            if (historyOrders.isEmpty()) {
                item {
                    EmptyOrdersCard(
                        title = strings.emptyHistoryTitle,
                        description = strings.emptyHistoryDescription,
                    )
                }
            } else {
                items(historyOrders, key = { it.id }) { order ->
                    HistoryOrderCard(
                        order = order,
                        restaurant = appState.restaurantForOrder(order),
                        rated = appState.reviewForOrder(order.id) != null,
                        showRefundNotice = order.id == recentRefundOrderId && nowMillis < recentRefundUntilMillis,
                        onRate = { reviewOrderId = order.id },
                        onReorder = { onReorder(order) },
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerOrderChatScreen(
    appState: FoodAppState,
    onBack: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val order = appState.trackedOrder
    if (order == null) {
        EmptyStateScreen(
            title = strings.noOrderSelected,
            description = strings.openChatFromOrder,
            actionLabel = strings.back,
            onAction = onBack,
            onBack = onBack,
        )
        return
    }
    if (order.status == OrderStatus.Delivered || order.status == OrderStatus.Cancelled) {
        EmptyStateScreen(
            title = strings.courierChat,
            description = strings.chatClosedAfterDelivery,
            actionLabel = strings.back,
            onAction = onBack,
            onBack = onBack,
        )
        return
    }
    val messages = appState.chatMessagesForOrder(order.id)
    var draft by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(order.id) {
        appState.markCustomerChatRead(order.id)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding(),
    ) {
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
                Column {
                    Text(strings.courierChat, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(order.restaurantName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(messages, key = { it.id }) { message ->
                CustomerChatBubble(
                    text = message.text,
                    time = message.timeLabel,
                    mine = message.author == ChatAuthor.Customer,
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
                placeholder = { Text(strings.writeToCourier) },
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
                    appState.sendCustomerMessage(order.id, draft)
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
private fun CustomerChatBubble(
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
private fun OrdersHeader(
    title: String,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            }
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun OrdersTabs(
    selectedTab: Int,
    ongoingLabel: String,
    historyLabel: String,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        OrderTabButton(
            label = ongoingLabel,
            selected = selectedTab == 0,
            onClick = { onSelect(0) },
            modifier = Modifier.weight(1f),
        )
        OrderTabButton(
            label = historyLabel,
            selected = selectedTab == 1,
            onClick = { onSelect(1) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun OrderTabButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = if (selected) Orange else Color(0xFF9BA0AF),
        )
        Spacer(modifier = Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(if (selected) Orange else Color(0xFFEDEEF3)),
        )
    }
}

@Composable
private fun RefundBalanceCard(balance: Int) {
    val strings = LocalAppStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(OrangeSoft)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(strings.refundBalance, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(balance.asPrice(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        Text(strings.returnedToAccount, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Success)
    }
}

@Composable
private fun ActiveOrderCard(
    order: OrderSummary,
    restaurant: Restaurant?,
    canCancel: Boolean,
    cancelTimeLeftMillis: Long,
    courierStatus: String?,
    chatUnread: Boolean,
    onTrack: () -> Unit,
    onOpenChat: () -> Unit,
    onCancel: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val deliveryStatus = courierStatus ?: order.customerStatusLabel(strings)
    OrderListItemScaffold(
        order = order,
        restaurant = restaurant,
        statusLabel = deliveryStatus,
        statusText = strings.ongoing,
        statusColor = Orange,
        metaText = order.itemsCountLabel(strings),
    ) {
        LiveOrderTracker(order = order)
        Text(
            text = strings.statusPrefix(deliveryStatus),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = order.eta,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = if (canCancel) {
                strings.cancelWindow(cancelTimeLeftMillis.asMinuteSecond())
            } else {
                strings.cancellationLocked
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (canCancel) InkSoft else Rose,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OrderActionButton(
                text = strings.trackOrder,
                filled = true,
                onClick = onTrack,
                modifier = Modifier.weight(1f),
            )
            OrderActionButton(
                text = if (chatUnread) "${strings.chat} (1)" else strings.chat,
                filled = true,
                onClick = onOpenChat,
                modifier = Modifier.weight(1f),
            )
            OrderActionButton(
                text = strings.cancel,
                filled = false,
                enabled = canCancel,
                onClick = onCancel,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun HistoryOrderCard(
    order: OrderSummary,
    restaurant: Restaurant?,
    rated: Boolean,
    showRefundNotice: Boolean,
    onRate: () -> Unit,
    onReorder: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val completed = order.status == OrderStatus.Delivered
    OrderListItemScaffold(
        order = order,
        restaurant = restaurant,
        statusLabel = order.activeCategoryLabel(strings),
        statusText = strings.status(order.status),
        statusColor = if (completed) Success else Rose,
        metaText = "${order.orderDateLabel()} · ${order.itemsCountLabel(strings)}",
    ) {
        if (showRefundNotice && order.refundedAmount > 0) {
            Text(
                text = strings.refundedToAccount(order.refundedAmount.asPrice()),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = Success,
            )
        }
        order.itemLines().take(4).forEach { line ->
            Text(
                text = line,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(40.dp),
        ) {
            OrderActionButton(
                text = if (rated) strings.rated else strings.rate,
                filled = false,
                onClick = onRate,
                modifier = Modifier.weight(1f),
            )
            OrderActionButton(
                text = strings.reorder,
                filled = true,
                onClick = onReorder,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun OrderReviewDialog(
    order: OrderSummary,
    existingReview: OrderReview?,
    onDismiss: () -> Unit,
    onSubmit: (courierRating: Int, orderRating: Int, comment: String) -> Unit,
) {
    val strings = LocalAppStrings.current
    var courierRating by rememberSaveable(order.id, existingReview?.courierRating) {
        mutableIntStateOf(existingReview?.courierRating ?: 5)
    }
    var orderRating by rememberSaveable(order.id, existingReview?.orderRating) {
        mutableIntStateOf(existingReview?.orderRating ?: 5)
    }
    var comment by rememberSaveable(order.id, existingReview?.comment) {
        mutableStateOf(existingReview?.comment.orEmpty())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.rateOrder) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(order.restaurantName, style = MaterialTheme.typography.titleMedium)
                RatingSelector(strings.courier, courierRating) { courierRating = it }
                RatingSelector(strings.orderLabel, orderRating) { orderRating = it }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    placeholder = { Text(strings.reviewPlaceholder) },
                    shape = RoundedCornerShape(8.dp),
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(courierRating, orderRating, comment) }) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        },
    )
}

@Composable
private fun RatingSelector(
    label: String,
    value: Int,
    onChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            (1..5).forEach { rating ->
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (rating <= value) Orange else Color(0xFFE1E4EA),
                    modifier = Modifier
                        .size(26.dp)
                        .clickable { onChange(rating) },
                )
            }
        }
    }
}

@Composable
private fun OrderListItemScaffold(
    order: OrderSummary,
    restaurant: Restaurant?,
    statusLabel: String,
    statusText: String,
    statusColor: Color,
    metaText: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(statusLabel, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Text(statusText, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = statusColor)
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFF0F1F5)),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FoodlyImage(
                model = restaurant?.imageUrl.orEmpty(),
                contentDescription = order.restaurantName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1F2F5)),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(order.restaurantName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(order.total.asPrice(), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .width(1.dp)
                            .height(14.dp)
                            .background(Color(0xFFDADDE5)),
                    )
                    Text(metaText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Text(
                text = order.shortOrderId(),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF8C93A5),
            )
        }
        content()
    }
}

@Composable
private fun LiveOrderTracker(order: OrderSummary) {
    val strings = LocalAppStrings.current
    val progress = when (order.status) {
        OrderStatus.Preparing -> 0.28f
        OrderStatus.OnTheWay -> 0.68f
        OrderStatus.Delivered -> 1f
        OrderStatus.Cancelled -> 0f
    }

    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(strings.liveTracker, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Text(order.visibleEta(strings), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFF0F1F5)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(Orange),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(strings.kitchen, style = MaterialTheme.typography.bodySmall, color = if (progress >= 0.28f) Orange else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(strings.courier, style = MaterialTheme.typography.bodySmall, color = if (progress >= 0.68f) Orange else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(strings.door, style = MaterialTheme.typography.bodySmall, color = if (progress >= 1f) Success else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun OrderActionButton(
    text: String,
    filled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val color = if (enabled) Orange else Color(0xFFC7CBD5)
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(if (filled && enabled) Orange else CardWhite)
            .border(1.dp, if (filled) Color.Transparent else color, RoundedCornerShape(7.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = if (filled && enabled) CardWhite else color,
        )
    }
}

private fun OrderSummary.activeCategoryLabel(strings: AppStrings): String =
    if (itemsLabel.contains("soda", ignoreCase = true) ||
        itemsLabel.contains("coffee", ignoreCase = true) ||
        itemsLabel.contains("smoothie", ignoreCase = true) ||
        itemsLabel.contains("drink", ignoreCase = true)
    ) {
        strings.menuGroup("Drink")
    } else {
        strings.food
    }

private fun CourierOrderStatus.customerLabel(strings: AppStrings): String = strings.courierCustomerStatus(this)

private fun OrderSummary.customerStatusLabel(strings: AppStrings): String = strings.customerOrderStatus(status)

private fun OrderSummary.itemsCountLabel(strings: AppStrings): String {
    val quantity = Regex("""(\d+)x""")
        .findAll(itemsLabel)
        .mapNotNull { match -> match.groupValues.getOrNull(1)?.toIntOrNull() }
        .sum()
        .takeIf { it > 0 }
        ?: itemsLabel.split(",").count { it.isNotBlank() }.coerceAtLeast(1)
    return strings.itemsCount(quantity)
}

private fun OrderSummary.shortOrderId(): String {
    val digits = id.filter(Char::isDigit).takeLast(6).ifBlank { id.takeLast(6) }
    return "#${digits.chunked(3).joinToString(" ")}"
}

private fun OrderSummary.orderDateLabel(): String =
    Instant.ofEpochMilli(createdAtMillis)
        .atZone(ZoneId.systemDefault())
        .format(OrderDateFormatter)

private fun DiscountCoupon.expiresAtLabel(): String =
    Instant.ofEpochMilli(expiresAtMillis)
        .atZone(ZoneId.systemDefault())
        .format(CouponDateFormatter)

private fun OrderSummary.itemLines(): List<String> =
    itemsLabel
        .split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }

private fun OrderSummary.visibleEta(strings: AppStrings): String =
    if (status == OrderStatus.Cancelled) strings.status(OrderStatus.Cancelled) else eta

private fun Long.asMinuteSecond(): String {
    val totalSeconds = (this / 1_000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%02d:%02d".format(Locale.US, minutes, seconds)
}

private fun GeoPoint.haversineDistanceKm(other: GeoPoint): Double {
    val earthRadiusKm = 6371.0
    val latitudeDelta = Math.toRadians(other.latitude - latitude)
    val longitudeDelta = Math.toRadians(other.longitude - longitude)
    val startLatitude = Math.toRadians(latitude)
    val endLatitude = Math.toRadians(other.latitude)
    val sinLatitude = kotlin.math.sin(latitudeDelta / 2)
    val sinLongitude = kotlin.math.sin(longitudeDelta / 2)
    val a = sinLatitude * sinLatitude +
        sinLongitude * sinLongitude * kotlin.math.cos(startLatitude) * kotlin.math.cos(endLatitude)
    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    return ((earthRadiusKm * c * 10.0).toInt() / 10.0)
}

private val OrderDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM, HH:mm", Locale.US)

private val CouponDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd.MM.yyyy")

private const val RefundNoticeDurationMillis = 5_000L

@Composable
fun TrackOrderScreen(
    order: OrderSummary,
    restaurant: Restaurant?,
    address: DeliveryAddress,
    courierOrder: CourierDeliveryOrder? = null,
    onBack: () -> Unit,
) {
    val strings = LocalAppStrings.current
    var mapExpanded by rememberSaveable { mutableStateOf(true) }
    val sheetScrollState = rememberScrollState()

    LaunchedEffect(sheetScrollState.value) {
        if (sheetScrollState.value > 18 && mapExpanded) {
            mapExpanded = false
        } else if (sheetScrollState.value == 0 && mapExpanded.not()) {
            mapExpanded = true
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        val expandedSheetTop = (maxHeight - 126.dp).coerceAtLeast(360.dp)
        val collapsedSheetTop = 112.dp
        val sheetTop by animateDpAsState(
            targetValue = if (mapExpanded) expandedSheetTop else collapsedSheetTop,
            animationSpec = tween(durationMillis = 340),
            label = "trackSheetTop",
        )
        val mapHeight by animateDpAsState(
            targetValue = if (mapExpanded) maxHeight else 0.dp,
            animationSpec = tween(durationMillis = 340),
            label = "trackMapHeight",
        )
        val toggleTop by animateDpAsState(
            targetValue = if (mapExpanded) (sheetTop - 24.dp).coerceAtLeast(88.dp) else 72.dp,
            animationSpec = tween(durationMillis = 340),
            label = "trackToggleTop",
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(mapHeight)
                .align(Alignment.TopCenter),
        ) {
            TrackRouteMapLayer(
                address = address,
                courierPoint = courierOrder?.courierPoint,
                modifier = Modifier.fillMaxSize(),
            )
        }

        TrackHeader(
            title = strings.trackOrder,
            onBack = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .zIndex(3f),
        )

        TrackMapToggleButton(
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
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(sheetScrollState)
                .padding(start = 22.dp, top = 10.dp, end = 22.dp, bottom = 28.dp)
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
            TrackRestaurantSummary(order = order, restaurant = restaurant)
            TrackProgressSection(order = order)
            TrackOrderDetails(order = order, address = address, courierOrder = courierOrder)
            CourierCard(status = order.status, courierOrder = courierOrder)
        }
    }
}

@Composable
private fun TrackHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .statusBarsPadding()
            .padding(start = 22.dp, top = 16.dp, end = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(15.dp),
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFF1F2028))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = CardWhite,
                modifier = Modifier.size(19.dp),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun TrackMapToggleButton(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
            contentDescription = null,
            tint = Orange,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun TrackRouteMapLayer(
    address: DeliveryAddress,
    courierPoint: GeoPoint?,
    modifier: Modifier = Modifier,
) {
    val language = LocalAppLanguage.current

    if (courierPoint != null) {
        YandexRouteMap(
            startPoint = courierPoint,
            endPoint = address.point,
            startLabel = language.courierMapLabel(),
            endLabel = language.customerSelfMapLabel(),
            modifier = modifier,
            cornerRadius = 0.dp,
        )
    } else {
        StaticTrackMapArt(modifier = modifier)
    }
}

@Composable
private fun TrackRestaurantSummary(
    order: OrderSummary,
    restaurant: Restaurant?,
) {
    val strings = LocalAppStrings.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        val imageUrl = restaurant?.imageUrl.orEmpty()
        if (imageUrl.isNotBlank()) {
            FoodlyImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(OrangeSoft),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(OrangeSoft),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = restaurant?.emoji ?: "Food", style = MaterialTheme.typography.bodySmall)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = order.restaurantName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = strings.orderAt(order.orderDateLabel()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            order.itemLines().take(4).forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun TrackProgressSection(order: OrderSummary) {
    val strings = LocalAppStrings.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        LiveOrderTracker(order = order)
        if (order.status == OrderStatus.Cancelled && order.refundedAmount > 0) {
            Text(
                text = strings.refundedToAccount(order.refundedAmount.asPrice()),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = Success,
            )
        }
    }
}

@Composable
private fun TrackOrderDetails(
    order: OrderSummary,
    address: DeliveryAddress,
    courierOrder: CourierDeliveryOrder?,
) {
    val strings = LocalAppStrings.current
    var liveDistanceKm by remember(order.id, courierOrder?.status, courierOrder?.courierPoint, address.point) {
        mutableStateOf<Double?>(null)
    }
    val shouldShowLiveDistance = courierOrder?.status != null &&
        courierOrder.status != CourierOrderStatus.Available &&
        courierOrder.courierPoint != null

    LaunchedEffect(order.id, courierOrder?.status, courierOrder?.courierPoint, address.point) {
        if (!shouldShowLiveDistance) {
            liveDistanceKm = null
            return@LaunchedEffect
        }
        val courierPoint = courierOrder?.courierPoint ?: return@LaunchedEffect
        liveDistanceKm = when (val result = YandexRoutingApi.distance(courierPoint, address.point)) {
            is BackendResult.Success -> ((result.data.distanceMeters / 100.0).toInt() / 10.0)
            is BackendResult.Error -> courierPoint.haversineDistanceKm(address.point)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TrackDetailRow(strings.eta, order.visibleEta(strings))
        liveDistanceKm?.let { distance ->
            TrackDetailRow(strings.distance, strings.kmByRoute(distance))
        }
        TrackDetailRow(strings.address, address.displayTitle(strings))
        TrackDetailRow(strings.paymentLabel, order.paymentLabel)
    }
}

@Composable
private fun TrackDetailRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LegacyTrackOrderScreen(
    order: OrderSummary,
    restaurant: Restaurant?,
    address: DeliveryAddress,
    onBack: () -> Unit,
) {
    val strings = LocalAppStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        TopBar(title = "Отслеживание", onBack = onBack)
        TrackMapCard(restaurant = restaurant, address = address)
        LiveTrackingPanel(order = order)
        SummaryCard(
            title = order.restaurantName,
            lines = buildList {
                add(order.itemsLabel)
                add(order.eta)
                order.deliveryDistanceKm?.let { add("$it км по маршруту") }
                add(address.displayTitle(strings))
                add("Оплата: ${order.paymentLabel}")
            },
        )
        CourierCard(status = order.status, courierOrder = null)
    }
}

@Composable
private fun LiveTrackingPanel(order: OrderSummary) {
    val strings = LocalAppStrings.current

    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            LiveOrderTracker(order = order)
            if (order.status == OrderStatus.Cancelled && order.refundedAmount > 0) {
                Text(
                    text = strings.refundedToAccount(order.refundedAmount.asPrice()),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Success,
                )
            }
        }
    }
}

@Composable
private fun CartItemRow(
    line: com.example.fooddeliveryapp.ui.data.CartLine,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onRemove: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(94.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(line.item.accent.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                if (line.item.imageUrl.isBlank()) {
                    Text(text = line.item.emoji, style = MaterialTheme.typography.displayLarge)
                } else {
                    FoodlyImage(
                        model = line.item.imageUrl,
                        contentDescription = line.item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 94.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text(
                    text = line.item.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = CardWhite,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = line.item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = CardWhite.copy(alpha = 0.68f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = (line.item.price * line.quantity).asPrice(),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = CardWhite,
                )
            }
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(34.dp),
            ) {
                Icon(Icons.Default.Close, null, tint = Rose, modifier = Modifier.size(19.dp))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QtyButton("-", onMinus)
            Text(
                line.quantity.toString(),
                modifier = Modifier.padding(horizontal = 14.dp),
                style = MaterialTheme.typography.titleMedium,
                color = CardWhite,
            )
            QtyButton("+", onPlus)
        }
    }
}

@Composable
private fun PaymentHeader(
    title: String,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
        }
        Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun PaymentMethodSelector(
    selectedMethod: PaymentMethod,
    onSelect: (PaymentMethod) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PaymentMethod.entries.forEach { method ->
            PaymentMethodCard(
                method = method,
                selected = selectedMethod == method,
                onClick = { onSelect(method) },
            )
        }
    }
}

@Composable
private fun PaymentMethodCard(
    method: PaymentMethod,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val strings = LocalAppStrings.current

    Box(
        modifier = Modifier
            .width(84.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.TopEnd,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = if (selected) 2.dp else 0.dp,
                        color = if (selected) Orange else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                PaymentMethodLogo(method = method)
            }
            Text(
                text = strings.paymentMethodTitle(method),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .offset(x = 6.dp, y = (-6).dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Orange),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CardWhite, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun PaymentMethodLogo(method: PaymentMethod) {
    when (method) {
        PaymentMethod.Cash -> Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Orange, modifier = Modifier.size(28.dp))
        PaymentMethod.Visa -> Text("VISA", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = Color(0xFF1D5CA7))
        PaymentMethod.MasterCard -> MastercardLogo()
        PaymentMethod.Uzcard -> Text("UZ", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = Color(0xFF2B72D6))
        PaymentMethod.HumoCard -> Text("HUMO", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black), color = Color(0xFF27A56C))
    }
}

@Composable
private fun MastercardLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(40.dp, 26.dp)) {
        drawCircle(color = Color(0xFFFF2B2B), radius = size.height * 0.38f, center = Offset(size.width * 0.42f, size.height * 0.5f))
        drawCircle(color = Color(0xFFFFB22E), radius = size.height * 0.38f, center = Offset(size.width * 0.58f, size.height * 0.5f))
    }
}

@Composable
private fun PaymentSavedArea(
    method: PaymentMethod,
    cards: List<PaymentCard>,
    selectedCardId: String?,
    expandedCardId: String?,
    checkoutMode: Boolean,
    onSelectCard: (PaymentCard) -> Unit,
    onDeleteCard: (PaymentCard) -> Unit,
) {
    when {
        method == PaymentMethod.Cash -> CashPaymentState()
        cards.isEmpty() -> EmptyPaymentCardState(method = method, checkoutMode = checkoutMode)
        else -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            cards.forEach { card ->
                SavedPaymentCardRow(
                    card = card,
                    selected = selectedCardId == card.id,
                    expanded = expandedCardId == card.id,
                    onClick = { onSelectCard(card) },
                    onDelete = { onDeleteCard(card) },
                )
            }
        }
    }
}

@Composable
private fun CashPaymentState() {
    val strings = LocalAppStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(18.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PaymentMethodLogo(PaymentMethod.Cash)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(strings.cashOnDelivery, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(strings.cashDescription, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyPaymentCardState(method: PaymentMethod, checkoutMode: Boolean) {
    val strings = LocalAppStrings.current
    val methodTitle = strings.paymentMethodTitle(method)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 22.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        EmptyCardIllustration(method = method)
        Text(
            text = strings.noPaymentMethodAdded(methodTitle),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = strings.addPaymentMethodHint(methodTitle, checkoutMode),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun EmptyCardIllustration(method: PaymentMethod) {
    Box(
        modifier = Modifier
            .width(166.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.linearGradient(listOf(Orange, Color(0xFFFFC35B), Rose))),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(CardWhite.copy(alpha = 0.84f)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 14.dp)
                .width(126.dp)
                .height(15.dp)
                .background(CardWhite.copy(alpha = 0.48f)),
        )
        PaymentMethodLogo(
            method = method,
        )
    }
}

@Composable
private fun SavedPaymentCardRow(
    card: PaymentCard,
    selected: Boolean,
    expanded: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val containerColor = if (selected) {
        Orange.copy(alpha = 0.18f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val borderColor = if (selected) Orange.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PaymentCardMiniLogo(brand = card.brand)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(card.brand, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                Text(card.maskedNumber(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = if (selected) Orange else Ink,
            )
        }
        if (expanded) {
            PaymentCardDetails(card = card, onDelete = onDelete)
        }
    }
}

@Composable
private fun PaymentCardMiniLogo(brand: String) {
    Box(
        modifier = Modifier
            .width(30.dp)
            .height(20.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (brand.equals("Visa", ignoreCase = true)) Color(0xFF1D5CA7) else Color(0xFF2D3242)),
        contentAlignment = Alignment.Center,
    ) {
        if (brand.equals("Mastercard", ignoreCase = true)) {
            MastercardLogo(modifier = Modifier.size(24.dp, 16.dp))
        } else {
            Text(brand.take(2).uppercase(Locale.US), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = CardWhite)
        }
    }
}

@Composable
private fun PaymentCardDetails(
    card: PaymentCard,
    onDelete: () -> Unit,
) {
    val strings = LocalAppStrings.current
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        BreakdownRow(strings.cardHolder, card.holderName)
        BreakdownRow(strings.cardNumber, card.maskedNumber())
        BreakdownRow(strings.expireDate, card.expiry)
        SecondaryButton(text = strings.deleteCard, onClick = onDelete, accent = Rose)
    }
}

@Composable
private fun AddNewPaymentButton(
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE8EAF0), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Orange, modifier = Modifier.size(20.dp))
            Text(LocalAppStrings.current.addNew, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Orange)
        }
    }
}

@Composable
private fun PaymentBottomBar(
    total: Int,
    isPlacingOrder: Boolean,
    onPay: () -> Unit,
) {
    val strings = LocalAppStrings.current
    Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(strings.total, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(total.asPrice(), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
        }
        PrimaryButton(
            text = if (isPlacingOrder) strings.processing else strings.payConfirm,
            onClick = onPay,
            enabled = !isPlacingOrder,
        )
    }
}

@Composable
private fun PromoCodeCard(
    code: String,
    appliedCoupon: DiscountCoupon?,
    discountAmount: Int,
    message: String?,
    messageIsError: Boolean,
    onCodeChange: (String) -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit,
) {
    val strings = LocalAppStrings.current
    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(strings.couponTitle, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                    Text(strings.couponSubtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                appliedCoupon?.let {
                    Text(
                        text = "-${discountAmount.asPrice()}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Success,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = code,
                    onValueChange = onCodeChange,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    placeholder = {
                        Text("FOODLY25", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Orange.copy(alpha = 0.42f),
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
                SecondaryButton(
                    text = if (appliedCoupon == null) strings.apply else strings.change,
                    onClick = onApply,
                    accent = Orange,
                )
            }
            appliedCoupon?.let { coupon ->
                Text(
                    text = "${coupon.title}: ${coupon.discountPercent}% - ${strings.couponValidUntil(coupon.expiresAtLabel())}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Success,
                )
                SecondaryButton(text = strings.removeCoupon, onClick = onClear, accent = Rose)
            }
            message?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (messageIsError) Rose else Success,
                )
            }
        }
    }
}

@Composable
private fun SavedPaymentCards(
    cards: List<PaymentCard>,
    selectedCardId: String?,
    onSelect: (String) -> Unit,
) {
    if (cards.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Сохраненные карты", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        cards.forEach { card ->
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (card.id == selectedCardId) Orange.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
                ),
                onClick = { onSelect(card.id) },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Orange.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.CreditCard, null, tint = Orange)
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${card.brand} •••• ${card.last4}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("${card.holderName} · ${card.expiry}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (card.id == selectedCardId) {
                        Icon(Icons.Default.CheckCircle, null, tint = Success)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddPaymentCardCard(
    number: String,
    onNumberChange: (String) -> Unit,
    holderName: String,
    onHolderNameChange: (String) -> Unit,
    expiry: String,
    onExpiryChange: (String) -> Unit,
    cvv: String,
    onCvvChange: (String) -> Unit,
    isSaving: Boolean,
    error: String?,
    onAdd: () -> Unit,
) {
    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Добавить карту", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Icon(Icons.Default.Add, null, tint = Orange)
            }
            AppTextField(
                value = number,
                onValueChange = onNumberChange,
                label = "Номер карты",
                placeholder = "8600 0000 0000 0000",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            AppTextField(
                value = holderName,
                onValueChange = onHolderNameChange,
                label = "Имя на карте",
                placeholder = "VISHAL KHADOK",
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppTextField(
                    value = expiry,
                    onValueChange = onExpiryChange,
                    label = "Срок",
                    placeholder = "MM/YY",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                AppTextField(
                    value = cvv,
                    onValueChange = onCvvChange,
                    label = "CVV",
                    placeholder = "123",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    isPassword = true,
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = "CVV нужен только для проверки формы и не сохраняется.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            error?.let { message ->
                Text(message, style = MaterialTheme.typography.bodyMedium, color = Rose)
            }
            SecondaryButton(
                text = if (isSaving) "Сохраняем..." else "Сохранить карту",
                onClick = onAdd,
                accent = Orange,
            )
        }
    }
}

@Composable
private fun CreditCardPreview(
    method: PaymentMethod,
    card: PaymentCard?,
) {
    val cardTitle = card?.brand ?: method.title()
    val cardNumber = card?.let { "••••  ••••  ••••  ${it.last4}" } ?: "Добавь карту для оплаты"
    val cardHolder = card?.holderName ?: "FOODLY USER"
    val cardExpiry = card?.expiry ?: "MM/YY"

    Card(shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = Orange)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Orange, OrangeDeep, Rose)))
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(cardTitle, style = MaterialTheme.typography.titleLarge, color = CardWhite)
                Icon(Icons.Default.CreditCard, null, tint = CardWhite)
            }
            Text(cardNumber, style = MaterialTheme.typography.headlineMedium, color = CardWhite)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(cardHolder, style = MaterialTheme.typography.bodySmall, color = CardWhite.copy(alpha = 0.76f))
                Text(cardExpiry, style = MaterialTheme.typography.bodySmall, color = CardWhite.copy(alpha = 0.76f))
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: OrderSummary,
    onTrack: () -> Unit,
    onReorder: () -> Unit,
) {
    val strings = LocalAppStrings.current
    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(order.restaurantName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                    Text(order.itemsLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusChip(order.status)
            }
            BreakdownRow("Сумма", order.total.asPrice())
            Text(order.visibleEta(strings), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (order.status != OrderStatus.Delivered && order.status != OrderStatus.Cancelled) {
                    SecondaryButton(text = "Отследить", onClick = onTrack)
                }
                SecondaryButton(text = "Повторить", onClick = onReorder, accent = Orange)
            }
        }
    }
}

@Composable
private fun TrackMapCard(
    restaurant: Restaurant?,
    address: DeliveryAddress,
) {
    Card(shape = RoundedCornerShape(34.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
        ) {
            StaticTrackMapArt()
        }
    }
}

@Composable
private fun StaticTrackMapArt(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(color = OrangeSoft.copy(alpha = 0.72f))
        repeat(6) { index ->
            drawLine(
                color = Color.White.copy(alpha = 0.72f),
                start = Offset(0f, size.height * index / 5f),
                end = Offset(size.width, size.height * (index + 0.2f) / 5f),
                strokeWidth = 12f,
            )
            drawLine(
                color = Color.White.copy(alpha = 0.72f),
                start = Offset(size.width * index / 5f, 0f),
                end = Offset(size.width * (index + 0.2f) / 5f, size.height),
                strokeWidth = 12f,
            )
        }
        val path = Path().apply {
            moveTo(size.width * 0.16f, size.height * 0.78f)
            cubicTo(size.width * 0.26f, size.height * 0.58f, size.width * 0.34f, size.height * 0.68f, size.width * 0.42f, size.height * 0.48f)
            cubicTo(size.width * 0.52f, size.height * 0.26f, size.width * 0.68f, size.height * 0.34f, size.width * 0.78f, size.height * 0.18f)
        }
        drawPath(path = path, color = Orange, style = Stroke(width = 18f, cap = StrokeCap.Round))
        drawCircle(color = Rose, radius = 28f, center = Offset(size.width * 0.16f, size.height * 0.78f))
        drawCircle(color = Success, radius = 28f, center = Offset(size.width * 0.78f, size.height * 0.18f))
    }
}

@Composable
private fun CourierCard(
    status: OrderStatus,
    courierOrder: CourierDeliveryOrder?,
) {
    val language = LocalAppLanguage.current
    val hasCourier = !courierOrder?.courierId.isNullOrBlank()
    val courierName = courierOrder?.courierName
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: if (hasCourier) language.assignedCourierLabel() else language.pendingCourierLabel()
    val courierPhone = courierOrder?.courierPhone
        ?.trim()
        ?.takeIf { it.isNotBlank() }
    val statusText = if (hasCourier) {
        language.courierTrackingState(status)
    } else {
        language.pendingCourierHint()
    }

    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(OrangeSoft),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "🧑‍🍳")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(courierName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                courierPhone?.let { phone ->
                    Text(
                        text = phone,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = Orange,
                    )
                }
                Text(
                    text = statusText,
                    /*
                        OrderStatus.Preparing -> "Ресторан собирает заказ"
                        OrderStatus.OnTheWay -> "Курьер уже в пути"
                        OrderStatus.Delivered -> "Заказ доставлен"
                        OrderStatus.Cancelled -> "Заказ отменен"
                    },
                    */
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (hasCourier) {
                CircleIconSurface(Icons.Default.NotificationsNone)
                CircleIconSurface(Icons.Default.LocalShipping)
            }
        }
    }
}

private fun AppLanguage.pendingCourierLabel(): String = when (this) {
    AppLanguage.English -> "Courier is being assigned"
    AppLanguage.Russian -> "Курьер определяется"
    AppLanguage.Uzbek -> "Kuryer aniqlanmoqda"
}

private fun AppLanguage.assignedCourierLabel(): String = when (this) {
    AppLanguage.English -> "Assigned courier"
    AppLanguage.Russian -> "Назначенный курьер"
    AppLanguage.Uzbek -> "Biriktirilgan kuryer"
}

private fun AppLanguage.pendingCourierHint(): String = when (this) {
    AppLanguage.English -> "Courier details will appear after the order is accepted."
    AppLanguage.Russian -> "Данные курьера появятся после принятия заказа."
    AppLanguage.Uzbek -> "Buyurtma qabul qilingandan keyin kuryer ma'lumotlari chiqadi."
}

private fun AppLanguage.courierTrackingState(status: OrderStatus): String = when (status) {
    OrderStatus.Preparing -> when (this) {
        AppLanguage.English -> "Courier accepted the order and is preparing for pickup."
        AppLanguage.Russian -> "Курьер принял заказ и готовится к получению."
        AppLanguage.Uzbek -> "Kuryer buyurtmani qabul qildi va olib ketishga tayyorlanmoqda."
    }
    OrderStatus.OnTheWay -> when (this) {
        AppLanguage.English -> "Courier is already on the way to the customer."
        AppLanguage.Russian -> "Курьер уже в пути к заказчику."
        AppLanguage.Uzbek -> "Kuryer allaqachon mijoz tomon yo'lda."
    }
    OrderStatus.Delivered -> when (this) {
        AppLanguage.English -> "Order has been delivered."
        AppLanguage.Russian -> "Заказ доставлен."
        AppLanguage.Uzbek -> "Buyurtma yetkazildi."
    }
    OrderStatus.Cancelled -> when (this) {
        AppLanguage.English -> "Order was cancelled."
        AppLanguage.Russian -> "Заказ отменён."
        AppLanguage.Uzbek -> "Buyurtma bekor qilindi."
    }
}

private fun AppLanguage.courierMapLabel(): String = when (this) {
    AppLanguage.English -> "Courier"
    AppLanguage.Russian -> "Курьер"
    AppLanguage.Uzbek -> "Kuryer"
}

private fun AppLanguage.customerMapLabel(): String = when (this) {
    AppLanguage.English -> "Me"
    AppLanguage.Russian -> "Заказчик"
    AppLanguage.Uzbek -> "Men"
}

private fun AppLanguage.customerSelfMapLabel(): String = when (this) {
    AppLanguage.English -> "Me"
    AppLanguage.Russian -> "Я"
    AppLanguage.Uzbek -> "Men"
}

private fun GeoPoint.asCartCurrentLocationAddress(
    strings: AppStrings,
    resolvedTitle: String? = null,
): DeliveryAddress =
    DeliveryAddress(
        label = AddressLabelOther,
        title = resolvedTitle?.ifBlank { strings.selectedMapPoint } ?: strings.selectedMapPoint,
        subtitle = strings.pointLabel(latitude, longitude),
        point = this,
    )

private fun hasCartLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

private fun isCartLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

private fun AppLanguage.cartLocationPermissionHint(): String = when (this) {
    AppLanguage.English -> "Allow location access so we can use your current delivery point."
    AppLanguage.Russian -> "Разреши доступ к геолокации, чтобы выбрать текущую точку доставки."
    AppLanguage.Uzbek -> "Joriy yetkazish nuqtasini tanlashimiz uchun geolokatsiyaga ruxsat bering."
}

private fun AppLanguage.cartLocationPermissionDenied(): String = when (this) {
    AppLanguage.English -> "Location access was denied. You can still choose an address manually."
    AppLanguage.Russian -> "Доступ к геолокации не выдан. Адрес можно выбрать вручную."
    AppLanguage.Uzbek -> "Geolokatsiyaga ruxsat berilmadi. Manzilni qo'lda tanlash mumkin."
}

private fun AppLanguage.cartLocationDisabled(): String = when (this) {
    AppLanguage.English -> "Location is turned off. Turn it on to use the current address."
    AppLanguage.Russian -> "Геолокация выключена. Включи ее, чтобы взять текущий адрес."
    AppLanguage.Uzbek -> "Joriy manzilni olish uchun geolokatsiyani yoqing."
}

private fun AppLanguage.currentAddressAction(): String = when (this) {
    AppLanguage.English -> "Current address"
    AppLanguage.Russian -> "Текущий адрес"
    AppLanguage.Uzbek -> "Joriy manzil"
}

private fun AppLanguage.currentAddressLoading(): String = when (this) {
    AppLanguage.English -> "Detecting..."
    AppLanguage.Russian -> "Определяем..."
    AppLanguage.Uzbek -> "Aniqlanmoqda..."
}

private fun AppLanguage.currentAddressSaved(): String = when (this) {
    AppLanguage.English -> "Current address saved and selected."
    AppLanguage.Russian -> "Текущий адрес сохранен и выбран."
    AppLanguage.Uzbek -> "Joriy manzil saqlandi va tanlandi."
}

@SuppressLint("MissingPermission")
private fun requestCurrentCartGeoPoint(
    context: Context,
    language: AppLanguage,
    onPoint: (GeoPoint) -> Unit,
    onError: (String) -> Unit,
) {
    if (hasCartLocationPermission(context).not()) {
        onError(language.cartLocationPermissionDenied())
        return
    }

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val providers = listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER,
        LocationManager.PASSIVE_PROVIDER,
    ).filter { provider ->
        runCatching { locationManager.isProviderEnabled(provider) }.getOrDefault(false)
    }

    if (providers.isEmpty()) {
        onError(language.cartLocationDisabled())
        return
    }

    val delivered = booleanArrayOf(false)
    val lastKnownLocation = providers
        .mapNotNull { provider -> runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull() }
        .maxByOrNull(Location::getTime)

    fun deliver(location: Location) {
        if (delivered[0]) return
        delivered[0] = true
        onPoint(
            GeoPoint(
                latitude = location.latitude,
                longitude = location.longitude,
            ),
        )
    }

    lastKnownLocation?.let { location ->
        if (System.currentTimeMillis() - location.time <= 2 * 60 * 1_000L) {
            deliver(location)
            return
        }
    }

    val provider = providers.firstOrNull { it == LocationManager.GPS_PROVIDER }
        ?: providers.firstOrNull()
        ?: run {
            onError(language.cartLocationDisabled())
            return
        }
    var listener: LocationListener? = null
    val currentListener = LocationListener { location ->
        listener?.let(locationManager::removeUpdates)
        deliver(location)
    }
    listener = currentListener

    runCatching {
        locationManager.requestSingleUpdate(provider, currentListener, null)
    }.onFailure {
        listener?.let(locationManager::removeUpdates)
        lastKnownLocation?.let(::deliver) ?: onError(it.message ?: "Location unavailable")
    }
}

private fun PaymentMethod.title(): String = when (this) {
    PaymentMethod.Cash -> "Cash"
    PaymentMethod.Visa -> "Visa"
    PaymentMethod.MasterCard -> "Mastercard"
    PaymentMethod.Uzcard -> "Uzcard"
    PaymentMethod.HumoCard -> "HumoCard"
}

private fun PaymentCard.matches(method: PaymentMethod): Boolean =
    when (method) {
        PaymentMethod.Cash -> false
        PaymentMethod.Visa -> brand.equals("Visa", ignoreCase = true)
        PaymentMethod.MasterCard -> brand.equals("Mastercard", ignoreCase = true)
        PaymentMethod.Uzcard -> brand.equals("Uzcard", ignoreCase = true)
        PaymentMethod.HumoCard -> brand.equals("HumoCard", ignoreCase = true) || brand.equals("Humo", ignoreCase = true)
    }

private fun PaymentCard.maskedNumber(): String = "**** **** **** $last4"

private fun String.cardDigits(): String = filter(Char::isDigit)

private fun String.formatCardNumberInput(): String =
    cardDigits()
        .take(16)
        .chunked(4)
        .joinToString(" ")

private fun String.formatExpiryInput(): String {
    val digits = cardDigits().take(6)
    return if (digits.length <= 2) {
        digits
    } else {
        digits.take(2) + "/" + digits.drop(2)
    }
}

private fun String.isValidExpiryInput(): Boolean =
    matches(Regex("""\d{2}/(\d{2}|\d{4})"""))

