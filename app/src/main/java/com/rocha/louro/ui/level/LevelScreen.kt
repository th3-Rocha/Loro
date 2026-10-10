package com.rocha.louro.ui.level

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rocha.louro.Feedback
import com.rocha.louro.LevelUi
import com.rocha.louro.content.Exercise
import com.rocha.louro.ui.ChunkyButton
import com.rocha.louro.ui.theme.LouroColors

/** What an exercise needs from the outside world. */
class ExerciseActions(
    val feedback: Feedback?,
    /** Grades the answer; the feedback panel then takes over the bottom of the screen. */
    val submit: (correct: Boolean, answer: String?, hint: String?) -> Unit,
    /** Moves on without grading, for cards, notes and reading. */
    val seen: () -> Unit,
    val next: () -> Unit,
    val speak: (text: String, audio: String?) -> Unit,
    val lookUp: (item: String, gloss: String) -> Unit,
)

@Composable
fun LevelScreen(
    ui: LevelUi,
    submit: (Boolean, String?, String?) -> Unit,
    next: () -> Unit,
    speak: (String, String?) -> Unit,
    lookUp: (String, String) -> Unit,
    exitRequested: Boolean,
    onExitRequest: (Boolean) -> Unit,
    onExit: () -> Unit,
) {
    Column(Modifier.fillMaxSize().imePadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "✕",
                color = LouroColors.Lilac,
                fontSize = 22.sp,
                modifier = Modifier.clip(CircleShape).clickable { onExitRequest(true) }.padding(6.dp),
            )
            Spacer(Modifier.width(12.dp))
            ProgressBar(ui.done, ui.total, Modifier.weight(1f))
        }
        if (ui.item.reviewItem != null) {
            Text(
                "🔁 Revisão",
                color = LouroColors.Orange,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        }

        val actions = ExerciseActions(
            feedback = ui.feedback,
            submit = submit,
            seen = { submit(true, null, null); next() },
            next = next,
            speak = speak,
            lookUp = lookUp,
        )
        // A repeated exercise starts from a clean slate.
        key(ui.item.key, ui.attempt) {
            when (val exercise = ui.item.exercise) {
                is Exercise.Card -> CardExercise(exercise, ui.path, actions)
                is Exercise.Note -> NoteExercise(exercise, actions)
                is Exercise.Choice -> ChoiceExercise(exercise, actions)
                is Exercise.Write -> WriteExercise(exercise, actions)
                is Exercise.Speak -> SpeakExercise(exercise, ui.path, actions)
                is Exercise.Read -> ReadExercise(exercise, actions)
            }
        }
    }

    if (exitRequested) {
        AlertDialog(
            onDismissRequest = { onExitRequest(false) },
            title = { Text("Sair do nível?") },
            text = { Text("O progresso deste nível será perdido.") },
            confirmButton = { TextButton(onClick = onExit) { Text("Sair") } },
            dismissButton = { TextButton(onClick = { onExitRequest(false) }) { Text("Continuar aqui") } },
        )
    }
}

@Composable
private fun ProgressBar(done: Int, total: Int, modifier: Modifier) {
    val fraction by animateFloatAsState(if (total == 0) 0f else done / total.toFloat(), label = "progress")
    Box(modifier.height(14.dp).clip(RoundedCornerShape(7.dp)).background(LouroColors.EggplantBright)) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction)
                .clip(RoundedCornerShape(7.dp))
                .background(LouroColors.Neon)
        )
    }
}

/** Scrollable body on top, and at the bottom either the exercise's action or the feedback. */
@Composable
fun ExerciseScaffold(
    actions: ExerciseActions,
    actionLabel: String,
    actionEnabled: Boolean,
    onAction: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
            content = content,
        )
        val feedback = actions.feedback
        if (feedback == null) {
            ChunkyButton(actionLabel, onAction, Modifier.padding(20.dp), enabled = actionEnabled)
        } else {
            FeedbackPanel(feedback, actions.next)
        }
    }
}

@Composable
private fun FeedbackPanel(feedback: Feedback, onNext: () -> Unit) {
    val tone = if (feedback.correct) LouroColors.Neon else LouroColors.Miss
    Column(Modifier.fillMaxWidth().background(LouroColors.EggplantRaised).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (feedback.correct) "Mandou bem!" else "Ainda não",
                color = tone,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
            )
            Spacer(Modifier.weight(1f))
            if (feedback.points > 0) {
                Text("+${feedback.points} ⭐", color = LouroColors.Orange, fontWeight = FontWeight.ExtraBold)
            }
        }
        feedback.hint?.let {
            Spacer(Modifier.height(6.dp))
            Text(it, color = LouroColors.Text)
        }
        if (!feedback.correct && feedback.answer != null) {
            Spacer(Modifier.height(6.dp))
            Text("Resposta certa:", color = LouroColors.Lilac, fontSize = 13.sp)
            Text(feedback.answer, color = LouroColors.Text, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        }
        Spacer(Modifier.height(14.dp))
        if (feedback.correct) {
            ChunkyButton("Continuar", onNext)
        } else {
            ChunkyButton("Continuar", onNext, color = LouroColors.Miss, edge = LouroColors.EggplantEdge)
        }
    }
}

/** Level-screen state that survives the exit dialog being opened and closed. */
@Composable
fun rememberExitRequest(): Pair<Boolean, (Boolean) -> Unit> {
    var requested by remember { mutableStateOf(false) }
    return requested to { requested = it }
}
