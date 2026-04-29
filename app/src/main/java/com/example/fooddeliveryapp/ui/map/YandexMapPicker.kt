package com.example.fooddeliveryapp.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.fooddeliveryapp.BuildConfig
import com.example.fooddeliveryapp.backend.BackendResult
import com.example.fooddeliveryapp.backend.YandexRoutingApi
import com.example.fooddeliveryapp.MapKitController
import com.example.fooddeliveryapp.ui.LocalAppStrings
import com.example.fooddeliveryapp.ui.data.GeoPoint
import com.example.fooddeliveryapp.ui.theme.CardWhite
import com.example.fooddeliveryapp.ui.theme.Cream
import com.example.fooddeliveryapp.ui.theme.Orange
import com.example.fooddeliveryapp.ui.theme.OrangeSoft
import com.example.fooddeliveryapp.ui.theme.Sky
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.LineStyle
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import com.yandex.runtime.image.ImageProvider
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun YandexMapPicker(
    selectedPoint: GeoPoint,
    onPointSelected: (GeoPoint) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (BuildConfig.MAPKIT_API_KEY.isBlank()) {
        TappableMapPicker(
            selectedPoint = selectedPoint,
            onPointSelected = onPointSelected,
            modifier = modifier,
        )
    } else {
        YandexMapKitPointPicker(
            selectedPoint = selectedPoint,
            onPointSelected = onPointSelected,
            modifier = modifier,
        )
    }
}

@Composable
private fun YandexMapKitPointPicker(
    selectedPoint: GeoPoint,
    onPointSelected: (GeoPoint) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val latestOnPointSelected by rememberUpdatedState(onPointSelected)
    val mapView = remember(context) {
        AccessibleMapView(context)
    }
    var cameraInitialized by remember { mutableStateOf(false) }

    DisposableEffect(mapView, lifecycleOwner) {
        var mapViewStarted = false
        val yandexMap = mapView.mapWindow.map.apply {
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
        }
        val userLocationLayer = if (hasLocationPermission(context)) {
            MapKitController.initialize(context)
            MapKitFactory.getInstance().createUserLocationLayer(mapView.mapWindow).apply {
                isVisible = true
            }
        } else {
            null
        }

        fun startMapView() {
            if (mapViewStarted) return
            mapView.onStart()
            mapViewStarted = true
        }
        fun stopMapView() {
            if (!mapViewStarted) return
            mapView.onStop()
            mapViewStarted = false
        }
        val cameraListener = object : CameraListener {
            override fun onCameraPositionChanged(
                map: Map,
                cameraPosition: CameraPosition,
                cameraUpdateReason: CameraUpdateReason,
                finished: Boolean,
            ) {
                if (!finished) return
                latestOnPointSelected(cameraPosition.target.toGeoPoint())
            }
        }
        yandexMap.addCameraListener(cameraListener)

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> startMapView()
                Lifecycle.Event.ON_STOP -> stopMapView()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        startMapView()

        onDispose {
            yandexMap.removeCameraListener(cameraListener)
            lifecycleOwner.lifecycle.removeObserver(observer)
            userLocationLayer?.isVisible = false
            stopMapView()
        }
    }

    LaunchedEffect(selectedPoint) {
        val yandexMap = mapView.mapWindow.map
        val targetPoint = selectedPoint.toYandexPoint()
        val currentTarget = yandexMap.cameraPosition.target
        if (!cameraInitialized || !currentTarget.isCloseTo(targetPoint)) {
            val zoom = yandexMap.cameraPosition.zoom.takeIf { cameraInitialized } ?: 16f
            yandexMap.move(
                CameraPosition(targetPoint, zoom, 0f, 0f),
                Animation(Animation.Type.SMOOTH, 0.22f),
                null,
            )
            cameraInitialized = true
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(CardWhite),
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView },
        )
        MapSelectionOverlay()
        ZoomControlColumn(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp),
            onZoomIn = { mapView.adjustZoomBy(1f) },
            onZoomOut = { mapView.adjustZoomBy(-1f) },
        )
    }
}

@Composable
private fun TappableMapPicker(
    selectedPoint: GeoPoint,
    onPointSelected: (GeoPoint) -> Unit,
    modifier: Modifier = Modifier,
) {
    var zoomFactor by remember { mutableStateOf(1.2f) }
    val latestOnPointSelected by rememberUpdatedState(onPointSelected)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(CardWhite)
            .pointerInput(selectedPoint, zoomFactor) {
                detectTapGestures { offset ->
                    val xFraction = (offset.x / size.width).coerceIn(0f, 1f)
                    val yFraction = (offset.y / size.height).coerceIn(0f, 1f)
                    val latitudeRange = 0.085 / zoomFactor
                    val longitudeRange = 0.085 / zoomFactor
                    val latitude = selectedPoint.latitude + (0.5f - yFraction) * latitudeRange
                    val longitude = selectedPoint.longitude + (xFraction - 0.5f) * longitudeRange
                    latestOnPointSelected(GeoPoint(latitude, longitude))
                }
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(ComposeColor(0xFFF9FAFB))
            val gridColor = ComposeColor(0xFFD6D7DB)
            val verticalStep = size.width / (7f + zoomFactor)
            val horizontalStep = size.height / (5f + zoomFactor)
            repeat(10) { index ->
                val x = verticalStep * index
                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 2f)
            }
            repeat(8) { index ->
                val y = horizontalStep * index
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 2f)
            }
        }
        MapSelectionOverlay()
        ZoomControlColumn(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp),
            onZoomIn = { zoomFactor = (zoomFactor * 1.18f).coerceAtMost(3.4f) },
            onZoomOut = { zoomFactor = (zoomFactor / 1.18f).coerceAtLeast(0.9f) },
        )
    }
}

@Composable
private fun MapSelectionOverlay() {
    Box(modifier = Modifier.fillMaxSize()) {
        Icon(
            imageVector = Icons.Rounded.LocationOn,
            contentDescription = null,
            tint = Orange,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-28).dp)
                .size(58.dp),
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(10.dp)
                .clip(CircleShape)
                .background(CardWhite),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Orange),
            )
        }
    }
}

@Composable
private fun ZoomControlColumn(
    modifier: Modifier = Modifier,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ZoomButton(
            icon = Icons.Default.Add,
            onClick = onZoomIn,
        )
        ZoomButton(
            icon = Icons.Default.Remove,
            onClick = onZoomOut,
        )
    }
}

@Composable
private fun ZoomButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(ComposeColor(0xFF0D1015)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CardWhite,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
fun YandexRouteMap(
    startPoint: GeoPoint,
    endPoint: GeoPoint,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 34.dp,
    courierPoint: GeoPoint? = null,
    restaurantPoint: GeoPoint? = null,
    startLabel: String = "A",
    endLabel: String = "B",
) {
    if (BuildConfig.MAPKIT_API_KEY.isBlank()) {
        RouteMapFallback(modifier = modifier)
        return
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val mapView = remember(context) {
        AccessibleMapView(context)
    }
    var routePoints by remember(startPoint, endPoint) {
        mutableStateOf(listOf(startPoint, endPoint))
    }

    LaunchedEffect(startPoint, endPoint) {
        routePoints = listOf(startPoint, endPoint)
        when (val result = YandexRoutingApi.route(startPoint, endPoint)) {
            is BackendResult.Success -> routePoints = result.data.points
            is BackendResult.Error -> Unit
        }
    }

    DisposableEffect(mapView, lifecycleOwner) {
        var mapViewStarted = false
        fun startMapView() {
            if (mapViewStarted) return
            mapView.onStart()
            mapViewStarted = true
        }
        fun stopMapView() {
            if (!mapViewStarted) return
            mapView.onStop()
            mapViewStarted = false
        }
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> startMapView()
                Lifecycle.Event.ON_STOP -> stopMapView()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        startMapView()

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            stopMapView()
        }
    }

    AndroidView(
        modifier = modifier.clip(RoundedCornerShape(cornerRadius)),
        factory = {
            mapView.apply {
                drawRouteObjects(
                    mapView = this,
                    routePoints = routePoints,
                    startPoint = startPoint,
                    endPoint = endPoint,
                    courierPoint = courierPoint,
                    restaurantPoint = restaurantPoint,
                    startLabel = startLabel,
                    endLabel = endLabel,
                    context = context,
                )
            }
        },
        update = { view ->
            drawRouteObjects(
                mapView = view,
                routePoints = routePoints,
                startPoint = startPoint,
                endPoint = endPoint,
                courierPoint = courierPoint,
                restaurantPoint = restaurantPoint,
                startLabel = startLabel,
                endLabel = endLabel,
                context = view.context,
            )
        },
    )
}

private fun drawRouteObjects(
    mapView: MapView,
    routePoints: List<GeoPoint>,
    startPoint: GeoPoint,
    endPoint: GeoPoint,
    courierPoint: GeoPoint?,
    restaurantPoint: GeoPoint?,
    startLabel: String,
    endLabel: String,
    context: Context,
) {
    val drawState = RouteDrawState(
        routePoints = routePoints,
        startPoint = startPoint,
        endPoint = endPoint,
        courierPoint = courierPoint,
        restaurantPoint = restaurantPoint,
        startLabel = startLabel,
        endLabel = endLabel,
    )
    if (mapView.tag == drawState) return

    val yandexMap = mapView.mapWindow.map
    yandexMap.mapObjects.clear()
    val yandexRoutePoints = routePoints.map { it.toYandexPoint() }
    yandexMap.mapObjects.addPolyline(Polyline(yandexRoutePoints)).apply {
        setStrokeColor(AndroidColor.rgb(255, 122, 28))
        setStyle(
            LineStyle()
                .setStrokeWidth(6f)
                .setOutlineColor(AndroidColor.WHITE)
                .setOutlineWidth(2f),
        )
    }

    val markers = buildList {
        val routeStartPoint = courierPoint ?: startPoint
        add(MapMarker(routeStartPoint, startLabel))
        if (!endPoint.isNear(routeStartPoint)) {
            add(MapMarker(endPoint, endLabel))
        }
    }

    markers.forEach { marker ->
        yandexMap.mapObjects.addPlacemark().apply {
            geometry = marker.point.toYandexPoint()
            setIcon(mapPinProvider(context, marker.label))
            setIconStyle(
                IconStyle().apply {
                    anchor = PointF(0.5f, 1f)
                    scale = 0.9f
                },
            )
        }
    }

    val cameraPoints = (routePoints + markers.map { it.point }).ifEmpty { listOf(startPoint, endPoint) }
    val midpoint = GeoPoint(
        latitude = (cameraPoints.minOf { it.latitude } + cameraPoints.maxOf { it.latitude }) / 2,
        longitude = (cameraPoints.minOf { it.longitude } + cameraPoints.maxOf { it.longitude }) / 2,
    )
    yandexMap.move(
        CameraPosition(
            midpoint.toYandexPoint(),
            routeZoom(cameraPoints),
            0f,
            0f,
        ),
    )
    mapView.tag = drawState
}

private fun drawPickerObject(
    mapView: MapView,
    point: Point,
    context: Context,
) {
    val drawState = PickerDrawState(point.latitude, point.longitude)
    if (mapView.tag == drawState) return

    val yandexMap = mapView.mapWindow.map
    yandexMap.mapObjects.clear()
    yandexMap.mapObjects.addPlacemark().apply {
        geometry = point
        setIcon(mapPinProvider(context))
        setIconStyle(
            IconStyle().apply {
                anchor = PointF(0.5f, 1f)
                scale = 0.95f
            },
        )
    }
    yandexMap.move(CameraPosition(point, 15f, 0f, 0f))
    mapView.tag = drawState
}

private fun mapPinProvider(
    context: Context,
    label: String? = null,
): ImageProvider =
    ImageProvider.fromBitmap(createMapPinBitmap(context, label))

private data class PickerDrawState(
    val latitude: Double,
    val longitude: Double,
)

private data class RouteDrawState(
    val routePoints: List<GeoPoint>,
    val startPoint: GeoPoint,
    val endPoint: GeoPoint,
    val courierPoint: GeoPoint?,
    val restaurantPoint: GeoPoint?,
    val startLabel: String,
    val endLabel: String,
)

private data class MapMarker(
    val point: GeoPoint,
    val label: String,
)

private class AccessibleMapView(context: Context) : MapView(context) {
    init {
        val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        var downX = 0f
        var downY = 0f

        setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.x
                    downY = event.y
                    view.parent?.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_MOVE -> view.parent?.requestDisallowInterceptTouchEvent(true)
                MotionEvent.ACTION_UP -> {
                    view.parent?.requestDisallowInterceptTouchEvent(false)
                    val isClick =
                        abs(event.x - downX) <= touchSlop &&
                            abs(event.y - downY) <= touchSlop
                    if (isClick) {
                        view.performClick()
                    }
                }
                MotionEvent.ACTION_CANCEL -> view.parent?.requestDisallowInterceptTouchEvent(false)
            }
            false
        }
    }

    override fun performClick(): Boolean = super.performClick()
}

private fun GeoPoint.toYandexPoint(): Point = Point(latitude, longitude)

private fun Point.toGeoPoint(): GeoPoint = GeoPoint(latitude, longitude)

private fun Point.isCloseTo(other: Point): Boolean =
    abs(latitude - other.latitude) < 0.00001 && abs(longitude - other.longitude) < 0.00001

private fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

private fun MapView.adjustZoomBy(delta: Float) {
    val map = mapWindow.map
    val camera = map.cameraPosition
    map.move(
        CameraPosition(
            camera.target,
            (camera.zoom + delta).coerceIn(11f, 20f),
            camera.azimuth,
            camera.tilt,
        ),
        Animation(Animation.Type.SMOOTH, 0.18f),
        null,
    )
}

private fun GeoPoint.isNear(other: GeoPoint): Boolean =
    abs(latitude - other.latitude) < 0.00001 && abs(longitude - other.longitude) < 0.00001

private fun routeZoom(points: List<GeoPoint>): Float {
    val minLatitude = points.minOfOrNull { it.latitude } ?: return 13.2f
    val maxLatitude = points.maxOfOrNull { it.latitude } ?: return 13.2f
    val minLongitude = points.minOfOrNull { it.longitude } ?: return 13.2f
    val maxLongitude = points.maxOfOrNull { it.longitude } ?: return 13.2f
    val delta = max(
        abs(maxLatitude - minLatitude),
        abs(maxLongitude - minLongitude),
    )
    return when {
        delta < 0.01 -> 14.5f
        delta < 0.03 -> 13.2f
        delta < 0.06 -> 12.2f
        else -> 11.2f
    }
}

private fun createMapPinBitmap(context: Context, label: String? = null): Bitmap {
    val density = context.resources.displayMetrics.density
    val labelText = label
        ?.trim()
        ?.replace(Regex("\\s+"), " ")
        ?.take(14)
        .orEmpty()
    val pinWidth = 44f * density
    val pinHeight = 58f * density
    val chipHeight = if (labelText.isBlank()) 0f else 28f * density
    val chipBottomSpacing = if (labelText.isBlank()) 0f else 10f * density
    val chipPadding = 12f * density
    val measurePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 12f * density
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    val labelWidth = if (labelText.isBlank()) 0f else measurePaint.measureText(labelText)
    val width = max(pinWidth, labelWidth + chipPadding * 2f).roundToInt()
    val height = (pinHeight + chipHeight + chipBottomSpacing).roundToInt()
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val centerX = width / 2f
    val circleY = chipHeight + chipBottomSpacing + 20f * density

    paint.style = Paint.Style.FILL
    paint.color = AndroidColor.argb(54, 0, 0, 0)
    canvas.drawOval(
        centerX - 9f * density,
        height - 9f * density,
        centerX + 9f * density,
        height - 3f * density,
        paint,
    )

    if (labelText.isNotBlank()) {
        val chipWidth = labelWidth + chipPadding * 2f
        val chipLeft = (width - chipWidth) / 2f
        val chipRect = RectF(chipLeft, 0f, chipLeft + chipWidth, chipHeight)

        paint.style = Paint.Style.FILL
        paint.color = AndroidColor.WHITE
        canvas.drawRoundRect(chipRect, chipHeight / 2f, chipHeight / 2f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f * density
        paint.color = AndroidColor.rgb(255, 122, 26)
        canvas.drawRoundRect(chipRect, chipHeight / 2f, chipHeight / 2f, paint)

        paint.style = Paint.Style.FILL
        paint.color = AndroidColor.rgb(255, 122, 26)
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 12f * density
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        val chipTextY = chipRect.centerY() - (paint.descent() + paint.ascent()) / 2f
        canvas.drawText(labelText, chipRect.centerX(), chipTextY, paint)
    }

    val pinPath = Path().apply {
        moveTo(centerX, height - 7f * density)
        cubicTo(
            centerX - 17f * density,
            circleY + 13f * density,
            centerX - 17f * density,
            circleY - 14f * density,
            centerX,
            4f * density,
        )
        cubicTo(
            centerX + 17f * density,
            circleY - 14f * density,
            centerX + 17f * density,
            circleY + 13f * density,
            centerX,
            height - 7f * density,
        )
        close()
    }

    paint.color = AndroidColor.rgb(255, 122, 26)
    paint.style = Paint.Style.FILL
    canvas.drawPath(pinPath, paint)

    paint.color = AndroidColor.WHITE
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 3f * density
    canvas.drawPath(pinPath, paint)

    paint.style = Paint.Style.FILL
    paint.color = AndroidColor.WHITE
    canvas.drawCircle(centerX, circleY, 7f * density, paint)
    paint.color = AndroidColor.rgb(255, 122, 26)
    canvas.drawCircle(centerX, circleY, 3f * density, paint)

    return bitmap
}

@Composable
private fun MissingMapKeyCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(30.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(OrangeSoft, Cream, CardWhite, Sky.copy(alpha = 0.18f)),
                    ),
                )
                .padding(28.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Orange.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = Orange,
                        modifier = Modifier.size(36.dp),
                    )
                }

                Text(
                    text = "Yandex карта готова к подключению",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Добавь ключ в local.properties как MAPKIT_API_KEY, и экран выбора адреса сразу заработает на реальной карте.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RouteMapFallback(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(OrangeSoft, Cream, CardWhite, Sky.copy(alpha = 0.18f)),
                ),
            )
            .padding(28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = null,
                tint = Orange,
                modifier = Modifier.size(42.dp),
            )
            Text(
                text = "Карта маршрута готова к подключению",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Добавь MAPKIT_API_KEY в local.properties, чтобы видеть маршрут курьера.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
