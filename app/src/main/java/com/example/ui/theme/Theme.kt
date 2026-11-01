package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SoftGold,
    onPrimary = DeepNavyDark,
    primaryContainer = DeepNavySurface,
    onPrimaryContainer = WarmCream,
    secondary = SoftGoldLight,
    onSecondary = DeepNavyDark,
    secondaryContainer = DeepNavyCard,
    onSecondaryContainer = WarmCreamLight,
    tertiary = WarmCream,
    onTertiary = DeepNavy,
    background = DarkBackground,
    onBackground = TextLight,
    surface = DarkSurface,
    onSurface = TextLight,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextMutedLight,
    outline = DarkBorder,
    outlineVariant = DeepNavyBorder,
    error = PriorityHigh,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = DeepNavy,
    onPrimary = WarmCream,
    primaryContainer = SoftGoldContainerLight,
    onPrimaryContainer = DeepNavy,
    secondary = SoftGold,
    onSecondary = DeepNavyDark,
    secondaryContainer = WarmCream,
    onSecondaryContainer = DeepNavy,
    tertiary = SoftGoldDark,
    onTertiary = WarmCreamLight,
    background = LightBackground,
    onBackground = TextDark,
    surface = LightSurface,
    onSurface = TextDark,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextMutedDark,
    outline = LightBorder,
    outlineVariant = WarmCreamDark,
    error = PriorityHigh,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use intentional Deep Navy & Soft Gold palette
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
