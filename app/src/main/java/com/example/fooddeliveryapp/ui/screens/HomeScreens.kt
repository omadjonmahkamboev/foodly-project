package com.example.fooddeliveryapp.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.example.fooddeliveryapp.ui.components.FoodlyImage
import com.example.fooddeliveryapp.backend.BackendResult
import com.example.fooddeliveryapp.backend.UserSession
import com.example.fooddeliveryapp.backend.YandexGeocodingApi
import com.example.fooddeliveryapp.ui.AddressLabelHome
import com.example.fooddeliveryapp.ui.AddressLabelOffice
import com.example.fooddeliveryapp.ui.AddressLabelOther
import com.example.fooddeliveryapp.ui.AppLanguage
import com.example.fooddeliveryapp.ui.FoodAppState
import com.example.fooddeliveryapp.ui.LocalAppLanguage
import com.example.fooddeliveryapp.ui.LocalAppStrings
import com.example.fooddeliveryapp.ui.UserProfileDetails
import com.example.fooddeliveryapp.ui.displayTitle
import com.example.fooddeliveryapp.ui.isGenericAddressTitle
import com.example.fooddeliveryapp.ui.localizedAddressLabel
import com.example.fooddeliveryapp.ui.normalizeAddressLabelForStorage
import com.example.fooddeliveryapp.ui.yandexGeocoderLanguage
import com.example.fooddeliveryapp.ui.components.AccentChip
import com.example.fooddeliveryapp.ui.components.AppTextField
import com.example.fooddeliveryapp.ui.components.CircleIconButton
import com.example.fooddeliveryapp.ui.components.CircleIconSurface
import com.example.fooddeliveryapp.ui.components.InfoPill
import com.example.fooddeliveryapp.ui.components.PrimaryButton
import com.example.fooddeliveryapp.ui.components.PrimaryMiniButton
import com.example.fooddeliveryapp.ui.components.SectionHeader
import com.example.fooddeliveryapp.ui.components.TopBar
import com.example.fooddeliveryapp.ui.components.asPrice
import com.example.fooddeliveryapp.ui.data.DeliveryAddress
import com.example.fooddeliveryapp.ui.data.DiscountCoupon
import com.example.fooddeliveryapp.ui.data.MenuItem
import com.example.fooddeliveryapp.ui.data.Restaurant
import com.example.fooddeliveryapp.ui.data.SampleData
import com.example.fooddeliveryapp.ui.data.usesTransparentCutoutArt
import com.example.fooddeliveryapp.ui.map.YandexMapPicker
import com.example.fooddeliveryapp.ui.theme.CardWhite
import com.example.fooddeliveryapp.ui.theme.Cream
import com.example.fooddeliveryapp.ui.theme.Gold
import com.example.fooddeliveryapp.ui.theme.Ink
import com.example.fooddeliveryapp.ui.theme.InkSoft
import com.example.fooddeliveryapp.ui.theme.Night
import com.example.fooddeliveryapp.ui.theme.Orange
import com.example.fooddeliveryapp.ui.theme.OrangeSoft
import com.example.fooddeliveryapp.ui.theme.Rose
import com.example.fooddeliveryapp.ui.theme.Sky
import com.example.fooddeliveryapp.ui.theme.Success
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun MapPickerScreen(
    currentAddress: DeliveryAddress,
    onBack: () -> Unit,
    onConfirm: (DeliveryAddress) -> Unit,
) {
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current
    val context = LocalContext.current
    var selectedPoint by remember(currentAddress.point) { mutableStateOf(currentAddress.point) }
    var addressLabel by rememberSaveable(currentAddress.label) {
        mutableStateOf(strings.localizedAddressLabel(currentAddress.label))
    }
    var addressTitle by rememberSaveable(currentAddress.title) {
        mutableStateOf(
            currentAddress.title
                .takeUnless { strings.isGenericAddressTitle(it) }
                .orEmpty(),
        )
    }
    var addressSubtitle by rememberSaveable(currentAddress.subtitle) {
        mutableStateOf(currentAddress.subtitle)
    }
    var addressTitleIsAutoManaged by rememberSaveable(currentAddress.title) {
        mutableStateOf(addressTitle.isBlank())
    }
    var mapExpanded by rememberSaveable { mutableStateOf(false) }
    var userPointApplied by rememberSaveable(currentAddress.point) { mutableStateOf(false) }
    var locationRequestNonce by rememberSaveable(currentAddress.point) { mutableStateOf(0) }
    val pointSubtitle = remember(selectedPoint, strings) {
        strings.pointLabel(selectedPoint.latitude, selectedPoint.longitude)
    }
    val fallbackStreetTitle = remember(selectedPoint, language) {
        SampleData.streetNameForPoint(selectedPoint, language)
    }
    val currentAddressLooksGeneric = remember(currentAddress, strings) {
        currentAddress.shouldAutoSelectUserLocation(strings)
    }
    val normalizedAddressLabel = remember(addressLabel) {
        normalizeAddressLabelForStorage(addressLabel)
    }
    val draftAddress = remember(normalizedAddressLabel, addressTitle, addressSubtitle, selectedPoint, pointSubtitle, fallbackStreetTitle) {
        DeliveryAddress(
            label = normalizedAddressLabel,
            title = addressTitle.ifBlank { fallbackStreetTitle },
            subtitle = addressSubtitle.ifBlank { pointSubtitle },
            point = selectedPoint,
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            locationRequestNonce += 1
        }
    }

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        if (hasMapPickerLocationPermission(context) && isMapPickerLocationEnabled(context)) {
            locationRequestNonce += 1
        }
    }

    LaunchedEffect(language) {
        if (normalizedAddressLabel in setOf(AddressLabelHome, AddressLabelOffice, AddressLabelOther)) {
            addressLabel = strings.localizedAddressLabel(normalizedAddressLabel)
        }
        addressTitleIsAutoManaged = true
    }

    LaunchedEffect(currentAddressLooksGeneric) {
        if (currentAddressLooksGeneric && !userPointApplied) {
            locationRequestNonce += 1
        }
    }

    LaunchedEffect(locationRequestNonce) {
        if (locationRequestNonce == 0 || userPointApplied) return@LaunchedEffect

        when {
            !hasMapPickerLocationPermission(context) -> permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
            !isMapPickerLocationEnabled(context) -> settingsLauncher.launch(
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
            )
            else -> requestCurrentGeoPointForMapPicker(context) { point ->
                userPointApplied = true
                selectedPoint = point
                addressTitleIsAutoManaged = true
            }
        }
    }

    LaunchedEffect(selectedPoint, language, addressTitleIsAutoManaged) {
        delay(250)

        val resolvedAddress = when (val result = YandexGeocodingApi.reverseGeocode(selectedPoint, language.yandexGeocoderLanguage())) {
            is BackendResult.Success -> result.data
            is BackendResult.Error -> null
        }
        val streetLine = resolvedAddress?.primaryAddressLine
            ?.ifBlank { resolvedAddress.secondaryAddressLine }
            ?.ifBlank { resolvedAddress.formattedAddress }
            ?.takeUnless { strings.isGenericAddressTitle(it) }
            ?.takeIf { it.isNotBlank() }
            ?: fallbackStreetTitle
        addressSubtitle = resolvedAddress?.formattedAddress
            ?.ifBlank { resolvedAddress?.secondaryAddressLine?.ifBlank { pointSubtitle } }
            ?: "$fallbackStreetTitle - $pointSubtitle"

        if (addressTitleIsAutoManaged) {
            addressTitle = streetLine
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        val sheetScrollState = rememberScrollState()
        val minSheetTop = 320.dp
        val collapsedSheetTop = (maxHeight * 0.46f)
            .coerceIn(minSheetTop, (maxHeight - 248.dp).coerceAtLeast(minSheetTop))
        val expandedSheetTop = (maxHeight - 144.dp).coerceAtLeast(96.dp)
        val sheetTop by animateDpAsState(
            targetValue = if (mapExpanded) expandedSheetTop else collapsedSheetTop,
            animationSpec = tween(durationMillis = 320),
            label = "mapPickerSheetTop",
        )
        val mapHeight by animateDpAsState(
            targetValue = if (mapExpanded) maxHeight else (collapsedSheetTop + 54.dp).coerceAtMost(maxHeight),
            animationSpec = tween(durationMillis = 320),
            label = "mapPickerMapHeight",
        )
        val toggleTop by animateDpAsState(
            targetValue = (sheetTop - 24.dp).coerceAtLeast(88.dp),
            animationSpec = tween(durationMillis = 320),
            label = "mapPickerToggleTop",
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(mapHeight)
                .align(Alignment.TopCenter),
        ) {
            YandexMapPicker(
                selectedPoint = selectedPoint,
                onPointSelected = {
                    selectedPoint = it
                    addressTitleIsAutoManaged = true
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 20.dp, top = 16.dp)
                .size(46.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
                .clickable(onClick = onBack)
                .zIndex(4f),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }

        MapSheetToggleButton(
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
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(sheetScrollState)
                .padding(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 30.dp)
                .zIndex(2f),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 70.dp, height = 5.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.34f)),
            )
            Text(
                text = strings.mapTitle,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f)),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        text = strings.addressData,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        listOf(AddressLabelHome, AddressLabelOffice, AddressLabelOther).forEach { label ->
                            AddressLabelChip(
                                label = strings.localizedAddressLabel(label),
                                selected = normalizedAddressLabel == label,
                                onClick = { addressLabel = strings.localizedAddressLabel(label) },
                            )
                        }
                    }
                    AppTextField(
                        value = addressLabel,
                        onValueChange = { addressLabel = it },
                        label = strings.label,
                        placeholder = strings.homeAddress,
                    )
                    AppTextField(
                        value = addressTitle,
                        onValueChange = {
                            addressTitle = it
                            addressTitleIsAutoManaged = false
                        },
                        label = strings.address,
                        placeholder = strings.selectedMapPoint,
                    )
                    Text(
                        text = addressSubtitle.ifBlank { pointSubtitle },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = pointSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                    )
                }
            }
            PrimaryButton(
                text = strings.saveAddress,
                onClick = { onConfirm(draftAddress) },
            )
        }
    }
}

@Composable
private fun AddressLabelChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) OrangeSoft else MaterialTheme.colorScheme.surfaceVariant),
        onClick = onClick,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) Orange else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MapSheetToggleButton(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.38f), CircleShape)
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

private fun DeliveryAddress.shouldAutoSelectUserLocation(strings: com.example.fooddeliveryapp.ui.AppStrings): Boolean {
    val coordinateSubtitle = strings.pointLabel(point.latitude, point.longitude)
    val genericTitle = title.isBlank() || strings.isGenericAddressTitle(title)
    val genericSubtitle = subtitle.isBlank() || subtitle == coordinateSubtitle
    return genericTitle || genericSubtitle
}

private fun hasMapPickerLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

private fun isMapPickerLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return runCatching { locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) }.getOrDefault(false) ||
        runCatching { locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }.getOrDefault(false)
}

@SuppressLint("MissingPermission")
private fun requestCurrentGeoPointForMapPicker(
    context: Context,
    onPoint: (com.example.fooddeliveryapp.ui.data.GeoPoint) -> Unit,
) {
    if (!hasMapPickerLocationPermission(context)) return

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val providers = listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER,
        LocationManager.PASSIVE_PROVIDER,
    ).filter { provider ->
        runCatching { locationManager.isProviderEnabled(provider) }.getOrDefault(false)
    }
    if (providers.isEmpty()) return

    val lastKnown = providers
        .mapNotNull { provider -> runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull() }
        .maxByOrNull(Location::getTime)
    val handler = Handler(Looper.getMainLooper())
    var delivered = false

    fun deliver(location: Location) {
        if (delivered) return
        delivered = true
        onPoint(com.example.fooddeliveryapp.ui.data.GeoPoint(location.latitude, location.longitude))
    }

    val timeout = Runnable {
        val fallback = lastKnown ?: return@Runnable
        deliver(fallback)
    }
    handler.postDelayed(timeout, 4_000L)

    val provider = providers.firstOrNull { it == LocationManager.GPS_PROVIDER } ?: providers.first()
    val listener = LocationListener { location ->
        handler.removeCallbacks(timeout)
        deliver(location)
    }

    runCatching {
        locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
    }.onFailure {
        handler.removeCallbacks(timeout)
        lastKnown?.let(::deliver)
    }
}

private val HomeFeaturedRestaurantIds = listOf(
    "burger_bistro",
    "fast_food",
    "national_food",
    "asian_food",
    "shawarma_doner",
    "chicken",
    "healthy_salads",
    "breakfasts",
    "soups",
    "desserts_bakery",
    "drinks",
)

@Composable
fun HomeScreen(
    appState: FoodAppState,
    onOpenSearch: () -> Unit,
    onOpenRestaurant: (String) -> Unit,
    onOpenCart: () -> Unit,
    onEditAddress: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val featuredCategories by remember {
        derivedStateOf {
            HomeFeaturedRestaurantIds.mapNotNull { restaurantId ->
                val restaurant = appState.restaurants.firstOrNull { it.id == restaurantId } ?: return@mapNotNull null
                HomeFoodCategory(
                    title = restaurant.name,
                    price = restaurant.menu.minOfOrNull { it.price } ?: 0,
                    imageUrl = restaurant.imageUrl,
                    restaurantId = restaurant.id,
                )
            }
        }
    }
    val welcomeCoupon = appState.pendingWelcomeCoupon
    val categoryFavoriteItems = remember(featuredCategories, appState.restaurants) {
        featuredCategories.associateWith { category ->
            appState.restaurants
                .firstOrNull { it.id == category.restaurantId }
                ?.menu
                ?.firstOrNull()
        }
    }

    LaunchedEffect(appState.currentUser?.id) {
        appState.pruneExpiredCoupons()
    }

    val categoryCarouselItems = remember(featuredCategories) {
        List(featuredCategories.size * 12) { index -> featuredCategories[index % featuredCategories.size] }
    }
    val categoryListState = rememberLazyListState(
        initialFirstVisibleItemIndex = featuredCategories.size * 4,
    )

    LaunchedEffect(categoryCarouselItems.size) {
        if (featuredCategories.size <= 1) return@LaunchedEffect

        while (true) {
            delay(3200)
            val nextIndex = categoryListState.firstVisibleItemIndex + 1
            categoryListState.animateScrollToItem(nextIndex)
            if (nextIndex >= categoryCarouselItems.size - featuredCategories.size * 2) {
                categoryListState.scrollToItem(
                    index = featuredCategories.size * 4 + (nextIndex % featuredCategories.size),
                    scrollOffset = categoryListState.firstVisibleItemScrollOffset,
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 22.dp, top = 30.dp, end = 22.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                HomeHeaderMockup(
                    cartCount = appState.cartCount,
                    deliverTo = strings.deliverTo,
                    address = appState.selectedAddress.displayTitle(strings),
                    onOpenCart = onOpenCart,
                    onEditAddress = onEditAddress,
                )
            }
            item {
                HomeGreeting(
                    user = appState.currentUser,
                    profile = appState.profileDetails,
                )
            }
            item {
                HomeSectionTitle(
                    title = strings.allCategories,
                    action = strings.seeAll,
                    onAction = onOpenSearch,
                )
            }
            item {
                LazyRow(
                    state = categoryListState,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 6.dp, bottom = 6.dp),
                ) {
                    items(
                        count = categoryCarouselItems.size,
                        key = { index -> "${categoryCarouselItems[index].title}_$index" },
                    ) { index ->
                        val category = categoryCarouselItems[index]
                        FoodCategoryPhotoCard(
                            category = category,
                            favoriteItem = categoryFavoriteItems[category],
                            isFavorite = categoryFavoriteItems[category]?.let { appState.isFavorite(it.id) } == true,
                            onClick = { onOpenRestaurant(category.restaurantId) },
                            onToggleFavorite = { item -> appState.toggleFavorite(item) },
                        )
                    }
                }
            }
            item {
                HomeSectionTitle(
                    title = strings.openRestaurants,
                    action = strings.seeAll,
                    onAction = onOpenSearch,
                )
            }
            items(appState.restaurants, key = { it.id }) { restaurant ->
                OpenRestaurantPhotoCard(
                    restaurant = restaurant,
                    onClick = { onOpenRestaurant(restaurant.id) },
                )
            }
        }

        welcomeCoupon?.let { coupon ->
            HurryCouponOverlay(
                coupon = coupon,
                onClose = appState::dismissPendingWelcomeCoupon,
            )
        }
    }
}

private data class HomeFoodCategory(
    val title: String,
    val price: Int,
    val imageUrl: String,
    val restaurantId: String,
)

@Composable
private fun HurryCouponOverlay(
    coupon: DiscountCoupon,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    var copied by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(1200)
            copied = false
        }
    }

    fun copyCode() {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(ClipData.newPlainText("Coupon code", coupon.code))
        copied = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x990F1720))
            .padding(horizontal = 22.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFFFDE2E),
                                Color(0xFFFFB300),
                                Color(0xFFFF7A00),
                            ),
                        ),
                    ),
            ) {
                CouponConfetti(modifier = Modifier.fillMaxSize())
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-18).dp)
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFCF78))
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Orange, modifier = Modifier.size(24.dp))
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    Text(
                        text = strings.welcomeGiftTitle,
                        style = MaterialTheme.typography.displayLarge,
                        color = CardWhite,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = coupon.description,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = CardWhite,
                        textAlign = TextAlign.Center,
                    )
                    SelectionContainer {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(2.dp, CardWhite, RoundedCornerShape(8.dp))
                                .clickable(onClick = ::copyCode)
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = coupon.code,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = CardWhite,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    Text(
                        text = if (copied) strings.copiedCode else strings.tapCodeToCopy,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = CardWhite,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = strings.couponValidUntil(coupon.expiresAtLabel()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = CardWhite,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable(onClick = ::copyCode),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (copied) strings.copiedUpper else strings.copyCodeUpper,
                            style = MaterialTheme.typography.labelLarge,
                            color = Orange,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, CardWhite, RoundedCornerShape(8.dp))
                            .clickable(onClick = onClose),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = strings.later,
                            style = MaterialTheme.typography.labelLarge,
                            color = CardWhite,
                        )
                    }
                }
            }
        }
    }
}

private fun DiscountCoupon.expiresAtLabel(): String =
    Instant.ofEpochMilli(expiresAtMillis)
        .atZone(ZoneId.systemDefault())
        .format(CouponDateFormatter)

private val CouponDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd.MM.yyyy")

@Composable
private fun CouponConfetti(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        fun triangle(center: Offset, radius: Float, rotation: Float): Path {
            val path = Path()
            repeat(3) { index ->
                val angle = Math.toRadians((rotation + index * 120f).toDouble())
                val point = Offset(
                    x = center.x + kotlin.math.cos(angle).toFloat() * radius,
                    y = center.y + kotlin.math.sin(angle).toFloat() * radius,
                )
                if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
            }
            path.close()
            return path
        }

        drawPath(triangle(Offset(size.width * 0.20f, size.height * 0.18f), 24f, -10f), Color.White.copy(alpha = 0.38f))
        drawPath(triangle(Offset(size.width * 0.84f, size.height * 0.22f), 30f, -42f), Color.White.copy(alpha = 0.52f))
        drawPath(triangle(Offset(size.width * 0.28f, size.height * 0.36f), 18f, 15f), Color.White.copy(alpha = 0.24f))
        drawPath(triangle(Offset(size.width * 0.52f, size.height * 0.14f), 18f, 12f), Color.White.copy(alpha = 0.44f))
        drawPath(triangle(Offset(size.width * 0.48f, size.height * 0.42f), 10f, 75f), Color.White.copy(alpha = 0.42f))
        drawLine(
            color = Color.White.copy(alpha = 0.36f),
            start = Offset(size.width * 0.92f, size.height * 0.44f),
            end = Offset(size.width * 0.96f, size.height * 0.52f),
            strokeWidth = 7f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
        )
        drawLine(
            color = Color.White.copy(alpha = 0.36f),
            start = Offset(size.width * 0.96f, size.height * 0.52f),
            end = Offset(size.width * 0.92f, size.height * 0.60f),
            strokeWidth = 7f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
        )
    }
}

@Composable
private fun HomeGreeting(
    user: UserSession?,
    profile: UserProfileDetails,
) {
    val strings = LocalAppStrings.current
    val displayName = remember(user?.name, profile.firstName, profile.fullName) {
        user?.name
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.split(Regex("\\s+"))
            ?.firstOrNull()
            ?: profile.firstName.ifBlank { profile.fullName }.trim()
    }.ifBlank { "Foodly" }
    var currentHour by remember { mutableStateOf(LocalTime.now().hour) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            currentHour = LocalTime.now().hour
        }
    }
    val greeting = strings.greetingForHour(currentHour)

    Text(
        text = buildAnnotatedString {
            append(strings.hey(displayName))
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(greeting)
            }
        },
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun HomeHeaderMockup(
    cartCount: Int,
    deliverTo: String,
    address: String,
    onOpenCart: () -> Unit,
    onEditAddress: () -> Unit,
) {
    val addressInteraction = remember { MutableInteractionSource() }
    val addressPressed by addressInteraction.collectIsPressedAsState()
    val addressScale by animateFloatAsState(
        targetValue = if (addressPressed) 0.97f else 1f,
        animationSpec = tween(160),
        label = "homeAddressScale",
    )
    val cartInteraction = remember { MutableInteractionSource() }
    val cartPressed by cartInteraction.collectIsPressedAsState()
    val cartScale by animateFloatAsState(
        targetValue = if (cartPressed) 0.92f else 1f,
        animationSpec = tween(160),
        label = "homeCartScale",
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Row(
            modifier = Modifier
                .scale(addressScale)
                .clickable(
                    interactionSource = addressInteraction,
                    indication = null,
                    onClick = onEditAddress,
                ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F2F4)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Menu, contentDescription = null, tint = Ink, modifier = Modifier.size(20.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = deliverTo,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Orange,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = address,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        Box(
            modifier = Modifier.padding(top = 4.dp, end = 4.dp),
            contentAlignment = Alignment.TopEnd,
        ) {
            Box(
                modifier = Modifier
                    .scale(cartScale)
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Ink)
                    .clickable(
                        interactionSource = cartInteraction,
                        indication = null,
                        onClick = onOpenCart,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = CardWhite, modifier = Modifier.size(22.dp))
            }
            if (cartCount > 0) {
                CartCountBadge(
                    cartCount = cartCount,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-6).dp)
                        .zIndex(2f),
                )
            }
        }
    }
}

@Composable
private fun HomeSectionTitle(
    title: String,
    action: String,
    onAction: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            modifier = Modifier.clickable(onClick = onAction),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = action,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = InkSoft,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun FoodCategoryPhotoCard(
    category: HomeFoodCategory,
    favoriteItem: MenuItem?,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: (MenuItem) -> Unit,
) {
    val strings = LocalAppStrings.current
    val title = strings.menuGroup(category.title)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = tween(160),
        label = "homeCategoryScale",
    )

    Box(
        modifier = Modifier
            .width(136.dp)
            .height(184.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FoodlyImage(
                    model = category.imageUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
                Text(
                    text = title,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = strings.starting,
                        modifier = Modifier.padding(start = 12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = category.price.asPrice(),
                        modifier = Modifier.padding(end = 12.dp),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        favoriteItem?.let { item ->
            FavoriteBadge(
                selected = isFavorite,
                onClick = { onToggleFavorite(item) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = 4.dp)
                    .zIndex(2f),
            )
        }
    }
}

@Composable
private fun OpenRestaurantPhotoCard(
    restaurant: Restaurant,
    onClick: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val deliveryFee = strings.deliveryFeeLabel(restaurant.deliveryFee)
    val deliveryTime = strings.deliveryTimeLabel(restaurant.deliveryTime)
    val tags = restaurant.tags.joinToString(" - ") { tag -> strings.menuGroup(tag) }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            FoodlyImage(
                model = restaurant.imageUrl,
                contentDescription = restaurant.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Text(
                text = restaurant.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 12.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = tags,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HomeMetaItem(icon = Icons.Default.Star, text = restaurant.rating.toString())
                HomeMetaItem(icon = Icons.Default.LocalShipping, text = deliveryFee)
                HomeMetaItem(icon = Icons.Default.AccessTime, text = deliveryTime)
            }
        }
    }
}

@Composable
private fun HomeMetaItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Orange, modifier = Modifier.size(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun SearchScreen(
    appState: FoodAppState,
    onBack: () -> Unit,
    onOpenCart: () -> Unit,
    onOpenRestaurant: (String) -> Unit,
    onOpenFood: (MenuItem) -> Unit,
    onAddToCart: (MenuItem) -> Unit,
) {
    val strings = LocalAppStrings.current
    var query by rememberSaveable { mutableStateOf("") }
    var selectedKeywordLabel by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedKeyword = searchKeywords.firstOrNull { it.label == selectedKeywordLabel }
    val menuPairs = remember(appState.restaurants) {
        appState.restaurants.flatMap { restaurant ->
            restaurant.menu.map { item -> item to restaurant }
        }
    }

    if (selectedKeyword == null) {
        SearchHomeContent(
            query = query,
            onQueryChange = { query = it },
            cartCount = appState.cartCount,
            restaurants = appState.restaurants,
            menuPairs = menuPairs,
            onBack = onBack,
            onOpenCart = onOpenCart,
            onKeywordClick = { keyword -> selectedKeywordLabel = keyword.label },
            onOpenRestaurant = onOpenRestaurant,
            onOpenFood = onOpenFood,
            onAddToCart = onAddToCart,
            isFavorite = appState::isFavorite,
            onToggleFavorite = appState::toggleFavorite,
        )
    } else {
        SearchCategoryContent(
            keyword = selectedKeyword,
            restaurants = appState.restaurants,
            menuPairs = menuPairs,
            onBack = { selectedKeywordLabel = null },
            onKeywordClick = { keyword -> selectedKeywordLabel = keyword.label },
            onOpenRestaurant = onOpenRestaurant,
            onOpenFood = onOpenFood,
            onAddToCart = onAddToCart,
            isFavorite = appState::isFavorite,
            onToggleFavorite = appState::toggleFavorite,
        )
    }
}

private data class SearchKeyword(
    val label: String,
    val title: String,
    val categoryIds: List<String>,
)

private val searchKeywords = listOf(
    SearchKeyword("Burger Bistro", "Burger Bistro", listOf("bistro_burgers", "bistro_combo", "bistro_sides")),
    SearchKeyword("Фастфуд", "Фастфуд", listOf("fast_burgers", "fast_pizza", "fast_fries", "fast_hotdogs", "fast_nuggets", "fast_combo")),
    SearchKeyword("Национальная еда", "Национальная еда", listOf("national_plov", "national_samsa", "national_manti", "national_lagman", "national_shashlik", "national_kazan_kebab")),
    SearchKeyword("Азиатская еда", "Азиатская еда", listOf("asian_sushi", "asian_rolls", "asian_wok", "asian_ramen", "asian_noodles", "asian_chicken_rice")),
    SearchKeyword("Шаурма и донер", "Шаурма и донер", listOf("shawarma", "doner", "lavash", "gyros")),
    SearchKeyword("Курица", "Курица", listOf("fried_chicken", "chicken_wings", "chicken_box", "chicken_sets")),
    SearchKeyword("Салаты и здоровая еда", "Салаты и здоровая еда", listOf("caesar_salad", "greek_salad", "bowls", "fitness_food", "vegetarian_food")),
    SearchKeyword("Завтраки", "Завтраки", listOf("omelet", "pancakes", "porridge", "sandwiches", "syrniki")),
    SearchKeyword("Супы", "Супы", listOf("chicken_soup", "lentil_soup", "mastava_soup", "cream_soup", "borscht")),
    SearchKeyword("Десерты и выпечка", "Десерты и выпечка", listOf("cakes", "cheesecake", "donuts", "icecream", "bakery_samsa", "croissants", "buns")),
    SearchKeyword("Напитки", "Напитки", listOf("soda", "juices", "water", "lemonades", "tea", "coffee", "milkshakes", "smoothies")),
)

@Composable
private fun SearchHomeContent(
    query: String,
    onQueryChange: (String) -> Unit,
    cartCount: Int,
    restaurants: List<Restaurant>,
    menuPairs: List<Pair<MenuItem, Restaurant>>,
    onBack: () -> Unit,
    onOpenCart: () -> Unit,
    onKeywordClick: (SearchKeyword) -> Unit,
    onOpenRestaurant: (String) -> Unit,
    onOpenFood: (MenuItem) -> Unit,
    onAddToCart: (MenuItem) -> Unit,
    isFavorite: (String) -> Boolean,
    onToggleFavorite: (MenuItem) -> Unit,
) {
    val strings = LocalAppStrings.current
    val suggestedRestaurants = remember(restaurants) {
        restaurants.take(3).map { restaurant ->
            SearchRestaurantPreview(restaurant.name, restaurant.rating, restaurant)
        }
    }
    val popularItems = remember(menuPairs) {
        listOf("bistro_burgers", "fast_burgers", "fast_pizza", "asian_rolls", "shawarma")
            .mapNotNull { category -> menuPairs.firstOrNull { (item, _) -> item.category == category } }
    }
    val normalizedQuery = query.normalizedSearchText()
    val isSearching = normalizedQuery.isNotBlank()
    val searchRestaurants = remember(normalizedQuery, restaurants) {
        if (normalizedQuery.isBlank()) {
            emptyList()
        } else {
            restaurants.filter { restaurant -> restaurant.matchesSearchQuery(normalizedQuery) }
        }
    }
    val searchMenuPairs = remember(normalizedQuery, menuPairs) {
        if (normalizedQuery.isBlank()) {
            emptyList()
        } else {
            menuPairs
                .filter { (item, restaurant) -> item.matchesSearchQuery(normalizedQuery, restaurant) }
                .take(12)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, top = 26.dp, end = 20.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            SearchHeader(
                title = strings.search,
                cartCount = cartCount,
                onBack = onBack,
                onOpenCart = onOpenCart,
            )
        }
        item {
            SearchInputMockup(
                query = query,
                onQueryChange = onQueryChange,
            )
        }
        if (isSearching) {
            item {
                Text(
                    text = "Results for \"$query\"",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (searchRestaurants.isEmpty() && searchMenuPairs.isEmpty()) {
                item {
                    EmptySearchResult(query = query)
                }
            }
            if (searchRestaurants.isNotEmpty()) {
                item {
                    Text(
                        text = strings.suggestedRestaurants,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                items(searchRestaurants, key = { it.id }) { restaurant ->
                    SuggestedRestaurantRow(
                        preview = SearchRestaurantPreview(
                            name = restaurant.name,
                            rating = restaurant.rating,
                            restaurant = restaurant,
                        ),
                        onClick = { onOpenRestaurant(restaurant.id) },
                    )
                }
            }
            if (searchMenuPairs.isNotEmpty()) {
                item {
                    Text(
                        text = strings.popularFastFood,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                item {
                    FoodProductGrid(
                        menuPairs = searchMenuPairs,
                        onOpenFood = onOpenFood,
                        onAddToCart = onAddToCart,
                        isFavorite = isFavorite,
                        onToggleFavorite = onToggleFavorite,
                    )
                }
            }
        } else {
            item { Text(strings.recentKeywords, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface) }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(searchKeywords, key = { it.label }) { keyword ->
                        RecentKeywordChip(
                            label = strings.menuGroup(keyword.label),
                            selected = false,
                            onClick = { onKeywordClick(keyword) },
                        )
                    }
                }
            }
            item { Text(strings.suggestedRestaurants, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface) }
            items(suggestedRestaurants, key = { it.name }) { preview ->
                SuggestedRestaurantRow(
                    preview = preview,
                    onClick = { onOpenRestaurant(preview.restaurant.id) },
                )
            }
            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.42f)),
                )
            }
            item { Text(strings.popularFastFood, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface) }
            item {
                FoodProductGrid(
                    menuPairs = popularItems,
                    onOpenFood = onOpenFood,
                    onAddToCart = onAddToCart,
                    isFavorite = isFavorite,
                    onToggleFavorite = onToggleFavorite,
                )
            }
        }
    }
}

private data class SearchRestaurantPreview(
    val name: String,
    val rating: Double,
    val restaurant: Restaurant,
)

private fun Restaurant.matchesSearchQuery(normalizedQuery: String): Boolean =
    listOf(
        id,
        id.searchAliases(),
        name,
        subtitle,
        description,
        tags.joinToString(" "),
    ).any { value -> value.normalizedSearchText().contains(normalizedQuery) }

private fun MenuItem.matchesSearchQuery(
    normalizedQuery: String,
    restaurant: Restaurant,
): Boolean =
    listOf(
        title,
        subtitle,
        category,
        category.searchAliases(),
        details,
        ingredients.joinToString(" "),
        restaurant.name,
        restaurant.subtitle,
        restaurant.tags.joinToString(" "),
    ).any { value -> value.normalizedSearchText().contains(normalizedQuery) }

private fun String.searchAliases(): String = when (this) {
    "burger_bistro", "bistro_burgers", "bistro_combo", "bistro_sides" ->
        "burger bistro smash burger burgers crispy chicken fries combo бургер бистро смэш бургер бургеры курица фри комбо"
    "fast_food", "fast_burgers", "fast_pizza", "fast_fries", "fast_hotdogs", "fast_nuggets", "fast_combo" ->
        "fast food burger burgers cheeseburger fries hot dog nuggets фастфуд бургер бургеры фри картошка хот-дог наггетсы"
    "national_food", "national_plov", "national_samsa", "national_manti", "national_lagman", "national_shashlik", "national_kazan_kebab" ->
        "uzbek national plov samsa manti lagman shashlik kazan kebab национальная еда плов самса манты лагман шашлык казан кебаб"
    "asian_food", "asian_sushi", "asian_rolls", "asian_wok", "asian_ramen", "asian_noodles", "asian_chicken_rice" ->
        "asian sushi rolls wok ramen noodles chicken rice азиатская еда суши роллы вок рамен лапша рис с курицей"
    "shawarma_doner", "shawarma", "doner", "lavash", "gyros" ->
        "shawarma doner lavash gyros шаурма донер лаваш гирос шаверма"
    "chicken", "fried_chicken", "chicken_wings", "chicken_box", "chicken_sets" ->
        "chicken fried wings strips box курица жареная курица крылышки chicken box куриные сеты"
    "healthy_salads", "caesar_salad", "greek_salad", "bowls", "fitness_food", "vegetarian_food" ->
        "salad healthy caesar greek bowl fitness vegetarian салаты здоровая еда цезарь греческий боулы фитнес вегетарианская еда"
    "breakfasts", "omelet", "pancakes", "porridge", "sandwiches", "syrniki" ->
        "breakfast omelet pancakes porridge sandwich syrniki завтрак завтраки омлет блины каша сэндвич сырники"
    "soups", "chicken_soup", "lentil_soup", "mastava_soup", "cream_soup", "borscht" ->
        "soup soups chicken lentil mastava cream borscht суп супы куриный чечевичный мастава крем суп борщ"
    "desserts_bakery", "cakes", "cheesecake", "donuts", "icecream", "bakery_samsa", "croissants", "buns" ->
        "dessert bakery cake cheesecake donuts ice cream samsa croissant buns десерт выпечка торт чизкейк донаты мороженое самса круассан булочки"
    "drinks", "soda", "juices", "water", "lemonades", "tea", "coffee", "milkshakes", "smoothies" ->
        "drink drinks beverage soda juice water lemonade tea coffee milkshake smoothie напиток напитки газировка сок вода лимонад чай кофе коктейль смузи"
    "pizza" ->
        "pizza пицца пиццы маргарита пепперони 4 сыра pitsa pitssa"
    else -> this
}

private fun String.normalizedSearchText(): String =
    trim()
        .lowercase(Locale.getDefault())
        .replace('ё', 'е')
        .replace('ʻ', '\'')
        .replace('`', '\'')

@Composable
private fun SearchCategoryContent(
    keyword: SearchKeyword,
    restaurants: List<Restaurant>,
    menuPairs: List<Pair<MenuItem, Restaurant>>,
    onBack: () -> Unit,
    onKeywordClick: (SearchKeyword) -> Unit,
    onOpenRestaurant: (String) -> Unit,
    onOpenFood: (MenuItem) -> Unit,
    onAddToCart: (MenuItem) -> Unit,
    isFavorite: (String) -> Boolean,
    onToggleFavorite: (MenuItem) -> Unit,
) {
    val strings = LocalAppStrings.current
    var showCategoryPicker by rememberSaveable(keyword.label) { mutableStateOf(false) }
    val filteredPairs = remember(keyword, menuPairs) {
        menuPairs
            .filter { (item, _) -> item.category in keyword.categoryIds }
            .take(4)
    }
    val highlightedRestaurant = remember(keyword, restaurants, filteredPairs) {
        filteredPairs.firstOrNull()?.second ?: restaurants.first()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, top = 28.dp, end = 20.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        item {
            SearchCategoryHeader(
                label = strings.menuGroup(keyword.label),
                onBack = onBack,
                onSearchClick = onBack,
                onCategoryClick = { showCategoryPicker = !showCategoryPicker },
            )
        }
        if (showCategoryPicker) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(searchKeywords, key = { it.label }) { option ->
                        RecentKeywordChip(
                            label = strings.menuGroup(option.label),
                            selected = option.label == keyword.label,
                            onClick = {
                                showCategoryPicker = false
                                onKeywordClick(option)
                            },
                        )
                    }
                }
            }
        }
        item {
            Text(
                text = strings.popularCategory(strings.menuGroup(keyword.title)),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        item {
            FoodProductGrid(
                menuPairs = filteredPairs,
                onOpenFood = onOpenFood,
                onAddToCart = onAddToCart,
                isFavorite = isFavorite,
                onToggleFavorite = onToggleFavorite,
            )
        }
        item {
            Text(strings.openRestaurants, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        }
        item {
            SearchRestaurantGalleryCard(
                restaurant = highlightedRestaurant,
                onClick = { onOpenRestaurant(highlightedRestaurant.id) },
            )
        }
    }
}

@Composable
private fun SearchHeader(
    title: String,
    cartCount: Int,
    onBack: () -> Unit,
    onOpenCart: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundIconAction(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                onClick = onBack,
            )
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        CartBadgeButton(cartCount = cartCount, onClick = onOpenCart)
    }
}

@Composable
private fun SearchCategoryHeader(
    label: String,
    onBack: () -> Unit,
    onSearchClick: () -> Unit,
    onCategoryClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoundIconAction(Icons.AutoMirrored.Filled.ArrowBack, onBack)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(22.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(22.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onCategoryClick)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label.uppercase(Locale.US),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Orange, modifier = Modifier.size(14.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface)
                    .clickable(onClick = onSearchClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(21.dp))
            }
        }
    }
}

@Composable
private fun RoundIconAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun CartBadgeButton(
    cartCount: Int,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(top = 4.dp, end = 4.dp),
        contentAlignment = Alignment.TopEnd,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.onSurface)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(21.dp))
        }
        if (cartCount > 0) {
            CartCountBadge(
                cartCount = cartCount,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .zIndex(2f),
            )
        }
    }
}

@Composable
private fun CartCountBadge(
    cartCount: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(21.dp)
            .clip(CircleShape)
            .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
            .background(Orange),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (cartCount > 99) "99+" else cartCount.toString(),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = CardWhite,
            maxLines = 1,
        )
    }
}

@Composable
private fun SearchInputMockup(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(19.dp),
        )
        AppTextFieldlessValue(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outline)
                .clickable { onQueryChange("") },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}

@Composable
private fun AppTextFieldlessValue(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current

    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
        decorationBox = { innerTextField ->
            if (value.isBlank()) {
                Text(strings.searchExample, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f))
            }
            innerTextField()
        },
    )
}

@Composable
private fun RecentKeywordChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .height(42.dp)
            .clip(RoundedCornerShape(21.dp))
            .border(
                1.dp,
                if (selected) Orange.copy(alpha = 0.72f) else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(21.dp),
            )
            .background(if (selected) Orange.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun EmptySearchResult(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Nothing found",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Try another keyword for \"$query\".",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SuggestedRestaurantRow(
    preview: SearchRestaurantPreview,
    onClick: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FoodlyImage(
                model = preview.restaurant.imageUrl,
                contentDescription = preview.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(preview.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Orange, modifier = Modifier.size(14.dp))
                    Text(preview.rating.toString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.42f)),
        )
    }
}

@Composable
private fun FoodProductGrid(
    menuPairs: List<Pair<MenuItem, Restaurant>>,
    onOpenFood: (MenuItem) -> Unit,
    onAddToCart: (MenuItem) -> Unit,
    isFavorite: (String) -> Boolean,
    onToggleFavorite: (MenuItem) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
        menuPairs.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                rowItems.forEach { (item, restaurant) ->
                    FoodProductCard(
                        item = item,
                        restaurant = restaurant,
                        isFavorite = isFavorite(item.id),
                        onClick = { onOpenFood(item) },
                        onAdd = { onAddToCart(item) },
                        onToggleFavorite = { onToggleFavorite(item) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun FoodProductCard(
    item: MenuItem,
    restaurant: Restaurant,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onAdd: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val transparentArt = item.usesTransparentCutoutArt()

    Box(
        modifier = modifier
            .height(184.dp)
            .clickable(onClick = onClick),
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(126.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp, top = 50.dp, end = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(restaurant.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(item.price.asPrice(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Orange)
                            .clickable(onClick = onAdd),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = CardWhite, modifier = Modifier.size(19.dp))
                    }
                }
            }
        }
        FoodlyImage(
            model = item.imageUrl.ifBlank { restaurant.imageUrl },
            contentDescription = item.title,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(if (transparentArt) 118.dp else 104.dp)
                .zIndex(1f),
        )
        FavoriteBadge(
            selected = isFavorite,
            onClick = onToggleFavorite,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 58.dp, end = 4.dp)
                .zIndex(2f),
        )
    }
}

@Composable
private fun FavoriteBadge(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(CardWhite.copy(alpha = 0.94f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (selected) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = null,
            tint = if (selected) Rose else InkSoft,
            modifier = Modifier.size(19.dp),
        )
    }
}

@Composable
private fun SearchRestaurantGalleryCard(
    restaurant: Restaurant,
    onClick: () -> Unit,
) {
    val strings = LocalAppStrings.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        FoodlyImage(
            model = restaurant.imageUrl,
            contentDescription = restaurant.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(138.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        Text(restaurant.name, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        Row(
            horizontalArrangement = Arrangement.spacedBy(22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HomeMetaItem(icon = Icons.Default.Star, text = restaurant.rating.toString())
            HomeMetaItem(icon = Icons.Default.LocalShipping, text = strings.deliveryFeeLabel(restaurant.deliveryFee))
            HomeMetaItem(icon = Icons.Default.AccessTime, text = strings.deliveryTimeLabel(restaurant.deliveryTime))
        }
    }
}

@Composable
fun FoodDetailsScreen(
    item: MenuItem,
    restaurant: Restaurant,
    onBack: () -> Unit,
    onAddToCart: (MenuItem) -> Unit,
    onDecreaseCartQuantity: (String) -> Unit,
    cartQuantityForItem: (String) -> Int,
    isFavorite: (String) -> Boolean,
    onToggleFavorite: (MenuItem) -> Unit,
) {
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current
    val transparentArt = item.usesTransparentCutoutArt()
    val sizeOptions = remember(item.id, item.price) { foodSizeOptions(item.price) }
    var selectedSize by rememberSaveable(item.id) { mutableStateOf(DefaultFoodSizeLabel) }
    val selectedSizeOption = sizeOptions.firstOrNull { it.label == selectedSize }
        ?: sizeOptions.first { it.label == DefaultFoodSizeLabel }
    val selectedCartItem = remember(item, selectedSizeOption) {
        item.withSizeOption(selectedSizeOption)
    }
    val quantity = cartQuantityForItem(selectedCartItem.id)
    val displayedPrice = selectedSizeOption.price * quantity.coerceAtLeast(1)
    val detailText = remember(item, restaurant.name, language) {
        item.localizedDetails(restaurant.name, language)
    }
    val favorite = isFavorite(item.id)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, top = 28.dp, end = 18.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RoundIconAction(Icons.AutoMirrored.Filled.ArrowBack, onBack)
                    Text(strings.details, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (transparentArt) 220.dp else 188.dp)
                        .clip(RoundedCornerShape(28.dp)),
                ) {
                    if (!transparentArt) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color(0xFFFFC46B)),
                        )
                    }
                    FoodlyImage(
                        model = item.imageUrl.ifBlank { restaurant.imageUrl },
                        contentDescription = item.title,
                        contentScale = if (transparentArt) ContentScale.FillHeight else ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize(),
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(18.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CardWhite.copy(alpha = 0.28f))
                            .clickable { onToggleFavorite(item) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (favorite) Rose else CardWhite,
                            modifier = Modifier.size(21.dp),
                        )
                    }
                }
            }
            item {
                RestaurantNamePill(name = restaurant.name)
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(item.title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                    Text(detailText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DetailsMetaItem(icon = Icons.Default.Star, text = restaurant.rating.toString())
                    DetailsMetaItem(icon = Icons.Default.LocalShipping, text = strings.deliveryFeeLabel(restaurant.deliveryFee))
                    DetailsMetaItem(icon = Icons.Default.AccessTime, text = strings.deliveryTimeLabel(restaurant.deliveryTime))
                }
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(strings.size, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    sizeOptions.forEach { size ->
                        SizeChip(
                            label = size.label,
                            selected = selectedSize == size.label,
                            onClick = { selectedSize = size.label },
                        )
                    }
                }
            }
            item {
                Text(
                    text = strings.ingredients,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    items(item.ingredients.ifEmpty { fallbackIngredients(item) }, key = { it }) { ingredient ->
                        IngredientBubble(ingredient = ingredient)
                    }
                }
            }
        }
        DetailsBottomBar(
            price = displayedPrice,
            quantity = quantity,
            onSelect = {
                onAddToCart(selectedCartItem)
            },
            onMinus = {
                onDecreaseCartQuantity(selectedCartItem.id)
            },
            onPlus = {
                onAddToCart(selectedCartItem)
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

private const val DefaultFoodSizeLabel = "Standard"

private data class FoodSizeOption(
    val label: String,
    val price: Int,
)

private fun foodSizeOptions(basePrice: Int): List<FoodSizeOption> =
    listOf(
        FoodSizeOption(DefaultFoodSizeLabel, basePrice.coerceAtLeast(1)),
        FoodSizeOption("Medium", ((basePrice * 5 + 2) / 4).coerceAtLeast(basePrice + 1)),
        FoodSizeOption("Premium", ((basePrice * 3 + 1) / 2).coerceAtLeast(basePrice + 2)),
    )

private fun MenuItem.withSizeOption(sizeOption: FoodSizeOption): MenuItem {
    if (sizeOption.label == DefaultFoodSizeLabel && sizeOption.price == price) return this

    val sizeId = sizeOption.label.lowercase(Locale.US)
    return copy(
        id = "${id}_$sizeId",
        title = "$title (${sizeOption.label})",
        price = sizeOption.price,
    )
}

private fun MenuItem.localizedDetails(
    restaurantName: String,
    language: AppLanguage,
): String {
    val baseDetails = details.ifBlank { subtitle }
    return when (language) {
        AppLanguage.English -> baseDetails.ifBlank {
            "Fresh ${category.readableCategoryName(language)} from $restaurantName with carefully selected ingredients."
        }
        AppLanguage.Russian -> "Свежее блюдо категории \"${category.readableCategoryName(language)}\" от $restaurantName: готовим с хорошими ингредиентами и подаем горячим."
        AppLanguage.Uzbek -> "$restaurantName dan yangi ${category.readableCategoryName(language)}: sifatli masalliqlar bilan tayyorlanadi va issiq yetkaziladi."
    }
}

private fun String.readableCategoryName(language: AppLanguage): String =
    when (this) {
        "fast_burgers", "fast_pizza", "fast_fries", "fast_hotdogs", "fast_nuggets", "fast_combo" -> when (language) {
            AppLanguage.English -> "fast food"
            AppLanguage.Russian -> "фастфуд"
            AppLanguage.Uzbek -> "fastfud"
        }
        "national_plov", "national_samsa", "national_manti", "national_lagman", "national_shashlik", "national_kazan_kebab" -> when (language) {
            AppLanguage.English -> "national food"
            AppLanguage.Russian -> "национальная еда"
            AppLanguage.Uzbek -> "milliy taom"
        }
        "asian_sushi", "asian_rolls", "asian_wok", "asian_ramen", "asian_noodles", "asian_chicken_rice" -> when (language) {
            AppLanguage.English -> "Asian food"
            AppLanguage.Russian -> "азиатская еда"
            AppLanguage.Uzbek -> "Osiyo taomi"
        }
        "shawarma", "doner", "lavash", "gyros" -> when (language) {
            AppLanguage.English -> "shawarma and doner"
            AppLanguage.Russian -> "шаурма и донер"
            AppLanguage.Uzbek -> "shaurma va doner"
        }
        "fried_chicken", "chicken_wings", "chicken_box", "chicken_sets" -> when (language) {
            AppLanguage.English -> "chicken"
            AppLanguage.Russian -> "курица"
            AppLanguage.Uzbek -> "tovuq"
        }
        "caesar_salad", "greek_salad", "bowls", "fitness_food", "vegetarian_food" -> when (language) {
            AppLanguage.English -> "healthy food"
            AppLanguage.Russian -> "салаты и здоровая еда"
            AppLanguage.Uzbek -> "foydali taom"
        }
        "omelet", "pancakes", "porridge", "sandwiches", "syrniki" -> when (language) {
            AppLanguage.English -> "breakfast"
            AppLanguage.Russian -> "завтрак"
            AppLanguage.Uzbek -> "nonushta"
        }
        "chicken_soup", "lentil_soup", "mastava_soup", "cream_soup", "borscht" -> when (language) {
            AppLanguage.English -> "soup"
            AppLanguage.Russian -> "суп"
            AppLanguage.Uzbek -> "sho'rva"
        }
        "cakes", "cheesecake", "donuts", "icecream", "bakery_samsa", "croissants", "buns" -> when (language) {
            AppLanguage.English -> "dessert and bakery"
            AppLanguage.Russian -> "десерт и выпечка"
            AppLanguage.Uzbek -> "desert va pishiriq"
        }
        "soda", "juices", "water", "lemonades", "tea", "coffee", "milkshakes", "smoothies" -> when (language) {
            AppLanguage.English -> "drink"
            AppLanguage.Russian -> "напиток"
            AppLanguage.Uzbek -> "ichimlik"
        }
        "fast_food" -> when (language) {
            AppLanguage.English -> "fast food"
            AppLanguage.Russian -> "фастфуд"
            AppLanguage.Uzbek -> "fastfud"
        }
        "pizza" -> when (language) {
            AppLanguage.English -> "pizza"
            AppLanguage.Russian -> "пицца"
            AppLanguage.Uzbek -> "pitsa"
        }
        "sushi_rolls" -> when (language) {
            AppLanguage.English -> "sushi and rolls"
            AppLanguage.Russian -> "суши и роллы"
            AppLanguage.Uzbek -> "sushi va rollar"
        }
        "national" -> when (language) {
            AppLanguage.English -> "national cuisine"
            AppLanguage.Russian -> "национальная кухня"
            AppLanguage.Uzbek -> "milliy oshxona"
        }
        "asian" -> when (language) {
            AppLanguage.English -> "Asian food"
            AppLanguage.Russian -> "азиатская кухня"
            AppLanguage.Uzbek -> "Osiyo taomi"
        }
        "european" -> when (language) {
            AppLanguage.English -> "European food"
            AppLanguage.Russian -> "европейская кухня"
            AppLanguage.Uzbek -> "Yevropa taomi"
        }
        "chicken" -> when (language) {
            AppLanguage.English -> "chicken"
            AppLanguage.Russian -> "курица"
            AppLanguage.Uzbek -> "tovuq"
        }
        "shawarma_doner" -> when (language) {
            AppLanguage.English -> "shawarma"
            AppLanguage.Russian -> "шаурма"
            AppLanguage.Uzbek -> "shaurma"
        }
        "soups" -> when (language) {
            AppLanguage.English -> "soup"
            AppLanguage.Russian -> "суп"
            AppLanguage.Uzbek -> "sho'rva"
        }
        "salads" -> when (language) {
            AppLanguage.English -> "salad"
            AppLanguage.Russian -> "салат"
            AppLanguage.Uzbek -> "salat"
        }
        "breakfasts" -> when (language) {
            AppLanguage.English -> "breakfast"
            AppLanguage.Russian -> "завтрак"
            AppLanguage.Uzbek -> "nonushta"
        }
        "desserts" -> when (language) {
            AppLanguage.English -> "dessert"
            AppLanguage.Russian -> "десерт"
            AppLanguage.Uzbek -> "shirinlik"
        }
        "drinks" -> when (language) {
            AppLanguage.English -> "drink"
            AppLanguage.Russian -> "напиток"
            AppLanguage.Uzbek -> "ichimlik"
        }
        "vegetarian" -> when (language) {
            AppLanguage.English -> "vegetarian food"
            AppLanguage.Russian -> "вегетарианское блюдо"
            AppLanguage.Uzbek -> "vegetarian taom"
        }
        "healthy" -> when (language) {
            AppLanguage.English -> "healthy food"
            AppLanguage.Russian -> "полезное блюдо"
            AppLanguage.Uzbek -> "foydali taom"
        }
        "family_sets" -> when (language) {
            AppLanguage.English -> "family set"
            AppLanguage.Russian -> "семейный сет"
            AppLanguage.Uzbek -> "oilaviy set"
        }
        "combo" -> when (language) {
            AppLanguage.English -> "combo"
            AppLanguage.Russian -> "комбо"
            AppLanguage.Uzbek -> "kombo"
        }
        "kids" -> when (language) {
            AppLanguage.English -> "kids meal"
            AppLanguage.Russian -> "детское блюдо"
            AppLanguage.Uzbek -> "bolalar taomi"
        }
        "bakery" -> when (language) {
            AppLanguage.English -> "bakery"
            AppLanguage.Russian -> "выпечка"
            AppLanguage.Uzbek -> "pishiriq"
        }
        "seafood" -> when (language) {
            AppLanguage.English -> "seafood"
            AppLanguage.Russian -> "морепродукты"
            AppLanguage.Uzbek -> "dengiz mahsulotlari"
        }
        else -> when (language) {
            AppLanguage.English -> "dish"
            AppLanguage.Russian -> "блюдо"
            AppLanguage.Uzbek -> "taom"
        }
    }

@Composable
private fun RestaurantNamePill(name: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(21.dp))
            .border(1.dp, Color(0xFFE9EAF0), RoundedCornerShape(21.dp))
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("🍅", style = MaterialTheme.typography.bodyMedium)
        Text(name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun DetailsMetaItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Orange, modifier = Modifier.size(20.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SizeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .height(42.dp)
            .widthIn(min = 82.dp)
            .clip(RoundedCornerShape(21.dp))
            .background(if (selected) Orange else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = if (selected) CardWhite else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun IngredientBubble(ingredient: String) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(Color(0xFFFFE6DF)),
        contentAlignment = Alignment.Center,
    ) {
        Text(ingredient.ingredientEmoji(), style = MaterialTheme.typography.titleMedium, color = Orange)
    }
}

@Composable
private fun DetailsBottomBar(
    price: Int,
    quantity: Int,
    onSelect: () -> Unit,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(price.asPrice(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
        if (quantity <= 0) {
            Box(
                modifier = Modifier
                    .height(48.dp)
                    .widthIn(min = 116.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(com.example.fooddeliveryapp.ui.theme.Night)
                    .clickable(onClick = onSelect)
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = strings.choose,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = CardWhite,
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(com.example.fooddeliveryapp.ui.theme.Night)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(CardWhite.copy(alpha = 0.18f))
                        .clickable(onClick = onMinus),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, tint = CardWhite, modifier = Modifier.size(15.dp))
                }
                Text(quantity.toString(), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = CardWhite)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(CardWhite.copy(alpha = 0.18f))
                        .clickable(onClick = onPlus),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = CardWhite, modifier = Modifier.size(15.dp))
                }
            }
        }
    }
}

private fun fallbackIngredients(item: MenuItem): List<String> =
    when (item.category) {
        "fast_pizza" -> listOf("tomato", "cheese", "onion", "basil", "pepper")
        "fast_burgers", "fast_hotdogs" -> listOf("bun", "meat", "cheese", "lettuce", "sauce")
        "fast_fries" -> listOf("potato", "salt", "sauce")
        "fast_nuggets", "fried_chicken", "chicken_wings", "chicken_box", "chicken_sets" -> listOf("chicken", "pepper", "sauce")
        "shawarma", "doner", "lavash", "gyros" -> listOf("lavash", "meat", "tomato", "cucumber", "sauce")
        "asian_sushi", "asian_rolls" -> listOf("rice", "fish", "nori", "sauce")
        "national_plov", "national_lagman", "national_kazan_kebab", "national_manti" -> listOf("rice", "meat", "carrot", "onion")
        "asian_wok", "asian_ramen", "asian_noodles", "asian_chicken_rice" -> listOf("noodles", "rice", "chicken", "sauce")
        "caesar_salad", "greek_salad", "bowls", "fitness_food", "vegetarian_food" -> listOf("greens", "tomato", "cucumber", "lemon")
        "cakes", "cheesecake", "donuts", "icecream", "croissants", "buns" -> listOf("cream", "berry", "sugar")
        "soda", "juices", "water", "lemonades", "tea", "coffee", "milkshakes", "smoothies" -> listOf("lemon", "ice", "mint")
        else -> listOf("fresh", "sauce", "herbs")
    }

private fun String.ingredientEmoji(): String =
    when (lowercase(Locale.US)) {
        "tomato" -> "🍅"
        "prosciutto", "salami", "pepperoni", "sausage", "meat", "beef", "crab" -> "🥓"
        "onion" -> "🧅"
        "mozzarella", "cheddar", "cheese", "cream" -> "🧀"
        "basil", "lettuce", "greens", "parsley", "mint", "herbs" -> "🌿"
        "bun", "dough", "lavash", "bread" -> "🥖"
        "chicken" -> "🍗"
        "pickle", "cucumber" -> "🥒"
        "potato" -> "🥔"
        "mushroom" -> "🍄"
        "pepper" -> "🌶️"
        "lemon" -> "🍋"
        "mango" -> "🥭"
        "banana" -> "🍌"
        "berry" -> "🍓"
        "rice", "oats" -> "🍚"
        "fish", "salmon", "tuna", "shrimp" -> "🍣"
        "nori", "avocado", "carrot", "lentil", "chickpea", "seeds" -> "🥑"
        "noodles", "pasta" -> "🍜"
        "egg" -> "🍳"
        "bbq", "sauce", "teriyaki", "honey", "mustard", "garlic" -> "🥣"
        "chocolate", "sugar" -> "🍫"
        "coffee", "tea", "milk", "soda", "water", "orange", "ice", "drink" -> "🥤"
        else -> "🥕"
    }

private fun String.shortFee(): String =
    if (contains("Бесплат", ignoreCase = true) || contains("Р‘РµСЃ", ignoreCase = true)) "Free" else this

private fun String.shortTime(): String =
    replace("мин", "min").replace("РјРёРЅ", "min")

private data class RestaurantMenuGroup(
    val label: String,
    val categories: Set<String>,
)

private val restaurantMenuGroups = listOf(
    RestaurantMenuGroup("Бургеры", setOf("fast_burgers")),
    RestaurantMenuGroup("Пицца", setOf("fast_pizza")),
    RestaurantMenuGroup("Картошка фри", setOf("fast_fries")),
    RestaurantMenuGroup("Хот-доги", setOf("fast_hotdogs")),
    RestaurantMenuGroup("Наггетсы", setOf("fast_nuggets")),
    RestaurantMenuGroup("Комбо", setOf("fast_combo")),
    RestaurantMenuGroup("Плов", setOf("national_plov")),
    RestaurantMenuGroup("Самса", setOf("national_samsa")),
    RestaurantMenuGroup("Манты", setOf("national_manti")),
    RestaurantMenuGroup("Лагман", setOf("national_lagman")),
    RestaurantMenuGroup("Шашлык", setOf("national_shashlik")),
    RestaurantMenuGroup("Казан-кебаб", setOf("national_kazan_kebab")),
    RestaurantMenuGroup("Суши", setOf("asian_sushi")),
    RestaurantMenuGroup("Роллы", setOf("asian_rolls")),
    RestaurantMenuGroup("Wok", setOf("asian_wok")),
    RestaurantMenuGroup("Рамен", setOf("asian_ramen")),
    RestaurantMenuGroup("Лапша", setOf("asian_noodles")),
    RestaurantMenuGroup("Рис с курицей", setOf("asian_chicken_rice")),
    RestaurantMenuGroup("Шаурма", setOf("shawarma")),
    RestaurantMenuGroup("Донер", setOf("doner")),
    RestaurantMenuGroup("Лаваш", setOf("lavash")),
    RestaurantMenuGroup("Гирос", setOf("gyros")),
    RestaurantMenuGroup("Жареная курица", setOf("fried_chicken")),
    RestaurantMenuGroup("Крылышки", setOf("chicken_wings")),
    RestaurantMenuGroup("Chicken box", setOf("chicken_box")),
    RestaurantMenuGroup("Куриные сеты", setOf("chicken_sets")),
    RestaurantMenuGroup("Цезарь", setOf("caesar_salad")),
    RestaurantMenuGroup("Греческий салат", setOf("greek_salad")),
    RestaurantMenuGroup("Боулы", setOf("bowls")),
    RestaurantMenuGroup("Фитнес-блюда", setOf("fitness_food")),
    RestaurantMenuGroup("Вегетарианская еда", setOf("vegetarian_food")),
    RestaurantMenuGroup("Омлет", setOf("omelet")),
    RestaurantMenuGroup("Блины", setOf("pancakes")),
    RestaurantMenuGroup("Каша", setOf("porridge")),
    RestaurantMenuGroup("Сэндвичи", setOf("sandwiches")),
    RestaurantMenuGroup("Сырники", setOf("syrniki")),
    RestaurantMenuGroup("Куриный суп", setOf("chicken_soup")),
    RestaurantMenuGroup("Чечевичный суп", setOf("lentil_soup")),
    RestaurantMenuGroup("Мастава", setOf("mastava_soup")),
    RestaurantMenuGroup("Крем-суп", setOf("cream_soup")),
    RestaurantMenuGroup("Борщ", setOf("borscht")),
    RestaurantMenuGroup("Торты", setOf("cakes")),
    RestaurantMenuGroup("Чизкейк", setOf("cheesecake")),
    RestaurantMenuGroup("Донаты", setOf("donuts")),
    RestaurantMenuGroup("Мороженое", setOf("icecream")),
    RestaurantMenuGroup("Самса", setOf("bakery_samsa")),
    RestaurantMenuGroup("Круассаны", setOf("croissants")),
    RestaurantMenuGroup("Булочки", setOf("buns")),
    RestaurantMenuGroup("Газировка", setOf("soda")),
    RestaurantMenuGroup("Соки", setOf("juices")),
    RestaurantMenuGroup("Вода", setOf("water")),
    RestaurantMenuGroup("Лимонады", setOf("lemonades")),
    RestaurantMenuGroup("Чай", setOf("tea")),
    RestaurantMenuGroup("Кофе", setOf("coffee")),
    RestaurantMenuGroup("Молочные коктейли", setOf("milkshakes")),
    RestaurantMenuGroup("Смузи", setOf("smoothies")),
)

@Composable
fun RestaurantScreen(
    restaurant: Restaurant,
    cartCount: Int,
    onBack: () -> Unit,
    onOpenFood: (MenuItem) -> Unit,
    onAddToCart: (MenuItem) -> Unit,
    isFavorite: (String) -> Boolean,
    onToggleFavorite: (MenuItem) -> Unit,
    onOpenCart: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val availableGroups = remember(restaurant.id, restaurant.menu) {
        restaurantMenuGroups.filter { group ->
            restaurant.menu.any { item -> item.category in group.categories }
        }
    }
    var selectedGroupLabel by rememberSaveable(restaurant.id) {
        mutableStateOf(availableGroups.firstOrNull()?.label ?: "Menu")
    }
    val selectedGroup = availableGroups.firstOrNull { it.label == selectedGroupLabel }
        ?: availableGroups.firstOrNull()
    val visibleItems = remember(restaurant.id, restaurant.menu, selectedGroupLabel) {
        val group = restaurantMenuGroups.firstOrNull { it.label == selectedGroupLabel }
        if (group == null) {
            restaurant.menu
        } else {
            restaurant.menu.filter { item -> item.category in group.categories }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 34.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            RestaurantImageHeader(
                restaurant = restaurant,
                cartCount = cartCount,
                onBack = onBack,
                onOpenCart = onOpenCart,
            )
        }
        item {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RestaurantMetaRow(restaurant = restaurant)
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = restaurant.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                )
            }
        }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(availableGroups, key = { it.label }) { group ->
                    RestaurantCategoryChip(
                        label = strings.menuGroup(group.label),
                        selected = group.label == (selectedGroup?.label ?: selectedGroupLabel),
                        onClick = { selectedGroupLabel = group.label },
                    )
                }
            }
        }
        item {
            Text(
                text = "${strings.menuGroup(selectedGroup?.label ?: selectedGroupLabel)} (${visibleItems.size})",
                modifier = Modifier.padding(horizontal = 20.dp),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        item {
            RestaurantMenuGrid(
                items = visibleItems,
                onOpenFood = onOpenFood,
                onAddToCart = onAddToCart,
                isFavorite = isFavorite,
                onToggleFavorite = onToggleFavorite,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
    }
}

@Composable
private fun RestaurantImageHeader(
    restaurant: Restaurant,
    cartCount: Int,
    onBack: () -> Unit,
    onOpenCart: () -> Unit,
) {
    val headerImages = remember(restaurant.id, restaurant.imageUrl, restaurant.imageUrls) {
        (restaurant.imageUrls + restaurant.imageUrl)
            .filter { imageUrl -> imageUrl.isNotBlank() }
            .distinct()
            .take(8)
    }
    val pagerState = rememberPagerState(pageCount = { headerImages.size.coerceAtLeast(1) })
    val activeImageIndex = pagerState.currentPage
        .coerceIn(0, (headerImages.size - 1).coerceAtLeast(0))

    LaunchedEffect(headerImages) {
        if (headerImages.size <= 1) return@LaunchedEffect
        while (true) {
            delay(3_500)
            val nextPage = (pagerState.currentPage + 1) % headerImages.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(282.dp)
            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                val imageUrl = headerImages.getOrNull(page)
                if (imageUrl == null) {
                    Box(
                        modifier = Modifier
                            .width(maxWidth)
                            .fillMaxHeight()
                            .background(restaurant.accent.copy(alpha = 0.22f)),
                    )
                } else {
                    FoodlyImage(
                        model = imageUrl,
                        contentDescription = restaurant.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(maxWidth)
                            .fillMaxHeight(),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x33000000),
                            Color.Transparent,
                            Color(0x22000000),
                        ),
                    ),
                ),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .zIndex(2f)
                .padding(start = 20.dp, top = 24.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RestaurantHeaderAction(Icons.AutoMirrored.Filled.ArrowBack, onClick = onBack)
            Box(
                modifier = Modifier.padding(top = 4.dp, end = 4.dp),
                contentAlignment = Alignment.TopEnd,
            ) {
                RestaurantHeaderAction(Icons.Default.ShoppingBag, onClick = onOpenCart)
                if (cartCount > 0) {
                    CartCountBadge(
                        cartCount = cartCount,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 3.dp, y = (-3).dp)
                            .zIndex(2f),
                    )
                }
            }
        }
        if (headerImages.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                headerImages.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(if (index == activeImageIndex) 12.dp else 7.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == activeImageIndex) {
                                    CardWhite
                                } else {
                                    CardWhite.copy(alpha = 0.45f)
                                },
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun RestaurantHeaderAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .zIndex(2f)
            .clip(CircleShape)
            .background(CardWhite.copy(alpha = 0.94f))
            .border(1.dp, Night.copy(alpha = 0.12f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = Night, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun RestaurantMetaRow(restaurant: Restaurant) {
    val strings = LocalAppStrings.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RestaurantMetaItem(icon = Icons.Default.Star, text = restaurant.rating.toString())
        RestaurantMetaItem(icon = Icons.Default.LocalShipping, text = strings.deliveryFeeLabel(restaurant.deliveryFee))
        RestaurantMetaItem(icon = Icons.Default.AccessTime, text = strings.deliveryTimeLabel(restaurant.deliveryTime))
    }
}

@Composable
private fun RestaurantMetaItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Orange, modifier = Modifier.size(20.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun RestaurantCategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .height(42.dp)
            .clip(RoundedCornerShape(21.dp))
            .border(1.dp, if (selected) Orange else MaterialTheme.colorScheme.outline, RoundedCornerShape(21.dp))
            .background(if (selected) Orange else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = if (selected) CardWhite else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun RestaurantMenuGrid(
    items: List<MenuItem>,
    onOpenFood: (MenuItem) -> Unit,
    onAddToCart: (MenuItem) -> Unit,
    isFavorite: (String) -> Boolean,
    onToggleFavorite: (MenuItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                rowItems.forEach { item ->
                    RestaurantMenuTile(
                        item = item,
                        isFavorite = isFavorite(item.id),
                        onOpen = { onOpenFood(item) },
                        onAdd = { onAddToCart(item) },
                        onToggleFavorite = { onToggleFavorite(item) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RestaurantMenuTile(
    item: MenuItem,
    isFavorite: Boolean,
    onOpen: () -> Unit,
    onAdd: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val transparentArt = item.usesTransparentCutoutArt()

    Card(
        modifier = modifier
            .height(190.dp)
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 7.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(86.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (transparentArt) {
                                Color.Transparent
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (item.imageUrl.isBlank()) {
                        Text(
                            text = item.emoji,
                            style = MaterialTheme.typography.displayLarge,
                        )
                    } else {
                        FoodlyImage(
                            model = item.imageUrl,
                            contentDescription = item.title,
                            contentScale = if (transparentArt) ContentScale.Fit else ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(if (transparentArt) 4.dp else 0.dp),
                        )
                    }
                }
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.price.asPrice(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 10.dp, bottom = 10.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Orange)
                    .clickable(onClick = onAdd),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = CardWhite, modifier = Modifier.size(19.dp))
            }
            FavoriteBadge(
                selected = isFavorite,
                onClick = onToggleFavorite,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            )
        }
    }
}

@Composable
private fun HomeHeader(
    address: DeliveryAddress,
    cartCount: Int,
    onOpenCart: () -> Unit,
    onEditAddress: () -> Unit,
) {
    val strings = LocalAppStrings.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.clickable(onClick = onEditAddress),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircleIconSurface(Icons.Default.Menu)
            Column {
                Text(strings.deliverTo, style = MaterialTheme.typography.bodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), color = Orange)
                Text(
                    text = address.displayTitle(strings),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Box(contentAlignment = Alignment.TopEnd) {
            CircleIconButton(Icons.Default.ShoppingBag, onOpenCart)
            if (cartCount > 0) {
                CartCountBadge(
                    cartCount = cartCount,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 7.dp, y = (-7).dp)
                        .zIndex(2f),
                )
            }
        }
    }
}

@Composable
private fun PromoCard(promo: com.example.fooddeliveryapp.ui.data.PromoOffer) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = promo.accent),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(promo.accent, promo.accent.copy(alpha = 0.82f), com.example.fooddeliveryapp.ui.theme.Night)))
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AccentChip(promo.badge, CardWhite.copy(alpha = 0.82f), onAccent = com.example.fooddeliveryapp.ui.theme.Night)
            Text(promo.title, style = MaterialTheme.typography.headlineMedium, color = CardWhite)
            Text(promo.description, style = MaterialTheme.typography.bodyLarge, color = CardWhite.copy(alpha = 0.84f))
        }
    }
}

@Composable
private fun CategoryCard(
    title: String,
    emoji: String,
    accent: Color,
) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = emoji)
            }
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun RestaurantCard(
    restaurant: Restaurant,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Brush.linearGradient(listOf(restaurant.accent.copy(alpha = 0.92f), CardWhite))),
            ) {
                DecorativeFoodArt(
                    modifier = Modifier.fillMaxSize(),
                    accent = restaurant.accent,
                    emoji = restaurant.emoji,
                )
            }
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(restaurant.name, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(restaurant.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoPill("⭐ ${restaurant.rating}", Gold)
                    InfoPill("🚚 ${restaurant.deliveryFee}", Sky)
                    InfoPill("⏱ ${restaurant.deliveryTime}", Orange)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DecorativeFoodArt(
    modifier: Modifier,
    accent: Color,
    emoji: String,
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = CardWhite.copy(alpha = 0.42f), radius = size.minDimension * 0.32f, center = Offset(size.width * 0.22f, size.height * 0.34f))
            drawCircle(color = accent.copy(alpha = 0.12f), radius = size.minDimension * 0.46f, center = Offset(size.width * 0.78f, size.height * 0.68f))
        }
        Text(
            text = emoji,
            modifier = Modifier.align(Alignment.Center).rotate(-8f),
            style = MaterialTheme.typography.displayLarge.copy(fontSize = MaterialTheme.typography.displayLarge.fontSize * 2.3),
        )
    }
}

@Composable
private fun PopularItemCard(
    item: MenuItem,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.width(170.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(item.accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = item.emoji, style = MaterialTheme.typography.displayLarge)
            }
            Text(item.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(item.price.asPrice(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun HeroRestaurantCard(restaurant: Restaurant) {
    Card(shape = RoundedCornerShape(34.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Brush.linearGradient(listOf(restaurant.accent.copy(alpha = 0.95f), OrangeSoft, CardWhite))),
            ) {
                DecorativeFoodArt(
                    modifier = Modifier.fillMaxSize(),
                    accent = restaurant.accent,
                    emoji = restaurant.emoji,
                )
            }
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(restaurant.name, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(restaurant.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoPill("⭐ ${restaurant.rating}", Gold)
                    InfoPill("🚚 ${restaurant.deliveryFee}", Sky)
                    InfoPill("⏱ ${restaurant.deliveryTime}", Orange)
                }
            }
        }
    }
}

@Composable
private fun MenuItemRow(
    item: MenuItem,
    onOpen: () -> Unit,
    onAdd: () -> Unit,
) {
    val transparentArt = item.usesTransparentCutoutArt()

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onOpen,
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
                    .size(72.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        if (transparentArt) {
                            Color.Transparent
                        } else {
                            item.accent.copy(alpha = 0.14f)
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (item.imageUrl.isBlank()) {
                    Text(
                        text = item.emoji,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = MaterialTheme.typography.displayLarge.fontSize * 1.3),
                    )
                } else {
                    FoodlyImage(
                        model = item.imageUrl,
                        contentDescription = item.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(if (transparentArt) 2.dp else 0.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(item.price.asPrice(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            }
            PrimaryMiniButton(text = "ADD", onClick = onAdd)
        }
    }
}

