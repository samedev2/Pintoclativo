package com.agrotech.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = OnPrimaryLight,
    secondary = GreenSecondary,
    background = CanvasLight,
    onBackground = InkLight,
    surface = SurfaceLight,
    onSurface = InkLight,
    surfaceVariant = SurfaceAltLight,
    onSurfaceVariant = MutedTextLight,
    outline = HairlineLight,
    outlineVariant = HairlineLight
)

private val DarkColors = darkColorScheme(
    primary = GreenSecondary,
    onPrimary = Color.Black,
    secondary = GreenPrimaryDark
)

@Composable
fun AgroTechTheme(content: @Composable () -> Unit) {
    val colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        shapes = AgroTechShapes,
        typography = AgroTechTypography,
        content = content
    )
}
