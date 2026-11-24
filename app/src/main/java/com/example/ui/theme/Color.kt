package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

val MosqueDeepEmerald = Color(0xFF061E1A)
val MosqueCardSurface = Color(0xFF0C352E)
val MosqueGoldAccent = Color(0xFFE6B84C)
val MosqueSoftGold = Color(0xFFFFE082)
val MosqueMintText = Color(0xFFB2DFDB)
val SettingsSurfaceDark = Color(0xFF0D1B1E)
val SettingsCardDark = Color(0xFF15292E)
val SettingsInputBg = Color(0xFF1D363C)

/**
 * Ensures text over a custom background color remains legible even if user picks extreme or transparent colors.
 */
fun ensureReadableColor(
    proposedTextColor: Color,
    backgroundColor: Color,
    backdropColor: Color = Color(0xFF10141C)
): Color {
    val a = backgroundColor.alpha
    val effectiveBg = if (a >= 0.95f) {
        backgroundColor
    } else {
        Color(
            red = backgroundColor.red * a + backdropColor.red * (1f - a),
            green = backgroundColor.green * a + backdropColor.green * (1f - a),
            blue = backgroundColor.blue * a + backdropColor.blue * (1f - a),
            alpha = 1f
        )
    }
    val bgLum = effectiveBg.luminance()
    val textLum = proposedTextColor.luminance()
    val contrast = (maxOf(bgLum, textLum) + 0.05f) / (minOf(bgLum, textLum) + 0.05f)
    return if (contrast >= 2.6f) {
        proposedTextColor
    } else {
        if (bgLum > 0.45f) Color(0xFF10181A) else Color(0xFFFFFFFF)
    }
}
