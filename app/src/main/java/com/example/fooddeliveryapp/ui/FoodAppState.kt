package com.example.fooddeliveryapp.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.fooddeliveryapp.BuildConfig
import com.example.fooddeliveryapp.backend.AddPaymentCardRequest
import com.example.fooddeliveryapp.backend.AuthRequest
import com.example.fooddeliveryapp.backend.BackendResult
import com.example.fooddeliveryapp.backend.CreateOrderRequest
import com.example.fooddeliveryapp.backend.DatabaseFoodBackend
import com.example.fooddeliveryapp.backend.DeliveryQuote
import com.example.fooddeliveryapp.backend.EmailVerificationChallenge
import com.example.fooddeliveryapp.backend.FoodBackend
import com.example.fooddeliveryapp.backend.GoogleAuthRequest
import com.example.fooddeliveryapp.backend.HttpOrderSyncBackend
import com.example.fooddeliveryapp.backend.NoopOrderSyncBackend
import com.example.fooddeliveryapp.backend.OrderSyncBackend
import com.example.fooddeliveryapp.backend.OrderSyncSnapshot
import com.example.fooddeliveryapp.backend.PasswordResetChallenge
import com.example.fooddeliveryapp.backend.UserSession
import com.example.fooddeliveryapp.ui.auth.GoogleAccountProfile
import com.example.fooddeliveryapp.ui.data.AppNotification
import com.example.fooddeliveryapp.ui.data.CartLine
import com.example.fooddeliveryapp.ui.data.ChatAuthor
import com.example.fooddeliveryapp.ui.data.CourierChatMessage
import com.example.fooddeliveryapp.ui.data.CourierDeliveryOrder
import com.example.fooddeliveryapp.ui.data.CourierOrderStatus
import com.example.fooddeliveryapp.ui.data.CourierProfileDetails
import com.example.fooddeliveryapp.ui.data.DeliveryAddress
import com.example.fooddeliveryapp.ui.data.DiscountCoupon
import com.example.fooddeliveryapp.ui.data.GeoPoint
import com.example.fooddeliveryapp.ui.data.MenuItem
import com.example.fooddeliveryapp.ui.data.OrderStatus
import com.example.fooddeliveryapp.ui.data.OrderReview
import com.example.fooddeliveryapp.ui.data.OrderSummary
import com.example.fooddeliveryapp.ui.data.PaymentCard
import com.example.fooddeliveryapp.ui.data.PaymentMethod
import com.example.fooddeliveryapp.ui.data.Restaurant
import com.example.fooddeliveryapp.ui.data.SampleData
import com.example.fooddeliveryapp.ui.data.cardBrandName
import com.example.fooddeliveryapp.ui.data.detectPaymentMethod
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

enum class AppThemeMode {
    Light,
    Dark,
}

enum class AppLanguage {
    English,
    Russian,
    Uzbek,
}

const val DEFAULT_PROFILE_PHOTO_URL = ""
const val WELCOME_COUPON_CODE = "FOODLY25"
const val WELCOME_COUPON_PERCENT = 25
private const val CustomerSessionKey = "customer_session"
private const val CustomerAddressesPrefix = "customer_addresses_"
private const val CustomerOrdersPrefix = "customer_orders_"
private const val CustomerFavoritesPrefix = "customer_favorites_"
private const val CustomerCouponNotificationsPrefix = "customer_coupon_notifications_"
private const val CustomerProfilePrefix = "customer_profile_"
private const val AdminRestaurantsKey = "admin_restaurants_foodly_categories_v2"
private const val AdminCouponsKey = "admin_coupons"
private const val AdminDishOrderCountsKey = "admin_dish_order_counts"
private const val CourierWorkspaceKey = "courier_workspace"
private const val CourierProfileKey = "courier_profile"
private const val CourierProfilePrefix = "courier_profile_"

data class PasswordResetCodeResult(
    val challenge: PasswordResetChallenge? = null,
    val error: String? = null,
)

data class EmailVerificationCodeResult(
    val challenge: EmailVerificationChallenge? = null,
    val error: String? = null,
)

data class UserProfileDetails(
    val firstName: String,
    val lastName: String,
    val phone: String,
    val isPhoneVerified: Boolean = false,
    val email: String,
    val bio: String = "I love fast food",
    val photoUri: String = DEFAULT_PROFILE_PHOTO_URL,
) {
    val fullName: String
        get() = listOf(firstName, lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { "Foodly guest" }

    companion object {
        fun fromSession(
            session: UserSession,
            phone: String = "",
            isPhoneVerified: Boolean = false,
            bio: String = "I love fast food",
            photoUri: String = DEFAULT_PROFILE_PHOTO_URL,
        ): UserProfileDetails {
            val parts = session.name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
            return UserProfileDetails(
                firstName = parts.firstOrNull().orEmpty(),
                lastName = parts.drop(1).joinToString(" "),
                phone = phone,
                isPhoneVerified = isPhoneVerified,
                email = session.email,
                bio = bio,
                photoUri = photoUri,
            )
        }
    }
}

@Stable
class FoodAppState(
    private val backend: FoodBackend,
    private val orderSyncBackend: OrderSyncBackend = NoopOrderSyncBackend,
    private val preferences: SharedPreferences? = null,
) {
    private val orderScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val orderCompletionJobs = mutableMapOf<String, Job>()
    private val orderReceiptRefundJobs = mutableMapOf<String, Job>()
    private val couponExpiryJobs = mutableMapOf<String, Job>()
    private var orderSyncJob: Job? = null
    private var activeCustomerId: String? = null

    val onboarding = SampleData.onboarding
    val categories = SampleData.categories
    val promos = SampleData.promos
    val restaurants = mutableStateListOf<Restaurant>().apply {
        addAll(SampleData.restaurants)
    }

    val cartItems = mutableStateListOf<CartLine>()
    val favoriteItemIds = mutableStateListOf<String>()
    val ongoingOrders = mutableStateListOf<OrderSummary>()
    val historyOrders = mutableStateListOf<OrderSummary>()
    val savedPaymentCards = mutableStateListOf<PaymentCard>()
    val coupons = mutableStateListOf<DiscountCoupon>()
    val notifications = mutableStateListOf(
        AppNotification(
            id = "version_1_0",
            title = "Version 1.0 is ready",
            message = "Foodly admin, courier chat and delivery tracking are available now.",
            read = true,
        ),
    )
    val orderReviews = mutableStateListOf<OrderReview>()
    val adminCoupons = mutableStateListOf<DiscountCoupon>()
    private val dishOrderCounts = mutableStateMapOf<String, Int>()
    private val courierReviewRatings = mutableStateMapOf<String, Int>()
    val courierOrders = mutableStateListOf<CourierDeliveryOrder>()
    val courierChatMessages = mutableStateListOf<CourierChatMessage>()
    val courierReviews = mutableStateListOf<OrderReview>()
    val unreadCourierChatOrderIds = mutableStateListOf<String>()
    val unreadCustomerChatOrderIds = mutableStateListOf<String>()
    val savedAddresses = mutableStateListOf<DeliveryAddress>().apply {
        addAll(defaultCustomerAddresses())
    }

    var currentUser by mutableStateOf<UserSession?>(null)
    var currentCourier by mutableStateOf<CourierProfileDetails?>(null)
    var currentAdminName by mutableStateOf<String?>(null)
        private set
    var isStartStateReady by mutableStateOf(false)
        private set
    var hasConfirmedAddress by mutableStateOf(false)
        private set
    var profileDetails by mutableStateOf(
        UserProfileDetails(
            firstName = "Vishal",
            lastName = "Khadok",
            phone = "",
            email = "hello@foodly.app",
            bio = "I love fast food",
            photoUri = DEFAULT_PROFILE_PHOTO_URL,
        ),
    )
    var courierProfileDetails by mutableStateOf(
        preferences.readLastCourierProfile() ?: defaultCourierProfileDetails(),
    )
    var themeMode by mutableStateOf(preferences.readThemeMode())
    var language by mutableStateOf(preferences.readLanguage())
    var selectedRestaurantId by mutableStateOf(restaurants.first().id)
    var selectedMenuItemId by mutableStateOf(restaurants.first().menu.first().id)
    var selectedAddress by mutableStateOf(savedAddresses.first())
    var editingAddressIndex by mutableStateOf<Int?>(null)
        private set
    var isAddingAddress by mutableStateOf(false)
        private set
    var selectedPaymentMethod by mutableStateOf(PaymentMethod.MasterCard)
    var selectedPaymentCardId by mutableStateOf<String?>(null)
    var trackedOrderId by mutableStateOf<String?>(null)
    var lastPlacedOrder by mutableStateOf<OrderSummary?>(null)
    var pendingWelcomeCouponCode by mutableStateOf<String?>(null)
        private set
    var appliedCouponCode by mutableStateOf<String?>(null)
        private set
    var deliveryQuote by mutableStateOf<DeliveryQuote?>(null)
    var refundBalance by mutableStateOf(0)
    var lastBackendError by mutableStateOf<String?>(null)
    var isPlacingOrder by mutableStateOf(false)
    var courierIsOnline by mutableStateOf(false)
    var selectedCourierOrderId by mutableStateOf<String?>(null)
    var courierWalletBalance by mutableStateOf(0)
    var courierRating by mutableStateOf(DefaultCourierTestRating)
        private set
    var courierBlockedUntilMillis by mutableStateOf(0L)
        private set
    var courierLastWithdrawalAtMillis by mutableStateOf(0L)
        private set
    var courierPayoutCard by mutableStateOf<PaymentCard?>(null)
        private set
    var lastFavoriteAddedEventId by mutableStateOf<String?>(null)
        private set
    var lastNotificationEventId by mutableStateOf<String?>(null)
        private set
    var lastDeliveredOrderEventId by mutableStateOf<String?>(null)
        private set

    init {
        restoreAdminCatalog()
        restoreAdminCoupons()
        restoreDishOrderCounts()
        restoreCourierWorkspace()
        if (orderSyncBackend.enabled) {
            startOrderSync()
        }
    }

    private fun restoreAdminCatalog(): Boolean {
        val savedRestaurants = preferences.readAdminRestaurants() ?: return false
        if (savedRestaurants.isEmpty()) return false

        restaurants.clear()
        restaurants.addAll(savedRestaurants)
        selectedRestaurantId = restaurants
            .firstOrNull { it.id == selectedRestaurantId }
            ?.id
            ?: restaurants.first().id
        selectedMenuItemId = restaurants
            .asSequence()
            .flatMap { it.menu.asSequence() }
            .firstOrNull { it.id == selectedMenuItemId }
            ?.id
            ?: restaurants.firstNotNullOfOrNull { restaurant -> restaurant.menu.firstOrNull()?.id }
            ?: selectedMenuItemId
        return true
    }

    private fun saveAdminCatalog() {
        preferences.writeAdminRestaurants(restaurants)
    }

    private fun restoreAdminCoupons() {
        adminCoupons.clear()
        adminCoupons.addAll(preferences.readAdminCoupons().orEmpty())
        pruneExpiredCoupons()
    }

    private fun saveAdminCoupons() {
        preferences.writeAdminCoupons(adminCoupons)
    }

    private fun restoreDishOrderCounts() {
        dishOrderCounts.clear()
        dishOrderCounts.putAll(preferences.readDishOrderCounts())
    }

    private fun saveDishOrderCounts() {
        preferences.writeDishOrderCounts(dishOrderCounts)
    }

    private fun restoreCourierWorkspace(): Boolean {
        val saved = preferences.readCourierWorkspace() ?: return false
        val realOrders = saved.orders.filterNot { it.id.startsWith("demo_") }
        val realMessages = saved.messages.filterNot { it.orderId.startsWith("demo_") || it.isAutomaticChatMessage() }
        courierOrders.clear()
        courierOrders.addAll(realOrders)
        courierChatMessages.clear()
        courierChatMessages.addAll(realMessages)
        courierReviews.clear()
        courierReviews.addAll(saved.reviews.distinctBy { it.orderId })
        unreadCourierChatOrderIds.clear()
        unreadCourierChatOrderIds.addAll(saved.unreadCourierChatOrderIds.filter { orderId -> realOrders.any { it.id == orderId } })
        courierWalletBalance = saved.walletBalance
        courierRating = saved.rating
        courierBlockedUntilMillis = saved.blockedUntilMillis
        courierLastWithdrawalAtMillis = saved.lastWithdrawalAtMillis
        courierPayoutCard = saved.payoutCard
        courierReviewRatings.clear()
        courierReviewRatings.putAll(saved.reviewRatings)
        clearExpiredCourierBlock()
        recalculateCourierRating()
        courierIsOnline = saved.isOnline
        selectedCourierOrderId = saved.selectedOrderId?.takeIf { orderId ->
            realOrders.any { it.id == orderId }
        }
        return realOrders.isNotEmpty() || realMessages.isNotEmpty()
    }

    suspend fun restoreSavedSession() {
        if (isStartStateReady) return

        try {
            val savedSession = preferences.readSavedCustomerSession()
            if (savedSession == null) {
                restoreDefaultAddresses()
                return
            }

            when (val result = withContext(Dispatchers.IO) { backend.restoreSession(savedSession.email) }) {
                is BackendResult.Success -> {
                    currentUser = result.data
                    activeCustomerId = result.data.id
                    profileDetails = UserProfileDetails.fromSession(
                        session = result.data,
                        phone = profileDetails.phone,
                        isPhoneVerified = profileDetails.isPhoneVerified,
                        bio = profileDetails.bio,
                        photoUri = profileDetails.photoUri,
                    )
                    preferences.readCustomerProfile(result.data.id)?.let { savedProfile ->
                        profileDetails = savedProfile
                        currentUser = currentUser?.copy(
                            name = savedProfile.fullName,
                            email = savedProfile.email.trim(),
                        )
                    }
                    resetCustomerState()
                    loadCustomerAddresses(result.data.id)
                    loadCustomerOrders(result.data.id)
                    loadCustomerFavorites(result.data.token, result.data.id)
                    notifyCustomerAboutAdminCoupons(result.data.id)
                    result.data.welcomeCouponExpiresAtMillis?.let { expiresAtMillis ->
                        grantWelcomeCoupon(
                            expiresAtMillis = expiresAtMillis,
                            showOffer = false,
                        )
                    }
                    refreshPaymentCards(result.data.token)
                    lastBackendError = null
                }
                is BackendResult.Error -> {
                    preferences.clearSavedCustomerSession()
                    restoreDefaultAddresses()
                    lastBackendError = result.message
                }
            }
        } catch (error: Exception) {
            if (currentUser == null) {
                restoreDefaultAddresses()
            }
            lastBackendError = error.message
        } finally {
            isStartStateReady = true
        }
    }

    val selectedRestaurant: Restaurant
        get() = restaurants.first { it.id == selectedRestaurantId }

    val selectedMenuItem: MenuItem
        get() = restaurants.asSequence()
            .flatMap { it.menu.asSequence() }
            .first { it.id == selectedMenuItemId }

    val selectedMenuRestaurant: Restaurant
        get() = restaurantForMenuItem(selectedMenuItem) ?: selectedRestaurant

    val cartRestaurant: Restaurant
        get() = cartItems.firstOrNull()
            ?.item
            ?.restaurantId
            ?.let { restaurantId -> restaurants.firstOrNull { it.id == restaurantId } }
            ?: selectedRestaurant

    val selectedPaymentCard: PaymentCard?
        get() = savedPaymentCards.firstOrNull { it.id == selectedPaymentCardId }

    val favoriteItems: List<MenuItem>
        get() = favoriteItemIds.mapNotNull { itemId ->
            restaurants.asSequence()
                .flatMap { it.menu.asSequence() }
                .firstOrNull { it.id == itemId }
        }

    val trackedOrder: OrderSummary?
        get() = trackedOrderId?.let { orderId ->
            ongoingOrders.firstOrNull { it.id == orderId }
                ?: historyOrders.firstOrNull { it.id == orderId }
        } ?: lastPlacedOrder

    val activeCoupons: List<DiscountCoupon>
        get() {
            val nowMillis = System.currentTimeMillis()
            return (coupons + adminCoupons)
                .filter { !it.isExpired(nowMillis) }
                .distinctBy { it.code.normalizeCouponCode() }
        }

    val pendingWelcomeCoupon: DiscountCoupon?
        get() = pendingWelcomeCouponCode?.let { code ->
            activeCoupons.firstOrNull { it.code.equals(code, ignoreCase = true) }
        }

    val appliedCoupon: DiscountCoupon?
        get() = appliedCouponCode?.let { code ->
            activeCoupons.firstOrNull { it.code.equals(code, ignoreCase = true) }
        }

    val cartCount: Int
        get() = cartItems.sumOf { it.quantity }

    val adminRevenue: Int
        get() = adminKnownOrders()
            .filter { it.status != OrderStatus.Cancelled }
            .sumOf { it.total }

    val adminCurrentOrders: Int
        get() = adminKnownOrders()
            .count { it.status != OrderStatus.Delivered && it.status != OrderStatus.Cancelled }

    val adminOrderRequests: Int
        get() = availableCourierOrders.size

    val adminReviewCount: Int
        get() = courierReviewRatings.size

    val adminAverageRating: Double
        get() = courierReviewRatings.values
            .takeIf { it.isNotEmpty() }
            ?.average()
            ?: restaurants.map { it.rating }.average()

    val topOrderedItems: List<Pair<MenuItem, Int>>
        get() {
            val menuById = restaurants
                .flatMap { it.menu }
                .associateBy { it.id }
            return dishOrderCounts.entries
                .filter { (_, count) -> count > 0 }
                .mapNotNull { (itemId, count) -> menuById[itemId]?.let { item -> item to count } }
                .sortedByDescending { it.second }
        }

    private fun adminKnownOrders(): List<OrderSummary> {
        val ordersById = linkedMapOf<String, OrderSummary>()
        courierOrders.forEach { order ->
            ordersById[order.id] = OrderSummary(
                id = order.id,
                restaurantId = order.restaurantId,
                restaurantName = order.restaurantName,
                itemsLabel = order.itemsLabel,
                total = order.total,
                eta = order.status.customerEta(),
                status = order.status.toCustomerOrderStatus(),
                createdAtMillis = order.createdAtMillis,
            )
        }
        ongoingOrders.forEach { order -> ordersById.putIfAbsent(order.id, order) }
        historyOrders.forEach { order -> ordersById.putIfAbsent(order.id, order) }
        return ordersById.values.toList()
    }

    val availableCourierOrders: List<CourierDeliveryOrder>
        get() {
            if (isCourierCurrentlyBlocked()) return emptyList()
            val orders = courierOrders.filter { it.status == CourierOrderStatus.Available }
            return if (courierRatingCount > 0 && courierRating < CourierReducedOrdersRatingThreshold) {
                orders.filterIndexed { index, _ -> index % 2 == 0 }
            } else {
                orders
            }
        }

    val activeCourierOrders: List<CourierDeliveryOrder>
        get() {
            val courierId = currentCourier?.id
            val orders = if (courierId != null) {
                courierOrders.filter { order ->
                    order.courierId == courierId && order.status != CourierOrderStatus.Delivered
                }
            } else {
                selectedCourierOrderId
                    ?.let { orderId -> courierOrders.filter { it.id == orderId && it.status != CourierOrderStatus.Delivered } }
                    .orEmpty()
            }
            return orders.sortedByDescending { it.createdAtMillis }
        }

    val activeCourierOrder: CourierDeliveryOrder?
        get() = activeCourierOrders.firstOrNull()

    val selectedCourierOrder: CourierDeliveryOrder?
        get() = selectedCourierOrderId?.let { orderId -> courierOrders.firstOrNull { it.id == orderId } }

    val courierChatThreads: List<CourierDeliveryOrder>
        get() {
            val courierId = currentCourier?.id
            return courierOrders
                .asSequence()
                .filter { order -> order.status != CourierOrderStatus.Delivered }
                .filter { order ->
                    order.status == CourierOrderStatus.Available ||
                        order.courierId == courierId ||
                        chatMessagesForOrder(order.id).isNotEmpty()
                }
                .groupBy { order -> order.chatCustomerKey() }
                .values
                .mapNotNull { orders -> orders.maxByOrNull { it.createdAtMillis } }
                .sortedByDescending { order ->
                    chatMessagesForOrder(order.id).lastOrNull()?.createdSortValue() ?: order.createdAtMillis
                }
        }

    val courierHistoryOrders: List<CourierDeliveryOrder>
        get() = currentCourier?.id?.let { courierId ->
            courierOrders.filter { it.courierId == courierId && it.status == CourierOrderStatus.Delivered }
        }.orEmpty()

    val courierDeliveredCount: Int
        get() = courierHistoryOrders.size

    val courierRatingCount: Int
        get() = courierReviewRatings.size

    val courierRatingLabel: String
        get() = if (courierRatingCount == 0) {
            String.format(Locale.US, "%.1f", DefaultCourierTestRating)
        } else {
            String.format(Locale.US, "%.1f", courierRating)
        }

    val courierRatingCycleLabel: String
        get() = "${courierRatingCount % CourierRatingReviewBatchSize}/$CourierRatingReviewBatchSize"

    val courierIsBlocked: Boolean
        get() = isCourierCurrentlyBlocked()

    val courierBlockMessage: String
        get() = if (courierIsBlocked) {
            "Courier is blocked until ${courierBlockedUntilMillis.toShortDateLabel()}"
        } else {
            ""
        }

    val courierLastWithdrawalLabel: String
        get() = courierLastWithdrawalAtMillis
            .takeIf { it > 0L }
            ?.toShortDateLabel()
            ?: "-"

    val unreadNotificationCount: Int
        get() = notifications.count { !it.read }

    val subtotal: Int
        get() = cartItems.sumOf { it.item.price * it.quantity }

    val deliveryFee: Int
        get() = if (cartItems.isEmpty()) 0 else deliveryQuote?.deliveryFee ?: 5

    val serviceFee: Int
        get() = if (cartItems.isEmpty()) 0 else 2

    val discountAmount: Int
        get() {
            val coupon = appliedCoupon ?: return 0
            if (cartItems.isEmpty() || subtotal <= 0) return 0
            return ((subtotal * coupon.discountPercent) / 100).coerceAtLeast(1)
        }

    val total: Int
        get() = (subtotal + deliveryFee + serviceFee - discountAmount).coerceAtLeast(0)

    fun selectRestaurant(restaurantId: String) {
        selectedRestaurantId = restaurantId
    }

    fun selectMenuItem(itemId: String) {
        val restaurant = restaurants.firstOrNull { restaurant ->
            restaurant.menu.any { item -> item.id == itemId }
        } ?: return
        selectedRestaurantId = restaurant.id
        selectedMenuItemId = itemId
    }

    val addressDraft: DeliveryAddress
        get() = editingAddressIndex
            ?.let { index -> savedAddresses.getOrNull(index) }
            ?: if (isAddingAddress) {
                DeliveryAddress(
                    label = "HOME",
                    title = "",
                    subtitle = selectedAddress.subtitle,
                    point = selectedAddress.point,
                )
            } else {
                selectedAddress
            }

    fun beginAddAddress() {
        editingAddressIndex = null
        isAddingAddress = true
    }

    fun beginEditAddress(index: Int) {
        if (savedAddresses.getOrNull(index) == null) return
        editingAddressIndex = index
        isAddingAddress = false
    }

    fun clearAddressDraft() {
        editingAddressIndex = null
        isAddingAddress = false
    }

    fun selectSavedAddress(index: Int) {
        val address = savedAddresses.getOrNull(index) ?: return
        selectedAddress = address
        deliveryQuote = null
        hasConfirmedAddress = true
        saveCurrentCustomerAddresses()
    }

    fun deleteSavedAddress(index: Int) {
        if (savedAddresses.size <= 1) return
        val removed = savedAddresses.getOrNull(index) ?: return
        savedAddresses.removeAt(index)
        if (selectedAddress == removed) {
            selectedAddress = savedAddresses.first()
            deliveryQuote = null
        }
        saveCurrentCustomerAddresses()
        clearAddressDraft()
    }

    fun updateAddress(address: DeliveryAddress) {
        val editIndex = editingAddressIndex
        when {
            editIndex != null && savedAddresses.getOrNull(editIndex) != null -> {
                savedAddresses[editIndex] = address
            }
            isAddingAddress -> {
                savedAddresses.add(address)
            }
            else -> {
                val selectedIndex = savedAddresses.indexOf(selectedAddress)
                if (selectedIndex >= 0) {
                    savedAddresses[selectedIndex] = address
                } else {
                    savedAddresses.add(0, address)
                }
            }
        }
        selectedAddress = address
        hasConfirmedAddress = true
        deliveryQuote = null
        clearAddressDraft()
        saveCurrentCustomerAddresses()
    }

    fun saveCurrentLocationAddress(address: DeliveryAddress) {
        val normalizedAddress = address.copy(
            label = normalizeAddressLabelForStorage(address.label.ifBlank { AddressLabelOther }),
        )
        val existingIndex = savedAddresses.indexOfFirst { it.point.isCloseTo(normalizedAddress.point) }
        if (existingIndex >= 0) {
            savedAddresses[existingIndex] = normalizedAddress
            selectedAddress = savedAddresses[existingIndex]
        } else {
            savedAddresses.add(0, normalizedAddress)
            selectedAddress = savedAddresses.first()
        }
        hasConfirmedAddress = true
        deliveryQuote = null
        clearAddressDraft()
        saveCurrentCustomerAddresses()
    }

    fun updateProfile(details: UserProfileDetails) {
        val cleanPhone = when {
            details.phone.hasUzbekPhoneDigits() -> details.phone.toStoredUzbekPhone()
            else -> details.phone.trim()
        }
        val cleanDetails = details.copy(
            phone = cleanPhone,
            isPhoneVerified = details.isPhoneVerified && cleanPhone.isNotBlank(),
            email = details.email.trim(),
        )
        profileDetails = cleanDetails

        val token = currentUser?.token
        val backendResult = token?.let {
            backend.updateUserProfile(
                token = it,
                name = cleanDetails.fullName,
                email = cleanDetails.email,
            )
        }

        when (backendResult) {
            is BackendResult.Success -> {
                currentUser = backendResult.data
                activeCustomerId = backendResult.data.id
                preferences.writeSavedCustomerSession(backendResult.data)
                preferences.writeCustomerProfile(backendResult.data.id, cleanDetails.copy(email = backendResult.data.email))
                lastBackendError = null
            }
            is BackendResult.Error -> {
                currentUser = currentUser?.copy(
                    name = cleanDetails.fullName,
                    email = cleanDetails.email,
                )
                (activeCustomerId ?: currentUser?.id)?.let { userId ->
                    preferences.writeCustomerProfile(userId, cleanDetails)
                }
                currentUser?.let { session -> preferences.writeSavedCustomerSession(session) }
                lastBackendError = backendResult.message
            }
            null -> {
                currentUser = currentUser?.copy(
                    name = cleanDetails.fullName,
                    email = cleanDetails.email,
                )
                (activeCustomerId ?: currentUser?.id)?.let { userId ->
                    preferences.writeCustomerProfile(userId, cleanDetails)
                }
                currentUser?.let { session -> preferences.writeSavedCustomerSession(session) }
            }
        }
    }

    fun logOutCustomer() {
        saveCurrentCustomerOrders()
        saveCurrentCustomerFavorites()
        currentUser = null
        activeCustomerId = null
        preferences.clearSavedCustomerSession()
        resetCustomerState()
        restoreDefaultAddresses()
    }

    fun logOutCourier() {
        (currentCourier ?: courierProfileDetails).let { preferences.writeCourierProfile(it) }
        courierIsOnline = false
        saveCourierWorkspace()
        currentCourier = null
        selectedCourierOrderId = null
        lastBackendError = null
    }

    private fun loadCustomerAddresses(userId: String) {
        val saved = preferences.readCustomerAddresses(userId)
        if (saved == null || saved.addresses.isEmpty()) {
            restoreDefaultAddresses()
            return
        }

        savedAddresses.clear()
        savedAddresses.addAll(saved.addresses)
        selectedAddress = saved.addresses.getOrElse(saved.selectedIndex) { saved.addresses.first() }
        hasConfirmedAddress = true
        deliveryQuote = null
        clearAddressDraft()
    }

    private fun restoreDefaultAddresses() {
        savedAddresses.clear()
        savedAddresses.addAll(defaultCustomerAddresses())
        selectedAddress = savedAddresses.first()
        hasConfirmedAddress = false
        deliveryQuote = null
        clearAddressDraft()
    }

    private fun saveCurrentCustomerAddresses() {
        val userId = currentUser?.id ?: return
        preferences.writeCustomerAddresses(
            userId = userId,
            addresses = savedAddresses,
            selectedAddress = selectedAddress,
        )
    }

    private fun loadCustomerOrders(userId: String) {
        val saved = preferences.readCustomerOrders(userId) ?: return
        ongoingOrders.clear()
        ongoingOrders.addAll(saved.ongoingOrders)
        historyOrders.clear()
        historyOrders.addAll(saved.historyOrders)
        orderReviews.clear()
        orderReviews.addAll(saved.orderReviews)
        saved.orderReviews.forEach { review ->
            if (courierReviews.none { it.orderId == review.orderId }) {
                courierReviews.add(review)
            }
        }
        refundBalance = saved.refundBalance
        trackedOrderId = saved.trackedOrderId
        lastPlacedOrder = (saved.lastPlacedOrderId ?: saved.trackedOrderId)?.let { orderId ->
            ongoingOrders.firstOrNull { it.id == orderId } ?: historyOrders.firstOrNull { it.id == orderId }
        }
        recalculateCourierRating()
        reconcileCustomerOrdersWithCourierOrders(userId)
        reconcileDeliveredReceiptRefunds()
    }

    private fun saveCurrentCustomerOrders() {
        val userId = activeCustomerId ?: currentUser?.id ?: return
        preferences.writeCustomerOrders(
            userId = userId,
            ongoingOrders = ongoingOrders,
            historyOrders = historyOrders,
            trackedOrderId = trackedOrderId,
            lastPlacedOrderId = lastPlacedOrder?.id,
            refundBalance = refundBalance,
            orderReviews = orderReviews,
        )
    }

    private suspend fun loadCustomerFavorites(token: String, userId: String) {
        val savedFavorites = when (val result = withContext(Dispatchers.IO) { backend.favoriteItemIds(token) }) {
            is BackendResult.Success -> result.data
            is BackendResult.Error -> {
                lastBackendError = result.message
                preferences.readCustomerFavorites(userId)
            }
        }
        favoriteItemIds.clear()
        favoriteItemIds.addAll(savedFavorites.filter { itemId ->
            restaurants.any { restaurant -> restaurant.menu.any { item -> item.id == itemId } }
        }.distinct())
        preferences.writeCustomerFavorites(userId, favoriteItemIds)
    }

    private fun saveCurrentCustomerFavorites() {
        val userId = activeCustomerId ?: currentUser?.id ?: return
        val itemIds = favoriteItemIds.toList()
        preferences.writeCustomerFavorites(userId, itemIds)
        currentUser?.token?.let { token ->
            orderScope.launch {
                when (val result = withContext(Dispatchers.IO) { backend.replaceFavoriteItemIds(token, itemIds) }) {
                    is BackendResult.Success -> Unit
                    is BackendResult.Error -> lastBackendError = result.message
                }
            }
        }
    }

    private fun notifyCustomerAboutAdminCoupons(userId: String) {
        val seenCodes = preferences.readNotifiedAdminCouponCodes(userId).toMutableSet()
        val newCoupons = adminCoupons
            .filter { !it.isExpired() }
            .filter { coupon -> coupon.code.normalizeCouponCode() !in seenCodes }
        if (newCoupons.isEmpty()) return

        newCoupons.asReversed().forEach { coupon ->
            addNotification(
                title = couponNotificationTitle(),
                message = couponNotificationMessage(coupon),
            )
            seenCodes.add(coupon.code.normalizeCouponCode())
        }
        preferences.writeNotifiedAdminCouponCodes(userId, seenCodes)
    }

    private fun couponNotificationTitle(): String = when (language) {
        AppLanguage.English -> "New discount"
        AppLanguage.Russian -> "Новая скидка"
        AppLanguage.Uzbek -> "Yangi chegirma"
    }

    private fun couponNotificationMessage(coupon: DiscountCoupon): String = when (language) {
        AppLanguage.English -> "${coupon.title}: ${coupon.discountPercent}% off. Code ${coupon.code}."
        AppLanguage.Russian -> "${coupon.title}: скидка ${coupon.discountPercent}%. Код ${coupon.code}."
        AppLanguage.Uzbek -> "${coupon.title}: ${coupon.discountPercent}% chegirma. Kod ${coupon.code}."
    }

    private fun reconcileCustomerOrdersWithCourierOrders(userId: String) {
        courierOrders
            .filter { it.customerUserId == userId }
            .forEach { courierOrder ->
                applyCourierStatusToCustomerOrder(courierOrder, persist = false)
            }
        saveCurrentCustomerOrders()
    }

    fun adminLogin(username: String, password: String): String? {
        val cleanUsername = username.trim()
        return if (cleanUsername.equals(AdminUsername, ignoreCase = true) && password == AdminPassword) {
            currentAdminName = cleanUsername
            lastBackendError = null
            null
        } else {
            "Use admin / 123456789"
        }
    }

    fun logOutAdmin() {
        currentAdminName = null
        lastBackendError = null
    }

    fun addAdminRestaurant(
        name: String,
        subtitle: String,
        description: String,
        imageUrl: String,
        imageUrls: List<String> = emptyList(),
        deliveryTime: String,
        deliveryFee: String,
        tags: List<String> = emptyList(),
        latitude: Double? = null,
        longitude: Double? = null,
    ): String? {
        val cleanName = name.trim()
        if (cleanName.length < 2) return "Enter restaurant name"

        val baseId = cleanName.toAdminId("restaurant")
        val restaurantId = uniqueAdminId(baseId, restaurants.map { it.id }.toSet())
        val indexOffset = restaurants.size * 0.004
        val cleanImageUrls = (listOf(imageUrl) + imageUrls)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
        val cleanTags = tags
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .ifEmpty { listOf("New", "Admin", "Popular") }
        val latitudeValue = latitude ?: 41.311090 + indexOffset
        val longitudeValue = longitude ?: 69.279782 + indexOffset
        val restaurant = Restaurant(
            id = restaurantId,
            name = cleanName,
            subtitle = subtitle.trim().ifBlank { "Fresh menu from admin" },
            description = description.trim().ifBlank { "New restaurant added from the admin dashboard." },
            rating = 4.8,
            deliveryTime = deliveryTime.trim().ifBlank { "18-25 min" },
            deliveryFee = deliveryFee.trim().ifBlank { "Free" },
            accent = categories.getOrNull(restaurants.size % categories.size)?.accent ?: restaurants.first().accent,
            emoji = "Food",
            tags = cleanTags,
            location = GeoPoint(latitudeValue, longitudeValue),
            menu = emptyList(),
            imageUrl = cleanImageUrls.firstOrNull() ?: DefaultAdminRestaurantImage,
            imageUrls = cleanImageUrls,
        )
        restaurants.add(0, restaurant)
        selectedRestaurantId = restaurant.id
        saveAdminCatalog()
        lastBackendError = null
        return null
    }

    fun addAdminMenuItem(
        restaurantId: String,
        title: String,
        subtitle: String,
        price: Int,
        category: String,
        imageUrl: String,
        ingredients: List<String>,
        details: String,
    ): String? {
        val restaurantIndex = restaurants.indexOfFirst { it.id == restaurantId }
        if (restaurantIndex < 0) return "Choose restaurant"

        val cleanTitle = title.trim()
        if (cleanTitle.length < 2) return "Enter item name"
        if (price <= 0) return "Enter correct price"

        val restaurant = restaurants[restaurantIndex]
        val cleanCategory = category.trim().ifBlank { "burger" }
        val baseId = "${restaurant.id}_${cleanTitle.toAdminId("item")}"
        val usedIds = restaurants.flatMap { it.menu }.map { it.id }.toSet()
        val itemId = uniqueAdminId(baseId, usedIds)
        val accent = categories.firstOrNull { it.id == cleanCategory }?.accent ?: restaurant.accent
        val item = MenuItem(
            id = itemId,
            restaurantId = restaurant.id,
            title = cleanTitle,
            subtitle = subtitle.trim().ifBlank { "Fresh item from admin" },
            price = price,
            emoji = "Food",
            accent = accent,
            category = cleanCategory,
            imageUrl = imageUrl.trim().ifBlank { restaurant.imageUrl },
            ingredients = ingredients.map { it.trim() }.filter { it.isNotBlank() },
            details = details.trim().ifBlank { "Added from the admin product flow." },
        )

        restaurants[restaurantIndex] = restaurant.copy(menu = listOf(item) + restaurant.menu)
        selectedRestaurantId = restaurant.id
        selectedMenuItemId = item.id
        dishOrderCounts[item.id] = 0
        saveAdminCatalog()
        lastBackendError = null
        return null
    }

    fun addAdminCoupon(
        code: String,
        title: String,
        percent: Int,
        activeDays: Int,
    ): String? {
        val cleanCode = code.normalizeCouponCode()
        if (cleanCode.length < 3) return "Enter coupon code"
        if (percent !in 1..90) return "Discount must be 1-90%"
        if (activeDays !in 1..365) return "Active days must be 1-365"

        val expiresAtMillis = System.currentTimeMillis() + activeDays.toLong() * 24 * 60 * 60 * 1_000L
        val coupon = DiscountCoupon(
            code = cleanCode,
            title = title.trim().ifBlank { "$percent% admin discount" },
            description = "$percent% off. Created in admin dashboard.",
            discountPercent = percent,
            expiresAtMillis = expiresAtMillis,
        )
        adminCoupons.removeAll { it.code.normalizeCouponCode() == cleanCode }
        adminCoupons.add(0, coupon)
        saveAdminCoupons()
        (activeCustomerId ?: currentUser?.id)?.let { userId ->
            notifyCustomerAboutAdminCoupons(userId)
        }
        lastBackendError = null
        return null
    }

    fun removeAdminCoupon(code: String) {
        val normalizedCode = code.normalizeCouponCode()
        adminCoupons.removeAll { it.code.normalizeCouponCode() == normalizedCode }
        if (appliedCouponCode?.normalizeCouponCode() == normalizedCode) {
            appliedCouponCode = null
        }
        saveAdminCoupons()
    }

    suspend fun courierLogin(email: String, password: String): String? {
        delay(60)
        if (!email.contains("@")) return "Check courier email"
        if (password.length < 4) return "Password must be at least 4 characters"
        val cleanEmail = email.trim()
        val savedProfile = preferences.readCourierProfile(cleanEmail)
        val profile = savedProfile ?: courierProfileDetails.copy(email = cleanEmail)
        courierProfileDetails = profile
        currentCourier = profile
        if (cleanEmail.equals(DemoCourierEmail, ignoreCase = true)) {
            courierRating = DefaultCourierTestRating
            courierBlockedUntilMillis = 0L
        }
        selectedCourierOrderId = activeCourierOrder?.id
        lastBackendError = null
        preferences.writeCourierProfile(profile)
        saveCourierWorkspace()
        syncRemoteOrdersSoon()
        return null
    }

    suspend fun courierRegister(
        name: String,
        email: String,
        phone: String,
        password: String,
    ): String? {
        delay(60)
        if (name.trim().length < 2) return "Enter courier name"
        if (!email.contains("@")) return "Check courier email"
        if (phone.hasUzbekPhoneDigits() && !phone.isCompleteUzbekPhone()) return "Check phone number"
        if (!phone.hasUzbekPhoneDigits() && phone.trim().length < 6) return "Enter phone number"
        if (password.length < 4) return "Password must be at least 4 characters"
        val cleanPhone = when {
            phone.hasUzbekPhoneDigits() -> phone.toStoredUzbekPhone()
            else -> phone.trim()
        }
        val profile = CourierProfileDetails(
            id = "courier_${System.currentTimeMillis()}",
            name = name.trim(),
            email = email.trim(),
            phone = cleanPhone,
            vehicle = courierProfileDetails.vehicle,
            photoUrl = courierProfileDetails.photoUrl,
        )
        courierProfileDetails = profile
        currentCourier = profile
        courierIsOnline = true
        lastBackendError = null
        preferences.writeCourierProfile(profile)
        saveCourierWorkspace()
        syncRemoteOrdersSoon()
        return null
    }

    fun updateCourierProfile(details: CourierProfileDetails) {
        val cleanPhone = when {
            details.phone.hasUzbekPhoneDigits() -> details.phone.toStoredUzbekPhone()
            else -> details.phone.trim()
        }
        val updatedDetails = details.copy(
            name = details.name.trim(),
            email = details.email.trim(),
            phone = cleanPhone,
            vehicle = details.vehicle.trim(),
        )
        courierProfileDetails = updatedDetails
        currentCourier = currentCourier?.let { current ->
            updatedDetails.copy(id = current.id)
        } ?: updatedDetails
        currentCourier?.let { preferences.writeCourierProfile(it) }
        saveCourierWorkspace()
    }

    fun toggleCourierOnline() {
        clearExpiredCourierBlock()
        if (!courierIsOnline && isCourierCurrentlyBlocked()) {
            lastBackendError = courierBlockMessage
            return
        }
        courierIsOnline = !courierIsOnline
        saveCourierWorkspace()
        if (courierIsOnline) {
            syncRemoteOrdersSoon()
        }
    }
    fun selectCourierOrder(orderId: String) {
        selectedCourierOrderId = orderId
        saveCourierWorkspace()
    }

    fun acceptCourierOrder(orderId: String): String? {
        clearExpiredCourierBlock()
        if (isCourierCurrentlyBlocked()) return courierBlockMessage
        val courier = currentCourier ?: return "Courier is not signed in"
        val index = courierOrders.indexOfFirst { it.id == orderId }
        if (index < 0) return "Order not found"
        val order = courierOrders[index]
        if (order.status != CourierOrderStatus.Available && order.courierId != courier.id) {
            return "This order has already been accepted"
        }
        val acceptedOrder = order.copy(
            status = CourierOrderStatus.Accepted,
            courierId = courier.id,
            courierName = courier.name.trim().ifBlank { null },
            courierPhone = courier.phone.trim().ifBlank { null },
            courierPoint = order.courierPoint,
        )
        courierOrders[index] = acceptedOrder
        selectedCourierOrderId = orderId
        updateCustomerOrderEta(orderId, "Courier accepted your order")
        persistCustomerOrderUpdate(acceptedOrder)
        addNotification(
            title = "Courier accepted",
            message = "${courier.name} accepted your order and is heading to the restaurant.",
        )
        saveCourierWorkspace()
        publishRemoteOrder(acceptedOrder)
        return null
    }

    fun updateCourierOrderStatus(orderId: String, status: CourierOrderStatus): String? {
        val index = courierOrders.indexOfFirst { it.id == orderId }
        if (index < 0) return "Order not found"
        val order = courierOrders[index]
        val updatedOrder = order.copy(
            status = status,
            courierPoint = status.defaultCourierPoint(order),
        )
        courierOrders[index] = updatedOrder
        when (status) {
            CourierOrderStatus.ArrivedAtRestaurant -> updateCustomerOrderEta(orderId, "Courier arrived at restaurant")
            CourierOrderStatus.PickedUp -> {
                updateCustomerOrderEta(orderId, "Courier picked up your order")
                addNotification(
                    title = "Order picked up",
                    message = "The courier picked up your food and will start delivery soon.",
                )
            }
            CourierOrderStatus.OnTheWay -> updateCustomerOrderStatus(orderId, OrderStatus.OnTheWay, "Courier is on the way")
            CourierOrderStatus.Delivered -> {
                updateCustomerOrderStatus(orderId, OrderStatus.Delivered, "Delivered just now")
                courierWalletBalance += order.earning
                selectedCourierOrderId = null
                closeChatForOrder(orderId)
                addNotification(
                    title = "Order delivered",
                    message = "Your food was delivered to the selected address.",
                    playGenericSound = false,
                )
                lastDeliveredOrderEventId = "courier_delivered_${updatedOrder.id}_${System.currentTimeMillis()}"
            }
            CourierOrderStatus.Available,
            CourierOrderStatus.Accepted -> Unit
        }
        persistCustomerOrderUpdate(updatedOrder)
        saveCourierWorkspace()
        publishRemoteOrderStatus(updatedOrder)
        return null
    }

    fun updateCourierLocationForActiveOrders(point: GeoPoint) {
        val courierId = currentCourier?.id ?: return
        var changed = false
        for (index in courierOrders.indices) {
            val order = courierOrders[index]
            if (
                order.courierId != courierId ||
                order.status == CourierOrderStatus.Available ||
                order.status == CourierOrderStatus.Delivered ||
                order.courierPoint?.isCloseTo(point) == true
            ) {
                continue
            }

            val updatedOrder = order.copy(courierPoint = point)
            courierOrders[index] = updatedOrder
            persistCustomerOrderUpdate(updatedOrder)
            publishRemoteOrderStatus(updatedOrder)
            changed = true
        }
        if (changed) {
            saveCourierWorkspace()
        }
    }

    fun withdrawCourierBalance(): String? {
        if (courierWalletBalance <= 0) return courierNoMoneyToWithdrawMessage()
        if (courierPayoutCard == null) return courierMissingPayoutCardMessage()
        courierWalletBalance = 0
        courierLastWithdrawalAtMillis = System.currentTimeMillis()
        saveCourierWorkspace()
        return null
    }

    fun saveCourierPayoutCard(
        number: String,
        holderName: String,
        expiry: String,
    ): String? {
        val digits = number.filter(Char::isDigit)
        val paymentMethod = detectPaymentMethod(digits) ?: return courierUnsupportedCardMessage()
        val cleanHolder = holderName.trim()
        val normalizedExpiry = expiry.trim().normalizeCardExpiry()

        if (cleanHolder.length < 2) return courierCardHolderMessage()
        if (digits.length != 16) return courierCardNumberMessage()
        if (!normalizedExpiry.matches(Regex("""\d{2}/\d{2}"""))) return courierCardExpiryMessage()

        courierPayoutCard = PaymentCard(
            id = "courier_card_${UUID.randomUUID()}",
            brand = paymentMethod.cardBrandName(),
            last4 = digits.takeLast(4),
            holderName = cleanHolder.uppercase(Locale.US),
            expiry = normalizedExpiry,
        )
        saveCourierWorkspace()
        return null
    }

    fun chatMessagesForOrder(orderId: String): List<CourierChatMessage> =
        courierChatMessages.filter { it.orderId == orderId && !it.isAutomaticChatMessage() }

    fun isCourierChatUnread(orderId: String): Boolean =
        orderId in unreadCourierChatOrderIds

    fun isCustomerChatUnread(orderId: String): Boolean =
        orderId in unreadCustomerChatOrderIds

    fun markCourierChatRead(orderId: String) {
        if (unreadCourierChatOrderIds.remove(orderId)) {
            saveCourierWorkspace()
        }
    }

    fun markCustomerChatRead(orderId: String) {
        unreadCustomerChatOrderIds.remove(orderId)
    }

    private fun closeChatForOrder(orderId: String) {
        unreadCourierChatOrderIds.remove(orderId)
        unreadCustomerChatOrderIds.remove(orderId)
    }

    fun sendCustomerMessage(orderId: String, text: String) {
        val cleanText = text.trim()
        if (cleanText.isBlank()) return
        val order = courierOrderForOrder(orderId) ?: return
        if (order.status == CourierOrderStatus.Delivered) return
        val message = CourierChatMessage(
            id = "msg_${System.currentTimeMillis()}_customer",
            orderId = orderId,
            author = ChatAuthor.Customer,
            text = cleanText,
            timeLabel = currentChatTimeLabel(),
        )
        courierChatMessages.add(message)
        unreadCourierChatOrderIds.addIfAbsent(orderId)
        lastNotificationEventId = message.id
        saveCourierWorkspace()
        publishRemoteMessage(message)
    }

    fun sendCourierMessage(orderId: String, text: String) {
        val cleanText = text.trim()
        if (cleanText.isBlank()) return
        val order = courierOrderForOrder(orderId) ?: return
        if (order.status == CourierOrderStatus.Delivered) return
        val message = CourierChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            orderId = orderId,
            author = ChatAuthor.Courier,
            text = cleanText,
            timeLabel = currentChatTimeLabel(),
        )
        courierChatMessages.add(message)
        unreadCustomerChatOrderIds.addIfAbsent(orderId)
        if (activeCustomerId == order.customerUserId || currentUser?.id == order.customerUserId) {
            addNotification(
                title = when (language) {
                    AppLanguage.English -> "New courier message"
                    AppLanguage.Russian -> "Новое сообщение от курьера"
                    AppLanguage.Uzbek -> "Kuryerdan yangi xabar"
                },
                message = cleanText,
            )
        } else {
            lastNotificationEventId = message.id
        }
        saveCourierWorkspace()
        publishRemoteMessage(message)
    }

    fun courierOrderForOrder(orderId: String): CourierDeliveryOrder? =
        courierOrders.firstOrNull { it.id == orderId }

    fun reviewForOrder(orderId: String): OrderReview? =
        orderReviews.firstOrNull { it.orderId == orderId }

    fun rateOrder(
        orderId: String,
        courierRatingValue: Int,
        orderRatingValue: Int,
        comment: String,
    ): String? {
        val order = historyOrders.firstOrNull { it.id == orderId } ?: return "Order is not finished yet"
        if (order.status != OrderStatus.Delivered) return "Only delivered orders can be rated"
        markCustomerReceivedOrder(orderId)
        val review = OrderReview(
            orderId = orderId,
            courierRating = courierRatingValue.coerceIn(1, 5),
            orderRating = orderRatingValue.coerceIn(1, 5),
            comment = comment.trim(),
        )
        orderReviews.removeAll { it.orderId == orderId }
        orderReviews.add(0, review)
        courierReviews.removeAll { it.orderId == orderId }
        courierReviews.add(0, review)
        courierReviewRatings[orderId] = review.courierRating
        recalculateCourierRating()
        addNotification(
            title = when (language) {
                AppLanguage.English -> "Review saved"
                AppLanguage.Russian -> "Оценка сохранена"
                AppLanguage.Uzbek -> "Baho saqlandi"
            },
            message = when (language) {
                AppLanguage.English -> "Thanks for rating your order from ${order.restaurantName}."
                AppLanguage.Russian -> "Спасибо за оценку заказа из ${order.restaurantName}."
                AppLanguage.Uzbek -> "${order.restaurantName} buyurtmasini baholaganingiz uchun rahmat."
            },
        )
        saveCurrentCustomerOrders()
        saveCourierWorkspace()
        return null
    }

    private fun recalculateCourierRating() {
        (orderReviews + courierReviews)
            .filter { it.courierRating in 1..5 }
            .forEach { review -> courierReviewRatings[review.orderId] = review.courierRating }
        val ratings = courierReviewRatings.values.filter { it in 1..5 }
        if (ratings.isEmpty()) {
            courierRating = DefaultCourierTestRating
            return
        }

        val activeRatingWindow = ratings.takeLast(CourierRatingReviewBatchSize)
        courierRating = activeRatingWindow.average()
        if (courierRating < CourierBlockRatingThreshold) {
            courierBlockedUntilMillis = System.currentTimeMillis() + CourierBlockDurationMs
            courierIsOnline = false
        } else {
            clearExpiredCourierBlock()
        }
    }

    private fun isCourierCurrentlyBlocked(nowMillis: Long = System.currentTimeMillis()): Boolean =
        courierBlockedUntilMillis > nowMillis

    private fun clearExpiredCourierBlock(nowMillis: Long = System.currentTimeMillis()) {
        if (courierBlockedUntilMillis in 1..nowMillis) {
            courierBlockedUntilMillis = 0L
        }
    }

    fun updateThemeMode(mode: AppThemeMode) {
        themeMode = mode
        preferences?.edit()?.putString(ThemeModeKey, mode.name)?.apply()
    }

    fun markNotificationsRead() {
        notifications.indices.forEach { index ->
            val notification = notifications[index]
            if (!notification.read) {
                notifications[index] = notification.copy(read = true)
            }
        }
    }

    fun updateLanguage(nextLanguage: AppLanguage) {
        language = nextLanguage
        preferences?.edit()?.putString(LanguageKey, nextLanguage.name)?.apply()
    }

    private fun courierNoMoneyToWithdrawMessage(): String = when (language) {
        AppLanguage.English -> "No money to withdraw yet"
        AppLanguage.Russian -> "Пока нечего выводить"
        AppLanguage.Uzbek -> "Hozircha yechish uchun pul yo'q"
    }

    private fun courierMissingPayoutCardMessage(): String = when (language) {
        AppLanguage.English -> "Add a payout card first"
        AppLanguage.Russian -> "Сначала добавьте карту для вывода"
        AppLanguage.Uzbek -> "Avval pul yechish kartasini qo'shing"
    }

    private fun courierUnsupportedCardMessage(): String = when (language) {
        AppLanguage.English -> "This card type is not supported"
        AppLanguage.Russian -> "Этот тип карты не поддерживается"
        AppLanguage.Uzbek -> "Bu karta turi qo'llab-quvvatlanmaydi"
    }

    private fun courierCardHolderMessage(): String = when (language) {
        AppLanguage.English -> "Enter the card holder name"
        AppLanguage.Russian -> "Укажите имя владельца карты"
        AppLanguage.Uzbek -> "Karta egasi ismini kiriting"
    }

    private fun courierCardNumberMessage(): String = when (language) {
        AppLanguage.English -> "Card number must contain 16 digits"
        AppLanguage.Russian -> "Номер карты должен содержать 16 цифр"
        AppLanguage.Uzbek -> "Karta raqami 16 ta raqamdan iborat bo'lishi kerak"
    }

    private fun courierCardExpiryMessage(): String = when (language) {
        AppLanguage.English -> "Enter expiry as MM/YY"
        AppLanguage.Russian -> "Укажите срок карты как MM/YY"
        AppLanguage.Uzbek -> "Muddatni MM/YY ko'rinishida kiriting"
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        selectedPaymentMethod = method
        val selectedCard = selectedPaymentCard
        if (method == PaymentMethod.Cash || selectedCard?.matchesPaymentMethod(method) != true) {
            selectedPaymentCardId = null
        }
    }

    fun selectPaymentCard(cardId: String) {
        val card = savedPaymentCards.firstOrNull { it.id == cardId } ?: return
        selectedPaymentCardId = card.id
        selectedPaymentMethod = card.paymentMethod()
    }

    fun deletePaymentCard(cardId: String) {
        val index = savedPaymentCards.indexOfFirst { it.id == cardId }
        if (index < 0) return

        savedPaymentCards.removeAt(index)
        if (selectedPaymentCardId == cardId) {
            selectedPaymentCardId = null
            savedPaymentCards.firstOrNull { it.matchesPaymentMethod(selectedPaymentMethod) }?.let {
                selectedPaymentCardId = it.id
            }
        }
    }

    fun isFavorite(itemId: String): Boolean = itemId in favoriteItemIds

    fun toggleFavorite(item: MenuItem) {
        if (item.id in favoriteItemIds) {
            favoriteItemIds.remove(item.id)
        } else {
            favoriteItemIds.add(0, item.id)
            lastFavoriteAddedEventId = "favorite_${item.id}_${System.currentTimeMillis()}"
        }
        saveCurrentCustomerFavorites()
    }

    fun addToCart(item: MenuItem) {
        val index = cartItems.indexOfFirst { it.item.id == item.id }
        if (index >= 0) {
            val line = cartItems[index]
            cartItems[index] = line.copy(quantity = line.quantity + 1)
        } else {
            cartItems.add(CartLine(item, 1))
        }
        deliveryQuote = null
    }

    fun cartQuantity(itemId: String): Int =
        cartItems.firstOrNull { it.item.id == itemId }?.quantity ?: 0

    fun changeQuantity(itemId: String, delta: Int) {
        val index = cartItems.indexOfFirst { it.item.id == itemId }
        if (index < 0) return

        val line = cartItems[index]
        val next = line.quantity + delta
        if (next <= 0) {
            cartItems.removeAt(index)
        } else {
            cartItems[index] = line.copy(quantity = next)
        }
        deliveryQuote = null
        clearCouponIfCartEmpty()
    }

    fun removeFromCart(itemId: String) {
        val index = cartItems.indexOfFirst { it.item.id == itemId }
        if (index >= 0) {
            cartItems.removeAt(index)
        }
        deliveryQuote = null
        clearCouponIfCartEmpty()
    }

    fun dismissPendingWelcomeCoupon() {
        pendingWelcomeCouponCode = null
    }

    fun pruneExpiredCoupons(nowMillis: Long = System.currentTimeMillis()) {
        val expiredCodes = (coupons + adminCoupons)
            .filter { it.isExpired(nowMillis) }
            .map { it.code }
            .toSet()
        if (expiredCodes.isEmpty()) return

        val removedAdminCoupon = adminCoupons.any { it.code in expiredCodes }
        coupons.removeAll { it.code in expiredCodes }
        adminCoupons.removeAll { it.code in expiredCodes }
        expiredCodes.forEach { code -> couponExpiryJobs.remove(code)?.cancel() }
        if (appliedCouponCode?.let { it in expiredCodes } == true) appliedCouponCode = null
        if (pendingWelcomeCouponCode?.let { it in expiredCodes } == true) pendingWelcomeCouponCode = null
        if (removedAdminCoupon) saveAdminCoupons()
    }

    fun applyCouponCode(rawCode: String): String? {
        pruneExpiredCoupons()
        if (cartItems.isEmpty()) {
            val message = "Добавь товары в корзину, чтобы применить купон"
            return message
        }

        val normalizedCode = rawCode.normalizeCouponCode()
        if (normalizedCode.isBlank()) {
            val message = "Введи код купона"
            return message
        }

        val coupon = activeCoupons.firstOrNull { it.code.normalizeCouponCode() == normalizedCode }
        if (coupon == null) {
            val message = "Купон не найден или срок уже истек"
            return message
        }

        appliedCouponCode = coupon.code
        lastBackendError = null
        return null
    }

    fun clearAppliedCoupon() {
        appliedCouponCode = null
    }

    suspend fun login(email: String, password: String): String? {
        val result = withContext(Dispatchers.IO) {
            backend.login(AuthRequest(email = email, password = password))
        }
        return handleAuthResult(result)
    }

    suspend fun register(name: String, email: String, password: String): String? {
        val result = withContext(Dispatchers.IO) {
            backend.register(AuthRequest(name = name, email = email, password = password))
        }
        return when (result) {
            is BackendResult.Success -> {
                lastBackendError = null
                null
            }
            is BackendResult.Error -> {
                lastBackendError = result.message
                result.message
            }
        }
    }

    suspend fun requestRegistrationVerificationCode(email: String): EmailVerificationCodeResult {
        val result = withContext(Dispatchers.IO) { backend.requestRegistrationVerification(email) }
        return when (result) {
            is BackendResult.Success -> {
                lastBackendError = null
                EmailVerificationCodeResult(challenge = result.data)
            }
            is BackendResult.Error -> {
                lastBackendError = result.message
                EmailVerificationCodeResult(error = result.message)
            }
        }
    }

    suspend fun verifyRegistrationCode(email: String, code: String): String? {
        val result = withContext(Dispatchers.IO) {
            backend.verifyRegistrationCode(email = email, code = code)
        }
        return when (result) {
            is BackendResult.Success -> {
                lastBackendError = null
                null
            }
            is BackendResult.Error -> {
                lastBackendError = result.message
                result.message
            }
        }
    }

    suspend fun loginWithGoogle(profile: GoogleAccountProfile): String? {
        val name = profile.name.ifBlank { profile.email.substringBefore("@") }
        val result = withContext(Dispatchers.IO) {
            backend.googleSignIn(
                GoogleAuthRequest(
                    name = name,
                    email = profile.email,
                    googleSubject = profile.subject,
                ),
            )
        }
        return handleAuthResult(result)
    }

    suspend fun requestPasswordResetCode(email: String): PasswordResetCodeResult {
        val result = withContext(Dispatchers.IO) { backend.requestPasswordReset(email) }
        return when (result) {
            is BackendResult.Success -> {
                lastBackendError = null
                PasswordResetCodeResult(challenge = result.data)
            }
            is BackendResult.Error -> {
                lastBackendError = result.message
                PasswordResetCodeResult(error = result.message)
            }
        }
    }

    suspend fun resetPassword(email: String, code: String, newPassword: String): String? {
        val result = withContext(Dispatchers.IO) {
            backend.resetPassword(email = email, code = code, newPassword = newPassword)
        }
        return handleAuthResult(result)
    }

    suspend fun addPaymentCard(
        number: String,
        holderName: String,
        expiry: String,
        cvv: String,
    ): String? {
        val token = currentUser?.token ?: return "Сначала войди в аккаунт"
        val result = withContext(Dispatchers.IO) {
            backend.addPaymentCard(
                AddPaymentCardRequest(
                    token = token,
                    number = number,
                    holderName = holderName,
                    expiry = expiry,
                    cvv = cvv,
                ),
            )
        }
        return when (result) {
            is BackendResult.Success -> {
                savedPaymentCards.add(result.data)
                selectPaymentCard(result.data.id)
                lastBackendError = null
                null
            }
            is BackendResult.Error -> {
                lastBackendError = result.message
                result.message
            }
        }
    }

    suspend fun refreshDeliveryQuote() {
        if (cartItems.isEmpty()) {
            deliveryQuote = null
            return
        }

        val restaurantPoint = cartRestaurant.location
        val addressPoint = selectedAddress.point
        when (val result = withContext(Dispatchers.IO) { backend.deliveryQuote(restaurantPoint, addressPoint) }) {
            is BackendResult.Success -> {
                deliveryQuote = result.data
                lastBackendError = null
            }
            is BackendResult.Error -> lastBackendError = result.message
        }
    }

    suspend fun placeOrder(): Boolean {
        val token = currentUser?.token
        if (token == null) {
            lastBackendError = "Сначала войди в аккаунт"
            return false
        }
        if (cartItems.isEmpty()) {
            lastBackendError = "Корзина пустая"
            return false
        }
        if (selectedPaymentMethod.requiresSavedCard() && selectedPaymentCard == null) {
            lastBackendError = "Добавь карту или выбери другой способ оплаты"
            return false
        }

        isPlacingOrder = true
        val restaurant = cartRestaurant
        val usedCouponCode = appliedCoupon?.code
        val orderedCartItems = cartItems.toList()
        val request = CreateOrderRequest(
            token = token,
            restaurantId = restaurant.id,
            restaurantName = restaurant.name,
            restaurantPoint = restaurant.location,
            address = selectedAddress,
            itemsLabel = cartItems.joinToString { "${it.quantity}x ${it.item.title}" },
            total = total,
            paymentMethod = selectedPaymentMethod,
            paymentCard = selectedPaymentCard,
        )
        val result = withContext(Dispatchers.IO) { backend.createOrder(request) }
        isPlacingOrder = false

        return when (result) {
            is BackendResult.Success -> {
                val order = result.data
                val deliveryAddress = selectedAddress
                ongoingOrders.add(0, order)
                trackedOrderId = order.id
                lastPlacedOrder = order
                publishCourierOrder(order, restaurant, deliveryAddress)
                saveCurrentCustomerOrders()
                addNotification(
                    title = "Order created",
                    message = "Your order from ${restaurant.name} is waiting for a courier.",
                )
                recordOrderedItems(orderedCartItems)
                usedCouponCode?.let { code ->
                    withContext(Dispatchers.IO) { backend.redeemWelcomeCoupon(token, code) }
                    removeCoupon(code)
                }
                cartItems.clear()
                deliveryQuote = null
                lastBackendError = null
                if (!orderSyncBackend.enabled) {
                    scheduleOrderCompletion(order.id)
                }
                true
            }
            is BackendResult.Error -> {
                lastBackendError = result.message
                false
            }
        }
    }

    fun track(orderId: String) {
        trackedOrderId = orderId
    }

    fun restaurantForOrder(order: OrderSummary): Restaurant? =
        restaurants.firstOrNull { it.id == order.restaurantId }
            ?: restaurants.firstOrNull { it.name == order.restaurantName }

    fun restaurantForMenuItem(item: MenuItem): Restaurant? =
        restaurants.firstOrNull { it.id == item.restaurantId }

    fun reorder(order: OrderSummary) {
        val restaurant = restaurantForOrder(order) ?: return
        restaurant.menu.take(2).forEach(::addToCart)
    }

    fun canCancelOrder(order: OrderSummary, nowMillis: Long = System.currentTimeMillis()): Boolean =
        order.status != OrderStatus.Delivered &&
            order.status != OrderStatus.Cancelled &&
            nowMillis - order.createdAtMillis <= CancellationWindowMs

    fun cancellationTimeLeftMillis(order: OrderSummary, nowMillis: Long = System.currentTimeMillis()): Long =
        (CancellationWindowMs - (nowMillis - order.createdAtMillis)).coerceAtLeast(0L)

    fun cancelOrder(orderId: String): String? {
        val index = ongoingOrders.indexOfFirst { it.id == orderId }
        if (index < 0) {
            val message = "Заказ уже закрыт"
            lastBackendError = message
            return message
        }

        val order = ongoingOrders[index]
        if (!canCancelOrder(order)) {
            val message = "Отмена доступна только первые 5 минут после оформления"
            lastBackendError = message
            return message
        }

        orderCompletionJobs.remove(orderId)?.cancel()
        orderReceiptRefundJobs.remove(orderId)?.cancel()
        val cancelledOrder = order.copy(
            status = OrderStatus.Cancelled,
            eta = language.cancelledEtaLabel(),
            refundedAmount = order.total,
        )
        ongoingOrders.removeAt(index)
        historyOrders.add(0, cancelledOrder)
        courierOrders.removeAll { it.id == orderId }
        refundBalance += order.total
        if (trackedOrderId == orderId) {
            trackedOrderId = cancelledOrder.id
        }
        if (lastPlacedOrder?.id == orderId) {
            lastPlacedOrder = cancelledOrder
        }
        lastBackendError = null
        saveCurrentCustomerOrders()
        saveCourierWorkspace()
        return null
    }

    private suspend fun handleAuthResult(result: BackendResult<UserSession>): String? =
        when (result) {
            is BackendResult.Success -> {
                currentUser = result.data
                activeCustomerId = result.data.id
                profileDetails = UserProfileDetails.fromSession(
                    session = result.data,
                    phone = "",
                    isPhoneVerified = false,
                    bio = profileDetails.bio,
                    photoUri = profileDetails.photoUri,
                )
                preferences.readCustomerProfile(result.data.id)?.let { savedProfile ->
                    profileDetails = savedProfile
                    currentUser = currentUser?.copy(
                        name = savedProfile.fullName,
                        email = savedProfile.email.trim(),
                    )
                }
                preferences.writeSavedCustomerSession(result.data)
                resetCustomerState()
                loadCustomerAddresses(result.data.id)
                loadCustomerOrders(result.data.id)
                loadCustomerFavorites(result.data.token, result.data.id)
                notifyCustomerAboutAdminCoupons(result.data.id)
                result.data.welcomeCouponExpiresAtMillis?.let { expiresAtMillis ->
                    grantWelcomeCoupon(
                        expiresAtMillis = expiresAtMillis,
                        showOffer = result.data.showsWelcomeCouponOffer,
                    )
                }
                refreshPaymentCards(result.data.token)
                lastBackendError = null
                null
            }
            is BackendResult.Error -> {
                lastBackendError = result.message
                result.message
            }
        }

    private suspend fun refreshPaymentCards(token: String) {
        when (val result = withContext(Dispatchers.IO) { backend.paymentCards(token) }) {
            is BackendResult.Success -> {
                savedPaymentCards.clear()
                selectedPaymentCardId = null
                savedPaymentCards.addAll(result.data)
                if (selectedPaymentCardId == null) {
                    savedPaymentCards.firstOrNull()?.let { selectPaymentCard(it.id) }
                }
            }
            is BackendResult.Error -> lastBackendError = result.message
        }
    }

    private fun resetCustomerState() {
        orderCompletionJobs.values.forEach { it.cancel() }
        orderCompletionJobs.clear()
        orderReceiptRefundJobs.values.forEach { it.cancel() }
        orderReceiptRefundJobs.clear()
        cartItems.clear()
        favoriteItemIds.clear()
        ongoingOrders.clear()
        historyOrders.clear()
        savedPaymentCards.clear()
        coupons.clear()
        notifications.clear()
        notifications.add(
            AppNotification(
                id = "version_1_0",
                title = "Version 1.0 is ready",
                message = "Foodly admin, courier chat and delivery tracking are available now.",
                read = true,
            ),
        )
        orderReviews.clear()
        couponExpiryJobs.values.forEach { it.cancel() }
        couponExpiryJobs.clear()
        selectedPaymentMethod = PaymentMethod.Cash
        selectedPaymentCardId = null
        trackedOrderId = null
        lastPlacedOrder = null
        pendingWelcomeCouponCode = null
        appliedCouponCode = null
        deliveryQuote = null
        refundBalance = 0
        lastBackendError = null
        isPlacingOrder = false
        lastFavoriteAddedEventId = null
        lastNotificationEventId = null
        lastDeliveredOrderEventId = null
    }

    private fun scheduleOrderCompletion(orderId: String) {
        orderCompletionJobs[orderId]?.cancel()
        orderCompletionJobs[orderId] = orderScope.launch {
            delay(OrderStatusStepDelayMs)
            updateOngoingOrder(orderId) { order ->
                order.copy(
                    status = OrderStatus.OnTheWay,
                    eta = "Курьер уже в пути",
                )
            }

            delay(OrderStatusStepDelayMs)
            completeOrder(orderId)
        }
    }

    private fun reconcileDeliveredReceiptRefunds() {
        historyOrders
            .filter { it.status == OrderStatus.Delivered }
            .forEach(::scheduleReceiptRefundIfNeeded)
    }

    private fun scheduleReceiptRefundIfNeeded(order: OrderSummary) {
        if (order.status != OrderStatus.Delivered) return
        if (order.customerReceivedAtMillis != null || order.refundedAmount > 0) return

        val deliveredAtMillis = order.deliveredAtMillis ?: order.createdAtMillis
        val delayMillis = deliveredAtMillis + CustomerPickupRefundWindowMs - System.currentTimeMillis()
        orderReceiptRefundJobs.remove(order.id)?.cancel()
        if (delayMillis <= 0L) {
            refundUnclaimedDeliveredOrder(order.id)
            return
        }

        orderReceiptRefundJobs[order.id] = orderScope.launch {
            delay(delayMillis)
            refundUnclaimedDeliveredOrder(order.id)
        }
    }

    private fun refundUnclaimedDeliveredOrder(orderId: String) {
        val index = historyOrders.indexOfFirst { it.id == orderId }
        if (index < 0) return

        val order = historyOrders[index]
        if (
            order.status != OrderStatus.Delivered ||
            order.customerReceivedAtMillis != null ||
            order.refundedAmount > 0
        ) {
            orderReceiptRefundJobs.remove(orderId)
            return
        }

        val refundedOrder = order.copy(refundedAmount = order.total)
        historyOrders[index] = refundedOrder
        refundBalance += order.total
        if (lastPlacedOrder?.id == orderId) {
            lastPlacedOrder = refundedOrder
        }
        if (trackedOrderId == orderId) {
            trackedOrderId = refundedOrder.id
        }
        addNotification(
            title = "Refund created",
            message = "The order was not confirmed within 2 hours, so the money was returned to the customer account.",
        )
        orderReceiptRefundJobs.remove(orderId)
        saveCurrentCustomerOrders()
    }

    private fun markCustomerReceivedOrder(orderId: String) {
        val index = historyOrders.indexOfFirst { it.id == orderId }
        if (index < 0) return
        val order = historyOrders[index]
        if (order.status != OrderStatus.Delivered || order.customerReceivedAtMillis != null) return

        val receivedOrder = order.copy(customerReceivedAtMillis = System.currentTimeMillis())
        historyOrders[index] = receivedOrder
        if (lastPlacedOrder?.id == orderId) {
            lastPlacedOrder = receivedOrder
        }
        orderReceiptRefundJobs.remove(orderId)?.cancel()
        saveCurrentCustomerOrders()
    }

    private fun OrderSummary.withDeliveryTimestamp(status: OrderStatus): OrderSummary =
        if (status == OrderStatus.Delivered && deliveredAtMillis == null) {
            copy(deliveredAtMillis = System.currentTimeMillis())
        } else {
            this
        }

    private fun grantWelcomeCoupon(
        expiresAtMillis: Long,
        showOffer: Boolean,
    ) {
        val coupon = DiscountCoupon(
            code = WELCOME_COUPON_CODE,
            title = "Скидка для нового пользователя",
            description = "Минус $WELCOME_COUPON_PERCENT% на первый заказ в Foodly",
            discountPercent = WELCOME_COUPON_PERCENT,
            expiresAtMillis = expiresAtMillis,
        )
        if (coupon.isExpired()) return

        coupons.removeAll { it.code.equals(coupon.code, ignoreCase = true) }
        coupons.add(0, coupon)
        if (showOffer) {
            pendingWelcomeCouponCode = coupon.code
        }
        scheduleCouponExpiry(coupon)
    }

    private fun scheduleCouponExpiry(coupon: DiscountCoupon) {
        couponExpiryJobs[coupon.code]?.cancel()
        val delayMillis = coupon.expiresAtMillis - System.currentTimeMillis()
        if (delayMillis <= 0L) {
            removeCoupon(coupon.code)
            return
        }
        couponExpiryJobs[coupon.code] = orderScope.launch {
            delay(delayMillis)
            removeCoupon(coupon.code)
        }
    }

    private fun removeCoupon(code: String) {
        val normalizedCode = code.normalizeCouponCode()
        coupons.removeAll { it.code.normalizeCouponCode() == normalizedCode }
        couponExpiryJobs.remove(code)?.cancel()
        if (appliedCouponCode?.normalizeCouponCode() == normalizedCode) {
            appliedCouponCode = null
        }
        if (pendingWelcomeCouponCode?.normalizeCouponCode() == normalizedCode) {
            pendingWelcomeCouponCode = null
        }
    }

    private fun clearCouponIfCartEmpty() {
        if (cartItems.isEmpty()) {
            appliedCouponCode = null
        }
    }

    private fun addNotification(
        title: String,
        message: String,
        playGenericSound: Boolean = true,
    ) {
        val notification = AppNotification(
            id = "note_${System.currentTimeMillis()}_${notifications.size}",
            title = title,
            message = message,
        )
        notifications.add(0, notification)
        if (playGenericSound) {
            lastNotificationEventId = notification.id
        }
    }

    private fun recordOrderedItems(lines: List<CartLine>) {
        lines.forEach { line ->
            val itemId = line.item.id.baseMenuItemId()
            dishOrderCounts[itemId] = (dishOrderCounts[itemId] ?: 0) + line.quantity
        }
        saveDishOrderCounts()
    }

    private fun publishCourierOrder(
        order: OrderSummary,
        restaurant: Restaurant,
        address: DeliveryAddress,
    ) {
        val courierOrder = CourierDeliveryOrder(
            id = order.id,
            customerName = profileDetails.fullName,
            customerPhone = profileDetails.phone,
            restaurantId = restaurant.id,
            restaurantName = restaurant.name,
            restaurantAddress = "${restaurant.name}, Tashkent",
            customerAddress = address,
            itemsLabel = order.itemsLabel,
            total = order.total,
            earning = (deliveryFee + serviceFee + 6).coerceAtLeast(6),
            status = CourierOrderStatus.Available,
            restaurantPoint = restaurant.location,
            createdAtMillis = order.createdAtMillis,
            customerUserId = activeCustomerId ?: currentUser?.id,
        )
        courierOrders.removeAll { it.id == order.id }
        courierOrders.add(0, courierOrder)
        saveCourierWorkspace()
        publishRemoteOrder(courierOrder)
    }

    private fun saveCourierWorkspace() {
        preferences.writeCourierWorkspace(
            orders = courierOrders,
            reviews = courierReviews,
            messages = courierChatMessages.filterNot { it.isAutomaticChatMessage() },
            unreadCourierChatOrderIds = unreadCourierChatOrderIds,
            walletBalance = courierWalletBalance,
            rating = courierRating,
            blockedUntilMillis = courierBlockedUntilMillis,
            lastWithdrawalAtMillis = courierLastWithdrawalAtMillis,
            reviewRatings = courierReviewRatings,
            payoutCard = courierPayoutCard,
            isOnline = courierIsOnline,
            selectedOrderId = selectedCourierOrderId,
        )
    }

    private fun persistCustomerOrderUpdate(courierOrder: CourierDeliveryOrder) {
        val userId = courierOrder.customerUserId ?: return
        if (activeCustomerId == userId && currentUser != null) {
            applyCourierStatusToCustomerOrder(courierOrder, persist = true)
            return
        }

        preferences.updateSavedCustomerOrder(
            userId = userId,
            orderId = courierOrder.id,
            status = courierOrder.status.toCustomerOrderStatus(),
            eta = courierOrder.status.customerEta(),
        )
    }

    private fun applyCourierStatusToCustomerOrder(
        courierOrder: CourierDeliveryOrder,
        persist: Boolean,
    ) {
        val status = courierOrder.status.toCustomerOrderStatus()
        val eta = courierOrder.status.customerEta()
        val index = ongoingOrders.indexOfFirst { it.id == courierOrder.id }
        if (index >= 0) {
            val updatedOrder = ongoingOrders[index]
                .copy(status = status, eta = eta)
                .withDeliveryTimestamp(status)
            if (status == OrderStatus.Delivered || status == OrderStatus.Cancelled) {
                ongoingOrders.removeAt(index)
                historyOrders.removeAll { it.id == updatedOrder.id }
                historyOrders.add(0, updatedOrder)
                scheduleReceiptRefundIfNeeded(updatedOrder)
            } else {
                ongoingOrders[index] = updatedOrder
            }
            if (trackedOrderId == courierOrder.id) trackedOrderId = updatedOrder.id
            if (lastPlacedOrder?.id == courierOrder.id) lastPlacedOrder = updatedOrder
            if (persist) saveCurrentCustomerOrders()
            return
        }

        val historyIndex = historyOrders.indexOfFirst { it.id == courierOrder.id }
        if (historyIndex >= 0) {
            val updatedOrder = historyOrders[historyIndex]
                .copy(status = status, eta = eta)
                .withDeliveryTimestamp(status)
            historyOrders[historyIndex] = updatedOrder
            scheduleReceiptRefundIfNeeded(updatedOrder)
            if (trackedOrderId == courierOrder.id) trackedOrderId = updatedOrder.id
            if (lastPlacedOrder?.id == courierOrder.id) lastPlacedOrder = updatedOrder
            if (persist) saveCurrentCustomerOrders()
        }
    }

    private fun startOrderSync() {
        orderSyncJob?.cancel()
        orderSyncJob = orderScope.launch {
            while (true) {
                syncRemoteOrders()
                delay(OrderSyncPollDelayMs)
            }
        }
    }

    private fun syncRemoteOrdersSoon() {
        if (!orderSyncBackend.enabled) return
        orderScope.launch { syncRemoteOrders() }
    }

    private suspend fun syncRemoteOrders() {
        when (val result = orderSyncBackend.fetchSnapshot()) {
            is BackendResult.Success -> applyRemoteSnapshot(result.data)
            is BackendResult.Error -> handleOrderSyncError(result.message)
        }
    }

    private fun applyRemoteSnapshot(snapshot: OrderSyncSnapshot) {
        snapshot.orders.forEach(::applyRemoteCourierOrder)
        courierOrders.sortWith(
            compareBy<CourierDeliveryOrder> { it.status == CourierOrderStatus.Delivered }
                .thenByDescending { it.createdAtMillis },
        )

        val existingMessageIds = courierChatMessages.map { it.id }.toHashSet()
        snapshot.messages.filterNot { it.isAutomaticChatMessage() }.forEach { message ->
            if (existingMessageIds.add(message.id)) {
                courierChatMessages.add(message)
                when (message.author) {
                    ChatAuthor.Customer -> unreadCourierChatOrderIds.addIfAbsent(message.orderId)
                    ChatAuthor.Courier -> unreadCustomerChatOrderIds.addIfAbsent(message.orderId)
                }
            }
        }
        saveCourierWorkspace()
    }

    private fun applyRemoteCourierOrder(remoteOrder: CourierDeliveryOrder) {
        val index = courierOrders.indexOfFirst { it.id == remoteOrder.id }
        val previousStatus = courierOrders.getOrNull(index)?.status
        if (index >= 0) {
            courierOrders[index] = remoteOrder
        } else {
            courierOrders.add(remoteOrder)
        }

        if (currentCourier?.id == remoteOrder.courierId && remoteOrder.status != CourierOrderStatus.Delivered) {
            selectedCourierOrderId = remoteOrder.id
        }
        if (remoteOrder.status == CourierOrderStatus.Delivered) {
            closeChatForOrder(remoteOrder.id)
        }
        applyRemoteCourierStatusToCustomer(remoteOrder, previousStatus)
        persistCustomerOrderUpdate(remoteOrder)
    }

    private fun applyRemoteCourierStatusToCustomer(
        order: CourierDeliveryOrder,
        previousStatus: CourierOrderStatus?,
    ) {
        val hasLocalOrder = ongoingOrders.any { it.id == order.id } || historyOrders.any { it.id == order.id }
        if (!hasLocalOrder) return

        when (order.status) {
            CourierOrderStatus.Available -> updateCustomerOrderStatus(
                orderId = order.id,
                status = OrderStatus.Preparing,
                eta = "Waiting for courier",
            )
            CourierOrderStatus.Accepted -> updateCustomerOrderStatus(
                orderId = order.id,
                status = OrderStatus.Preparing,
                eta = "Courier accepted your order",
            )
            CourierOrderStatus.ArrivedAtRestaurant -> updateCustomerOrderStatus(
                orderId = order.id,
                status = OrderStatus.Preparing,
                eta = "Courier arrived at restaurant",
            )
            CourierOrderStatus.PickedUp -> updateCustomerOrderStatus(
                orderId = order.id,
                status = OrderStatus.Preparing,
                eta = "Courier picked up your order",
            )
            CourierOrderStatus.OnTheWay -> updateCustomerOrderStatus(
                orderId = order.id,
                status = OrderStatus.OnTheWay,
                eta = "Courier is on the way",
            )
            CourierOrderStatus.Delivered -> updateCustomerOrderStatus(
                orderId = order.id,
                status = OrderStatus.Delivered,
                eta = "Delivered just now",
            )
        }

        if (previousStatus == order.status) return
        when (order.status) {
            CourierOrderStatus.Accepted -> addNotification(
                title = "Courier accepted",
                message = "Courier accepted your order and is heading to the restaurant.",
            )
            CourierOrderStatus.PickedUp -> addNotification(
                title = "Order picked up",
                message = "The courier picked up your food and will start delivery soon.",
            )
            CourierOrderStatus.Delivered -> addNotification(
                title = "Order delivered",
                message = "Your food was delivered to the selected address.",
                playGenericSound = false,
            )
            CourierOrderStatus.Available,
            CourierOrderStatus.ArrivedAtRestaurant,
            CourierOrderStatus.OnTheWay -> Unit
        }
        if (order.status == CourierOrderStatus.Delivered) {
            lastDeliveredOrderEventId = "remote_delivered_${order.id}_${System.currentTimeMillis()}"
        }
    }

    private fun publishRemoteOrder(order: CourierDeliveryOrder) {
        if (!orderSyncBackend.enabled) return
        orderScope.launch {
            when (val result = orderSyncBackend.upsertOrder(order)) {
                is BackendResult.Success -> Unit
                is BackendResult.Error -> handleOrderSyncError(result.message)
            }
        }
    }

    private fun publishRemoteOrderStatus(order: CourierDeliveryOrder) {
        if (!orderSyncBackend.enabled) return
        orderScope.launch {
            val result = orderSyncBackend.updateOrderStatus(
                orderId = order.id,
                status = order.status,
                courierId = order.courierId,
                courierPoint = order.courierPoint,
            )
            if (result is BackendResult.Error) {
                when (val fallback = orderSyncBackend.upsertOrder(order)) {
                    is BackendResult.Success -> Unit
                    is BackendResult.Error -> handleOrderSyncError(fallback.message)
                }
            }
        }
    }

    private fun publishRemoteMessage(message: CourierChatMessage) {
        if (!orderSyncBackend.enabled) return
        orderScope.launch {
            when (val result = orderSyncBackend.addChatMessage(message)) {
                is BackendResult.Success -> Unit
                is BackendResult.Error -> handleOrderSyncError(result.message)
            }
        }
    }

    private fun handleOrderSyncError(message: String) {
        if (currentUser != null || currentCourier != null) {
            lastBackendError = message
        }
    }

    private fun updateCustomerOrderEta(orderId: String, eta: String) {
        updateOngoingOrder(orderId) { order -> order.copy(eta = eta) }
        saveCurrentCustomerOrders()
    }

    private fun updateCustomerOrderStatus(
        orderId: String,
        status: OrderStatus,
        eta: String,
    ) {
        val index = ongoingOrders.indexOfFirst { it.id == orderId }
        if (index < 0) {
            updateOngoingOrder(orderId) { order -> order.copy(status = status, eta = eta) }
            return
        }

        val updatedOrder = ongoingOrders[index]
            .copy(status = status, eta = eta)
            .withDeliveryTimestamp(status)
        if (status == OrderStatus.Delivered || status == OrderStatus.Cancelled) {
            ongoingOrders.removeAt(index)
            historyOrders.add(0, updatedOrder)
            scheduleReceiptRefundIfNeeded(updatedOrder)
        } else {
            ongoingOrders[index] = updatedOrder
        }
        if (lastPlacedOrder?.id == orderId) {
            lastPlacedOrder = updatedOrder
        }
        if (trackedOrderId == orderId) {
            trackedOrderId = updatedOrder.id
        }
        saveCurrentCustomerOrders()
    }

    private fun updateOngoingOrder(
        orderId: String,
        update: (OrderSummary) -> OrderSummary,
    ) {
        val index = ongoingOrders.indexOfFirst { it.id == orderId }
        if (index < 0) return

        val updatedOrder = update(ongoingOrders[index])
        ongoingOrders[index] = updatedOrder
        if (lastPlacedOrder?.id == orderId) {
            lastPlacedOrder = updatedOrder
        }
        saveCurrentCustomerOrders()
    }

    private fun completeOrder(orderId: String) {
        val index = ongoingOrders.indexOfFirst { it.id == orderId }
        if (index < 0) {
            orderCompletionJobs.remove(orderId)
            return
        }

        val deliveredOrder = ongoingOrders.removeAt(index).copy(
            status = OrderStatus.Delivered,
            eta = "Доставлен только что",
            deliveredAtMillis = System.currentTimeMillis(),
        )
        historyOrders.add(0, deliveredOrder)
        scheduleReceiptRefundIfNeeded(deliveredOrder)
        if (lastPlacedOrder?.id == orderId) {
            lastPlacedOrder = deliveredOrder
        }
        addNotification(
            title = "Order delivered",
            message = "Your food was delivered to the selected address.",
            playGenericSound = false,
        )
        lastDeliveredOrderEventId = "local_delivered_${deliveredOrder.id}_${System.currentTimeMillis()}"
        orderCompletionJobs.remove(orderId)
        saveCurrentCustomerOrders()
    }

    private companion object {
        const val OrderStatusStepDelayMs = 5 * 60 * 1_000L
        const val OrderSyncPollDelayMs = 3_000L
        const val CancellationWindowMs = 5 * 60 * 1_000L
        const val CustomerPickupRefundWindowMs = 2 * 60 * 60 * 1_000L
        const val CourierRatingReviewBatchSize = 50
        const val CourierReducedOrdersRatingThreshold = 4.5
        const val CourierBlockRatingThreshold = 4.0
        const val CourierBlockDurationMs = 7 * 24 * 60 * 60 * 1_000L
        const val DefaultCourierTestRating = 4.8
        const val DemoCourierEmail = "courier@foodly.app"
        const val ThemeModeKey = "theme_mode"
        const val LanguageKey = "language"
        const val AdminUsername = "admin"
        const val AdminPassword = "123456789"
        const val DefaultAdminRestaurantImage = ""
    }
}

private data class SavedCustomerSession(
    val id: String,
    val name: String,
    val email: String,
)

private data class SavedCustomerAddresses(
    val addresses: List<DeliveryAddress>,
    val selectedIndex: Int,
)

private data class SavedCustomerOrders(
    val ongoingOrders: List<OrderSummary>,
    val historyOrders: List<OrderSummary>,
    val trackedOrderId: String?,
    val lastPlacedOrderId: String?,
    val refundBalance: Int,
    val orderReviews: List<OrderReview>,
)

private data class SavedCourierWorkspace(
    val orders: List<CourierDeliveryOrder>,
    val reviews: List<OrderReview>,
    val messages: List<CourierChatMessage>,
    val unreadCourierChatOrderIds: List<String>,
    val walletBalance: Int,
    val rating: Double,
    val blockedUntilMillis: Long,
    val lastWithdrawalAtMillis: Long,
    val reviewRatings: Map<String, Int>,
    val payoutCard: PaymentCard?,
    val isOnline: Boolean,
    val selectedOrderId: String?,
)

private fun CourierOrderStatus.toCustomerOrderStatus(): OrderStatus = when (this) {
    CourierOrderStatus.Available,
    CourierOrderStatus.Accepted,
    CourierOrderStatus.ArrivedAtRestaurant,
    CourierOrderStatus.PickedUp -> OrderStatus.Preparing
    CourierOrderStatus.OnTheWay -> OrderStatus.OnTheWay
    CourierOrderStatus.Delivered -> OrderStatus.Delivered
}

private fun CourierOrderStatus.defaultCourierPoint(order: CourierDeliveryOrder): GeoPoint? = when (this) {
    CourierOrderStatus.Available -> order.courierPoint
    CourierOrderStatus.Accepted,
    CourierOrderStatus.ArrivedAtRestaurant,
    CourierOrderStatus.PickedUp,
    CourierOrderStatus.OnTheWay -> order.courierPoint
    CourierOrderStatus.Delivered -> order.customerAddress.point
}

private fun GeoPoint.isCloseTo(other: GeoPoint): Boolean =
    kotlin.math.abs(latitude - other.latitude) < 0.00003 &&
        kotlin.math.abs(longitude - other.longitude) < 0.00003

private fun CourierOrderStatus.customerEta(): String = when (this) {
    CourierOrderStatus.Available -> "Waiting for courier"
    CourierOrderStatus.Accepted -> "Courier accepted your order"
    CourierOrderStatus.ArrivedAtRestaurant -> "Courier arrived at restaurant"
    CourierOrderStatus.PickedUp -> "Courier picked up your order"
    CourierOrderStatus.OnTheWay -> "Courier is on the way"
    CourierOrderStatus.Delivered -> "Delivered just now"
}

private fun defaultCourierProfileDetails(): CourierProfileDetails =
    CourierProfileDetails(
        id = "courier_demo",
        name = "Robert Fox",
        email = "courier@foodly.app",
        phone = "+998 90 700 12 34",
        vehicle = "Bike",
        photoUrl = DEFAULT_PROFILE_PHOTO_URL,
    )

private fun defaultCustomerAddresses(): List<DeliveryAddress> =
    listOf(
        SampleData.defaultAddress.copy(
            label = "HOME",
            title = "Улица Ислама Каримова",
            subtitle = "Tashkent City, блок B",
        ),
        DeliveryAddress(
            label = "WORK",
            title = "Улица Амира Темура",
            subtitle = "Мирабад, Ц-1",
            point = GeoPoint(41.307455, 69.279728),
        ),
    )

private fun SharedPreferences?.readSavedCustomerSession(): SavedCustomerSession? {
    val raw = this?.getString(CustomerSessionKey, null) ?: return null
    return runCatching {
        val json = JSONObject(raw)
        SavedCustomerSession(
            id = json.optString("id"),
            name = json.optString("name"),
            email = json.optString("email"),
        )
    }.getOrNull()?.takeIf { it.id.isNotBlank() && it.email.contains("@") }
}

private fun SharedPreferences?.writeSavedCustomerSession(session: UserSession) {
    this?.edit()
        ?.putString(
            CustomerSessionKey,
            JSONObject()
                .put("id", session.id)
                .put("name", session.name)
                .put("email", session.email)
                .toString(),
        )
        ?.apply()
}

private fun SharedPreferences?.clearSavedCustomerSession() {
    this?.edit()?.remove(CustomerSessionKey)?.apply()
}

private fun SharedPreferences?.readCustomerAddresses(userId: String): SavedCustomerAddresses? {
    val raw = this?.getString(customerAddressesKey(userId), null) ?: return null
    return runCatching {
        val json = JSONObject(raw)
        val addressesJson = json.optJSONArray("addresses") ?: JSONArray()
        val addresses = buildList {
            for (index in 0 until addressesJson.length()) {
                addressesJson.optJSONObject(index)?.toDeliveryAddressOrNull()?.let(::add)
            }
        }
        if (addresses.isEmpty()) return@runCatching null
        SavedCustomerAddresses(
            addresses = addresses,
            selectedIndex = json.optInt("selectedIndex", 0).coerceIn(0, addresses.lastIndex),
        )
    }.getOrNull()
}

private fun SharedPreferences?.writeCustomerAddresses(
    userId: String,
    addresses: List<DeliveryAddress>,
    selectedAddress: DeliveryAddress,
) {
    if (addresses.isEmpty()) return
    val selectedIndex = addresses.indexOf(selectedAddress).takeIf { it >= 0 } ?: 0
    val jsonAddresses = JSONArray().apply {
        addresses.forEach { address -> put(address.toPreferenceJson()) }
    }
    this?.edit()
        ?.putString(
            customerAddressesKey(userId),
            JSONObject()
                .put("selectedIndex", selectedIndex)
                .put("addresses", jsonAddresses)
                .toString(),
        )
        ?.apply()
}

private fun customerAddressesKey(userId: String): String =
    "$CustomerAddressesPrefix$userId"

private fun DeliveryAddress.toPreferenceJson(): JSONObject =
    JSONObject()
        .put("label", label)
        .put("title", title)
        .put("subtitle", subtitle)
        .put(
            "point",
            JSONObject()
                .put("latitude", point.latitude)
                .put("longitude", point.longitude),
        )

private fun JSONObject.toDeliveryAddressOrNull(): DeliveryAddress? =
    runCatching {
        val pointJson = getJSONObject("point")
        DeliveryAddress(
            label = optString("label"),
            title = optString("title"),
            subtitle = optString("subtitle"),
            point = GeoPoint(
                latitude = pointJson.getDouble("latitude"),
                longitude = pointJson.getDouble("longitude"),
            ),
        )
    }.getOrNull()

private fun SharedPreferences?.readCustomerOrders(userId: String): SavedCustomerOrders? {
    val raw = this?.getString(customerOrdersKey(userId), null) ?: return null
    return runCatching {
        val json = JSONObject(raw)
        SavedCustomerOrders(
            ongoingOrders = json.optJSONArray("ongoingOrders").toOrderSummaries(),
            historyOrders = json.optJSONArray("historyOrders").toOrderSummaries(),
            trackedOrderId = json.nullableString("trackedOrderId"),
            lastPlacedOrderId = json.nullableString("lastPlacedOrderId"),
            refundBalance = json.optInt("refundBalance", 0),
            orderReviews = json.optJSONArray("orderReviews").toOrderReviews(),
        )
    }.getOrNull()
}

private fun SharedPreferences?.writeCustomerOrders(
    userId: String,
    ongoingOrders: List<OrderSummary>,
    historyOrders: List<OrderSummary>,
    trackedOrderId: String?,
    lastPlacedOrderId: String?,
    refundBalance: Int,
    orderReviews: List<OrderReview>,
) {
    this?.edit()
        ?.putString(
            customerOrdersKey(userId),
            JSONObject()
                .put("ongoingOrders", ongoingOrders.toJsonArray { it.toPreferenceJson() })
                .put("historyOrders", historyOrders.toJsonArray { it.toPreferenceJson() })
                .putNullable("trackedOrderId", trackedOrderId)
                .putNullable("lastPlacedOrderId", lastPlacedOrderId)
                .put("refundBalance", refundBalance)
                .put("orderReviews", orderReviews.toJsonArray { it.toPreferenceJson() })
                .toString(),
        )
        ?.apply()
}

private fun SharedPreferences?.updateSavedCustomerOrder(
    userId: String,
    orderId: String,
    status: OrderStatus,
    eta: String,
) {
    val saved = readCustomerOrders(userId) ?: return
    val ongoing = saved.ongoingOrders.toMutableList()
    val history = saved.historyOrders.toMutableList()
    val ongoingIndex = ongoing.indexOfFirst { it.id == orderId }
    if (ongoingIndex >= 0) {
        val statusUpdatedOrder = ongoing[ongoingIndex].copy(status = status, eta = eta)
        val updatedOrder = if (status == OrderStatus.Delivered && statusUpdatedOrder.deliveredAtMillis == null) {
            statusUpdatedOrder.copy(deliveredAtMillis = System.currentTimeMillis())
        } else {
            statusUpdatedOrder
        }
        if (status == OrderStatus.Delivered || status == OrderStatus.Cancelled) {
            ongoing.removeAt(ongoingIndex)
            history.removeAll { it.id == orderId }
            history.add(0, updatedOrder)
        } else {
            ongoing[ongoingIndex] = updatedOrder
        }
    } else {
        val historyIndex = history.indexOfFirst { it.id == orderId }
        if (historyIndex < 0) return
        val statusUpdatedOrder = history[historyIndex].copy(status = status, eta = eta)
        history[historyIndex] = if (status == OrderStatus.Delivered && statusUpdatedOrder.deliveredAtMillis == null) {
            statusUpdatedOrder.copy(deliveredAtMillis = System.currentTimeMillis())
        } else {
            statusUpdatedOrder
        }
    }

    writeCustomerOrders(
        userId = userId,
        ongoingOrders = ongoing,
        historyOrders = history,
        trackedOrderId = saved.trackedOrderId,
        lastPlacedOrderId = saved.lastPlacedOrderId,
        refundBalance = saved.refundBalance,
        orderReviews = saved.orderReviews,
    )
}

private fun customerOrdersKey(userId: String): String =
    "$CustomerOrdersPrefix$userId"

private fun SharedPreferences?.readCustomerFavorites(userId: String): List<String> {
    val raw = this?.getString(customerFavoritesKey(userId), null) ?: return emptyList()
    return runCatching {
        JSONArray(raw).toStringList()
    }.getOrDefault(emptyList())
}

private fun SharedPreferences?.writeCustomerFavorites(
    userId: String,
    itemIds: List<String>,
) {
    this?.edit()
        ?.putString(customerFavoritesKey(userId), itemIds.distinct().toStringJsonArray().toString())
        ?.apply()
}

private fun customerFavoritesKey(userId: String): String =
    "$CustomerFavoritesPrefix$userId"

private fun SharedPreferences?.readNotifiedAdminCouponCodes(userId: String): Set<String> {
    val raw = this?.getString(customerCouponNotificationsKey(userId), null) ?: return emptySet()
    return runCatching {
        JSONArray(raw).toStringList()
            .map { it.normalizeCouponCode() }
            .filter { it.isNotBlank() }
            .toSet()
    }.getOrDefault(emptySet())
}

private fun SharedPreferences?.writeNotifiedAdminCouponCodes(
    userId: String,
    codes: Set<String>,
) {
    this?.edit()
        ?.putString(customerCouponNotificationsKey(userId), codes.toList().sorted().toStringJsonArray().toString())
        ?.apply()
}

private fun customerCouponNotificationsKey(userId: String): String =
    "$CustomerCouponNotificationsPrefix$userId"

private fun SharedPreferences?.readAdminRestaurants(): List<Restaurant>? {
    val raw = this?.getString(AdminRestaurantsKey, null) ?: return null
    return runCatching {
        val array = if (raw.trimStart().startsWith("[")) {
            JSONArray(raw)
        } else {
            JSONObject(raw).optJSONArray("restaurants") ?: JSONArray()
        }
        array.toRestaurants()
    }.getOrNull()?.takeIf { it.isNotEmpty() }
}

private fun SharedPreferences?.writeAdminRestaurants(restaurants: List<Restaurant>) {
    this?.edit()
        ?.putString(
            AdminRestaurantsKey,
            JSONObject()
                .put("schemaVersion", 1)
                .put("restaurants", restaurants.toJsonArray { it.toPreferenceJson() })
                .toString(),
        )
        ?.apply()
}

private fun SharedPreferences?.readAdminCoupons(): List<DiscountCoupon>? {
    val raw = this?.getString(AdminCouponsKey, null) ?: return null
    return runCatching {
        JSONArray(raw).toDiscountCoupons()
    }.getOrNull()
}

private fun SharedPreferences?.writeAdminCoupons(coupons: List<DiscountCoupon>) {
    this?.edit()
        ?.putString(AdminCouponsKey, coupons.toJsonArray { it.toPreferenceJson() }.toString())
        ?.apply()
}

private fun SharedPreferences?.readDishOrderCounts(): Map<String, Int> {
    val raw = this?.getString(AdminDishOrderCountsKey, null) ?: return emptyMap()
    return runCatching {
        val json = JSONObject(raw)
        buildMap {
            val keys = json.keys()
            while (keys.hasNext()) {
                val itemId = keys.next()
                val count = json.optInt(itemId, 0)
                if (itemId.isNotBlank() && count > 0) {
                    put(itemId, count)
                }
            }
        }
    }.getOrDefault(emptyMap())
}

private fun SharedPreferences?.writeDishOrderCounts(counts: Map<String, Int>) {
    val json = JSONObject()
    counts
        .filterValues { it > 0 }
        .forEach { (itemId, count) -> json.put(itemId, count) }
    this?.edit()
        ?.putString(AdminDishOrderCountsKey, json.toString())
        ?.apply()
}

private fun SharedPreferences?.readCourierWorkspace(): SavedCourierWorkspace? {
    val raw = this?.getString(CourierWorkspaceKey, null) ?: return null
    return runCatching {
        val json = JSONObject(raw)
        SavedCourierWorkspace(
            orders = json.optJSONArray("orders").toCourierOrders(),
            reviews = json.optJSONArray("reviews").toOrderReviews(),
            messages = json.optJSONArray("messages").toCourierMessages(),
            unreadCourierChatOrderIds = json.optJSONArray("unreadCourierChatOrderIds").toStringList(),
            walletBalance = json.optInt("walletBalance", 0),
            rating = json.optDouble("rating", 4.8),
            blockedUntilMillis = json.optLong("blockedUntilMillis", 0L),
            lastWithdrawalAtMillis = json.optLong("lastWithdrawalAtMillis", 0L),
            reviewRatings = json.optJSONArray("reviewRatings").toReviewRatingMap(),
            payoutCard = json.optJSONObject("payoutCard")?.toPaymentCardOrNull(),
            isOnline = json.optBoolean("isOnline", false),
            selectedOrderId = json.nullableString("selectedOrderId"),
        )
    }.getOrNull()
}

private fun SharedPreferences?.writeCourierWorkspace(
    orders: List<CourierDeliveryOrder>,
    reviews: List<OrderReview>,
    messages: List<CourierChatMessage>,
    unreadCourierChatOrderIds: List<String>,
    walletBalance: Int,
    rating: Double,
    blockedUntilMillis: Long,
    lastWithdrawalAtMillis: Long,
    reviewRatings: Map<String, Int>,
    payoutCard: PaymentCard?,
    isOnline: Boolean,
    selectedOrderId: String?,
) {
    this?.edit()
        ?.putString(
            CourierWorkspaceKey,
            JSONObject()
                .put("orders", orders.toJsonArray { it.toPreferenceJson() })
                .put("reviews", reviews.distinctBy { it.orderId }.toJsonArray { it.toPreferenceJson() })
                .put("messages", messages.distinctBy { it.id }.toJsonArray { it.toPreferenceJson() })
                .put("unreadCourierChatOrderIds", unreadCourierChatOrderIds.distinct().toStringJsonArray())
                .put("walletBalance", walletBalance)
                .put("rating", rating)
                .put("blockedUntilMillis", blockedUntilMillis)
                .put("lastWithdrawalAtMillis", lastWithdrawalAtMillis)
                .put("reviewRatings", reviewRatings.toJsonArray())
                .put("payoutCard", payoutCard?.toPreferenceJson() ?: JSONObject.NULL)
                .put("isOnline", isOnline)
                .putNullable("selectedOrderId", selectedOrderId)
                .toString(),
        )
        ?.apply()
}

private fun Map<String, Int>.toJsonArray(): JSONArray =
    JSONArray().also { array ->
        forEach { (orderId, rating) ->
            array.put(
                JSONObject()
                    .put("orderId", orderId)
                    .put("rating", rating),
            )
        }
    }

private fun JSONArray?.toReviewRatingMap(): Map<String, Int> {
    if (this == null) return emptyMap()
    return buildMap {
        for (index in 0 until length()) {
            val json = optJSONObject(index) ?: continue
            val orderId = json.optString("orderId")
            val rating = json.optInt("rating", 0)
            if (orderId.isNotBlank() && rating in 1..5) {
                put(orderId, rating)
            }
        }
    }
}

private fun SharedPreferences?.readLastCourierProfile(): CourierProfileDetails? {
    val raw = this?.getString(CourierProfileKey, null) ?: return null
    return raw.toCourierProfileDetailsOrNull()
}

private fun SharedPreferences?.readCourierProfile(email: String): CourierProfileDetails? {
    val raw = this?.getString(courierProfileKey(email), null) ?: return null
    return raw.toCourierProfileDetailsOrNull()
}

private fun SharedPreferences?.writeCourierProfile(profile: CourierProfileDetails) {
    val json = profile.toPreferenceJson().toString()
    this?.edit()
        ?.putString(CourierProfileKey, json)
        ?.putString(courierProfileKey(profile.email), json)
        ?.apply()
}

private fun courierProfileKey(email: String): String =
    "$CourierProfilePrefix${email.trim().lowercase(Locale.US)}"

private fun String.toCourierProfileDetailsOrNull(): CourierProfileDetails? =
    runCatching {
        JSONObject(this).toCourierProfileDetailsOrNull()
    }.getOrNull()

private fun CourierProfileDetails.toPreferenceJson(): JSONObject =
    JSONObject()
        .put("id", id)
        .put("name", name)
        .put("email", email)
        .put("phone", phone)
        .put("vehicle", vehicle)
        .put("photoUrl", photoUrl)

private fun JSONObject.toCourierProfileDetailsOrNull(): CourierProfileDetails? =
    runCatching {
        CourierProfileDetails(
            id = optString("id").ifBlank { "courier_${System.currentTimeMillis()}" },
            name = optString("name"),
            email = optString("email"),
            phone = optString("phone"),
            vehicle = optString("vehicle"),
            photoUrl = optString("photoUrl"),
        )
    }.getOrNull()?.takeIf { it.email.contains("@") }

private fun OrderSummary.toPreferenceJson(): JSONObject =
    JSONObject()
        .put("id", id)
        .put("restaurantId", restaurantId)
        .put("restaurantName", restaurantName)
        .put("itemsLabel", itemsLabel)
        .put("total", total)
        .put("eta", eta)
        .put("status", status.name)
        .put("paymentLabel", paymentLabel)
        .putNullable("deliveryDistanceKm", deliveryDistanceKm)
        .put("createdAtMillis", createdAtMillis)
        .put("refundedAmount", refundedAmount)
        .putNullable("deliveredAtMillis", deliveredAtMillis)
        .putNullable("customerReceivedAtMillis", customerReceivedAtMillis)

private fun JSONObject.toOrderSummaryOrNull(): OrderSummary? =
    runCatching {
        OrderSummary(
            id = getString("id"),
            restaurantId = optString("restaurantId"),
            restaurantName = optString("restaurantName"),
            itemsLabel = optString("itemsLabel"),
            total = optInt("total"),
            eta = optString("eta"),
            status = optEnum("status", OrderStatus.Preparing),
            paymentLabel = optString("paymentLabel", "Cash"),
            deliveryDistanceKm = nullableDouble("deliveryDistanceKm"),
            createdAtMillis = optLong("createdAtMillis", System.currentTimeMillis()),
            refundedAmount = optInt("refundedAmount", 0),
            deliveredAtMillis = nullableLong("deliveredAtMillis"),
            customerReceivedAtMillis = nullableLong("customerReceivedAtMillis"),
        )
    }.getOrNull()

private fun OrderReview.toPreferenceJson(): JSONObject =
    JSONObject()
        .put("orderId", orderId)
        .put("courierRating", courierRating)
        .put("orderRating", orderRating)
        .put("comment", comment)
        .put("createdAtMillis", createdAtMillis)

private fun JSONObject.toOrderReviewOrNull(): OrderReview? =
    runCatching {
        OrderReview(
            orderId = getString("orderId"),
            courierRating = optInt("courierRating", 5).coerceIn(1, 5),
            orderRating = optInt("orderRating", 5).coerceIn(1, 5),
            comment = optString("comment"),
            createdAtMillis = optLong("createdAtMillis", System.currentTimeMillis()),
        )
    }.getOrNull()

private fun PaymentCard.toPreferenceJson(): JSONObject =
    JSONObject()
        .put("id", id)
        .put("brand", brand)
        .put("last4", last4)
        .put("holderName", holderName)
        .put("expiry", expiry)

private fun JSONObject.toPaymentCardOrNull(): PaymentCard? =
    runCatching {
        PaymentCard(
            id = optString("id").ifBlank { "card_${System.currentTimeMillis()}" },
            brand = optString("brand"),
            last4 = optString("last4"),
            holderName = optString("holderName"),
            expiry = optString("expiry"),
        )
    }.getOrNull()?.takeIf { it.brand.isNotBlank() && it.last4.length == 4 }

private fun DiscountCoupon.toPreferenceJson(): JSONObject =
    JSONObject()
        .put("code", code)
        .put("title", title)
        .put("description", description)
        .put("discountPercent", discountPercent)
        .put("expiresAtMillis", expiresAtMillis)

private fun JSONObject.toDiscountCouponOrNull(): DiscountCoupon? =
    runCatching {
        DiscountCoupon(
            code = getString("code").normalizeCouponCode(),
            title = optString("title"),
            description = optString("description"),
            discountPercent = optInt("discountPercent").coerceIn(1, 90),
            expiresAtMillis = optLong("expiresAtMillis"),
        )
    }.getOrNull()?.takeIf { it.code.isNotBlank() && !it.isExpired() }

private fun Restaurant.toPreferenceJson(): JSONObject =
    JSONObject()
        .put("id", id)
        .put("name", name)
        .put("subtitle", subtitle)
        .put("description", description)
        .put("rating", rating)
        .put("deliveryTime", deliveryTime)
        .put("deliveryFee", deliveryFee)
        .put("emoji", emoji)
        .put("tags", tags.toStringJsonArray())
        .put("location", location.toPreferenceJson())
        .put("imageUrl", imageUrl)
        .put("imageUrls", imageUrls.toStringJsonArray())
        .put("menu", menu.toJsonArray { it.toPreferenceJson() })

private fun JSONObject.toRestaurantOrNull(): Restaurant? =
    runCatching {
        val id = getString("id")
        val fallback = SampleData.restaurants.firstOrNull { it.id == id }
        val accent = fallback?.accent ?: SampleData.categories.first().accent
        val location = optJSONObject("location")?.toGeoPointOrNull()
            ?: fallback?.location
            ?: SampleData.defaultAddress.point
        val imageUrl = optString("imageUrl", fallback?.imageUrl.orEmpty())
        val imageUrls = optJSONArray("imageUrls").toStringList()
            .ifEmpty {
                listOf(imageUrl)
                    .filter { it.isNotBlank() }
                    .ifEmpty { fallback?.imageUrls ?: emptyList() }
            }
        Restaurant(
            id = id,
            name = optString("name", fallback?.name.orEmpty()),
            subtitle = optString("subtitle", fallback?.subtitle.orEmpty()),
            description = optString("description", fallback?.description.orEmpty()),
            rating = optDouble("rating", fallback?.rating ?: 4.8),
            deliveryTime = optString("deliveryTime", fallback?.deliveryTime ?: "18-25 min"),
            deliveryFee = optString("deliveryFee", fallback?.deliveryFee ?: "Free"),
            accent = accent,
            emoji = optString("emoji", fallback?.emoji ?: "Food"),
            tags = optJSONArray("tags").toStringList().ifEmpty { fallback?.tags ?: emptyList() },
            location = location,
            menu = optJSONArray("menu").toMenuItems(id, accent),
            imageUrl = imageUrl,
            imageUrls = imageUrls,
        )
    }.getOrNull()?.takeIf { it.id.isNotBlank() && it.name.isNotBlank() }

private fun MenuItem.toPreferenceJson(): JSONObject =
    JSONObject()
        .put("id", id)
        .put("restaurantId", restaurantId)
        .put("title", title)
        .put("subtitle", subtitle)
        .put("price", price)
        .put("emoji", emoji)
        .put("category", category)
        .put("imageUrl", imageUrl)
        .put("ingredients", ingredients.toStringJsonArray())
        .put("details", details)

private fun JSONObject.toMenuItemOrNull(
    restaurantId: String,
    restaurantAccent: Color,
): MenuItem? =
    runCatching {
        val id = getString("id")
        val fallback = SampleData.restaurants
            .asSequence()
            .flatMap { it.menu.asSequence() }
            .firstOrNull { it.id == id }
        val cleanRestaurantId = optString("restaurantId").ifBlank { restaurantId }
        val category = optString("category", fallback?.category ?: "burger")
        val accent = fallback?.accent
            ?: SampleData.categories.firstOrNull { it.id == category }?.accent
            ?: restaurantAccent
        MenuItem(
            id = id,
            restaurantId = cleanRestaurantId,
            title = optString("title", fallback?.title.orEmpty()),
            subtitle = optString("subtitle", fallback?.subtitle.orEmpty()),
            price = optInt("price", fallback?.price ?: 0),
            emoji = optString("emoji", fallback?.emoji ?: "Food"),
            accent = accent,
            category = category,
            imageUrl = optString("imageUrl", fallback?.imageUrl.orEmpty()),
            ingredients = optJSONArray("ingredients").toStringList().ifEmpty { fallback?.ingredients ?: emptyList() },
            details = optString("details", fallback?.details.orEmpty()),
        )
    }.getOrNull()?.takeIf { it.id.isNotBlank() && it.title.isNotBlank() && it.price > 0 }

private fun CourierDeliveryOrder.toPreferenceJson(): JSONObject =
    JSONObject()
        .put("id", id)
        .put("customerName", customerName)
        .put("customerPhone", customerPhone)
        .put("restaurantId", restaurantId)
        .put("restaurantName", restaurantName)
        .put("restaurantAddress", restaurantAddress)
        .put("customerAddress", customerAddress.toPreferenceJson())
        .put("itemsLabel", itemsLabel)
        .put("total", total)
        .put("earning", earning)
        .put("status", status.name)
        .put("restaurantPoint", restaurantPoint.toPreferenceJson())
        .put("createdAtMillis", createdAtMillis)
        .putNullable("courierId", courierId)
        .putNullable("courierName", courierName)
        .putNullable("courierPhone", courierPhone)
        .putNullable("customerUserId", customerUserId)
        .putNullable("courierPoint", courierPoint?.toPreferenceJson())

private fun JSONObject.toCourierOrderOrNull(): CourierDeliveryOrder? =
    runCatching {
        CourierDeliveryOrder(
            id = getString("id"),
            customerName = optString("customerName"),
            customerPhone = optString("customerPhone"),
            restaurantId = optString("restaurantId"),
            restaurantName = optString("restaurantName"),
            restaurantAddress = optString("restaurantAddress"),
            customerAddress = getJSONObject("customerAddress").toDeliveryAddressOrNull() ?: return@runCatching null,
            itemsLabel = optString("itemsLabel"),
            total = optInt("total"),
            earning = optInt("earning"),
            status = optEnum("status", CourierOrderStatus.Available),
            restaurantPoint = getJSONObject("restaurantPoint").toGeoPointOrNull() ?: return@runCatching null,
            createdAtMillis = optLong("createdAtMillis", System.currentTimeMillis()),
            courierId = nullableString("courierId"),
            courierName = nullableString("courierName"),
            courierPhone = nullableString("courierPhone"),
            customerUserId = nullableString("customerUserId"),
            courierPoint = optJSONObject("courierPoint")?.toGeoPointOrNull(),
        )
    }.getOrNull()

private fun CourierChatMessage.toPreferenceJson(): JSONObject =
    JSONObject()
        .put("id", id)
        .put("orderId", orderId)
        .put("author", author.name)
        .put("text", text)
        .put("timeLabel", timeLabel)

private fun JSONObject.toCourierMessageOrNull(): CourierChatMessage? =
    runCatching {
        CourierChatMessage(
            id = getString("id"),
            orderId = getString("orderId"),
            author = optEnum("author", ChatAuthor.Customer),
            text = optString("text"),
            timeLabel = optString("timeLabel"),
        )
    }.getOrNull()

private fun GeoPoint.toPreferenceJson(): JSONObject =
    JSONObject()
        .put("latitude", latitude)
        .put("longitude", longitude)

private fun JSONObject.toGeoPointOrNull(): GeoPoint? =
    runCatching {
        GeoPoint(
            latitude = getDouble("latitude"),
            longitude = getDouble("longitude"),
        )
    }.getOrNull()

private fun JSONArray?.toOrderSummaries(): List<OrderSummary> =
    toTypedList { it.toOrderSummaryOrNull() }

private fun JSONArray?.toOrderReviews(): List<OrderReview> =
    toTypedList { it.toOrderReviewOrNull() }

private fun JSONArray?.toDiscountCoupons(): List<DiscountCoupon> =
    toTypedList { it.toDiscountCouponOrNull() }

private fun JSONArray?.toRestaurants(): List<Restaurant> =
    toTypedList { it.toRestaurantOrNull() }

private fun JSONArray?.toMenuItems(
    restaurantId: String,
    restaurantAccent: Color,
): List<MenuItem> =
    toTypedList { it.toMenuItemOrNull(restaurantId, restaurantAccent) }

private fun JSONArray?.toCourierOrders(): List<CourierDeliveryOrder> =
    toTypedList { it.toCourierOrderOrNull() }

private fun JSONArray?.toCourierMessages(): List<CourierChatMessage> =
    toTypedList { it.toCourierMessageOrNull() }

private fun CourierChatMessage.isAutomaticChatMessage(): Boolean {
    val cleanText = text.trim()
    return cleanText.equals("Hi, I am waiting for the order.", ignoreCase = true) ||
        cleanText.equals("Thank you for accepting my order.", ignoreCase = true) ||
        cleanText.equals("Ok, thank you. I will wait.", ignoreCase = true)
}

private fun CourierDeliveryOrder.chatCustomerKey(): String =
    customerUserId
        ?: customerPhone.filter(Char::isDigit).takeIf { it.isNotBlank() }
        ?: customerName.trim().lowercase(Locale.US).ifBlank { id }

private fun CourierChatMessage.createdSortValue(): Long =
    id.substringAfter("msg_", missingDelimiterValue = "")
        .substringBefore("_")
        .toLongOrNull()
        ?: 0L

private fun MutableList<String>.addIfAbsent(value: String) {
    if (value.isNotBlank() && value !in this) {
        add(value)
    }
}

private fun String.normalizeCardExpiry(): String =
    if (matches(Regex("""\d{2}/\d{4}"""))) {
        take(3) + takeLast(2)
    } else {
        this
    }

private fun List<String>.toStringJsonArray(): JSONArray =
    JSONArray().also { array -> forEach { value -> array.put(value) } }

private inline fun <T> List<T>.toJsonArray(toJson: (T) -> JSONObject): JSONArray =
    JSONArray().also { array -> forEach { item -> array.put(toJson(item)) } }

private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            optString(index).trim().takeIf { it.isNotBlank() }?.let(::add)
        }
    }
}

private inline fun <T> JSONArray?.toTypedList(read: (JSONObject) -> T?): List<T> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            optJSONObject(index)?.let(read)?.let(::add)
        }
    }
}

private inline fun <reified T : Enum<T>> JSONObject.optEnum(name: String, fallback: T): T =
    nullableString(name)
        ?.let { value -> runCatching { enumValueOf<T>(value) }.getOrNull() }
        ?: fallback

private fun JSONObject.nullableString(name: String): String? =
    if (!has(name) || isNull(name)) null else optString(name)

private fun JSONObject.nullableDouble(name: String): Double? =
    if (!has(name) || isNull(name)) null else optDouble(name)

private fun JSONObject.nullableLong(name: String): Long? =
    if (!has(name) || isNull(name)) null else optLong(name)

private fun JSONObject.putNullable(name: String, value: String?): JSONObject =
    put(name, value ?: JSONObject.NULL)

private fun JSONObject.putNullable(name: String, value: Double?): JSONObject =
    put(name, value ?: JSONObject.NULL)

private fun JSONObject.putNullable(name: String, value: Long?): JSONObject =
    put(name, value ?: JSONObject.NULL)

private fun JSONObject.putNullable(name: String, value: JSONObject?): JSONObject =
    put(name, value ?: JSONObject.NULL)

private fun SharedPreferences?.readCustomerProfile(userId: String): UserProfileDetails? {
    val raw = this?.getString(customerProfileKey(userId), null) ?: return null
    return runCatching {
        val json = JSONObject(raw)
        UserProfileDetails(
            firstName = json.optString("firstName"),
            lastName = json.optString("lastName"),
            phone = json.optString("phone"),
            isPhoneVerified = json.optBoolean("isPhoneVerified", false),
            email = json.optString("email"),
            bio = json.optString("bio", "I love fast food"),
            photoUri = json.optString("photoUri"),
        )
    }.getOrNull()
}

private fun SharedPreferences?.writeCustomerProfile(
    userId: String,
    profile: UserProfileDetails,
) {
    this?.edit()
        ?.putString(
            customerProfileKey(userId),
            JSONObject()
                .put("firstName", profile.firstName)
                .put("lastName", profile.lastName)
                .put("phone", profile.phone)
                .put("isPhoneVerified", profile.isPhoneVerified)
                .put("email", profile.email)
                .put("bio", profile.bio)
                .put("photoUri", profile.photoUri)
                .toString(),
        )
        ?.apply()
}

private fun customerProfileKey(userId: String): String =
    "$CustomerProfilePrefix$userId"

private fun SharedPreferences?.readThemeMode(): AppThemeMode =
    this?.getString("theme_mode", null)
        ?.let { value -> runCatching { AppThemeMode.valueOf(value) }.getOrNull() }
        ?: AppThemeMode.Light

private fun SharedPreferences?.readLanguage(): AppLanguage =
    this?.getString("language", null)
        ?.let { value -> runCatching { AppLanguage.valueOf(value) }.getOrNull() }
        ?: AppLanguage.Russian

private fun Int.asPriceLabel(language: AppLanguage): String = formatCurrency(this, language)

private fun AppLanguage.cancelledEtaLabel(): String = when (this) {
    AppLanguage.English -> "Cancelled"
    AppLanguage.Russian -> "Отменен"
    AppLanguage.Uzbek -> "Bekor qilingan"
}

private fun String.normalizeCouponCode(): String =
    trim()
        .removePrefix("#")
        .uppercase()

private fun String.toAdminId(fallback: String): String {
    val collapsed = trim()
        .lowercase(Locale.US)
        .map { char -> if (char.isLetterOrDigit()) char else '_' }
        .joinToString("")
        .replace(Regex("_+"), "_")
        .trim('_')
    return collapsed.ifBlank { fallback }
}

private fun uniqueAdminId(baseId: String, usedIds: Set<String>): String {
    var candidate = baseId
    var suffix = 2
    while (candidate in usedIds) {
        candidate = "${baseId}_$suffix"
        suffix += 1
    }
    return candidate
}

private fun String.baseMenuItemId(): String =
    replace(Regex("_(10|14|16|18)in$"), "")
        .replace(Regex("_(standard|medium|premium)$"), "")

private fun PaymentMethod.requiresSavedCard(): Boolean = this != PaymentMethod.Cash

private fun PaymentCard.paymentMethod(): PaymentMethod =
    when {
        brand.equals("Visa", ignoreCase = true) -> PaymentMethod.Visa
        brand.equals("Uzcard", ignoreCase = true) -> PaymentMethod.Uzcard
        brand.equals("HumoCard", ignoreCase = true) || brand.equals("Humo", ignoreCase = true) -> PaymentMethod.HumoCard
        else -> PaymentMethod.MasterCard
    }

private fun PaymentCard.matchesPaymentMethod(method: PaymentMethod): Boolean =
    method != PaymentMethod.Cash && paymentMethod() == method

private fun currentChatTimeLabel(): String =
    LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm a")).lowercase()

private val ShortDateLabelFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd.MM.yyyy")

private fun Long.toShortDateLabel(): String =
    Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .format(ShortDateLabelFormatter)

@Composable
fun rememberFoodAppState(): FoodAppState {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        FoodAppState(
            backend = DatabaseFoodBackend(context),
            orderSyncBackend = HttpOrderSyncBackend(BuildConfig.ORDER_SYNC_BASE_URL),
            preferences = context.getSharedPreferences("food_delivery_settings", Context.MODE_PRIVATE),
        )
    }
}
