package com.example.fooddeliveryapp.ui.data

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
)

@Immutable
data class DeliveryAddress(
    val label: String,
    val title: String,
    val subtitle: String,
    val point: GeoPoint,
)

@Immutable
data class OnboardingSlide(
    val title: String,
    val description: String,
    val emoji: String,
    val accent: Color,
)

@Immutable
data class AppCategory(
    val id: String,
    val title: String,
    val emoji: String,
    val accent: Color,
)

@Immutable
data class PromoOffer(
    val title: String,
    val description: String,
    val badge: String,
    val accent: Color,
)

@Immutable
data class DiscountCoupon(
    val code: String,
    val title: String,
    val description: String,
    val discountPercent: Int,
    val expiresAtMillis: Long,
) {
    fun isExpired(nowMillis: Long = System.currentTimeMillis()): Boolean =
        expiresAtMillis <= nowMillis
}

@Immutable
data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val read: Boolean = false,
)

@Immutable
data class OrderReview(
    val orderId: String,
    val courierRating: Int,
    val orderRating: Int,
    val comment: String,
    val createdAtMillis: Long = System.currentTimeMillis(),
)

@Immutable
data class MenuItem(
    val id: String,
    val restaurantId: String,
    val title: String,
    val subtitle: String,
    val price: Int,
    val emoji: String,
    val accent: Color,
    val category: String,
    val imageUrl: String = "",
    val ingredients: List<String> = emptyList(),
    val details: String = "",
)

fun MenuItem.usesTransparentCutoutArt(): Boolean =
    imageUrl.contains("_cutout", ignoreCase = true)

@Immutable
data class Restaurant(
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
    val menu: List<MenuItem>,
    val imageUrl: String = "",
    val imageUrls: List<String> = emptyList(),
)

@Immutable
data class CartLine(
    val item: MenuItem,
    val quantity: Int,
)

enum class PaymentMethod {
    Cash,
    Visa,
    MasterCard,
    Uzcard,
    HumoCard,
}

fun detectPaymentMethod(number: String): PaymentMethod? {
    val digits = number.filter(Char::isDigit)
    if (digits.isEmpty()) return null

    return when {
        digits.startsWith("9860") -> PaymentMethod.HumoCard
        digits.hasAnyPrefix("8600", "5614", "6262", "5440") -> PaymentMethod.Uzcard
        digits.startsWith("4") -> PaymentMethod.Visa
        digits.hasPrefixRange(length = 2, range = 51..55) -> PaymentMethod.MasterCard
        digits.hasPrefixRange(length = 6, range = 222100..272099) -> PaymentMethod.MasterCard
        else -> null
    }
}

fun PaymentMethod.cardBrandName(): String = when (this) {
    PaymentMethod.Cash -> "Cash"
    PaymentMethod.Visa -> "Visa"
    PaymentMethod.MasterCard -> "Mastercard"
    PaymentMethod.Uzcard -> "Uzcard"
    PaymentMethod.HumoCard -> "HumoCard"
}

private fun String.hasAnyPrefix(vararg prefixes: String): Boolean =
    prefixes.any(::startsWith)

private fun String.hasPrefixRange(length: Int, range: IntRange): Boolean =
    take(length).toIntOrNull()?.let { it in range } == true

@Immutable
data class PaymentCard(
    val id: String,
    val brand: String,
    val last4: String,
    val holderName: String,
    val expiry: String,
)

enum class OrderStatus {
    Preparing,
    OnTheWay,
    Delivered,
    Cancelled,
}

@Immutable
data class OrderSummary(
    val id: String,
    val restaurantId: String,
    val restaurantName: String,
    val itemsLabel: String,
    val total: Int,
    val eta: String,
    val status: OrderStatus,
    val paymentLabel: String = "Cash",
    val deliveryDistanceKm: Double? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val refundedAmount: Int = 0,
    val deliveredAtMillis: Long? = null,
    val customerReceivedAtMillis: Long? = null,
)
