package com.example.fooddeliveryapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Orange,
    onPrimary = CardWhite,
    secondary = Gold,
    onSecondary = Night,
    tertiary = Sky,
    background = Cream,
    onBackground = Ink,
    surface = CardWhite,
    onSurface = Ink,
    surfaceVariant = Sand,
    onSurfaceVariant = InkSoft,
    outline = Border,
)

private val DarkColorScheme = darkColorScheme(
    primary = Orange,
    onPrimary = CardWhite,
    secondary = Gold,
    onSecondary = Night,
    tertiary = Sky,
    background = Night,
    onBackground = CardWhite,
    surface = NightSoft,
    onSurface = CardWhite,
    surfaceVariant = Color(0xFF202329),
    onSurfaceVariant = CardWhite.copy(alpha = 0.72f),
    outline = Color(0xFF323640),
)

@Composable
fun FoodDeliveryAppTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme && dynamicColor.not()) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
