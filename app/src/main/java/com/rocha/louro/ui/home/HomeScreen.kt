package com.rocha.louro.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rocha.louro.AppState
import com.rocha.louro.content.LevelEntry
import com.rocha.louro.content.LevelType
import com.rocha.louro.ui.theme.LouroColors
import kotlin.math.sin

private enum class BubbleState { DONE, CURRENT, LOCKED }

@Composable
fun HomeScreen(state: AppState, onLevel: (LevelEntry) -> Unit, onLanguages: () -> Unit) {
    val language = state.language ?: return
    val progress = state.progress.language(language.id)
    val currentIndex = state.levels.indexOfFirst { it.id !in progress.completed }
    val listState = rememberLazyListState()

    LaunchedEffect(language.id, currentIndex) {
        if (currentIndex > 1) listState.scrollToItem(currentIndex - 1)
    }

    Column(Modifier.fillMaxSize()) {
        TopBar(
            flag = language.info.flag,
            name = language.info.name,
            streak = state.progress.streakOn(state.today),
            points = progress.points,
            onLanguages = onLanguages,
        )
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            itemsIndexed(state.levels, key = { _, level -> level.id }) { index, level ->
                val bubble = when {
                    level.id in progress.completed -> BubbleState.DONE
                    index == currentIndex -> BubbleState.CURRENT
                    else -> BubbleState.LOCKED
                }
                // The path snakes left and right as it goes down.
                val sway = (sin(index * 0.85) * 82).dp
                LevelBubble(
                    level = level,
                    number = index + 1,
                    state = bubble,
                    modifier = Modifier.offset(x = sway),
                    onClick = { onLevel(level) },
                )
            }
            if (currentIndex == -1 && state.levels.isNotEmpty()) {
                item {
                    Text(
                        "🦜 Você chegou ao fim do caminho.\nNovos níveis aparecem aqui.",
                        color = LouroColors.Lilac,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBar(flag: String, name: String, streak: Int, points: Int, onLanguages: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(LouroColors.EggplantRaised)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onLanguages)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(flag, fontSize = 24.sp)
            Spacer(Modifier.width(8.dp))
            Text(name, color = LouroColors.Text, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        }
        Spacer(Modifier.weight(1f))
        Stat("🔥", streak.toString(), if (streak > 0) LouroColors.Orange else LouroColors.Lilac)
        Spacer(Modifier.width(18.dp))
        Stat("⭐", points.toString(), LouroColors.Orange)
    }
}

@Composable
private fun Stat(icon: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 18.sp)
        Spacer(Modifier.width(4.dp))
        Text(value, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
    }
}

@Composable
private fun LevelBubble(
    level: LevelEntry,
    number: Int,
    state: BubbleState,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val face = when (state) {
        BubbleState.DONE -> LouroColors.Neon
        BubbleState.CURRENT -> LouroColors.Orange
        BubbleState.LOCKED -> LouroColors.EggplantBright
    }
    val edge = when (state) {
        BubbleState.DONE -> LouroColors.NeonEdge
        BubbleState.CURRENT -> LouroColors.OrangeEdge
        BubbleState.LOCKED -> LouroColors.EggplantEdge
    }
    Column(
        modifier.padding(vertical = 10.dp).width(150.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(78.dp, 85.dp)) {
            Box(Modifier.size(78.dp).offset(y = 7.dp).clip(CircleShape).background(edge))
            Box(
                Modifier
                    .size(78.dp)
                    .clip(CircleShape)
                    .background(face)
                    .clickable(enabled = state != BubbleState.LOCKED, onClick = onClick),
                contentAlignment = Alignment.Center,
            ) {
                Text(if (state == BubbleState.LOCKED) "🔒" else level.level.type.icon, fontSize = 30.sp)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "$number · ${level.level.title}",
            color = if (state == BubbleState.LOCKED) LouroColors.Lilac.copy(alpha = 0.6f) else LouroColors.Text,
            fontWeight = if (state == BubbleState.CURRENT) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

val LevelType.icon: String
    get() = when (this) {
        LevelType.VOCABULARY -> "🧩"
        LevelType.LISTEN_SPEAK -> "🎙️"
        LevelType.WRITING -> "✏️"
        LevelType.READING -> "📖"
        LevelType.GRAMMAR -> "🧠"
    }
