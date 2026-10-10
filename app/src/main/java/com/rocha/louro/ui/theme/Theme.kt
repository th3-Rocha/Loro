package com.rocha.louro.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Palette borrowed from EVA-01: eggplant purple, neon green for action, orange for rewards. */
object LouroColors {
    val Eggplant = Color(0xFF1E0B36)
    val EggplantRaised = Color(0xFF2E1552)
    val EggplantBright = Color(0xFF45247A)
    val EggplantEdge = Color(0xFF150726)
    val Lilac = Color(0xFFB79CE8)
    val Neon = Color(0xFFA6F21C)
    val NeonEdge = Color(0xFF6FA80C)
    val Orange = Color(0xFFFF7A1A)
    val OrangeEdge = Color(0xFFB8500A)
    val Miss = Color(0xFFFF5C7A)
    val Text = Color(0xFFF4EEFF)
}

private val scheme = darkColorScheme(
    primary = LouroColors.Neon,
    onPrimary = LouroColors.Eggplant,
    secondary = LouroColors.Orange,
    onSecondary = LouroColors.Eggplant,
    background = LouroColors.Eggplant,
    onBackground = LouroColors.Text,
    surface = LouroColors.EggplantRaised,
    onSurface = LouroColors.Text,
    surfaceVariant = LouroColors.EggplantBright,
    onSurfaceVariant = LouroColors.Lilac,
    outline = LouroColors.EggplantBright,
    error = LouroColors.Miss,
)

@Composable
fun LouroTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, typography = Typography(), content = content)
}
