package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MosqueDarkColorScheme = darkColorScheme(
    primary = MosqueGoldAccent,
    onPrimary = Color(0xFF1A1400),
    secondary = MosqueMintText,
    onSecondary = Color(0xFF04221E),
    tertiary = MosqueSoftGold,
    background = MosqueDeepEmerald,
    onBackground = Color.White,
    surface = SettingsCardDark,
    onSurface = Color.White,
    surfaceVariant = SettingsInputBg,
    onSurfaceVariant = Color(0xFFE0ECEA)
)

@Composable
fun MyApplicationTheme(
    fontFamilyKey: String = "cairo",
    content: @Composable () -> Unit
) {
    val chosenFamily = resolveFontFamily(fontFamilyKey)
    MaterialTheme(
        colorScheme = MosqueDarkColorScheme,
        typography = mosqueTypography(chosenFamily),
        content = content
    )
}
