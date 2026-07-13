package com.ql.chat.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = DarkBackground,
    primaryContainer = GoldContainer,
    onPrimaryContainer = GoldLight,
    secondary = GoldLight,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = WhiteSoft,
    surface = DarkSurface,
    onSurface = WhiteSoft,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = WhiteMuted,
    outline = DarkOutline,
    outlineVariant = DarkSurfaceVariant
)

private val QLShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun QLTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = QLTypography,
        shapes = QLShapes,
        content = content
    )
}
