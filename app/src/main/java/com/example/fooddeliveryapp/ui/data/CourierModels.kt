package com.example.fooddeliveryapp.ui.data

import androidx.compose.runtime.Immutable

@Immutable
data class CourierProfileDetails(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val vehicle: String,
    val photoUrl: String = "",
)

enum class CourierOrderStatus {
    Available,
    Accepted,
    ArrivedAtRestaurant,
    PickedUp,
    OnTheWay,
    Delivered,
}

@Immutable
data class CourierDeliveryOrder(
    val id: String,
    val customerName: String,
    val customerPhone: String,
    val restaurantId: String,
    val restaurantName: String,
    val restaurantAddress: String,
    val customerAddress: DeliveryAddress,
    val itemsLabel: String,
    val total: Int,
    val earning: Int,
    val status: CourierOrderStatus,
    val restaurantPoint: GeoPoint,
    val createdAtMillis: Long,
    val courierId: String? = null,
    val courierName: String? = null,
    val courierPhone: String? = null,
    val customerUserId: String? = null,
    val courierPoint: GeoPoint? = null,
)

enum class ChatAuthor {
    Customer,
    Courier,
}

@Immutable
data class CourierChatMessage(
    val id: String,
    val orderId: String,
    val author: ChatAuthor,
    val text: String,
    val timeLabel: String,
)
