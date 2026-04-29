package com.example.fooddeliveryapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.fooddeliveryapp.ui.LocalAppLanguage
import com.example.fooddeliveryapp.ui.LocalAppStrings
import com.example.fooddeliveryapp.ui.formatCurrency
import com.example.fooddeliveryapp.ui.data.OrderStatus
import com.example.fooddeliveryapp.ui.theme.Border
import com.example.fooddeliveryapp.ui.theme.CardWhite
import com.example.fooddeliveryapp.ui.theme.Cream
import com.example.fooddeliveryapp.ui.theme.Gold
import com.example.fooddeliveryapp.ui.theme.Ink
import com.example.fooddeliveryapp.ui.theme.InkSoft
import com.example.fooddeliveryapp.ui.theme.Night
import com.example.fooddeliveryapp.ui.theme.Orange
import com.example.fooddeliveryapp.ui.theme.OrangeDeep
import com.example.fooddeliveryapp.ui.theme.OrangeMist
import com.example.fooddeliveryapp.ui.theme.OrangeSoft
import com.example.fooddeliveryapp.ui.theme.Rose
import com.example.fooddeliveryapp.ui.theme.Success

@Composable
fun FoodlyImage(
    model: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val drawableResourceId = FoodDrawableRegistry.resolve(model)

    if (drawableResourceId != null) {
        Image(
            painter = painterResource(drawableResourceId),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
        )
    } else {
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
        )
    }
}

@Composable
fun DecorativeBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Orange.copy(alpha = 0.25f), Color.Transparent),
            ),
            radius = size.minDimension * 0.36f,
            center = Offset(size.width * 0.9f, size.height * 0.15f),
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(com.example.fooddeliveryapp.ui.theme.Sky.copy(alpha = 0.18f), Color.Transparent),
            ),
            radius = size.minDimension * 0.42f,
            center = Offset(size.width * 0.1f, size.height * 0.88f),
        )
        drawArc(
            color = Orange.copy(alpha = 0.18f),
            startAngle = 180f,
            sweepAngle = 70f,
            useCenter = false,
            topLeft = Offset(size.width * 0.64f, size.height * 0.72f),
            size = Size(size.width * 0.52f, size.width * 0.52f),
            style = Stroke(width = 22f, cap = StrokeCap.Round),
        )
    }
}

@Composable
fun BrandMark() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Food",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Orange, OrangeDeep))),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "ly",
                style = MaterialTheme.typography.titleMedium,
                color = CardWhite,
            )
        }
    }
}

@Composable
fun TopBar(
    title: String,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CircleIconButton(Icons.AutoMirrored.Filled.ArrowBack, onBack)
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        TextButton(onClick = onAction) {
            Text(actionLabel, color = Orange)
        }
    }
}

@Composable
fun AccentChip(
    text: String,
    accent: Color,
    onAccent: Color = Ink,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(accent.copy(alpha = 0.16f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = onAccent,
        )
    }
}

@Composable
fun InfoPill(text: String, accent: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(accent.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun CircleIconSurface(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun CircleIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 58.dp),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = CardWhite),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    accent: Color = Night,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = accent.copy(alpha = 0.08f),
            contentColor = accent,
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun PrimaryMiniButton(
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Orange),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = CardWhite,
        )
    }
}

@Composable
fun QtyButton(
    symbol: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(CardWhite.copy(alpha = 0.12f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, style = MaterialTheme.typography.titleMedium, color = CardWhite)
    }
}

@Composable
fun BreakdownRow(
    label: String,
    value: String,
    highlight: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = if (highlight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            color = if (highlight) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = if (highlight) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun StatusChip(status: OrderStatus) {
    val strings = LocalAppStrings.current
    val (label, accent) = when (status) {
        OrderStatus.Preparing -> strings.status(status) to Gold
        OrderStatus.OnTheWay -> strings.status(status) to Orange
        OrderStatus.Delivered -> strings.status(status) to Success
        OrderStatus.Cancelled -> strings.status(status) to Rose
    }
    AccentChip(label, accent)
}

@Composable
fun SmallMetric(value: String, subtitle: String) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite.copy(alpha = 0.08f)),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(value, style = MaterialTheme.typography.titleMedium, color = CardWhite)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = CardWhite.copy(alpha = 0.68f))
        }
    }
}

@Composable
fun BrandHeaderChip() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Orange.copy(alpha = 0.18f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = "FOODLY DELIVERY",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = Orange,
        )
    }
}

@Composable
fun ToggleRow(
    isPrimary: Boolean,
    firstLabel: String,
    secondLabel: String,
    onFirst: () -> Unit,
    onSecond: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
    ) {
        Button(
            onClick = onFirst,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPrimary.not()) Orange else Color.Transparent,
                contentColor = if (isPrimary.not()) CardWhite else MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            elevation = null,
        ) {
            Text(firstLabel)
        }
        Button(
            onClick = onSecond,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPrimary) Orange else Color.Transparent,
                contentColor = if (isPrimary) CardWhite else MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            elevation = null,
        ) {
            Text(secondLabel)
        }
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isPassword: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = keyboardOptions,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            placeholder = {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            leadingIcon = leading,
            trailingIcon = trailing,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Orange.copy(alpha = 0.42f),
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
        )
    }
}

@Composable
fun SearchFieldStub(onClick: () -> Unit) {
    val strings = LocalAppStrings.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = strings.searchPlaceholder,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    lines: List<String>,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                if (actionLabel != null && onAction != null) {
                    TextButton(onClick = onAction) { Text(actionLabel, color = Orange) }
                }
            }
            lines.forEachIndexed { index, text ->
                Text(
                    text = text,
                    style = if (index == 0) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                    color = if (index == 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun PaymentBreakdownCard(
    subtotal: Int,
    delivery: Int,
    service: Int,
    total: Int,
    discount: Int = 0,
) {
    val strings = LocalAppStrings.current
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BreakdownRow(strings.items, subtotal.asPrice())
            BreakdownRow(strings.delivery, delivery.asPrice())
            BreakdownRow(strings.service, service.asPrice())
            if (discount > 0) {
                BreakdownRow(strings.discount, "-${discount.asPrice()}")
            }
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp),
            ) {
                drawLine(color = Border, start = Offset.Zero, end = Offset(size.width, 0f), strokeWidth = size.height)
            }
            BreakdownRow(strings.total.removeSuffix(":"), total.asPrice(), highlight = true)
        }
    }
}

@Composable
fun EmptyStateScreen(
    title: String,
    description: String,
    actionLabel: String,
    onAction: () -> Unit,
    onBack: () -> Unit,
) {
    val strings = LocalAppStrings.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
    ) {
        TopBar(title = strings.cart, onBack = onBack)
        Spacer(modifier = Modifier.weight(1f))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(34.dp))
                    .background(OrangeSoft),
                contentAlignment = Alignment.Center,
            ) {
                EmptyPlateIllustration(modifier = Modifier.size(92.dp))
            }
            Text(title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
            Text(description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            PrimaryButton(text = actionLabel, onClick = onAction)
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun EmptyPlateIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val plateCenter = Offset(size.width * 0.5f, size.height * 0.55f)
        val plateRadius = size.minDimension * 0.30f
        drawCircle(
            color = CardWhite,
            radius = plateRadius,
            center = plateCenter,
        )
        drawCircle(
            color = Orange.copy(alpha = 0.16f),
            radius = plateRadius * 0.68f,
            center = plateCenter,
        )
        drawCircle(
            color = CardWhite,
            radius = plateRadius * 0.48f,
            center = plateCenter,
        )
        drawLine(
            color = Orange,
            start = Offset(size.width * 0.18f, size.height * 0.22f),
            end = Offset(size.width * 0.18f, size.height * 0.83f),
            strokeWidth = 8f,
            cap = StrokeCap.Round,
        )
        repeat(3) { index ->
            drawLine(
                color = Orange,
                start = Offset(size.width * (0.12f + index * 0.06f), size.height * 0.22f),
                end = Offset(size.width * (0.12f + index * 0.06f), size.height * 0.39f),
                strokeWidth = 5f,
                cap = StrokeCap.Round,
            )
        }
        drawLine(
            color = OrangeDeep,
            start = Offset(size.width * 0.82f, size.height * 0.24f),
            end = Offset(size.width * 0.82f, size.height * 0.83f),
            strokeWidth = 8f,
            cap = StrokeCap.Round,
        )
        drawCircle(
            color = OrangeDeep,
            radius = size.minDimension * 0.07f,
            center = Offset(size.width * 0.82f, size.height * 0.23f),
        )
    }
}

@Composable
fun EmptyOrdersCard(
    title: String,
    description: String,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "📦", style = MaterialTheme.typography.displayLarge)
            Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun Int.asPrice(): String = formatCurrency(this, LocalAppLanguage.current)
