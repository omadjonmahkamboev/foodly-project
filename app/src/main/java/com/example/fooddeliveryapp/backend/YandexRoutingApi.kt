package com.example.fooddeliveryapp.backend

import com.example.fooddeliveryapp.BuildConfig
import com.example.fooddeliveryapp.ui.data.GeoPoint
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.DrivingSummarySession
import com.yandex.mapkit.directions.driving.Summary
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.geometry.Point
import com.yandex.runtime.Error

data class YandexRouteDetails(
    val points: List<GeoPoint>,
    val distanceMeters: Double,
    val durationSeconds: Double,
)

data class YandexDistanceMatrixElement(
    val distanceMeters: Double,
    val durationSeconds: Double,
)

object YandexRoutingApi {
    private val routeCache = mutableMapOf<String, YandexRouteDetails>()
    private val matrixCache = mutableMapOf<String, YandexDistanceMatrixElement>()
    private val drivingRouter by lazy {
        DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.COMBINED)
    }

    suspend fun route(
        startPoint: GeoPoint,
        endPoint: GeoPoint,
    ): BackendResult<YandexRouteDetails> {
        if (BuildConfig.MAPKIT_API_KEY.isBlank()) {
            return BackendResult.Error("Yandex MapKit key is not configured")
        }

        val cacheKey = "${startPoint.cacheKey()}|${endPoint.cacheKey()}"
        synchronized(routeCache) {
            routeCache[cacheKey]?.let { return BackendResult.Success(it) }
        }

        return when (val response = requestDrivingRoutes(startPoint, endPoint)) {
            is BackendResult.Error -> response
            is BackendResult.Success -> {
                val details = response.data
                    .map(::toRouteDetails)
                    .minByOrNull(YandexRouteDetails::distanceMeters)
                    ?: return BackendResult.Error("Yandex route response is empty")
                synchronized(routeCache) {
                    routeCache[cacheKey] = details
                    trimCache(routeCache)
                }
                synchronized(matrixCache) {
                    matrixCache[cacheKey] = YandexDistanceMatrixElement(
                        distanceMeters = details.distanceMeters,
                        durationSeconds = details.durationSeconds,
                    )
                    trimCache(matrixCache)
                }
                BackendResult.Success(details)
            }
        }
    }

    suspend fun distance(
        startPoint: GeoPoint,
        endPoint: GeoPoint,
    ): BackendResult<YandexDistanceMatrixElement> {
        if (BuildConfig.MAPKIT_API_KEY.isBlank()) {
            return BackendResult.Error("Yandex MapKit key is not configured")
        }

        val cacheKey = "${startPoint.cacheKey()}|${endPoint.cacheKey()}"
        synchronized(routeCache) {
            routeCache[cacheKey]?.let {
                return BackendResult.Success(
                    YandexDistanceMatrixElement(
                        distanceMeters = it.distanceMeters,
                        durationSeconds = it.durationSeconds,
                    ),
                )
            }
        }
        synchronized(matrixCache) {
            matrixCache[cacheKey]?.let { return BackendResult.Success(it) }
        }

        return when (val response = requestDrivingSummaries(startPoint, endPoint)) {
            is BackendResult.Error -> response
            is BackendResult.Success -> {
                val element = response.data
                    .map(::toDistanceElement)
                    .minByOrNull(YandexDistanceMatrixElement::distanceMeters)
                    ?: return BackendResult.Error("Yandex distance matrix response is empty")
                synchronized(matrixCache) {
                    matrixCache[cacheKey] = element
                    trimCache(matrixCache)
                }
                BackendResult.Success(element)
            }
        }
    }

    private suspend fun requestDrivingRoutes(
        startPoint: GeoPoint,
        endPoint: GeoPoint,
    ): BackendResult<List<DrivingRoute>> =
        withContext(Dispatchers.Main.immediate) {
            suspendCancellableCoroutine { continuation ->
                val listener = object : DrivingSession.DrivingRouteListener {
                    override fun onDrivingRoutes(routes: List<DrivingRoute>) {
                        continuation.resume(
                            if (routes.isEmpty()) {
                                BackendResult.Error("Yandex route response is empty")
                            } else {
                                BackendResult.Success(routes.toList())
                            },
                        )
                    }

                    override fun onDrivingRoutesError(error: Error) {
                        continuation.resume(BackendResult.Error(error.toRoutingMessage()))
                    }
                }

                var session: DrivingSession? = null
                continuation.invokeOnCancellation { session?.cancel() }
                session = drivingRouter.requestRoutes(
                    buildRoutePoints(startPoint, endPoint),
                    DrivingOptions().apply { routesCount = AlternativeRoutesCount },
                    VehicleOptions(),
                    listener,
                )
            }
        }

    private suspend fun requestDrivingSummaries(
        startPoint: GeoPoint,
        endPoint: GeoPoint,
    ): BackendResult<List<Summary>> =
        withContext(Dispatchers.Main.immediate) {
            suspendCancellableCoroutine { continuation ->
                val listener = object : DrivingSummarySession.DrivingSummaryListener {
                    override fun onDrivingSummaries(summaries: List<Summary>) {
                        continuation.resume(
                            if (summaries.isEmpty()) {
                                BackendResult.Error("Yandex distance matrix response is empty")
                            } else {
                                BackendResult.Success(summaries.toList())
                            },
                        )
                    }

                    override fun onDrivingSummariesError(error: Error) {
                        continuation.resume(BackendResult.Error(error.toRoutingMessage()))
                    }
                }

                var session: DrivingSummarySession? = null
                continuation.invokeOnCancellation { session?.cancel() }
                session = drivingRouter.requestRoutesSummary(
                    buildRoutePoints(startPoint, endPoint),
                    DrivingOptions().apply { routesCount = AlternativeRoutesCount },
                    VehicleOptions(),
                    listener,
                )
            }
        }

    private fun buildRoutePoints(
        startPoint: GeoPoint,
        endPoint: GeoPoint,
    ): List<RequestPoint> =
        listOf(
            RequestPoint(startPoint.toYandexPoint(), RequestPointType.WAYPOINT, null, null, null),
            RequestPoint(endPoint.toYandexPoint(), RequestPointType.WAYPOINT, null, null, null),
        )

    private fun toRouteDetails(route: DrivingRoute): YandexRouteDetails {
        val geometry = route.geometry.points.map { point ->
            GeoPoint(latitude = point.latitude, longitude = point.longitude)
        }.deduplicateAdjacent()
        val weight = route.metadata.weight
        val distance = weight.distance.value.toDouble()
        val duration = weight.timeWithTraffic.value.toDouble()
        return YandexRouteDetails(
            points = geometry,
            distanceMeters = distance,
            durationSeconds = duration,
        )
    }

    private fun toDistanceElement(summary: Summary): YandexDistanceMatrixElement {
        val weight = summary.weight
        return YandexDistanceMatrixElement(
            distanceMeters = weight.distance.value.toDouble(),
            durationSeconds = weight.timeWithTraffic.value.toDouble(),
        )
    }

    private fun List<GeoPoint>.deduplicateAdjacent(): List<GeoPoint> =
        fold(mutableListOf()) { result, point ->
            if (result.lastOrNull() != point) result.add(point)
            result
        }

    private fun GeoPoint.toYandexPoint(): Point =
        Point(latitude, longitude)

    private fun GeoPoint.cacheKey(): String =
        "%.4f,%.4f".format(Locale.US, latitude, longitude)

    private fun Error.toRoutingMessage(): String =
        "Yandex routing unavailable: ${javaClass.simpleName.ifBlank { "UnknownError" }}"

    private fun <T> trimCache(cache: MutableMap<String, T>) {
        if (cache.size <= MaxCacheEntries) return
        val keysToRemove = cache.keys.take(cache.size - MaxCacheEntries)
        keysToRemove.forEach(cache::remove)
    }

    private const val MaxCacheEntries = 48
    private const val AlternativeRoutesCount = 3
}
