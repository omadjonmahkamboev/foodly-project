package com.example.fooddeliveryapp.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddeliveryapp.ui.components.FoodlyImage
import com.example.fooddeliveryapp.BuildConfig
import com.example.fooddeliveryapp.backend.UserSession
import com.example.fooddeliveryapp.ui.AppLanguage
import com.example.fooddeliveryapp.ui.AppThemeMode
import com.example.fooddeliveryapp.ui.displayLabel
import com.example.fooddeliveryapp.ui.displayTitle
import com.example.fooddeliveryapp.ui.formatUzbekPhoneInput
import com.example.fooddeliveryapp.ui.hasUzbekPhoneDigits
import com.example.fooddeliveryapp.ui.isOfficeAddressLabel
import com.example.fooddeliveryapp.ui.isCompleteUzbekPhone
import com.example.fooddeliveryapp.ui.LocalAppStrings
import com.example.fooddeliveryapp.ui.UzbekistanPhonePrefix
import com.example.fooddeliveryapp.ui.toStoredUzbekPhone
import com.example.fooddeliveryapp.ui.UserProfileDetails
import com.example.fooddeliveryapp.ui.UzbekistanPhoneMask
import com.example.fooddeliveryapp.ui.uzbekPhoneDigitsKey
import com.example.fooddeliveryapp.ui.components.TopBar
import com.example.fooddeliveryapp.ui.components.asPrice
import com.example.fooddeliveryapp.ui.data.AppNotification
import com.example.fooddeliveryapp.ui.data.DeliveryAddress
import com.example.fooddeliveryapp.ui.data.DiscountCoupon
import com.example.fooddeliveryapp.ui.data.MenuItem
import com.example.fooddeliveryapp.ui.data.Restaurant
import com.example.fooddeliveryapp.ui.data.usesTransparentCutoutArt
import com.example.fooddeliveryapp.ui.theme.CardWhite
import com.example.fooddeliveryapp.ui.theme.Orange
import com.example.fooddeliveryapp.ui.theme.OrangeDeep
import com.example.fooddeliveryapp.ui.theme.Rose
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.random.Random

@Composable
fun ProfileScreen(
    user: UserSession?,
    profile: UserProfileDetails,
    address: DeliveryAddress,
    paymentCardsCount: Int,
    couponCount: Int,
    favoriteCount: Int,
    notificationCount: Int,
    language: AppLanguage,
    onBack: () -> Unit = {},
    onOpenPersonalInfo: () -> Unit,
    onEditAddress: () -> Unit,
    onEditProfile: () -> Unit,
    onOpenCart: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenCoupons: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenPayment: () -> Unit,
    onOpenFaqs: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogOut: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val colors = profileColors()
    var menuExpanded by remember { mutableStateOf(false) }
    var showLogOutDialog by rememberSaveable { mutableStateOf(false) }
    val displayName = user?.name ?: profile.fullName
    val primaryRows = listOf(
        ProfileMenuEntry(
            icon = Icons.Outlined.PersonOutline,
            title = strings.personalInfo,
            accent = ProfileMockOrange,
            onClick = onOpenPersonalInfo,
        ),
        ProfileMenuEntry(
            icon = Icons.Outlined.Map,
            title = strings.addresses,
            accent = ProfileMockBlue,
            onClick = onEditAddress,
        ),
    )
    val shoppingRows = listOf(
        ProfileMenuEntry(Icons.Outlined.ShoppingBag, strings.cart, ProfileMockSky, onOpenCart),
        ProfileMenuEntry(Icons.Outlined.FavoriteBorder, strings.favorites(favoriteCount), ProfileMockPurple, onOpenFavorites),
        ProfileMenuEntry(Icons.Outlined.LocalOffer, strings.coupons(couponCount), ProfileMockGold, onOpenCoupons),
        ProfileMenuEntry(
            Icons.Outlined.NotificationsNone,
            strings.notifications,
            ProfileMockGold,
            onOpenNotifications,
            showIndicator = notificationCount > 0,
        ),
        ProfileMenuEntry(Icons.Outlined.CreditCard, strings.paymentMethod(paymentCardsCount), ProfileMockSky, onOpenPayment),
    )
    val supportRows = listOf(
        ProfileMenuEntry(Icons.AutoMirrored.Outlined.HelpOutline, strings.faqs, ProfileMockCoral, onOpenFaqs),
        ProfileMenuEntry(
            icon = Icons.Outlined.Settings,
            title = strings.settings,
            accent = ProfileMockIndigo,
            onClick = onOpenSettings,
        ),
    )
    val logoutRows = listOf(
        ProfileMenuEntry(
            icon = Icons.AutoMirrored.Outlined.Logout,
            title = strings.logOut,
            accent = ProfileMockRose,
            onClick = {
            showLogOutDialog = true
            },
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, top = 12.dp, end = 19.dp, bottom = 25.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.soft),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = colors.text,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = strings.profile,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 17.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = colors.text,
            )
            Spacer(modifier = Modifier.weight(1f))
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.soft),
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = null,
                        tint = colors.text,
                        modifier = Modifier.size(20.dp),
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(strings.editProfile) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onEditProfile()
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(19.dp))
        ProfileIdentityHeader(
            name = displayName,
            bio = profile.bio.ifBlank { strings.defaultBio },
            photoUri = profile.photoUri,
        )
        Spacer(modifier = Modifier.height(30.dp))
        ProfileMenuGroup(rows = primaryRows)
        Spacer(modifier = Modifier.height(17.dp))
        ProfileMenuGroup(rows = shoppingRows)
        Spacer(modifier = Modifier.height(18.dp))
        ProfileMenuGroup(rows = supportRows)
        Spacer(modifier = Modifier.height(32.dp))
        ProfileMenuGroup(rows = logoutRows, rowHeight = 49.dp, verticalPadding = 0.dp)
    }

    if (showLogOutDialog) {
        AlertDialog(
            onDismissRequest = { showLogOutDialog = false },
            title = { Text(strings.logOut) },
            text = {
                Text(
                    text = "Do you want to leave this account? Your local session will be cleared.",
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
                    Text(strings.logOut)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogOutDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
        )
    }
}

private data class ProfileMenuEntry(
    val icon: ImageVector,
    val title: String,
    val accent: Color,
    val onClick: () -> Unit = {},
    val showIndicator: Boolean = false,
)

@Composable
private fun ProfileIdentityHeader(
    name: String,
    bio: String,
    photoUri: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProfileAvatar(
            photoUri = photoUri,
            modifier = Modifier
                .size(78.dp)
                .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        Spacer(modifier = Modifier.width(28.dp))
        Column(
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = bio,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Normal,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ProfileAvatar(
    photoUri: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (photoUri.isBlank()) {
            Icon(
                imageVector = Icons.Outlined.PersonOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxSize(0.62f),
            )
        } else {
            FoodlyImage(
                model = photoUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun ProfileMenuGroup(
    rows: List<ProfileMenuEntry>,
    rowHeight: androidx.compose.ui.unit.Dp = 50.dp,
    verticalPadding: androidx.compose.ui.unit.Dp = 9.dp,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = verticalPadding),
    ) {
        rows.forEach { row ->
            ProfileMenuRow(row = row, rowHeight = rowHeight)
        }
    }
}

@Composable
private fun ProfileMenuRow(
    row: ProfileMenuEntry,
    rowHeight: androidx.compose.ui.unit.Dp,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight)
            .clickable(onClick = row.onClick)
            .padding(start = 15.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = row.icon,
                contentDescription = null,
                tint = row.accent,
                modifier = Modifier.size(15.dp),
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = row.title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 15.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (row.showIndicator) {
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Orange),
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(17.dp),
        )
    }
}

@Composable
fun CouponsScreen(
    coupons: List<DiscountCoupon>,
    onBack: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    var copiedCode by rememberSaveable { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { TopBar(title = strings.couponsTitle, onBack = onBack) }
        item {
            Text(
                text = strings.activeDiscounts,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (coupons.isEmpty()) {
            item { EmptyCouponsCatalog() }
        } else {
            items(coupons, key = { it.code }) { coupon ->
                CouponCatalogCard(
                    coupon = coupon,
                    copied = copiedCode == coupon.code,
                    onCopy = {
                        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboardManager.setPrimaryClip(ClipData.newPlainText("Coupon code", coupon.code))
                        copiedCode = coupon.code
                    },
                )
            }
        }
    }
}

@Composable
fun NotificationsScreen(
    notifications: List<AppNotification>,
    onBack: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { TopBar(title = LocalAppStrings.current.notifications, onBack = onBack) }
        if (notifications.isEmpty()) {
            item {
                ProfileEmptyState(
                    icon = Icons.Outlined.NotificationsNone,
                    title = "No notifications yet",
                    description = "Order updates and app version notes will appear here.",
                )
            }
        } else {
            items(notifications, key = { it.id }) { notification ->
                NotificationCard(notification = notification)
            }
        }
    }
}

@Composable
private fun NotificationCard(notification: AppNotification) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Orange.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.NotificationsNone, contentDescription = null, tint = Orange)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(notification.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(notification.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(notification.createdAtMillis.notificationDateLabel(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ProfileEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(icon, contentDescription = null, tint = Orange, modifier = Modifier.size(38.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

private fun Long.notificationDateLabel(): String =
    Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))

@Composable
private fun EmptyCouponsCatalog() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(ProfileMockGold.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.LocalOffer, contentDescription = null, tint = ProfileMockGold)
            }
            Text(
                text = LocalAppStrings.current.noCouponsTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = LocalAppStrings.current.noCouponsDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun CouponCatalogCard(
    coupon: DiscountCoupon,
    copied: Boolean,
    onCopy: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = coupon.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = coupon.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Orange.copy(alpha = 0.14f))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                ) {
                    Text(
                        text = "-${coupon.discountPercent}%",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = OrangeDeep,
                    )
                }
            }
            SelectionContainer {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(onClick = onCopy)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = coupon.code,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Text(
                text = LocalAppStrings.current.couponValidUntil(coupon.expiresAtLabel()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onCopy,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = CardWhite),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (copied) LocalAppStrings.current.copiedCode else LocalAppStrings.current.copyCode)
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
fun FavoriteScreen(
    favorites: List<MenuItem>,
    restaurantForMenuItem: (MenuItem) -> Restaurant?,
    onBack: () -> Unit,
    onOpenFood: (MenuItem) -> Unit,
    onAddToCart: (MenuItem) -> Unit,
    onRemoveFavorite: (MenuItem) -> Unit,
) {
    val strings = LocalAppStrings.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, top = 12.dp, end = 19.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ProfileMockTopBar(title = strings.favoriteTitle, onBack = onBack)
        if (favorites.isEmpty()) {
            ProfileEmptyPanel(
                title = strings.noFavoriteTitle,
                description = strings.noFavoriteDescription,
            )
        } else {
            Text(
                text = strings.savedItems(favorites.size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            favorites.forEach { item ->
                FavoriteFoodCard(
                    item = item,
                    restaurant = restaurantForMenuItem(item),
                    onOpenFood = { onOpenFood(item) },
                    onAddToCart = { onAddToCart(item) },
                    onRemoveFavorite = { onRemoveFavorite(item) },
                )
            }
        }
    }
}

@Composable
private fun FavoriteFoodCard(
    item: MenuItem,
    restaurant: Restaurant?,
    onOpenFood: () -> Unit,
    onAddToCart: () -> Unit,
    onRemoveFavorite: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val transparentArt = item.usesTransparentCutoutArt()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onOpenFood)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FoodlyImage(
            model = item.imageUrl.ifBlank { restaurant?.imageUrl.orEmpty() },
            contentDescription = item.title,
            contentScale = if (transparentArt) ContentScale.Fit else ContentScale.Crop,
            modifier = Modifier
                .size(74.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (transparentArt) {
                        Color.Transparent
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                ),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(item.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(restaurant?.name.orEmpty(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.price.asPrice(), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Button(
                onClick = onAddToCart,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ProfileMockButtonOrange),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
            ) {
                Text(strings.orderAgain, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = CardWhite)
            }
        }
        IconButton(onClick = onRemoveFavorite) {
            Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, tint = Rose)
        }
    }
}

@Composable
fun FaqScreen(onBack: () -> Unit) {
    val strings = LocalAppStrings.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, top = 12.dp, end = 19.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ProfileMockTopBar(title = strings.faqs, onBack = onBack)
        strings.faqItems.forEach { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(item.first, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                Text(item.second, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ProfileEmptyPanel(
    title: String,
    description: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, tint = ProfileMockButtonOrange, modifier = Modifier.size(34.dp))
        Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
        Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
fun PersonalInfoScreen(
    user: UserSession?,
    profile: UserProfileDetails,
    onBack: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val displayName = user?.name ?: profile.fullName
    val displayEmail = user?.email ?: profile.email.ifBlank { "hello@foodly.app" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 20.dp, top = 12.dp, end = 19.dp, bottom = 24.dp),
    ) {
        ProfileMockTopBar(title = strings.personalInfo, onBack = onBack)
        Spacer(modifier = Modifier.height(28.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProfileAvatar(
                photoUri = profile.photoUri,
                modifier = Modifier
                    .size(94.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 17.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = profile.bio.ifBlank { strings.defaultBio },
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(28.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 18.dp, vertical = 8.dp),
        ) {
            ReadOnlyInfoRow(label = strings.fullName, value = displayName)
            ReadOnlyInfoRow(label = "EMAIL", value = displayEmail)
            ReadOnlyInfoRow(label = strings.phone, value = profile.phone.ifBlank { "Not added yet" })
            ReadOnlyInfoRow(label = strings.bio, value = profile.bio.ifBlank { strings.defaultBio })
        }
    }
}

@Composable
private fun ReadOnlyInfoRow(
    label: String,
    value: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 11.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 10.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun AddressListScreen(
    addresses: List<DeliveryAddress>,
    selectedAddress: DeliveryAddress,
    onBack: () -> Unit,
    onSelectAddress: (Int) -> Unit,
    onAddAddress: () -> Unit,
    onEditAddress: (Int) -> Unit,
    onDeleteAddress: (Int) -> Unit,
) {
    val strings = LocalAppStrings.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 23.dp, top = 15.dp, end = 20.dp, bottom = 20.dp),
    ) {
        ProfileMockTopBar(title = strings.myAddress, onBack = onBack)
        Spacer(modifier = Modifier.height(20.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(17.dp),
        ) {
            addresses.forEachIndexed { index, address ->
                AddressListCard(
                    address = address,
                    selected = address == selectedAddress,
                    onSelect = { onSelectAddress(index) },
                    onEdit = { onEditAddress(index) },
                    onDelete = { onDeleteAddress(index) },
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onAddAddress,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(7.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ProfileMockButtonOrange,
                contentColor = Color.White,
            ),
        ) {
            Text(
                text = strings.addAddress.uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                ),
            )
        }
    }
}

@Composable
private fun AddressListCard(
    address: DeliveryAddress,
    selected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val label = address.displayLabel(strings).uppercase()
    val isWork = label.contains("WORK") || label.contains("OFFICE") || label.contains("ОФ")
    val officeLabel = isOfficeAddressLabel(address.label)
    val accent = if (officeLabel) ProfileMockPurple else ProfileMockSky
    val icon = if (officeLabel) Icons.Outlined.BusinessCenter else Icons.Outlined.Home

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) {
                    Orange.copy(alpha = 0.12f)
                } else {
                    MaterialTheme.colorScheme.surface
                },
            )
            .clickable(onClick = onSelect)
            .padding(start = 15.dp, top = 14.dp, end = 10.dp, bottom = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(43.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.width(13.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = address.displayTitle(strings),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Normal,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = address.subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Normal,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(
            modifier = Modifier.align(Alignment.Top),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = ProfileMockButtonOrange,
                    modifier = Modifier.size(17.dp),
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = null,
                    tint = ProfileMockButtonOrange,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun ProfileMockTopBar(
    title: String,
    onBack: () -> Unit,
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
        Spacer(modifier = Modifier.width(13.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 15.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun EditProfileScreen(
    profile: UserProfileDetails,
    language: AppLanguage,
    onBack: () -> Unit,
    onSave: (UserProfileDetails) -> Unit,
) {
    val strings = LocalAppStrings.current
    val colors = profileColors()
    var fullName by rememberSaveable(profile.fullName) { mutableStateOf(profile.fullName) }
    var email by rememberSaveable(profile.email) { mutableStateOf(profile.email) }
    val originalPhoneKey = remember(profile.phone) { profile.phone.uzbekPhoneDigitsKey() }
    var phoneField by rememberSaveable(profile.phone, stateSaver = TextFieldValue.Saver) {
        val formattedPhone = profile.phone.takeIf { it.isNotBlank() }?.formatUzbekPhoneInput().orEmpty()
        mutableStateOf(TextFieldValue(formattedPhone, TextRange(formattedPhone.length)))
    }
    var bio by rememberSaveable(profile.bio) { mutableStateOf(profile.bio) }
    var photoUri by rememberSaveable(profile.photoUri) {
        mutableStateOf(profile.photoUri)
    }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var phoneVerified by rememberSaveable(profile.phone, profile.isPhoneVerified) {
        mutableStateOf(profile.isPhoneVerified && originalPhoneKey.isNotBlank())
    }
    var showPhoneVerificationDialog by rememberSaveable { mutableStateOf(false) }
    var verificationCode by rememberSaveable { mutableStateOf("") }
    var pendingPhoneCode by rememberSaveable { mutableStateOf<String?>(null) }
    var verificationError by rememberSaveable { mutableStateOf<String?>(null) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            photoUri = uri.toString()
        }
    }

    fun buildNextProfile(verified: Boolean): UserProfileDetails {
        val nameParts = fullName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        val phone = phoneField.text
        val storedPhone = if (phone.hasUzbekPhoneDigits()) phone.toStoredUzbekPhone() else ""
        return UserProfileDetails(
            firstName = nameParts.firstOrNull().orEmpty(),
            lastName = nameParts.drop(1).joinToString(" "),
            phone = storedPhone,
            isPhoneVerified = verified && storedPhone.isNotBlank(),
            email = email.trim(),
            bio = bio.trim(),
            photoUri = photoUri,
        )
    }

    fun validateProfile(next: UserProfileDetails): String? = when {
        fullName.trim().length < 2 -> language.fullNameError()
        next.email.contains("@").not() -> language.emailError()
        phoneField.text.hasUzbekPhoneDigits() && !phoneField.text.isCompleteUzbekPhone() -> language.phoneError()
        else -> null
    }

    fun saveProfileIfReady() {
        val next = buildNextProfile(verified = phoneVerified)
        error = validateProfile(next)
        if (error != null) return

        val phoneChanged = next.phone != profile.phone
        if (next.phone.isNotBlank() && phoneChanged && !phoneVerified) {
            pendingPhoneCode = "%06d".format(Random.nextInt(1_000_000))
            verificationCode = ""
            verificationError = null
            showPhoneVerificationDialog = true
            return
        }

        onSave(next)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        EditProfileTopBar(title = strings.editProfile, onBack = onBack, colors = colors)
        Spacer(modifier = Modifier.height(2.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            EditProfileAvatar(
                photoUri = photoUri,
                colors = colors,
                onChangePhoto = { imagePicker.launch("image/*") },
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        EditProfileField(
            value = fullName,
            onValueChange = { fullName = it },
            label = strings.fullName,
            placeholder = "Vishal Khadok",
            colors = colors,
        )
        EditProfileField(
            value = email,
            onValueChange = { email = it },
            label = "EMAIL",
            placeholder = "hello@foodly.app",
            colors = colors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = strings.phone,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                ),
                color = colors.muted,
            )
            OutlinedTextField(
                value = phoneField,
                onValueChange = { next ->
                    val formatted = next.text.takeIf { it.hasUzbekPhoneDigits() }?.formatUzbekPhoneInput().orEmpty()
                    phoneField = TextFieldValue(formatted, TextRange(formatted.length))
                    error = null
                    verificationError = null
                    phoneVerified = profile.isPhoneVerified && phoneField.text.uzbekPhoneDigitsKey() == originalPhoneKey
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                prefix = {
                    Text(
                        text = UzbekistanPhonePrefix,
                        color = colors.text,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                placeholder = {
                    Text(
                        text = UzbekistanPhoneMask,
                        color = colors.muted,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = colors.soft,
                    focusedContainerColor = colors.soft,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedTextColor = colors.text,
                    focusedTextColor = colors.text,
                    cursorColor = Orange,
                ),
            )
        }
        if (phoneField.text.isNotBlank()) {
            Text(
                text = if (phoneVerified) language.phoneVerifiedLabel() else language.phoneVerificationHint(),
                style = MaterialTheme.typography.bodySmall,
                color = if (phoneVerified) Orange else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        EditProfileField(
            value = bio,
            onValueChange = { bio = it },
            label = strings.bio,
            placeholder = strings.defaultBio,
            colors = colors,
            singleLine = false,
            minHeight = 100,
            minLines = 4,
        )
        error?.let {
            Text(text = it, style = MaterialTheme.typography.bodyMedium, color = Rose)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = ::saveProfileIfReady,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = CardWhite),
        ) {
            Text(
                text = strings.save,
                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.sp),
            )
        }
    }

    if (showPhoneVerificationDialog) {
        AlertDialog(
            onDismissRequest = {
                showPhoneVerificationDialog = false
                verificationError = null
            },
            title = {
                Text(
                    text = language.phoneVerificationTitle(),
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = language.phoneVerificationSubtitle(
                            phoneField.text.toStoredUzbekPhone().ifBlank { phoneField.text },
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = verificationCode,
                        onValueChange = {
                            verificationCode = it.filter(Char::isDigit).take(6)
                            verificationError = null
                        },
                        label = { Text(language.phoneVerificationCodeLabel()) },
                        placeholder = { Text("000000") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(),
                    )
                    if (BuildConfig.DEBUG) {
                        Text(
                            text = language.phoneVerificationDebugCode(pendingPhoneCode.orEmpty()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    verificationError?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = Rose,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val expectedCode = pendingPhoneCode.orEmpty()
                        if (verificationCode != expectedCode) {
                            verificationError = language.phoneVerificationCodeError()
                            return@TextButton
                        }
                        phoneVerified = true
                        showPhoneVerificationDialog = false
                        verificationError = null
                        pendingPhoneCode = null
                        onSave(buildNextProfile(verified = true))
                    },
                ) {
                    Text(language.phoneVerificationConfirm())
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPhoneVerificationDialog = false
                        verificationError = null
                    },
                ) {
                    Text(language.phoneVerificationCancel())
                }
            },
        )
    }
}

@Composable
fun SettingsScreen(
    themeMode: AppThemeMode,
    language: AppLanguage,
    onBack: () -> Unit,
    onThemeChange: (AppThemeMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
) {
    val colors = profileColors()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { ProfileTopBar(title = language.settingsTitle(), onBack = onBack, colors = colors) }
        item {
            SettingsSection(
                title = language.themeTitle(),
                subtitle = language.themeDescription(),
                colors = colors,
            ) {
                SettingsChoiceRow(
                    icon = Icons.Outlined.LightMode,
                    title = language.lightThemeLabel(),
                    subtitle = language.lightThemeSubtitle(),
                    selected = themeMode == AppThemeMode.Light,
                    colors = colors,
                    onClick = { onThemeChange(AppThemeMode.Light) },
                )
                SettingsChoiceRow(
                    icon = Icons.Outlined.DarkMode,
                    title = language.darkThemeLabel(),
                    subtitle = language.darkThemeSubtitle(),
                    selected = themeMode == AppThemeMode.Dark,
                    colors = colors,
                    onClick = { onThemeChange(AppThemeMode.Dark) },
                )
            }
        }
        item {
            SettingsSection(
                title = language.languageTitle(),
                subtitle = language.languageDescription(),
                colors = colors,
            ) {
                listOf(AppLanguage.English, AppLanguage.Russian, AppLanguage.Uzbek).forEach { option ->
                    SettingsChoiceRow(
                        icon = Icons.Outlined.Language,
                        title = option.displayName(),
                        subtitle = option.languageHint(),
                        selected = language == option,
                        colors = colors,
                        onClick = { onLanguageChange(option) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EditProfileTopBar(
    title: String,
    onBack: () -> Unit,
    colors: ProfileColors,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.soft),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.text)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
            color = colors.text,
        )
    }
}

@Composable
private fun EditProfileAvatar(
    photoUri: String,
    colors: ProfileColors,
    onChangePhoto: () -> Unit,
) {
    Box(modifier = Modifier.size(124.dp)) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .align(Alignment.Center)
                .clip(CircleShape)
                .background(colors.soft),
            contentAlignment = Alignment.Center,
        ) {
            ProfileAvatar(
                photoUri = photoUri,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(Orange)
                .clickable(onClick = onChangePhoto),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Change profile photo",
                tint = CardWhite,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun EditProfileField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    colors: ProfileColors,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    minHeight: Int = 54,
    minLines: Int = 1,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
            ),
            color = colors.muted,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = singleLine,
            minLines = minLines,
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = keyboardOptions,
            placeholder = {
                Text(text = placeholder, color = colors.muted, style = MaterialTheme.typography.bodyLarge)
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = colors.soft,
                focusedContainerColor = colors.soft,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedTextColor = colors.text,
                focusedTextColor = colors.text,
                cursorColor = Orange,
            ),
        )
    }
}

@Composable
private fun ProfileHeroCard(
    user: UserSession?,
    profile: UserProfileDetails,
) {
    val strings = LocalAppStrings.current
    val displayName = user?.name ?: profile.fullName
    val displayEmail = user?.email ?: profile.email.ifBlank { "hello@foodly.app" }

    Card(shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = Orange)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Orange, OrangeDeep)))
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(CardWhite.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    ProfileAvatar(
                        photoUri = profile.photoUri,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleLarge,
                        color = CardWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = displayEmail,
                        style = MaterialTheme.typography.bodyLarge,
                        color = CardWhite.copy(alpha = 0.78f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = profile.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = CardWhite.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (profile.bio.isNotBlank()) {
                Text(
                    text = profile.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CardWhite.copy(alpha = 0.82f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(strings.availableBonus, style = MaterialTheme.typography.bodyMedium, color = CardWhite.copy(alpha = 0.76f))
            Text("\$500.00", style = MaterialTheme.typography.displayLarge, color = CardWhite)
        }
    }
}

@Composable
private fun AddressSummaryCard(
    title: String,
    address: DeliveryAddress,
    actionLabel: String,
    onAction: () -> Unit,
    colors: ProfileColors,
) {
    val strings = LocalAppStrings.current

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = colors.text)
                TextButton(onClick = onAction) { Text(actionLabel, color = Orange) }
            }
            Text(address.displayLabel(strings), style = MaterialTheme.typography.bodyLarge, color = colors.text)
            Text(address.displayTitle(strings), style = MaterialTheme.typography.bodyMedium, color = colors.muted)
            Text(address.subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.muted)
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    colors: ProfileColors,
    accent: Color = colors.text,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = accent)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = colors.text)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.muted)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = colors.muted,
                modifier = Modifier.rotate(180f),
            )
        }
    }
}

@Composable
private fun ProfileTopBar(
    title: String,
    onBack: () -> Unit,
    colors: ProfileColors,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(colors.surface),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.text)
        }
        Text(text = title, style = MaterialTheme.typography.headlineMedium, color = colors.text)
    }
}

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    colors: ProfileColors,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = colors.muted)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = keyboardOptions,
            placeholder = {
                Text(text = placeholder, color = colors.muted, style = MaterialTheme.typography.bodyLarge)
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = colors.soft,
                focusedContainerColor = colors.soft,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Orange.copy(alpha = 0.48f),
                unfocusedTextColor = colors.text,
                focusedTextColor = colors.text,
                cursorColor = Orange,
            ),
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    subtitle: String,
    colors: ProfileColors,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = colors.text)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.muted)
            Spacer(modifier = Modifier.height(2.dp))
            content()
        }
    }
}

@Composable
private fun SettingsChoiceRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    colors: ProfileColors,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Orange.copy(alpha = 0.12f) else colors.soft)
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = 72.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (selected) Orange.copy(alpha = 0.16f) else colors.surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = if (selected) Orange else colors.muted)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = colors.text)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.muted)
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Orange),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = CardWhite, modifier = Modifier.size(18.dp))
            }
        } else {
            Spacer(modifier = Modifier.width(28.dp))
        }
    }
}

@Composable
private fun profileColors(): ProfileColors = ProfileColors(
    background = MaterialTheme.colorScheme.background,
    surface = MaterialTheme.colorScheme.surface,
    soft = MaterialTheme.colorScheme.surfaceVariant,
    text = MaterialTheme.colorScheme.onSurface,
    muted = MaterialTheme.colorScheme.onSurfaceVariant,
)

private data class ProfileColors(
    val background: Color,
    val surface: Color,
    val soft: Color,
    val text: Color,
    val muted: Color,
)

private val ProfileMockBackground = Color(0xFFFEFEFF)
private val ProfileMockButton = Color(0xFFF3F4F6)
private val ProfileMockButtonOrange = Color(0xFFFF7622)
private val ProfileMockGroup = Color(0xFFF8F8FA)
private val ProfileMockText = Color(0xFF3F4050)
private val ProfileMockMuted = Color(0xFFA8A6B8)
private val ProfileMockChevron = Color(0xFFA9ADBA)
private val ProfileMockOrange = Color(0xFFFF744F)
private val ProfileMockBlue = Color(0xFF6F73FF)
private val ProfileMockSky = Color(0xFF62B5FF)
private val ProfileMockPurple = Color(0xFFC468FF)
private val ProfileMockGold = Color(0xFFFFB95F)
private val ProfileMockCoral = Color(0xFFFF7D62)
private val ProfileMockMint = Color(0xFF37D3C0)
private val ProfileMockIndigo = Color(0xFF7776FF)
private val ProfileMockRose = Color(0xFFFF6675)

private data class FaqItem(
    val question: String,
    val answer: String,
)

private val faqItems = listOf(
    FaqItem("How do I add food to the cart?", "Open a restaurant or category, tap + on a food card, then open Cart from Profile or the cart icon."),
    FaqItem("How do favourites work?", "Tap the heart on any food card. The item is saved in Favourite, where you can open it or order again."),
    FaqItem("Which payment methods are available?", "You can pay by cash, Visa, Mastercard, Uzcard, or HumoCard. Saved cards can be opened, checked, and deleted."),
    FaqItem("Can I cancel an order?", "Cancellation is available during the first 5 minutes while the restaurant is preparing the order."),
    FaqItem("What happens after cancellation?", "If an order is cancelled in the allowed time, the app adds the paid amount to the refund balance."),
    FaqItem("Can I change the delivery address?", "Yes. Open Addresses from Profile or edit the address before checkout."),
    FaqItem("Safety rule", "Never share card CVV or passwords with couriers. The app does not save CVV after card validation."),
)

private fun AppLanguage.profileTitle(): String = when (this) {
    AppLanguage.English -> "Profile"
    AppLanguage.Russian -> "Профиль"
    AppLanguage.Uzbek -> "Profil"
}

private fun AppLanguage.editProfileTitle(): String = when (this) {
    AppLanguage.English -> "Edit profile"
    AppLanguage.Russian -> "Изменить профиль"
    AppLanguage.Uzbek -> "Profilni o'zgartirish"
}

private fun AppLanguage.editProfileDescription(): String = when (this) {
    AppLanguage.English -> "Update your first name, last name, phone number and Gmail."
    AppLanguage.Russian -> "Измени имя, фамилию, номер телефона и Gmail пользователя."
    AppLanguage.Uzbek -> "Ism, familiya, telefon raqami va Gmail ma'lumotlarini o'zgartir."
}

private fun AppLanguage.myAddressTitle(): String = when (this) {
    AppLanguage.English -> "My address"
    AppLanguage.Russian -> "Мой адрес"
    AppLanguage.Uzbek -> "Mening manzilim"
}

private fun AppLanguage.refreshLabel(): String = when (this) {
    AppLanguage.English -> "Update"
    AppLanguage.Russian -> "Обновить"
    AppLanguage.Uzbek -> "Yangilash"
}

private fun AppLanguage.deliveryAddressesTitle(): String = when (this) {
    AppLanguage.English -> "Delivery addresses"
    AppLanguage.Russian -> "Адреса доставки"
    AppLanguage.Uzbek -> "Yetkazish manzillari"
}

private fun AppLanguage.deliveryAddressesSubtitle(): String = when (this) {
    AppLanguage.English -> "Manage points and labels"
    AppLanguage.Russian -> "Управляй точками и метками"
    AppLanguage.Uzbek -> "Nuqtalar va belgilarni boshqar"
}

private fun AppLanguage.paymentMethodsTitle(): String = when (this) {
    AppLanguage.English -> "Payment methods"
    AppLanguage.Russian -> "Способы оплаты"
    AppLanguage.Uzbek -> "To'lov usullari"
}

private fun AppLanguage.paymentMethodsSubtitle(count: Int): String = when (this) {
    AppLanguage.English -> if (count == 0) "Add a card on the payment screen" else "$count card(s) in account"
    AppLanguage.Russian -> if (count == 0) "Добавь карту на экране оплаты" else "$count карт(ы) в аккаунте"
    AppLanguage.Uzbek -> if (count == 0) "To'lov ekranida karta qo'sh" else "Akkauntda $count ta karta"
}

private fun AppLanguage.notificationsTitle(): String = when (this) {
    AppLanguage.English -> "Notifications"
    AppLanguage.Russian -> "Уведомления"
    AppLanguage.Uzbek -> "Bildirishnomalar"
}

private fun AppLanguage.notificationsSubtitle(): String = when (this) {
    AppLanguage.English -> "Orders, promos and delivery status"
    AppLanguage.Russian -> "Заказы, акции и статус доставки"
    AppLanguage.Uzbek -> "Buyurtmalar, aksiyalar va yetkazish holati"
}

private fun AppLanguage.supportTitle(): String = when (this) {
    AppLanguage.English -> "Support"
    AppLanguage.Russian -> "Поддержка"
    AppLanguage.Uzbek -> "Yordam"
}

private fun AppLanguage.supportSubtitle(): String = when (this) {
    AppLanguage.English -> "Fast contact with service"
    AppLanguage.Russian -> "Быстрая связь с сервисом"
    AppLanguage.Uzbek -> "Servis bilan tez aloqa"
}

private fun AppLanguage.settingsTitle(): String = when (this) {
    AppLanguage.English -> "Settings"
    AppLanguage.Russian -> "Настройки"
    AppLanguage.Uzbek -> "Sozlamalar"
}

private fun AppLanguage.settingsSubtitle(): String = when (this) {
    AppLanguage.English -> "Theme, language, privacy"
    AppLanguage.Russian -> "Тема, язык, конфиденциальность"
    AppLanguage.Uzbek -> "Mavzu, til, maxfiylik"
}

private fun AppLanguage.logoutTitle(): String = when (this) {
    AppLanguage.English -> "Log out"
    AppLanguage.Russian -> "Выйти"
    AppLanguage.Uzbek -> "Chiqish"
}

private fun AppLanguage.logoutSubtitle(): String = when (this) {
    AppLanguage.English -> "Switch account"
    AppLanguage.Russian -> "Сменить аккаунт"
    AppLanguage.Uzbek -> "Akkauntni almashtirish"
}

private fun AppLanguage.firstNameLabel(): String = when (this) {
    AppLanguage.English -> "First name"
    AppLanguage.Russian -> "Имя"
    AppLanguage.Uzbek -> "Ism"
}

private fun AppLanguage.lastNameLabel(): String = when (this) {
    AppLanguage.English -> "Last name"
    AppLanguage.Russian -> "Фамилия"
    AppLanguage.Uzbek -> "Familiya"
}

private fun AppLanguage.phoneLabel(): String = when (this) {
    AppLanguage.English -> "Phone number"
    AppLanguage.Russian -> "Номер телефона"
    AppLanguage.Uzbek -> "Telefon raqami"
}

private fun AppLanguage.gmailLabel(): String = when (this) {
    AppLanguage.English -> "User Gmail"
    AppLanguage.Russian -> "Gmail пользователя"
    AppLanguage.Uzbek -> "Foydalanuvchi Gmail"
}

private fun AppLanguage.saveProfileLabel(): String = when (this) {
    AppLanguage.English -> "Save profile"
    AppLanguage.Russian -> "Сохранить профиль"
    AppLanguage.Uzbek -> "Profilni saqlash"
}

private fun AppLanguage.firstNameError(): String = when (this) {
    AppLanguage.English -> "Enter at least 2 letters for the first name"
    AppLanguage.Russian -> "Укажи имя минимум из 2 букв"
    AppLanguage.Uzbek -> "Ism kamida 2 harfdan iborat bo'lsin"
}

private fun AppLanguage.fullNameError(): String = when (this) {
    AppLanguage.English -> "Enter at least 2 letters for the full name"
    AppLanguage.Russian -> "Укажи полное имя минимум из 2 букв"
    AppLanguage.Uzbek -> "To'liq ism kamida 2 harfdan iborat bo'lsin"
}

private fun AppLanguage.emailError(): String = when (this) {
    AppLanguage.English -> "Check Gmail address"
    AppLanguage.Russian -> "Проверь Gmail"
    AppLanguage.Uzbek -> "Gmail manzilini tekshir"
}

private fun AppLanguage.phoneError(): String = when (this) {
    AppLanguage.English -> "Check phone number"
    AppLanguage.Russian -> "Проверь номер телефона"
    AppLanguage.Uzbek -> "Telefon raqamini tekshir"
}

private fun AppLanguage.phoneVerifiedLabel(): String = when (this) {
    AppLanguage.English -> "Phone number is confirmed"
    AppLanguage.Russian -> "Номер подтвержден"
    AppLanguage.Uzbek -> "Telefon raqami tasdiqlangan"
}

private fun AppLanguage.phoneVerificationHint(): String = when (this) {
    AppLanguage.English -> "We'll send an SMS code when you save the new number."
    AppLanguage.Russian -> "После сохранения отправим SMS-код для подтверждения номера."
    AppLanguage.Uzbek -> "Yangi raqamni saqlaganda SMS-kod yuboramiz."
}

private fun AppLanguage.phoneVerificationTitle(): String = when (this) {
    AppLanguage.English -> "Confirm phone number"
    AppLanguage.Russian -> "Подтверждение номера"
    AppLanguage.Uzbek -> "Telefonni tasdiqlash"
}

private fun AppLanguage.phoneVerificationSubtitle(phone: String): String = when (this) {
    AppLanguage.English -> "Enter the SMS code sent to $phone."
    AppLanguage.Russian -> "Введи SMS-код, который отправили на $phone."
    AppLanguage.Uzbek -> "$phone raqamiga yuborilgan SMS-kodni kiriting."
}

private fun AppLanguage.phoneVerificationCodeLabel(): String = when (this) {
    AppLanguage.English -> "SMS code"
    AppLanguage.Russian -> "SMS-код"
    AppLanguage.Uzbek -> "SMS-kod"
}

private fun AppLanguage.phoneVerificationConfirm(): String = when (this) {
    AppLanguage.English -> "Confirm"
    AppLanguage.Russian -> "Подтвердить"
    AppLanguage.Uzbek -> "Tasdiqlash"
}

private fun AppLanguage.phoneVerificationCancel(): String = when (this) {
    AppLanguage.English -> "Later"
    AppLanguage.Russian -> "Позже"
    AppLanguage.Uzbek -> "Keyinroq"
}

private fun AppLanguage.phoneVerificationCodeError(): String = when (this) {
    AppLanguage.English -> "Check the SMS code"
    AppLanguage.Russian -> "Проверь SMS-код"
    AppLanguage.Uzbek -> "SMS-kodni tekshir"
}

private fun AppLanguage.phoneVerificationDebugCode(code: String): String = when (this) {
    AppLanguage.English -> "Debug code: $code"
    AppLanguage.Russian -> "Код для отладки: $code"
    AppLanguage.Uzbek -> "Debug kod: $code"
}

private fun AppLanguage.themeTitle(): String = when (this) {
    AppLanguage.English -> "Theme"
    AppLanguage.Russian -> "Тема"
    AppLanguage.Uzbek -> "Mavzu"
}

private fun AppLanguage.themeDescription(): String = when (this) {
    AppLanguage.English -> "Choose a white or black app style."
    AppLanguage.Russian -> "Выбери белый или черный стиль приложения."
    AppLanguage.Uzbek -> "Ilovaning oq yoki qora uslubini tanla."
}

private fun AppLanguage.lightThemeLabel(): String = when (this) {
    AppLanguage.English -> "White theme"
    AppLanguage.Russian -> "Белая тема"
    AppLanguage.Uzbek -> "Oq mavzu"
}

private fun AppLanguage.lightThemeSubtitle(): String = when (this) {
    AppLanguage.English -> "Bright interface for daytime"
    AppLanguage.Russian -> "Светлый интерфейс для дня"
    AppLanguage.Uzbek -> "Kunduz uchun yorug' interfeys"
}

private fun AppLanguage.darkThemeLabel(): String = when (this) {
    AppLanguage.English -> "Black theme"
    AppLanguage.Russian -> "Черная тема"
    AppLanguage.Uzbek -> "Qora mavzu"
}

private fun AppLanguage.darkThemeSubtitle(): String = when (this) {
    AppLanguage.English -> "Dark interface for evening"
    AppLanguage.Russian -> "Темный интерфейс для вечера"
    AppLanguage.Uzbek -> "Kechqurun uchun qorong'i interfeys"
}

private fun AppLanguage.languageTitle(): String = when (this) {
    AppLanguage.English -> "Language"
    AppLanguage.Russian -> "Язык"
    AppLanguage.Uzbek -> "Til"
}

private fun AppLanguage.languageDescription(): String = when (this) {
    AppLanguage.English -> "Switch between English, Russian and Uzbek."
    AppLanguage.Russian -> "Выбери английский, русский или узбекский."
    AppLanguage.Uzbek -> "Ingliz, rus yoki o'zbek tilini tanla."
}

private fun AppLanguage.displayName(): String = when (this) {
    AppLanguage.English -> "English"
    AppLanguage.Russian -> "Русский"
    AppLanguage.Uzbek -> "O'zbekcha"
}

private fun AppLanguage.languageHint(): String = when (this) {
    AppLanguage.English -> "English interface labels"
    AppLanguage.Russian -> "Русские подписи интерфейса"
    AppLanguage.Uzbek -> "O'zbekcha interfeys matnlari"
}

