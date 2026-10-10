package com.rocha.louro.notify

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.rocha.louro.MainActivity
import com.rocha.louro.R
import com.rocha.louro.data.Progress
import com.rocha.louro.data.ProgressStore
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

data class ReminderText(val title: String, val body: String)

/** Daily nudge to keep the streak alive. Stays quiet once a level was done that day. */
object Reminder {
    private const val CHANNEL = "streak"
    private const val NOTIFICATION_ID = 1
    private val TIME: LocalTime = LocalTime.of(19, 0)
    private const val WINDOW_MILLIS = 30 * 60 * 1000L

    fun text(progress: Progress, today: Long): ReminderText? {
        if (progress.currentLanguage == null || progress.lastActiveDay == today) return null
        val streak = progress.streakOn(today)
        return when {
            streak == 1 -> ReminderText("🔥 Sua ofensiva de 1 dia está em jogo", "Um nível hoje mantém o foguinho aceso.")
            streak > 1 -> ReminderText("🔥 Sua ofensiva de $streak dias está em jogo", "Um nível hoje mantém o foguinho aceso.")
            else -> ReminderText("🦜 O Louro está te esperando", "Um nível hoje já começa uma nova ofensiva.")
        }
    }

    /** Safe to call on every app start and after a reboot: it replaces the previous alarm. */
    fun schedule(context: Context) {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(TIME)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val at = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val intent = PendingIntent.getBroadcast(
            context, 0, Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        // A one-shot with a short window; the receiver schedules the next day. A repeating
        // inexact alarm could drift by most of a day.
        context.getSystemService(AlarmManager::class.java)
            .setWindow(AlarmManager.RTC_WAKEUP, at, WINDOW_MILLIS, intent)
    }

    fun notifyIfNeeded(context: Context) {
        val progress = ProgressStore(File(context.filesDir, "progress.json")).load()
        val text = text(progress, LocalDate.now().toEpochDay()) ?: return
        val allowed = Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!allowed) return

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL, "Lembrete diário", NotificationManager.IMPORTANCE_DEFAULT)
        )
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFFFF7A1A.toInt())
            .setContentTitle(text.title)
            .setContentText(text.body)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    fun cancelShown(context: Context) {
        context.getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID)
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) Reminder.notifyIfNeeded(context)
        Reminder.schedule(context)
    }
}
