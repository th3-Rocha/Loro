package com.rocha.louro.ui.level

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rocha.louro.content.Exercise
import com.rocha.louro.content.Token
import com.rocha.louro.session.AnswerChecker
import com.rocha.louro.session.WriteResult
import com.rocha.louro.ui.theme.LouroColors

@Composable
private fun Instruction(text: String) {
    Text(text, color = LouroColors.Lilac, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun ListenButton(onClick: () -> Unit, modifier: Modifier = Modifier, big: Boolean = false) {
    Box(
        modifier
            .size(if (big) 96.dp else 48.dp)
            .clip(CircleShape)
            .background(LouroColors.EggplantBright)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text("🔊", fontSize = if (big) 40.sp else 22.sp)
    }
}

private fun audioPath(levelPath: String, file: String?): String? = file?.let { "$levelPath/$it" }

@Composable
fun CardExercise(exercise: Exercise.Card, levelPath: String, actions: ExerciseActions) {
    val audio = audioPath(levelPath, exercise.audio)
    LaunchedEffect(Unit) { actions.speak(exercise.term, audio) }
    ExerciseScaffold(actions, "Continuar", true, actions.seen) {
        Instruction("Palavra nova")
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(LouroColors.EggplantRaised)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            exercise.emoji?.let {
                Text(it, fontSize = 84.sp)
                Spacer(Modifier.height(12.dp))
            }
            Text(
                exercise.term,
                color = LouroColors.Neon,
                fontWeight = FontWeight.Black,
                fontSize = 34.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(exercise.translation, color = LouroColors.Text, fontSize = 20.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            ListenButton({ actions.speak(exercise.term, audio) })
        }
        exercise.example?.let { example ->
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(example, color = LouroColors.Text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    exercise.exampleTranslation?.let { Text(it, color = LouroColors.Lilac, fontSize = 15.sp) }
                }
                ListenButton({ actions.speak(example, null) })
            }
        }
    }
}

@Composable
fun NoteExercise(exercise: Exercise.Note, actions: ExerciseActions) {
    ExerciseScaffold(actions, "Entendi", true, actions.seen) {
        Instruction("Gramática")
        Text(exercise.title, color = LouroColors.Neon, fontWeight = FontWeight.Black, fontSize = 26.sp)
        Spacer(Modifier.height(12.dp))
        Text(exercise.text, color = LouroColors.Text, fontSize = 17.sp, lineHeight = 25.sp)
        exercise.examples.forEach { example ->
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(LouroColors.EggplantRaised)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(example.text, color = LouroColors.Text, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(example.translation, color = LouroColors.Lilac, fontSize = 15.sp)
                }
                ListenButton({ actions.speak(example.text, null) })
            }
        }
    }
}

@Composable
fun ChoiceExercise(exercise: Exercise.Choice, actions: ExerciseActions) {
    // Shuffled once per attempt so the right answer is never always in the same spot.
    val order = remember { exercise.options.indices.shuffled() }
    var selected by remember { mutableStateOf<Int?>(null) }
    val locked = actions.feedback != null
    exercise.speak?.let { text -> LaunchedEffect(Unit) { actions.speak(text, null) } }

    ExerciseScaffold(
        actions,
        "Verificar",
        selected != null,
        onAction = {
            actions.submit(selected == exercise.answer, exercise.options.getOrNull(exercise.answer), null)
        },
    ) {
        Instruction(exercise.prompt)
        exercise.speak?.let { text ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                ListenButton({ actions.speak(text, null) })
                Spacer(Modifier.width(12.dp))
                Text(text, color = LouroColors.Text, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            }
            Spacer(Modifier.height(20.dp))
        }
        order.forEach { index ->
            val isSelected = selected == index
            val tone = when {
                locked && index == exercise.answer -> LouroColors.Neon
                locked && isSelected -> LouroColors.Miss
                isSelected -> LouroColors.Neon
                else -> LouroColors.EggplantBright
            }
            Text(
                exercise.options[index],
                color = LouroColors.Text,
                fontSize = 18.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) LouroColors.EggplantBright else LouroColors.EggplantRaised)
                    .border(2.dp, tone, RoundedCornerShape(16.dp))
                    .clickable(enabled = !locked) { selected = index }
                    .padding(horizontal = 18.dp, vertical = 16.dp),
            )
        }
    }
}

@Composable
fun WriteExercise(exercise: Exercise.Write, actions: ExerciseActions) {
    var text by remember { mutableStateOf("") }
    val locked = actions.feedback != null
    val check = {
        when (AnswerChecker.check(text, exercise.answers)) {
            WriteResult.CORRECT -> actions.submit(true, null, null)
            WriteResult.ACCENT ->
                actions.submit(false, exercise.answers.firstOrNull(), "Quase! Olho nos acentos.")
            WriteResult.WRONG -> actions.submit(false, exercise.answers.firstOrNull(), null)
        }
    }
    ExerciseScaffold(actions, "Verificar", text.isNotBlank(), check) {
        Instruction("Escreva na língua que você está aprendendo")
        Text(exercise.prompt, color = LouroColors.Text, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            enabled = !locked,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Digite aqui", color = LouroColors.Lilac.copy(alpha = 0.6f)) },
            shape = RoundedCornerShape(16.dp),
            minLines = 3,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { if (text.isNotBlank() && !locked) check() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LouroColors.Neon,
                unfocusedBorderColor = LouroColors.EggplantBright,
                disabledBorderColor = LouroColors.EggplantBright,
                focusedTextColor = LouroColors.Text,
                unfocusedTextColor = LouroColors.Text,
                disabledTextColor = LouroColors.Text,
                cursorColor = LouroColors.Neon,
            ),
        )
    }
}

@Composable
fun SpeakExercise(exercise: Exercise.Speak, levelPath: String, actions: ExerciseActions) {
    val audio = audioPath(levelPath, exercise.audio)
    LaunchedEffect(Unit) { actions.speak(exercise.text, audio) }
    ExerciseScaffold(actions, "Consegui repetir", true, { actions.submit(true, null, null) }) {
        Instruction("Ouça e repita em voz alta")
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(12.dp))
            ListenButton({ actions.speak(exercise.text, audio) }, big = true)
            Spacer(Modifier.height(24.dp))
            Text(
                exercise.text,
                color = LouroColors.Neon,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
            )
            exercise.translation?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = LouroColors.Lilac, fontSize = 17.sp, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(28.dp))
            Text(
                "A avaliação automática da pronúncia ainda não está disponível. Por enquanto, você mesmo confere.",
                color = LouroColors.Lilac.copy(alpha = 0.8f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReadExercise(exercise: Exercise.Read, actions: ExerciseActions) {
    val lookedUp = remember { mutableStateMapOf<Int, Boolean>() }
    var selected by remember { mutableStateOf<Token?>(null) }
    var showTranslation by remember { mutableStateOf(false) }

    ExerciseScaffold(actions, "Continuar", true, actions.seen) {
        Instruction("Leia. Toque numa palavra para ver o significado.")
        exercise.title?.let {
            Text(it, color = LouroColors.Neon, fontWeight = FontWeight.Black, fontSize = 24.sp)
            Spacer(Modifier.height(12.dp))
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            exercise.tokens.forEachIndexed { index, token ->
                val gloss = token.g
                Text(
                    token.t,
                    color = if (lookedUp[index] == true) LouroColors.Orange else LouroColors.Text,
                    fontSize = 22.sp,
                    modifier = if (gloss == null) Modifier else Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            lookedUp[index] = true
                            selected = token
                            actions.lookUp(token.item, gloss)
                            actions.speak(token.item, null)
                        },
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        selected?.let { token ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(LouroColors.EggplantRaised)
                    .padding(16.dp),
            ) {
                Text(token.item, color = LouroColors.Orange, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(token.g.orEmpty(), color = LouroColors.Text, fontSize = 17.sp)
                Spacer(Modifier.height(4.dp))
                Text("Vai voltar nas suas revisões.", color = LouroColors.Lilac, fontSize = 12.sp)
            }
            Spacer(Modifier.height(16.dp))
        }
        exercise.translation?.let { translation ->
            if (showTranslation) {
                Text(translation, color = LouroColors.Lilac, fontSize = 16.sp, lineHeight = 23.sp)
            } else {
                Text(
                    "Ver tradução",
                    color = LouroColors.Lilac,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { showTranslation = true }.padding(6.dp),
                )
            }
        }
    }
}
