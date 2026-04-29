package com.example.fooddeliveryapp.backend

import com.example.fooddeliveryapp.ui.data.ChatAuthor
import com.example.fooddeliveryapp.ui.data.CourierChatMessage
import com.example.fooddeliveryapp.ui.data.CourierDeliveryOrder
import com.example.fooddeliveryapp.ui.data.CourierOrderStatus
import com.example.fooddeliveryapp.ui.data.DeliveryAddress
import com.example.fooddeliveryapp.ui.data.GeoPoint
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class OrderSyncSnapshot(
    val orders: List<CourierDeliveryOrder>,
    val messages: List<CourierChatMessage>,
)

interface OrderSyncBackend {
    val enabled: Boolean
    suspend fun fetchSnapshot(): BackendResult<OrderSyncSnapshot>
    suspend fun upsertOrder(order: CourierDeliveryOrder): BackendResult<Unit>
    suspend fun updateOrderStatus(
        orderId: String,
        status: CourierOrderStatus,
        courierId: String?,
        courierPoint: GeoPoint?,
    ): BackendResult<Unit>
    suspend fun addChatMessage(message: CourierChatMessage): BackendResult<Unit>
}

object NoopOrderSyncBackend : OrderSyncBackend {
    override val enabled: Boolean = false

    override suspend fun fetchSnapshot(): BackendResult<OrderSyncSnapshot> =
        BackendResult.Success(OrderSyncSnapshot(emptyList(), emptyList()))

    override suspend fun upsertOrder(order: CourierDeliveryOrder): BackendResult<Unit> =
        BackendResult.Success(Unit)

    override suspend fun updateOrderStatus(
        orderId: String,
        status: CourierOrderStatus,
        courierId: String?,
        courierPoint: GeoPoint?,
    ): BackendResult<Unit> = BackendResult.Success(Unit)

    override suspend fun addChatMessage(message: CourierChatMessage): BackendResult<Unit> =
        BackendResult.Success(Unit)
}

class HttpOrderSyncBackend(baseUrl: String) : OrderSyncBackend {
    private val rootUrl = baseUrl.trim().trimEnd('/')
    override val enabled: Boolean = rootUrl.startsWith("http://") || rootUrl.startsWith("https://")

    override suspend fun fetchSnapshot(): BackendResult<OrderSyncSnapshot> =
        when (val result = request(method = "GET", path = "snapshot")) {
            is BackendResult.Success -> {
                val json = runCatching { JSONObject(result.data.ifBlank { "{}" }) }.getOrNull()
                    ?: return BackendResult.Error("Order sync returned invalid JSON")
                BackendResult.Success(
                    OrderSyncSnapshot(
                        orders = json.optJSONArray("orders").toCourierOrders(),
                        messages = json.optJSONArray("messages").toChatMessages(),
                    ),
                )
            }
            is BackendResult.Error -> result
        }

    override suspend fun upsertOrder(order: CourierDeliveryOrder): BackendResult<Unit> =
        requestUnit(
            method = "POST",
            path = "orders",
            body = order.toJson(),
        )

    override suspend fun updateOrderStatus(
        orderId: String,
        status: CourierOrderStatus,
        courierId: String?,
        courierPoint: GeoPoint?,
    ): BackendResult<Unit> =
        requestUnit(
            method = "POST",
            path = "orders/${orderId.urlEncode()}/status",
            body = JSONObject()
                .put("status", status.name)
                .putNullable("courierId", courierId)
                .putNullable("courierPoint", courierPoint?.toJson()),
        )

    override suspend fun addChatMessage(message: CourierChatMessage): BackendResult<Unit> =
        requestUnit(
            method = "POST",
            path = "messages",
            body = message.toJson(),
        )

    private suspend fun requestUnit(
        method: String,
        path: String,
        body: JSONObject,
    ): BackendResult<Unit> =
        when (val result = request(method = method, path = path, body = body)) {
            is BackendResult.Success -> BackendResult.Success(Unit)
            is BackendResult.Error -> result
        }

    private suspend fun request(
        method: String,
        path: String,
        body: JSONObject? = null,
    ): BackendResult<String> = withContext(Dispatchers.IO) {
        if (!enabled) return@withContext BackendResult.Error("Order sync URL is not configured")

        val connection = runCatching {
            (URL("$rootUrl/${path.trimStart('/')}").openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = ConnectionTimeoutMs
                readTimeout = ReadTimeoutMs
                setRequestProperty("Accept", "application/json")
                if (body != null) {
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                }
            }
        }.getOrElse { error ->
            return@withContext BackendResult.Error(error.toOrderSyncMessage())
        }

        try {
            if (body != null) {
                connection.outputStream.use { stream ->
                    stream.write(body.toString().toByteArray(Charsets.UTF_8))
                }
            }
            val code = connection.responseCode
            val text = (if (code in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader(Charsets.UTF_8)
                ?.use { it.readText() }
                .orEmpty()

            if (code in 200..299) {
                BackendResult.Success(text)
            } else {
                val message = runCatching { JSONObject(text).optString("error") }.getOrNull()
                    ?.takeIf { it.isNotBlank() }
                    ?: "Order sync failed with HTTP $code"
                BackendResult.Error(message)
            }
        } catch (error: Exception) {
            BackendResult.Error(error.toOrderSyncMessage())
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val ConnectionTimeoutMs = 8_000
        const val ReadTimeoutMs = 8_000
    }
}

private fun CourierDeliveryOrder.toJson(): JSONObject =
    JSONObject()
        .put("id", id)
        .put("customerName", customerName)
        .put("customerPhone", customerPhone)
        .put("restaurantId", restaurantId)
        .put("restaurantName", restaurantName)
        .put("restaurantAddress", restaurantAddress)
        .put("customerAddress", customerAddress.toJson())
        .put("itemsLabel", itemsLabel)
        .put("total", total)
        .put("earning", earning)
        .put("status", status.name)
        .put("restaurantPoint", restaurantPoint.toJson())
        .put("createdAtMillis", createdAtMillis)
        .putNullable("courierId", courierId)
        .putNullable("courierName", courierName)
        .putNullable("courierPhone", courierPhone)
        .putNullable("customerUserId", customerUserId)
        .putNullable("courierPoint", courierPoint?.toJson())

private fun CourierChatMessage.toJson(): JSONObject =
    JSONObject()
        .put("id", id)
        .put("orderId", orderId)
        .put("author", author.name)
        .put("text", text)
        .put("timeLabel", timeLabel)

private fun DeliveryAddress.toJson(): JSONObject =
    JSONObject()
        .put("label", label)
        .put("title", title)
        .put("subtitle", subtitle)
        .put("point", point.toJson())

private fun GeoPoint.toJson(): JSONObject =
    JSONObject()
        .put("latitude", latitude)
        .put("longitude", longitude)

private fun JSONArray?.toCourierOrders(): List<CourierDeliveryOrder> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            optJSONObject(index)?.toCourierOrderOrNull()?.let(::add)
        }
    }
}

private fun JSONArray?.toChatMessages(): List<CourierChatMessage> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            optJSONObject(index)?.toChatMessageOrNull()?.let(::add)
        }
    }
}

private fun JSONObject.toCourierOrderOrNull(): CourierDeliveryOrder? =
    runCatching {
        CourierDeliveryOrder(
            id = getString("id"),
            customerName = optString("customerName"),
            customerPhone = optString("customerPhone"),
            restaurantId = optString("restaurantId"),
            restaurantName = optString("restaurantName"),
            restaurantAddress = optString("restaurantAddress"),
            customerAddress = getJSONObject("customerAddress").toDeliveryAddress(),
            itemsLabel = optString("itemsLabel"),
            total = optInt("total"),
            earning = optInt("earning"),
            status = optEnum("status", CourierOrderStatus.Available),
            restaurantPoint = getJSONObject("restaurantPoint").toGeoPoint(),
            createdAtMillis = optLong("createdAtMillis", System.currentTimeMillis()),
            courierId = nullableString("courierId"),
            courierName = nullableString("courierName"),
            courierPhone = nullableString("courierPhone"),
            customerUserId = nullableString("customerUserId"),
            courierPoint = optJSONObject("courierPoint")?.toGeoPoint(),
        )
    }.getOrNull()

private fun JSONObject.toChatMessageOrNull(): CourierChatMessage? =
    runCatching {
        CourierChatMessage(
            id = getString("id"),
            orderId = getString("orderId"),
            author = optEnum("author", ChatAuthor.Customer),
            text = optString("text"),
            timeLabel = optString("timeLabel"),
        )
    }.getOrNull()

private fun JSONObject.toDeliveryAddress(): DeliveryAddress =
    DeliveryAddress(
        label = optString("label"),
        title = optString("title"),
        subtitle = optString("subtitle"),
        point = getJSONObject("point").toGeoPoint(),
    )

private fun JSONObject.toGeoPoint(): GeoPoint =
    GeoPoint(
        latitude = optDouble("latitude"),
        longitude = optDouble("longitude"),
    )

private inline fun <reified T : Enum<T>> JSONObject.optEnum(name: String, fallback: T): T =
    nullableString(name)
        ?.let { value -> runCatching { enumValueOf<T>(value) }.getOrNull() }
        ?: fallback

private fun JSONObject.nullableString(name: String): String? =
    if (!has(name) || isNull(name)) null else optString(name)

private fun JSONObject.putNullable(name: String, value: String?): JSONObject =
    put(name, value ?: JSONObject.NULL)

private fun JSONObject.putNullable(name: String, value: JSONObject?): JSONObject =
    put(name, value ?: JSONObject.NULL)

private fun String.urlEncode(): String =
    URLEncoder.encode(this, Charsets.UTF_8.name())

private fun Throwable.toOrderSyncMessage(): String =
    "Order sync unavailable: ${message ?: javaClass.simpleName}"
