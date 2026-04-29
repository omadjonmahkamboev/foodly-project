package com.example.fooddeliveryapp.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fooddeliveryapp.ui.components.FoodlyImage
import com.example.fooddeliveryapp.ui.AppLanguage
import com.example.fooddeliveryapp.ui.FoodAppState
import com.example.fooddeliveryapp.ui.components.asPrice
import com.example.fooddeliveryapp.ui.data.AppCategory
import com.example.fooddeliveryapp.ui.data.DiscountCoupon
import com.example.fooddeliveryapp.ui.data.MenuItem
import com.example.fooddeliveryapp.ui.data.Restaurant
import com.example.fooddeliveryapp.ui.theme.Border
import com.example.fooddeliveryapp.ui.theme.CardWhite
import com.example.fooddeliveryapp.ui.theme.Ink
import com.example.fooddeliveryapp.ui.theme.Orange
import com.example.fooddeliveryapp.ui.theme.OrangeSoft
import com.example.fooddeliveryapp.ui.theme.Rose
import com.example.fooddeliveryapp.ui.theme.Sky
import com.example.fooddeliveryapp.ui.theme.Success
import kotlin.math.roundToInt

@Composable
fun AdminDashboardScreen(
    appState: FoodAppState,
    onBackToLogin: () -> Unit,
) {
    val text = AdminStrings(appState.language)
    if (appState.currentAdminName == null) {
        AdminClosedSession(text, onBackToLogin)
        return
    }

    var selectedSectionName by rememberSaveable { mutableStateOf(AdminPanelSection.Statistics.name) }
    val selectedSection = AdminPanelSection.valueOf(selectedSectionName)
    val logout = {
        appState.logOutAdmin()
        onBackToLogin()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 34.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            AdminHeader(
                adminName = appState.currentAdminName.orEmpty(),
                text = text,
                onLogout = logout,
            )
        }
        item {
            AdminSectionTabs(
                selected = selectedSection,
                text = text,
                onSelect = { selectedSectionName = it.name },
            )
        }
        when (selectedSection) {
            AdminPanelSection.Statistics -> {
                item { AdminMetricGrid(appState = appState, text = text) }
                item { AdminOverviewSection(appState = appState, text = text) }
            }
            AdminPanelSection.Add -> item {
                AdminAddSection(appState = appState, text = text)
            }
            AdminPanelSection.Users -> item {
                AdminUsersSection(appState = appState, text = text)
            }
            AdminPanelSection.Settings -> item {
                AdminSettingsSection(
                    appState = appState,
                    text = text,
                    onLogout = logout,
                )
            }
        }
    }
}

@Composable
private fun AdminHeader(
    adminName: String,
    text: AdminStrings,
    onLogout: () -> Unit,
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
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Orange),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = CardWhite,
                    modifier = Modifier.size(28.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = text.adminTitle,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = text.signedInAs(adminName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        IconButton(
            onClick = onLogout,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Ink)
        }
    }
}

@Composable
private fun AdminLanguageSelector(
    currentLanguage: AppLanguage,
    text: AdminStrings,
    onLanguageChange: (AppLanguage) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = text.language,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(AppLanguage.values().toList(), key = { it.name }) { language ->
                AdminChoiceChip(
                    text = language.adminDisplayName(),
                    selected = language == currentLanguage,
                    onClick = { onLanguageChange(language) },
                )
            }
        }
    }
}

@Composable
private fun AdminMetricGrid(appState: FoodAppState, text: AdminStrings) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdminMetricCard(
                value = appState.adminCurrentOrders.toString().padStart(2, '0'),
                label = text.runningOrders,
                icon = Icons.Default.LocalShipping,
                accent = Orange,
                modifier = Modifier.weight(1f),
            )
            AdminMetricCard(
                value = appState.adminOrderRequests.toString().padStart(2, '0'),
                label = text.orderRequests,
                icon = Icons.Default.History,
                accent = Sky,
                modifier = Modifier.weight(1f),
            )
        }
        AdminRevenueCard(
            revenue = appState.adminRevenue,
            coupons = appState.activeCoupons.size,
            restaurants = appState.restaurants.size,
            rating = appState.adminAverageRating,
            reviews = appState.adminReviewCount,
            text = text,
        )
    }
}

@Composable
private fun AdminMetricCard(
    value: String,
    label: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.height(118.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
                }
            }
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AdminRevenueCard(
    revenue: Int,
    coupons: Int,
    restaurants: Int,
    rating: Double,
    reviews: Int,
    text: AdminStrings,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Text(
                        text = text.totalRevenue,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = revenue.asPrice(),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                AdminSmallPill(text.discountsCount(coupons))
            }
            AdminSparkline()
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminTinyStat(text.restaurants, restaurants.toString(), Modifier.weight(1f))
                AdminTinyStat(text.rating, "%.1f".format(rating), Modifier.weight(1f))
                AdminTinyStat(text.reviews, reviews.toString(), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AdminSparkline() {
    val values = listOf(18, 28, 24, 39, 33, 47, 44, 58)
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp),
    ) {
        val max = values.maxOrNull()?.toFloat() ?: 1f
        val min = values.minOrNull()?.toFloat() ?: 0f
        val spread = (max - min).coerceAtLeast(1f)
        val step = size.width / (values.lastIndex.coerceAtLeast(1))
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = step * index
            val y = size.height - ((value - min) / spread) * size.height
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawLine(
            color = Border,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 2f,
        )
        drawPath(
            path = path,
            color = Orange,
            style = Stroke(width = 5f, cap = StrokeCap.Round),
        )
        val activeX = step * 3
        val activeY = size.height - ((values[3] - min) / spread) * size.height
        drawCircle(color = CardWhite, radius = 11f, center = Offset(activeX, activeY))
        drawCircle(color = Orange, radius = 7f, center = Offset(activeX, activeY))
    }
}

@Composable
private fun AdminSectionTabs(
    selected: AdminPanelSection,
    text: AdminStrings,
    onSelect: (AdminPanelSection) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(AdminPanelSection.values().toList(), key = { it.name }) { section ->
            AdminNavChip(
                icon = section.icon(),
                label = text.sectionTitle(section),
                selected = section == selected,
                onClick = { onSelect(section) },
            )
        }
    }
}

@Composable
private fun AdminNavChip(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Orange else MaterialTheme.colorScheme.surface)
            .border(1.dp, if (selected) Orange else Border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (selected) CardWhite else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(19.dp),
        )
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = if (selected) CardWhite else MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun AdminPanelSection.icon(): ImageVector = when (this) {
    AdminPanelSection.Statistics -> Icons.Default.History
    AdminPanelSection.Add -> Icons.Default.Add
    AdminPanelSection.Users -> Icons.Default.People
    AdminPanelSection.Settings -> Icons.Default.Settings
}

@Composable
private fun AdminOverviewSection(appState: FoodAppState, text: AdminStrings) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AdminSectionTitle(text.topOrderedDishes, text.liveRanking)
        val topItems = appState.topOrderedItems.take(5)
        if (topItems.isEmpty()) {
            AdminHintCard(text.noOrdersYet, text.noOrdersHint)
        } else {
            topItems.forEachIndexed { index, entry ->
                val (item, count) = entry
                AdminTopDishCard(
                    rank = index + 1,
                    item = item,
                    orders = count,
                    restaurantName = appState.restaurantForMenuItem(item)?.name.orEmpty(),
                )
            }
        }
        AdminSectionTitle(text.quickCatalog, text.latestRestaurants)
        appState.restaurants.take(3).forEach { restaurant ->
            AdminRestaurantCompactCard(restaurant = restaurant)
        }
    }
}

@Composable
private fun AdminAddSection(appState: FoodAppState, text: AdminStrings) {
    Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
        AdminDishSection(appState = appState, text = text)
        AdminRestaurantSection(appState = appState, text = text)
        AdminDiscountSection(appState = appState, text = text)
    }
}

@Composable
private fun AdminUsersSection(appState: FoodAppState, text: AdminStrings) {
    val user = appState.currentUser
    val courier = appState.currentCourier ?: appState.courierProfileDetails
    val activeCourierOrder = appState.activeCourierOrder

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AdminSectionTitle(text.userData, text.customerAndCourier)
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val showTwoColumns = maxWidth >= 780.dp
            if (showTwoColumns) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    AdminUsersPanel(
                        title = text.customerProfile,
                        badge = if (user == null) text.inactive else text.activeCustomerSession,
                        icon = Icons.Default.People,
                        modifier = Modifier.weight(1f),
                    ) {
                        if (user == null) {
                            AdminHintCard(text.noUserData, text.noUserDataHint)
                        } else {
                            AdminInfoCard(
                                icon = Icons.Default.People,
                                title = appState.profileDetails.fullName,
                                lines = listOf(
                                    appState.profileDetails.email,
                                    appState.profileDetails.phone.ifBlank { text.phoneNotAdded },
                                    appState.profileDetails.bio,
                                ),
                            )
                            AdminInfoCard(
                                icon = Icons.Default.Storefront,
                                title = text.savedAddresses,
                                lines = appState.savedAddresses.map { address ->
                                    "${address.label} - ${address.title.ifBlank { text.addressWithoutStreet }}"
                                },
                            )
                            AdminInfoCard(
                                icon = Icons.Default.History,
                                title = text.customerActivity,
                                lines = listOf(
                                    text.runningOrdersValue(appState.ongoingOrders.size),
                                    text.historyOrdersValue(appState.historyOrders.size),
                                    text.favoritesValue(appState.favoriteItemIds.size),
                                    text.paymentCardsValue(appState.savedPaymentCards.size),
                                ),
                            )
                        }
                    }
                    AdminUsersPanel(
                        title = text.courierData,
                        badge = text.onlineStatus(appState.courierIsOnline),
                        icon = Icons.Default.LocalShipping,
                        modifier = Modifier.weight(1f),
                    ) {
                        AdminInfoCard(
                            icon = Icons.Default.LocalShipping,
                            title = courier.name,
                            lines = listOf(
                                courier.email,
                                courier.phone.ifBlank { text.phoneNotAdded },
                                text.vehicleValue(courier.vehicle),
                                text.ratingValue(appState.courierRatingLabel),
                            ),
                        )
                        AdminInfoCard(
                            icon = Icons.Default.Star,
                            title = text.courierOrders,
                            lines = listOf(
                                text.currentCourierOrdersValue(appState.activeCourierOrders.size),
                                text.availableOrdersValue(appState.availableCourierOrders.size),
                                text.deliveredOrdersValue(appState.courierDeliveredCount),
                                text.walletValue(appState.courierWalletBalance.asPrice()),
                                text.reviewsValue(appState.courierRatingCount),
                                text.lastWithdrawalValue(appState.courierLastWithdrawalLabel),
                            ),
                        )
                        if (activeCourierOrder == null) {
                            AdminHintCard(text.noActiveRoute, text.noActiveRouteHint)
                        } else {
                            AdminInfoCard(
                                icon = Icons.Default.Storefront,
                                title = text.activeRoute,
                                lines = listOf(
                                    text.pickupValue(activeCourierOrder.restaurantName),
                                    activeCourierOrder.restaurantAddress,
                                    text.dropOffValue(activeCourierOrder.customerName),
                                    activeCourierOrder.customerAddress.title.ifBlank { activeCourierOrder.customerAddress.subtitle },
                                ),
                            )
                        }
                        if (appState.courierIsBlocked) {
                            AdminHintCard(text.courierBlocked, appState.courierBlockMessage)
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    AdminUsersPanel(
                        title = text.customerProfile,
                        badge = if (user == null) text.inactive else text.activeCustomerSession,
                        icon = Icons.Default.People,
                    ) {
                        if (user == null) {
                            AdminHintCard(text.noUserData, text.noUserDataHint)
                        } else {
                            AdminInfoCard(
                                icon = Icons.Default.People,
                                title = appState.profileDetails.fullName,
                                lines = listOf(
                                    appState.profileDetails.email,
                                    appState.profileDetails.phone.ifBlank { text.phoneNotAdded },
                                    appState.profileDetails.bio,
                                ),
                            )
                            AdminInfoCard(
                                icon = Icons.Default.Storefront,
                                title = text.savedAddresses,
                                lines = appState.savedAddresses.map { address ->
                                    "${address.label} - ${address.title.ifBlank { text.addressWithoutStreet }}"
                                },
                            )
                            AdminInfoCard(
                                icon = Icons.Default.History,
                                title = text.customerActivity,
                                lines = listOf(
                                    text.runningOrdersValue(appState.ongoingOrders.size),
                                    text.historyOrdersValue(appState.historyOrders.size),
                                    text.favoritesValue(appState.favoriteItemIds.size),
                                    text.paymentCardsValue(appState.savedPaymentCards.size),
                                ),
                            )
                        }
                    }
                    AdminUsersPanel(
                        title = text.courierData,
                        badge = text.onlineStatus(appState.courierIsOnline),
                        icon = Icons.Default.LocalShipping,
                    ) {
                        AdminInfoCard(
                            icon = Icons.Default.LocalShipping,
                            title = courier.name,
                            lines = listOf(
                                courier.email,
                                courier.phone.ifBlank { text.phoneNotAdded },
                                text.vehicleValue(courier.vehicle),
                                text.ratingValue(appState.courierRatingLabel),
                            ),
                        )
                        AdminInfoCard(
                            icon = Icons.Default.Star,
                            title = text.courierOrders,
                            lines = listOf(
                                text.currentCourierOrdersValue(appState.activeCourierOrders.size),
                                text.availableOrdersValue(appState.availableCourierOrders.size),
                                text.deliveredOrdersValue(appState.courierDeliveredCount),
                                text.walletValue(appState.courierWalletBalance.asPrice()),
                                text.reviewsValue(appState.courierRatingCount),
                                text.lastWithdrawalValue(appState.courierLastWithdrawalLabel),
                            ),
                        )
                        if (activeCourierOrder == null) {
                            AdminHintCard(text.noActiveRoute, text.noActiveRouteHint)
                        } else {
                            AdminInfoCard(
                                icon = Icons.Default.Storefront,
                                title = text.activeRoute,
                                lines = listOf(
                                    text.pickupValue(activeCourierOrder.restaurantName),
                                    activeCourierOrder.restaurantAddress,
                                    text.dropOffValue(activeCourierOrder.customerName),
                                    activeCourierOrder.customerAddress.title.ifBlank { activeCourierOrder.customerAddress.subtitle },
                                ),
                            )
                        }
                        if (appState.courierIsBlocked) {
                            AdminHintCard(text.courierBlocked, appState.courierBlockMessage)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminSettingsSection(
    appState: FoodAppState,
    text: AdminStrings,
    onLogout: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AdminSectionTitle(text.settings, text.workspace)
        AdminLanguageSelector(
            currentLanguage = appState.language,
            text = text,
            onLanguageChange = appState::updateLanguage,
        )
        AdminInfoCard(
            icon = Icons.Default.Settings,
            title = text.catalogStatus,
            lines = listOf(
                text.restaurantsValue(appState.restaurants.size),
                text.menuItemsValue(appState.restaurants.sumOf { it.menu.size }),
                text.discountsCount(appState.activeCoupons.size),
            ),
        )
        AdminPrimaryButton(text.backToLogin, onClick = onLogout, icon = Icons.AutoMirrored.Filled.Logout)
    }
}

@Composable
private fun AdminInfoCard(
    icon: ImageVector,
    title: String,
    lines: List<String>,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Orange.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = Orange, modifier = Modifier.size(22.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                lines.filter { it.isNotBlank() }.forEach { line ->
                    Text(
                        line,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminUsersPanel(
    title: String,
    badge: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Orange.copy(alpha = 0.13f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(icon, contentDescription = null, tint = Orange, modifier = Modifier.size(22.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    AdminSmallPill(badge)
                }
                content()
            },
        )
    }
}

@Composable
private fun AdminDishSection(appState: FoodAppState, text: AdminStrings) {
    val firstRestaurantId = appState.restaurants.firstOrNull()?.id.orEmpty()
    val firstCategoryId = appState.categories.firstOrNull()?.id ?: "burger"
    var selectedRestaurantId by rememberSaveable(appState.restaurants.size) { mutableStateOf(firstRestaurantId) }
    var selectedCategoryId by rememberSaveable { mutableStateOf(firstCategoryId) }
    var name by rememberSaveable { mutableStateOf("") }
    var subtitle by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var imageUrl by rememberSaveable { mutableStateOf("") }
    var ingredients by rememberSaveable { mutableStateOf("salt, chicken, onion") }
    var details by rememberSaveable { mutableStateOf("") }
    var pickup by rememberSaveable { mutableStateOf(true) }
    var delivery by rememberSaveable { mutableStateOf(true) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var isError by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            imageUrl = it.toString()
        }
    }

    fun addDish() {
        val result = appState.addAdminMenuItem(
            restaurantId = selectedRestaurantId,
            title = name,
            subtitle = subtitle,
            price = price.toIntOrNull() ?: 0,
            category = selectedCategoryId,
            imageUrl = imageUrl,
            ingredients = ingredients.split(","),
            details = details,
        )
        if (result == null) {
            name = ""
            subtitle = ""
            price = ""
            imageUrl = ""
            details = ""
            message = text.dishAdded
            isError = false
        } else {
            message = result
            isError = true
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AdminSectionTitle(text.addNewItem, text.productFlow)
        AdminRestaurantSelector(
            restaurants = appState.restaurants,
            selectedRestaurantId = selectedRestaurantId,
            text = text,
            onSelect = { selectedRestaurantId = it },
        )
        AdminField(text.itemName, name, { name = it }, "Mazali chicken Halim")
        AdminUploadStrip(
            imageUrl = imageUrl,
            text = text,
            onPickPhoto = { imagePicker.launch(arrayOf("image/*")) },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdminField(
                label = text.price,
                value = price,
                onValueChange = { price = it.filter(Char::isDigit) },
                placeholder = "50",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
            )
            AdminCheckOption(text.pickup, pickup, { pickup = it }, Modifier.weight(1f))
            AdminCheckOption(text.delivery, delivery, { delivery = it }, Modifier.weight(1f))
        }
        AdminField(text.shortText, subtitle, { subtitle = it }, "Chicken, rice and house sauce")
        AdminCategorySelector(
            categories = appState.categories,
            selectedCategoryId = selectedCategoryId,
            text = text,
            onSelect = { selectedCategoryId = it },
        )
        AdminField(text.ingredients, ingredients, { ingredients = it }, "salt, chicken, onion")
        AdminField(
            label = text.details,
            value = details,
            onValueChange = { details = it },
            placeholder = text.writeDescription,
            singleLine = false,
            minLines = 4,
        )
        AdminPrimaryButton(text.saveChanges, onClick = ::addDish, icon = Icons.Default.Add)
        AdminMessage(message = message, isError = isError)
        AdminSectionTitle(text.recentDishes, text.itemsCount(appState.restaurants.sumOf { it.menu.size }))
        appState.restaurants
            .flatMap { restaurant -> restaurant.menu.map { item -> restaurant to item } }
            .take(6)
            .forEach { (restaurant, item) ->
                AdminDishCompactCard(item = item, restaurant = restaurant)
            }
    }
}

@Composable
private fun AdminRestaurantSection(appState: FoodAppState, text: AdminStrings) {
    var name by rememberSaveable { mutableStateOf("") }
    var subtitle by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var imageUrls by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var deliveryTime by rememberSaveable { mutableStateOf("18-25 min") }
    var deliveryFee by rememberSaveable { mutableStateOf("Free") }
    var tags by rememberSaveable { mutableStateOf("New, Admin, Popular") }
    var latitude by rememberSaveable { mutableStateOf("41.311090") }
    var longitude by rememberSaveable { mutableStateOf("69.279782") }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var isError by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        val picked = uris.map { uri ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            uri.toString()
        }
        imageUrls = (imageUrls + picked).distinct()
    }

    fun addRestaurant() {
        val latitudeValue = latitude.toDoubleOrNull()
        val longitudeValue = longitude.toDoubleOrNull()
        if ((latitude.isNotBlank() && latitudeValue == null) || (longitude.isNotBlank() && longitudeValue == null)) {
            message = "Enter correct coordinates"
            isError = true
            return
        }
        val result = appState.addAdminRestaurant(
            name = name,
            subtitle = subtitle,
            description = description,
            imageUrl = imageUrls.firstOrNull().orEmpty(),
            imageUrls = imageUrls,
            deliveryTime = deliveryTime,
            deliveryFee = deliveryFee,
            tags = tags.split(","),
            latitude = latitudeValue,
            longitude = longitudeValue,
        )
        if (result == null) {
            name = ""
            subtitle = ""
            description = ""
            imageUrls = emptyList()
            tags = "New, Admin, Popular"
            latitude = "41.311090"
            longitude = "69.279782"
            message = text.restaurantAdded
            isError = false
        } else {
            message = result
            isError = true
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AdminSectionTitle(text.addRestaurant, text.newKitchen)
        AdminField(text.restaurantName, name, { name = it }, "Halal Lab office")
        AdminUploadStrip(
            imageUrl = imageUrls.firstOrNull().orEmpty(),
            text = text,
            onPickPhoto = { imagePicker.launch(arrayOf("image/*")) },
        )
        if (imageUrls.isNotEmpty()) {
            Text(
                text = text.photosSelected(imageUrls.size),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(imageUrls, key = { it }) { image ->
                    AdminMediaSlot(
                        title = text.photo,
                        subtitle = text.photoReady,
                        imageUrl = image,
                        onClick = { imagePicker.launch(arrayOf("image/*")) },
                        modifier = Modifier.width(118.dp),
                    )
                }
            }
        }
        AdminField(text.subtitle, subtitle, { subtitle = it }, "Fresh bowls and grilled food")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdminField(text.deliveryTime, deliveryTime, { deliveryTime = it }, "18-25 min", modifier = Modifier.weight(1f))
            AdminField(text.deliveryFee, deliveryFee, { deliveryFee = it }, "Free", modifier = Modifier.weight(1f))
        }
        AdminField("Tags", tags, { tags = it }, "Burger, Grill, Popular")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdminField("Latitude", latitude, { latitude = it }, "41.311090", modifier = Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
            AdminField("Longitude", longitude, { longitude = it }, "69.279782", modifier = Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
        }
        AdminField(
            label = text.description,
            value = description,
            onValueChange = { description = it },
            placeholder = text.describeRestaurant,
            singleLine = false,
            minLines = 4,
        )
        AdminPrimaryButton(text.saveRestaurant, onClick = ::addRestaurant, icon = Icons.Default.Storefront)
        AdminMessage(message = message, isError = isError)
        AdminSectionTitle(text.restaurants, text.activeCount(appState.restaurants.size))
        appState.restaurants.forEach { restaurant ->
            AdminRestaurantCompactCard(restaurant = restaurant)
        }
    }
}

@Composable
private fun AdminDiscountSection(appState: FoodAppState, text: AdminStrings) {
    var code by rememberSaveable { mutableStateOf("SAVE15") }
    var title by rememberSaveable { mutableStateOf("Weekend discount") }
    var percent by rememberSaveable { mutableStateOf("15") }
    var activeDays by rememberSaveable { mutableStateOf("14") }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var isError by rememberSaveable { mutableStateOf(false) }

    fun addCoupon() {
        val result = appState.addAdminCoupon(
            code = code,
            title = title,
            percent = percent.toIntOrNull() ?: 0,
            activeDays = activeDays.toIntOrNull() ?: 0,
        )
        if (result == null) {
            message = text.discountSaved
            isError = false
        } else {
            message = result
            isError = true
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AdminSectionTitle(text.addDiscount, text.couponManager)
        AdminField(text.couponCode, code, { code = it.uppercase() }, "SAVE15")
        AdminField(text.title, title, { title = it }, "Weekend discount")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdminField(
                label = text.percent,
                value = percent,
                onValueChange = { percent = it.filter(Char::isDigit) },
                placeholder = "15",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
            )
            AdminField(
                label = text.activeDays,
                value = activeDays,
                onValueChange = { activeDays = it.filter(Char::isDigit) },
                placeholder = "14",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
            )
        }
        AdminPrimaryButton(text.saveDiscount, onClick = ::addCoupon, icon = Icons.Default.LocalOffer)
        AdminMessage(message = message, isError = isError)
        AdminSectionTitle(text.activeDiscounts, text.liveCount(appState.activeCoupons.size))
        if (appState.activeCoupons.isEmpty()) {
            AdminHintCard(text.noDiscountsYet, text.discountsWillAppear)
        } else {
            appState.activeCoupons.forEach { coupon ->
                AdminCouponCard(
                    coupon = coupon,
                    removable = coupon in appState.adminCoupons,
                    text = text,
                    onRemove = { appState.removeAdminCoupon(coupon.code) },
                )
            }
        }
    }
}

@Composable
private fun AdminTopDishCard(
    rank: Int,
    item: MenuItem,
    orders: Int,
    restaurantName: String,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Orange.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("#$rank", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Orange)
            }
            AdminFoodImage(imageUrl = item.imageUrl, fallback = item.emoji, modifier = Modifier.size(58.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(restaurantName.ifBlank { item.subtitle }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(orders.toString(), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                Text("orders", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AdminDishCompactCard(
    item: MenuItem,
    restaurant: Restaurant,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AdminFoodImage(imageUrl = item.imageUrl, fallback = item.emoji, modifier = Modifier.size(58.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(restaurant.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(item.price.asPrice(), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Orange)
        }
    }
}

@Composable
private fun AdminRestaurantCompactCard(restaurant: Restaurant) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AdminFoodImage(imageUrl = restaurant.imageUrl, fallback = restaurant.emoji, modifier = Modifier.size(64.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(restaurant.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(restaurant.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    AdminMetaText(Icons.Default.Star, restaurant.rating.toString())
                    AdminMetaText(Icons.Default.RestaurantMenu, "${restaurant.menu.size}")
                    AdminMetaText(Icons.Default.LocalShipping, restaurant.deliveryTime)
                }
            }
        }
    }
}

@Composable
private fun AdminCouponCard(
    coupon: DiscountCoupon,
    removable: Boolean,
    text: AdminStrings,
    onRemove: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AdminSmallPill(coupon.code)
                Text("${coupon.discountPercent}%", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = Orange)
            }
            Text(coupon.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(coupon.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(coupon.daysLeftLabel(text), style = MaterialTheme.typography.bodySmall, color = Success)
                if (removable) {
                    TextButton(onClick = onRemove) {
                        Text(text.remove, color = Rose)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminRestaurantSelector(
    restaurants: List<Restaurant>,
    selectedRestaurantId: String,
    text: AdminStrings,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text.restaurant, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(restaurants, key = { it.id }) { restaurant ->
                AdminChoiceChip(
                    text = restaurant.name,
                    selected = restaurant.id == selectedRestaurantId,
                    onClick = { onSelect(restaurant.id) },
                )
            }
        }
    }
}

@Composable
private fun AdminCategorySelector(
    categories: List<AppCategory>,
    selectedCategoryId: String,
    text: AdminStrings,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text.ingredientsCategory, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text.seeAll, style = MaterialTheme.typography.bodySmall, color = Orange)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(categories, key = { it.id }) { category ->
                AdminChoiceChip(
                    text = category.title,
                    selected = category.id == selectedCategoryId,
                    onClick = { onSelect(category.id) },
                )
            }
        }
    }
}

@Composable
private fun AdminUploadStrip(
    imageUrl: String,
    text: AdminStrings,
    onPickPhoto: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text.uploadPhoto, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdminMediaSlot(
                title = text.photo,
                subtitle = if (imageUrl.isBlank()) text.chooseFromDevice else text.photoReady,
                imageUrl = imageUrl,
                onClick = onPickPhoto,
                modifier = Modifier.weight(1f),
            )
            AdminMediaSlot(
                title = text.video,
                subtitle = text.addLater,
                imageUrl = "",
                onClick = {},
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun AdminMediaSlot(
    title: String,
    subtitle: String,
    imageUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(92.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Border, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUrl.isNotBlank()) {
            FoodlyImage(
                model = imageUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(OrangeSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Orange, modifier = Modifier.size(19.dp))
                }
                Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AdminFoodImage(
    imageUrl: String,
    fallback: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUrl.isBlank()) {
            Text(fallback, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        } else {
            FoodlyImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun AdminField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Orange.copy(alpha = 0.6f),
                unfocusedBorderColor = Border,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
        )
    }
}

@Composable
private fun AdminCheckOption(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .heightIn(min = 58.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Border, RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = Orange),
        )
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}

@Composable
private fun AdminChoiceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .height(42.dp)
            .widthIn(min = 72.dp)
            .clip(RoundedCornerShape(21.dp))
            .background(if (selected) Orange else MaterialTheme.colorScheme.surface)
            .border(1.dp, if (selected) Orange else Border, RoundedCornerShape(21.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = if (selected) CardWhite else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AdminPrimaryButton(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 58.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = CardWhite),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text.uppercase(), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun AdminHintCard(
    title: String,
    text: String,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Success)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AdminSectionTitle(
    title: String,
    action: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
        Text(action, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Orange)
    }
}

@Composable
private fun AdminSmallPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(OrangeSoft)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Orange)
    }
}

@Composable
private fun AdminTinyStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun AdminMetaText(
    icon: ImageVector,
    text: String,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Orange, modifier = Modifier.size(14.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun AdminMessage(
    message: String?,
    isError: Boolean,
) {
    if (message == null) return
    Text(
        text = message,
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        color = if (isError) Rose else Success,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun AdminClosedSession(
    text: AdminStrings,
    onBackToLogin: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text.sessionClosed, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(16.dp))
        AdminPrimaryButton(text.backToLogin, onClick = onBackToLogin)
    }
}

private enum class AdminPanelSection {
    Statistics,
    Add,
    Users,
    Settings,
}

private fun DiscountCoupon.daysLeftLabel(text: AdminStrings): String {
    val millis = expiresAtMillis - System.currentTimeMillis()
    val days = (millis.toDouble() / (24 * 60 * 60 * 1_000L)).roundToInt().coerceAtLeast(0)
    return text.daysLeft(days)
}

private class AdminStrings(private val locale: AppLanguage) {
    val adminTitle: String get() = when (locale) {
        AppLanguage.English -> "Foodly Admin"
        AppLanguage.Russian -> "Админ Foodly"
        AppLanguage.Uzbek -> "Foodly admin"
    }
    fun signedInAs(name: String): String = when (locale) {
        AppLanguage.English -> "Signed in as $name"
        AppLanguage.Russian -> "Вход выполнен: $name"
        AppLanguage.Uzbek -> "$name sifatida kirildi"
    }
    val language: String get() = when (locale) {
        AppLanguage.English -> "Language"
        AppLanguage.Russian -> "Язык"
        AppLanguage.Uzbek -> "Til"
    }
    val runningOrders: String get() = when (locale) {
        AppLanguage.English -> "Running orders"
        AppLanguage.Russian -> "Текущие заказы"
        AppLanguage.Uzbek -> "Joriy buyurtmalar"
    }
    val orderRequests: String get() = when (locale) {
        AppLanguage.English -> "Order requests"
        AppLanguage.Russian -> "Заявки заказов"
        AppLanguage.Uzbek -> "Buyurtma so'rovlari"
    }
    val totalRevenue: String get() = when (locale) {
        AppLanguage.English -> "Total revenue"
        AppLanguage.Russian -> "Общая выручка"
        AppLanguage.Uzbek -> "Umumiy tushum"
    }
    fun discountsCount(count: Int): String = when (locale) {
        AppLanguage.English -> "$count discounts"
        AppLanguage.Russian -> "$count скидок"
        AppLanguage.Uzbek -> "$count chegirma"
    }
    val restaurants: String get() = when (locale) {
        AppLanguage.English -> "Restaurants"
        AppLanguage.Russian -> "Рестораны"
        AppLanguage.Uzbek -> "Restoranlar"
    }
    val rating: String get() = when (locale) {
        AppLanguage.English -> "Rating"
        AppLanguage.Russian -> "Рейтинг"
        AppLanguage.Uzbek -> "Reyting"
    }
    val reviews: String get() = when (locale) {
        AppLanguage.English -> "Reviews"
        AppLanguage.Russian -> "Отзывы"
        AppLanguage.Uzbek -> "Sharhlar"
    }
    val userData: String get() = when (locale) {
        AppLanguage.English -> "User data"
        AppLanguage.Russian -> "Данные пользователей"
        AppLanguage.Uzbek -> "Foydalanuvchi ma'lumotlari"
    }
    val currentSession: String get() = when (locale) {
        AppLanguage.English -> "Current session"
        AppLanguage.Russian -> "Текущая сессия"
        AppLanguage.Uzbek -> "Joriy sessiya"
    }
    val customerAndCourier: String get() = when (locale) {
        AppLanguage.English -> "Customer + courier"
        AppLanguage.Russian -> "Клиент и курьер"
        AppLanguage.Uzbek -> "Mijoz va kuryer"
    }
    val customerProfile: String get() = when (locale) {
        AppLanguage.English -> "Customer profile"
        AppLanguage.Russian -> "Профиль клиента"
        AppLanguage.Uzbek -> "Mijoz profili"
    }
    val courierData: String get() = when (locale) {
        AppLanguage.English -> "Courier data"
        AppLanguage.Russian -> "Данные курьера"
        AppLanguage.Uzbek -> "Kuryer ma'lumotlari"
    }
    val activeCustomerSession: String get() = when (locale) {
        AppLanguage.English -> "Active"
        AppLanguage.Russian -> "Активен"
        AppLanguage.Uzbek -> "Faol"
    }
    val inactive: String get() = when (locale) {
        AppLanguage.English -> "Inactive"
        AppLanguage.Russian -> "Неактивен"
        AppLanguage.Uzbek -> "Nofaol"
    }
    val noUserData: String get() = when (locale) {
        AppLanguage.English -> "No active customer"
        AppLanguage.Russian -> "Активного пользователя нет"
        AppLanguage.Uzbek -> "Faol mijoz yo'q"
    }
    val noUserDataHint: String get() = when (locale) {
        AppLanguage.English -> "Customer data appears here after a user signs in on this device."
        AppLanguage.Russian -> "Данные появятся здесь после входа пользователя на этом устройстве."
        AppLanguage.Uzbek -> "Mijoz qurilmada kirgandan keyin ma'lumot shu yerda chiqadi."
    }
    val phoneNotAdded: String get() = when (locale) {
        AppLanguage.English -> "Phone not added"
        AppLanguage.Russian -> "Телефон не добавлен"
        AppLanguage.Uzbek -> "Telefon kiritilmagan"
    }
    val savedAddresses: String get() = when (locale) {
        AppLanguage.English -> "Saved addresses"
        AppLanguage.Russian -> "Сохраненные адреса"
        AppLanguage.Uzbek -> "Saqlangan manzillar"
    }
    val addressWithoutStreet: String get() = when (locale) {
        AppLanguage.English -> "Address without street"
        AppLanguage.Russian -> "Адрес без улицы"
        AppLanguage.Uzbek -> "Ko'chasiz manzil"
    }
    val customerActivity: String get() = when (locale) {
        AppLanguage.English -> "Customer activity"
        AppLanguage.Russian -> "Активность пользователя"
        AppLanguage.Uzbek -> "Mijoz faolligi"
    }
    val orders: String get() = when (locale) {
        AppLanguage.English -> "Orders"
        AppLanguage.Russian -> "Заказы"
        AppLanguage.Uzbek -> "Buyurtmalar"
    }
    val courierOrders: String get() = when (locale) {
        AppLanguage.English -> "Courier orders"
        AppLanguage.Russian -> "Заказы курьера"
        AppLanguage.Uzbek -> "Kuryer buyurtmalari"
    }
    val activeRoute: String get() = when (locale) {
        AppLanguage.English -> "Active route"
        AppLanguage.Russian -> "Текущий маршрут"
        AppLanguage.Uzbek -> "Joriy marshrut"
    }
    val noActiveRoute: String get() = when (locale) {
        AppLanguage.English -> "No active route"
        AppLanguage.Russian -> "Активного маршрута нет"
        AppLanguage.Uzbek -> "Faol marshrut yo'q"
    }
    val noActiveRouteHint: String get() = when (locale) {
        AppLanguage.English -> "The route to restaurant and customer will appear here once the courier accepts an order."
        AppLanguage.Russian -> "Маршрут до ресторана и клиента появится здесь, как только курьер примет заказ."
        AppLanguage.Uzbek -> "Kuryer buyurtmani qabul qilgach, restoran va mijoz marshruti shu yerda ko'rinadi."
    }
    val courierBlocked: String get() = when (locale) {
        AppLanguage.English -> "Courier block"
        AppLanguage.Russian -> "Блокировка курьера"
        AppLanguage.Uzbek -> "Kuryer bloklandi"
    }
    val settings: String get() = when (locale) {
        AppLanguage.English -> "Settings"
        AppLanguage.Russian -> "Настройки"
        AppLanguage.Uzbek -> "Sozlamalar"
    }
    val workspace: String get() = when (locale) {
        AppLanguage.English -> "Workspace"
        AppLanguage.Russian -> "Рабочее место"
        AppLanguage.Uzbek -> "Ish joyi"
    }
    val catalogStatus: String get() = when (locale) {
        AppLanguage.English -> "Catalog status"
        AppLanguage.Russian -> "Состояние каталога"
        AppLanguage.Uzbek -> "Katalog holati"
    }
    fun savedAddressesCount(count: Int): String = when (locale) {
        AppLanguage.English -> "$count addresses"
        AppLanguage.Russian -> "$count адресов"
        AppLanguage.Uzbek -> "$count ta manzil"
    }
    fun ordersCount(count: Int): String = when (locale) {
        AppLanguage.English -> "$count orders"
        AppLanguage.Russian -> "$count заказов"
        AppLanguage.Uzbek -> "$count ta buyurtma"
    }
    fun runningOrdersValue(count: Int): String = when (locale) {
        AppLanguage.English -> "Active: $count"
        AppLanguage.Russian -> "Активные: $count"
        AppLanguage.Uzbek -> "Faol: $count"
    }
    fun historyOrdersValue(count: Int): String = when (locale) {
        AppLanguage.English -> "History: $count"
        AppLanguage.Russian -> "История: $count"
        AppLanguage.Uzbek -> "Tarix: $count"
    }
    fun favoritesValue(count: Int): String = when (locale) {
        AppLanguage.English -> "Favorites: $count"
        AppLanguage.Russian -> "Избранное: $count"
        AppLanguage.Uzbek -> "Sevimlilar: $count"
    }
    fun paymentCardsValue(count: Int): String = when (locale) {
        AppLanguage.English -> "Payment cards: $count"
        AppLanguage.Russian -> "Карты оплаты: $count"
        AppLanguage.Uzbek -> "To'lov kartalari: $count"
    }
    fun vehicleValue(vehicle: String): String = when (locale) {
        AppLanguage.English -> "Vehicle: $vehicle"
        AppLanguage.Russian -> "Транспорт: $vehicle"
        AppLanguage.Uzbek -> "Transport: $vehicle"
    }
    fun ratingValue(value: String): String = when (locale) {
        AppLanguage.English -> "Rating: $value"
        AppLanguage.Russian -> "Рейтинг: $value"
        AppLanguage.Uzbek -> "Reyting: $value"
    }
    fun onlineStatus(isOnline: Boolean): String = when (locale) {
        AppLanguage.English -> if (isOnline) "Online" else "Offline"
        AppLanguage.Russian -> if (isOnline) "Онлайн" else "Оффлайн"
        AppLanguage.Uzbek -> if (isOnline) "Onlayn" else "Oflayn"
    }
    fun currentCourierOrdersValue(count: Int): String = when (locale) {
        AppLanguage.English -> "Current: $count"
        AppLanguage.Russian -> "Текущие: $count"
        AppLanguage.Uzbek -> "Joriy: $count"
    }
    fun availableOrdersValue(count: Int): String = when (locale) {
        AppLanguage.English -> "Available: $count"
        AppLanguage.Russian -> "Доступные: $count"
        AppLanguage.Uzbek -> "Mavjud: $count"
    }
    fun deliveredOrdersValue(count: Int): String = when (locale) {
        AppLanguage.English -> "Delivered: $count"
        AppLanguage.Russian -> "Доставлено: $count"
        AppLanguage.Uzbek -> "Yetkazilgan: $count"
    }
    fun walletValue(value: String): String = when (locale) {
        AppLanguage.English -> "Wallet: $value"
        AppLanguage.Russian -> "Кошелек: $value"
        AppLanguage.Uzbek -> "Hamyon: $value"
    }
    fun reviewsValue(count: Int): String = when (locale) {
        AppLanguage.English -> "Reviews: $count"
        AppLanguage.Russian -> "Отзывы: $count"
        AppLanguage.Uzbek -> "Sharhlar: $count"
    }
    fun lastWithdrawalValue(value: String): String = when (locale) {
        AppLanguage.English -> "Last withdrawal: $value"
        AppLanguage.Russian -> "Последний вывод: $value"
        AppLanguage.Uzbek -> "Oxirgi yechish: $value"
    }
    fun pickupValue(value: String): String = when (locale) {
        AppLanguage.English -> "Pickup: $value"
        AppLanguage.Russian -> "Забрать: $value"
        AppLanguage.Uzbek -> "Olib ketish: $value"
    }
    fun dropOffValue(value: String): String = when (locale) {
        AppLanguage.English -> "Drop off: $value"
        AppLanguage.Russian -> "Доставить: $value"
        AppLanguage.Uzbek -> "Yetkazish: $value"
    }
    fun restaurantsValue(count: Int): String = when (locale) {
        AppLanguage.English -> "Restaurants: $count"
        AppLanguage.Russian -> "Рестораны: $count"
        AppLanguage.Uzbek -> "Restoranlar: $count"
    }
    fun menuItemsValue(count: Int): String = when (locale) {
        AppLanguage.English -> "Menu items: $count"
        AppLanguage.Russian -> "Блюда: $count"
        AppLanguage.Uzbek -> "Taomlar: $count"
    }
    val topOrderedDishes: String get() = when (locale) {
        AppLanguage.English -> "Top ordered dishes"
        AppLanguage.Russian -> "Популярные блюда"
        AppLanguage.Uzbek -> "Mashhur taomlar"
    }
    val liveRanking: String get() = when (locale) {
        AppLanguage.English -> "Live ranking"
        AppLanguage.Russian -> "Живой рейтинг"
        AppLanguage.Uzbek -> "Jonli reyting"
    }
    val noOrdersYet: String get() = when (locale) {
        AppLanguage.English -> "No orders yet"
        AppLanguage.Russian -> "Заказов пока нет"
        AppLanguage.Uzbek -> "Hozircha buyurtma yo'q"
    }
    val noOrdersHint: String get() = when (locale) {
        AppLanguage.English -> "New completed orders will raise dishes in this list."
        AppLanguage.Russian -> "Реальные завершенные заказы будут поднимать блюда в этом списке."
        AppLanguage.Uzbek -> "Yakunlangan haqiqiy buyurtmalar taomlarni shu ro'yxatda ko'taradi."
    }
    val quickCatalog: String get() = when (locale) {
        AppLanguage.English -> "Quick catalog"
        AppLanguage.Russian -> "Каталог"
        AppLanguage.Uzbek -> "Katalog"
    }
    val latestRestaurants: String get() = when (locale) {
        AppLanguage.English -> "Latest restaurants"
        AppLanguage.Russian -> "Последние рестораны"
        AppLanguage.Uzbek -> "So'nggi restoranlar"
    }
    val addNewItem: String get() = when (locale) {
        AppLanguage.English -> "Add new item"
        AppLanguage.Russian -> "Добавить блюдо"
        AppLanguage.Uzbek -> "Yangi taom qo'shish"
    }
    val productFlow: String get() = when (locale) {
        AppLanguage.English -> "Product flow"
        AppLanguage.Russian -> "Товар"
        AppLanguage.Uzbek -> "Mahsulot"
    }
    val itemName: String get() = when (locale) {
        AppLanguage.English -> "Item name"
        AppLanguage.Russian -> "Название блюда"
        AppLanguage.Uzbek -> "Taom nomi"
    }
    val price: String get() = when (locale) {
        AppLanguage.English -> "Price"
        AppLanguage.Russian -> "Цена"
        AppLanguage.Uzbek -> "Narx"
    }
    val pickup: String get() = when (locale) {
        AppLanguage.English -> "Pick up"
        AppLanguage.Russian -> "Самовывоз"
        AppLanguage.Uzbek -> "Olib ketish"
    }
    val delivery: String get() = when (locale) {
        AppLanguage.English -> "Delivery"
        AppLanguage.Russian -> "Доставка"
        AppLanguage.Uzbek -> "Yetkazish"
    }
    val shortText: String get() = when (locale) {
        AppLanguage.English -> "Short text"
        AppLanguage.Russian -> "Короткое описание"
        AppLanguage.Uzbek -> "Qisqa matn"
    }
    val ingredientsCategory: String get() = when (locale) {
        AppLanguage.English -> "Ingredients category"
        AppLanguage.Russian -> "Категория"
        AppLanguage.Uzbek -> "Kategoriya"
    }
    val seeAll: String get() = when (locale) {
        AppLanguage.English -> "See all"
        AppLanguage.Russian -> "Все"
        AppLanguage.Uzbek -> "Hammasi"
    }
    val ingredients: String get() = when (locale) {
        AppLanguage.English -> "Ingredients"
        AppLanguage.Russian -> "Ингредиенты"
        AppLanguage.Uzbek -> "Tarkibi"
    }
    val details: String get() = when (locale) {
        AppLanguage.English -> "Details"
        AppLanguage.Russian -> "Детали"
        AppLanguage.Uzbek -> "Tafsilotlar"
    }
    val writeDescription: String get() = when (locale) {
        AppLanguage.English -> "Write item description"
        AppLanguage.Russian -> "Напишите описание блюда"
        AppLanguage.Uzbek -> "Taom tavsifini yozing"
    }
    val saveChanges: String get() = when (locale) {
        AppLanguage.English -> "Save changes"
        AppLanguage.Russian -> "Сохранить"
        AppLanguage.Uzbek -> "Saqlash"
    }
    val dishAdded: String get() = when (locale) {
        AppLanguage.English -> "Dish added to catalog"
        AppLanguage.Russian -> "Блюдо добавлено в каталог"
        AppLanguage.Uzbek -> "Taom katalogga qo'shildi"
    }
    val recentDishes: String get() = when (locale) {
        AppLanguage.English -> "Recent dishes"
        AppLanguage.Russian -> "Последние блюда"
        AppLanguage.Uzbek -> "So'nggi taomlar"
    }
    fun itemsCount(count: Int): String = when (locale) {
        AppLanguage.English -> "$count items"
        AppLanguage.Russian -> "$count позиций"
        AppLanguage.Uzbek -> "$count ta mahsulot"
    }
    val addRestaurant: String get() = when (locale) {
        AppLanguage.English -> "Add restaurant"
        AppLanguage.Russian -> "Добавить ресторан"
        AppLanguage.Uzbek -> "Restoran qo'shish"
    }
    val newKitchen: String get() = when (locale) {
        AppLanguage.English -> "New kitchen"
        AppLanguage.Russian -> "Новая кухня"
        AppLanguage.Uzbek -> "Yangi oshxona"
    }
    val restaurantName: String get() = when (locale) {
        AppLanguage.English -> "Restaurant name"
        AppLanguage.Russian -> "Название ресторана"
        AppLanguage.Uzbek -> "Restoran nomi"
    }
    val subtitle: String get() = when (locale) {
        AppLanguage.English -> "Subtitle"
        AppLanguage.Russian -> "Подзаголовок"
        AppLanguage.Uzbek -> "Izoh"
    }
    val deliveryTime: String get() = when (locale) {
        AppLanguage.English -> "Delivery time"
        AppLanguage.Russian -> "Время доставки"
        AppLanguage.Uzbek -> "Yetkazish vaqti"
    }
    val deliveryFee: String get() = when (locale) {
        AppLanguage.English -> "Delivery fee"
        AppLanguage.Russian -> "Цена доставки"
        AppLanguage.Uzbek -> "Yetkazish narxi"
    }
    val description: String get() = when (locale) {
        AppLanguage.English -> "Description"
        AppLanguage.Russian -> "Описание"
        AppLanguage.Uzbek -> "Tavsif"
    }
    val describeRestaurant: String get() = when (locale) {
        AppLanguage.English -> "Describe this restaurant"
        AppLanguage.Russian -> "Опишите ресторан"
        AppLanguage.Uzbek -> "Restoranni tasvirlang"
    }
    val saveRestaurant: String get() = when (locale) {
        AppLanguage.English -> "Save restaurant"
        AppLanguage.Russian -> "Сохранить ресторан"
        AppLanguage.Uzbek -> "Restoranni saqlash"
    }
    val restaurantAdded: String get() = when (locale) {
        AppLanguage.English -> "Restaurant added"
        AppLanguage.Russian -> "Ресторан добавлен"
        AppLanguage.Uzbek -> "Restoran qo'shildi"
    }
    fun activeCount(count: Int): String = when (locale) {
        AppLanguage.English -> "$count active"
        AppLanguage.Russian -> "$count активных"
        AppLanguage.Uzbek -> "$count faol"
    }
    val addDiscount: String get() = when (locale) {
        AppLanguage.English -> "Add discount"
        AppLanguage.Russian -> "Добавить скидку"
        AppLanguage.Uzbek -> "Chegirma qo'shish"
    }
    val couponManager: String get() = when (locale) {
        AppLanguage.English -> "Coupon manager"
        AppLanguage.Russian -> "Купоны"
        AppLanguage.Uzbek -> "Kuponlar"
    }
    val couponCode: String get() = when (locale) {
        AppLanguage.English -> "Coupon code"
        AppLanguage.Russian -> "Код купона"
        AppLanguage.Uzbek -> "Kupon kodi"
    }
    val title: String get() = when (locale) {
        AppLanguage.English -> "Title"
        AppLanguage.Russian -> "Название"
        AppLanguage.Uzbek -> "Sarlavha"
    }
    val percent: String get() = when (locale) {
        AppLanguage.English -> "Percent"
        AppLanguage.Russian -> "Процент"
        AppLanguage.Uzbek -> "Foiz"
    }
    val activeDays: String get() = when (locale) {
        AppLanguage.English -> "Active days"
        AppLanguage.Russian -> "Дней активности"
        AppLanguage.Uzbek -> "Faol kunlar"
    }
    val saveDiscount: String get() = when (locale) {
        AppLanguage.English -> "Save discount"
        AppLanguage.Russian -> "Сохранить скидку"
        AppLanguage.Uzbek -> "Chegirmani saqlash"
    }
    val discountSaved: String get() = when (locale) {
        AppLanguage.English -> "Discount saved"
        AppLanguage.Russian -> "Скидка сохранена"
        AppLanguage.Uzbek -> "Chegirma saqlandi"
    }
    val activeDiscounts: String get() = when (locale) {
        AppLanguage.English -> "Active discounts"
        AppLanguage.Russian -> "Активные скидки"
        AppLanguage.Uzbek -> "Faol chegirmalar"
    }
    val noDiscountsYet: String get() = when (locale) {
        AppLanguage.English -> "No discounts yet"
        AppLanguage.Russian -> "Пока что нет"
        AppLanguage.Uzbek -> "Hozircha yo'q"
    }
    val discountsWillAppear: String get() = when (locale) {
        AppLanguage.English -> "Saved admin discounts will appear here and stay visible for customers."
        AppLanguage.Russian -> "Сохраненные скидки админа появятся здесь и будут видны пользователям."
        AppLanguage.Uzbek -> "Admin saqlagan chegirmalar shu yerda chiqadi va mijozlarga ko'rinadi."
    }
    fun liveCount(count: Int): String = when (locale) {
        AppLanguage.English -> "$count live"
        AppLanguage.Russian -> "$count активных"
        AppLanguage.Uzbek -> "$count faol"
    }
    val remove: String get() = when (locale) {
        AppLanguage.English -> "Remove"
        AppLanguage.Russian -> "Удалить"
        AppLanguage.Uzbek -> "O'chirish"
    }
    val restaurant: String get() = when (locale) {
        AppLanguage.English -> "Restaurant"
        AppLanguage.Russian -> "Ресторан"
        AppLanguage.Uzbek -> "Restoran"
    }
    val uploadPhoto: String get() = when (locale) {
        AppLanguage.English -> "Upload photo from device"
        AppLanguage.Russian -> "Загрузить фото с устройства"
        AppLanguage.Uzbek -> "Rasmni qurilmadan yuklash"
    }
    val photo: String get() = when (locale) {
        AppLanguage.English -> "Photo"
        AppLanguage.Russian -> "Фото"
        AppLanguage.Uzbek -> "Rasm"
    }
    val chooseFromDevice: String get() = when (locale) {
        AppLanguage.English -> "Choose file"
        AppLanguage.Russian -> "Выбрать файл"
        AppLanguage.Uzbek -> "Fayl tanlash"
    }
    val photoReady: String get() = when (locale) {
        AppLanguage.English -> "Ready"
        AppLanguage.Russian -> "Готово"
        AppLanguage.Uzbek -> "Tayyor"
    }
    fun photosSelected(count: Int): String = when (locale) {
        AppLanguage.English -> "$count photos selected"
        AppLanguage.Russian -> "Выбрано фото: $count"
        AppLanguage.Uzbek -> "$count ta rasm tanlandi"
    }
    val video: String get() = when (locale) {
        AppLanguage.English -> "Video"
        AppLanguage.Russian -> "Видео"
        AppLanguage.Uzbek -> "Video"
    }
    val addLater: String get() = when (locale) {
        AppLanguage.English -> "Add later"
        AppLanguage.Russian -> "Позже"
        AppLanguage.Uzbek -> "Keyinroq"
    }
    val sessionClosed: String get() = when (locale) {
        AppLanguage.English -> "Admin session closed"
        AppLanguage.Russian -> "Сессия админа закрыта"
        AppLanguage.Uzbek -> "Admin sessiyasi yopildi"
    }
    val backToLogin: String get() = when (locale) {
        AppLanguage.English -> "Back to login"
        AppLanguage.Russian -> "Вернуться ко входу"
        AppLanguage.Uzbek -> "Kirishga qaytish"
    }

    fun sectionTitle(section: AdminPanelSection): String = when (section) {
        AdminPanelSection.Statistics -> when (locale) {
            AppLanguage.English -> "Statistics"
            AppLanguage.Russian -> "Статистика"
            AppLanguage.Uzbek -> "Statistika"
        }
        AdminPanelSection.Add -> when (locale) {
            AppLanguage.English -> "Add"
            AppLanguage.Russian -> "Добавить"
            AppLanguage.Uzbek -> "Qo'shish"
        }
        AdminPanelSection.Users -> userData
        AdminPanelSection.Settings -> settings
    }

    fun daysLeft(days: Int): String = when (locale) {
        AppLanguage.English -> if (days == 1) "1 day left" else "$days days left"
        AppLanguage.Russian -> if (days == 1) "Остался 1 день" else "Осталось $days дн."
        AppLanguage.Uzbek -> "$days kun qoldi"
    }
}

private fun AppLanguage.adminDisplayName(): String = when (this) {
    AppLanguage.English -> "English"
    AppLanguage.Russian -> "Русский"
    AppLanguage.Uzbek -> "O'zbekcha"
}


