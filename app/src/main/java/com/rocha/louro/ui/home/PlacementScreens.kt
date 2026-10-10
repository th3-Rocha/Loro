package com.rocha.louro.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rocha.louro.PlacementResult
import com.rocha.louro.session.PlacementQuestion
import com.rocha.louro.ui.ChunkyButton
import com.rocha.louro.ui.theme.LouroColors

@Composable
private fun Centered(bottom: @Composable () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) { content() }
        bottom()
    }
}

@Composable
fun PlacementIntroScreen(languageName: String, onStart: () -> Unit, onSkip: () -> Unit) {
    Centered(
        bottom = {
            ChunkyButton("Fazer o teste", onStart)
            Spacer(Modifier.height(12.dp))
            ChunkyButton(
                "Começar do início",
                onSkip,
                color = LouroColors.EggplantBright,
                edge = LouroColors.EggplantEdge,
                textColor = LouroColors.Text,
            )
        },
    ) {
        Text("🦜", fontSize = 84.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            "Já sabe um pouco de $languageName?",
            color = LouroColors.Neon,
            fontWeight = FontWeight.Black,
            fontSize = 28.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Um teste rápido de vocabulário descobre quantas palavras você já conhece e pula os níveis que você não precisa fazer.",
            color = LouroColors.Lilac,
            fontSize = 17.sp,
            lineHeight = 24.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun PlacementQuestionScreen(
    question: PlacementQuestion,
    number: Int,
    speak: (String) -> Unit,
    onAnswer: (Int?) -> Unit,
) {
    LaunchedEffect(question) { speak(question.item) }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp)) {
            Text("Teste de nível · pergunta $number", color = LouroColors.Orange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))
            Text("O que significa?", color = LouroColors.Lilac, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(12.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    question.item,
                    color = LouroColors.Neon,
                    fontWeight = FontWeight.Black,
                    fontSize = 34.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(10.dp))
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(LouroColors.EggplantBright)
                        .clickable { speak(question.item) },
                    contentAlignment = Alignment.Center,
                ) { Text("🔊", fontSize = 22.sp) }
            }
            Spacer(Modifier.height(24.dp))
            question.options.forEachIndexed { index, option ->
                Text(
                    option,
                    color = LouroColors.Text,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(LouroColors.EggplantRaised)
                        .border(2.dp, LouroColors.EggplantBright, RoundedCornerShape(16.dp))
                        .clickable { onAnswer(index) }
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                )
            }
        }
        ChunkyButton(
            "Não sei",
            { onAnswer(null) },
            Modifier.padding(20.dp),
            color = LouroColors.EggplantBright,
            edge = LouroColors.EggplantEdge,
            textColor = LouroColors.Text,
        )
    }
}

@Composable
fun PlacementDoneScreen(result: PlacementResult, onContinue: () -> Unit) {
    Centered(bottom = { ChunkyButton("Ir para o caminho", onContinue) }) {
        Text("🦜", fontSize = 84.sp)
        Spacer(Modifier.height(16.dp))
        Text("${result.knownWords}", color = LouroColors.Orange, fontWeight = FontWeight.Black, fontSize = 64.sp)
        Text(
            if (result.knownWords == 1) "palavra conhecida" else "palavras conhecidas",
            color = LouroColors.Text,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            when (result.skippedLevels) {
                0 -> "Você começa do primeiro nível que ainda falta. Bora!"
                1 -> "Pulamos 1 nível que você já domina. As palavras dele voltam nas revisões."
                else -> "Pulamos ${result.skippedLevels} níveis que você já domina. As palavras deles voltam nas revisões."
            },
            color = LouroColors.Lilac,
            fontSize = 17.sp,
            lineHeight = 24.sp,
            textAlign = TextAlign.Center,
        )
    }
}
