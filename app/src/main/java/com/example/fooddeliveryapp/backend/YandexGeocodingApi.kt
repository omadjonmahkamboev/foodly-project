package com.example.fooddeliveryapp.backend

import com.example.fooddeliveryapp.BuildConfig
import com.example.fooddeliveryapp.ui.data.GeoPoint
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class YandexGeocodedAddress(
    val formattedAddress: String,
    val primaryAddressLine: String,
    val secondaryAddressLine: String,
)

object YandexGeocodingApi {
    private val cache = mutableMapOf<String, YandexGeocodedAddress>()

    suspend fun reverseGeocode(
        point: GeoPoint,
        languageCode: String,
    ): BackendResult<YandexGeocodedAddress> {
        val apiKey = BuildConfig.MAPKIT_API_KEY
        if (apiKey.isBlank()) return BackendResult.Error("Yandex geocoding key is not configured")

        val cacheKey = "${point.cacheKey()}|$languageCode"
        synchronized(cache) {
            cache[cacheKey]?.let { return BackendResult.Success(it) }
        }

        return withContext(Dispatchers.IO) {
            val url = "$GeocoderRoot?" + listOf(
                "apikey" to apiKey,
                "geocode" to point.geocodePoint(),
                "sco" to "longlat",
                "lang" to languageCode,
                "format" to "json",
                "results" to "1",
            ).toQueryString()

            when (val response = requestJson(url)) {
                is BackendResult.Error -> response
                is BackendResult.Success -> {
                    val address = response.data.toGeocodedAddressOrNull()
                        ?: return@withContext BackendResult.Error("Yandex geocoding response is empty")
                    synchronized(cache) {
                        cache[cacheKey] = address
                        trimCache(cache)
                    }
                    BackendResult.Success(address)
                }
            }
        }
    }

    private fun requestJson(url: String): BackendResult<JSONObject> {
        val connection = runCatching {
            (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = ConnectionTimeoutMs
                readTimeout = ReadTimeoutMs
                setRequestProperty("Accept", "application/json")
            }
        }.getOrElse { error ->
            return BackendResult.Error(error.toGeocodingMessage())
        }

        return try {
            val code = connection.responseCode
            val text = (if (code in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader(Charsets.UTF_8)
                ?.use { it.readText() }
                .orEmpty()

            if (code !in 200..299) {
                BackendResult.Error(text.toYandexErrorMessage().ifBlank { "Yandex geocoding failed with HTTP $code" })
            } else {
                BackendResult.Success(JSONObject(text.ifBlank { "{}" }))
            }
        } catch (error: Exception) {
            BackendResult.Error(error.toGeocodingMessage())
        } finally {
            connection.disconnect()
        }
    }

    private fun JSONObject.toGeocodedAddressOrNull(): YandexGeocodedAddress? {
        val geoObject = optJSONObject("response")
            ?.optJSONObject("GeoObjectCollection")
            ?.optJSONArray("featureMember")
            ?.optJSONObject(0)
            ?.optJSONObject("GeoObject")
            ?: return null

        val metadata = geoObject
            .optJSONObject("metaDataProperty")
            ?.optJSONObject("GeocoderMetaData")
        val addressMetadata = metadata?.optJSONObject("Address")

        val formattedAddress = listOfNotNull(
            addressMetadata?.optString("formatted")?.normalizeAddressPart(),
            metadata?.optString("text")?.takeIf { it.isNotBlank() },
            listOf(
                geoObject.optString("name").normalizeAddressPart(),
                geoObject.optString("description").normalizeAddressPart(),
            ).filterNotNull().joinToString(", ").takeIf { it.isNotBlank() },
        ).firstOrNull()?.normalizeAddressPart()
            ?: return null

        val geoName = geoObject.optString("name").normalizeAddressPart()
        val geoDescription = geoObject.optString("description").normalizeAddressPart()
        val street = addressMetadata?.optJSONArray("Components").componentValue("street")
            ?: addressMetadata?.optJSONArray("Components").componentValue("route")
        val house = addressMetadata?.optJSONArray("Components").componentValue("house")
        val premise = addressMetadata?.optJSONArray("Components").componentValue("premise")
        val locality = addressMetadata?.optJSONArray("Components").componentValue("locality")
            ?: addressMetadata?.optJSONArray("Components").componentValue("district")
            ?: addressMetadata?.optJSONArray("Components").componentValue("area")
        val streetAddress = listOfNotNull(
            street,
            house ?: premise,
        ).joinToString(", ").normalizeAddressPart()

        val primaryAddressLine = listOfNotNull(
            geoName?.takeUnless { it.equals(locality, ignoreCase = true) },
            streetAddress?.takeUnless { it.equals(geoName, ignoreCase = true) },
            locality?.takeUnless { it.equals(geoName, ignoreCase = true) },
            formattedAddress,
        ).firstOrNull()
            ?: formattedAddress

        val secondaryAddressLine = listOfNotNull(
            formattedAddress.takeUnless { it.equals(primaryAddressLine, ignoreCase = true) },
            geoDescription?.takeUnless { it.equals(primaryAddressLine, ignoreCase = true) },
            locality?.takeUnless { it.equals(primaryAddressLine, ignoreCase = true) },
        ).firstOrNull()
            ?: formattedAddress

        return YandexGeocodedAddress(
            formattedAddress = formattedAddress,
            primaryAddressLine = primaryAddressLine,
            secondaryAddressLine = secondaryAddressLine,
        )
    }

    private fun String.toYandexErrorMessage(): String =
        runCatching {
            val json = JSONObject(this)
            json.optString("message").takeIf { it.isNotBlank() }
                ?: json.optJSONArray("errors")
                    ?.let { errors ->
                        buildList {
                            for (index in 0 until errors.length()) {
                                errors.optString(index).takeIf { it.isNotBlank() }?.let(::add)
                            }
                        }.joinToString("; ")
                    }
                    .orEmpty()
        }.getOrDefault("")

    private fun GeoPoint.geocodePoint(): String =
        "%.6f,%.6f".format(Locale.US, longitude, latitude)

    private fun GeoPoint.cacheKey(): String =
        "%.4f,%.4f".format(Locale.US, latitude, longitude)

    private fun List<Pair<String, String>>.toQueryString(): String =
        joinToString("&") { (name, value) -> "${name.urlEncode()}=${value.urlEncode()}" }

    private fun String.urlEncode(): String =
        URLEncoder.encode(this, Charsets.UTF_8.name())

    private fun Throwable.toGeocodingMessage(): String =
        "Yandex geocoding unavailable: ${message ?: javaClass.simpleName}"

    private fun JSONArray?.componentValue(kind: String): String? {
        if (this == null) return null

        for (index in 0 until length()) {
            val component = optJSONObject(index) ?: continue
            if (!component.optString("kind").equals(kind, ignoreCase = true)) continue
            component.optString("name").normalizeAddressPart()?.let { return it }
        }
        return null
    }

    private fun String?.normalizeAddressPart(): String? =
        this
            ?.trim()
            ?.replace(Regex("\\s+"), " ")
            ?.takeIf { it.isNotBlank() }

    private fun <T> trimCache(cache: MutableMap<String, T>) {
        if (cache.size <= MaxCacheEntries) return
        val keysToRemove = cache.keys.take(cache.size - MaxCacheEntries)
        keysToRemove.forEach(cache::remove)
    }

    private const val GeocoderRoot = "https://geocode-maps.yandex.ru/v1"
    private const val ConnectionTimeoutMs = 4_500
    private const val ReadTimeoutMs = 5_500
    private const val MaxCacheEntries = 64
}
