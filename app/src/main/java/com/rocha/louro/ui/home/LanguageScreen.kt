package com.rocha.louro.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rocha.louro.content.Language
import com.rocha.louro.ui.theme.LouroColors

@Composable
fun LanguageScreen(languages: List<Language>, current: Language?, onPick: (Language) -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("🦜", fontSize = 72.sp)
        Spacer(Modifier.height(8.dp))
        Text("Louro", color = LouroColors.Neon, fontWeight = FontWeight.Black, fontSize = 40.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            "O que você quer aprender?",
            color = LouroColors.Lilac,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        if (languages.isEmpty()) {
            Text("Nenhum idioma encontrado na pasta de conteúdo.", color = LouroColors.Miss)
        }
        languages.forEach { language ->
            val selected = language.id == current?.id
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (selected) LouroColors.EggplantBright else LouroColors.EggplantRaised)
                    .clickable { onPick(language) }
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(language.info.flag, fontSize = 30.sp)
                Spacer(Modifier.width(16.dp))
                Text(language.info.name, color = LouroColors.Text, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }
    }
}
