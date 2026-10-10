package com.rocha.louro.ui.level

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rocha.louro.Summary
import com.rocha.louro.ui.ChunkyButton
import com.rocha.louro.ui.theme.LouroColors

@Composable
fun DoneScreen(summary: Summary, onContinue: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("🦜", fontSize = 96.sp)
            Spacer(Modifier.height(12.dp))
            Text("Nível concluído!", color = LouroColors.Neon, fontWeight = FontWeight.Black, fontSize = 32.sp)
            Text(summary.title, color = LouroColors.Lilac, fontSize = 18.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(32.dp))
            Text("+${summary.points} ⭐", color = LouroColors.Orange, fontWeight = FontWeight.Black, fontSize = 40.sp)
            Spacer(Modifier.height(12.dp))
            val days = if (summary.streak == 1) "1 dia seguido" else "${summary.streak} dias seguidos"
            Text(
                if (summary.streakGrew) "🔥 Ofensiva: $days" else "🔥 $days",
                color = LouroColors.Orange,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
        }
        ChunkyButton("Continuar", onContinue)
    }
}
