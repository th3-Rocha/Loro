package com.rocha.louro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rocha.louro.ui.theme.LouroColors

private val Depth = 5.dp

/** Chunky button with a darker edge underneath that it sinks into when pressed. */
@Composable
fun ChunkyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = LouroColors.Neon,
    edge: Color = LouroColors.NeonEdge,
    textColor: Color = LouroColors.Eggplant,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val shape = RoundedCornerShape(16.dp)
    val face = if (enabled) color else LouroColors.EggplantBright
    val under = if (enabled) edge else LouroColors.EggplantEdge
    Box(modifier.fillMaxWidth().height(56.dp + Depth)) {
        Box(Modifier.fillMaxWidth().height(56.dp).offset(y = Depth).clip(shape).background(under))
        Box(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .offset(y = if (pressed && enabled) Depth else 0.dp)
                .clip(shape)
                .background(face)
                .clickable(interaction, indication = null, enabled = enabled, onClick = onClick)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text.uppercase(),
                color = if (enabled) textColor else LouroColors.Lilac,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                letterSpacing = 1.sp,
            )
        }
    }
}
