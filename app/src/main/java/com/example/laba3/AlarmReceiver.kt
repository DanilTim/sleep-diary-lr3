package com.example.laba3

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * BroadcastReceiver — срабатывает когда AlarmManager посылает сигнал.
 * Поддерживает два типа: "sleep" (отход ко сну) и "wake" (пробуждение).
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID      = "alarm_channel"
        const val NOTIFICATION_ID = 1001
    }

    override fun onReceive(context: Context, intent: Intent) {
        createNotificationChannel(context)

        val alarmType  = intent.getStringExtra("alarm_type")  ?: "wake"
        val alarmTitle = intent.getStringExtra("alarm_title") ?: "⏰ Будильник"

        // Для будильника сна — только уведомление без звука
        // Для пробуждения — уведомление со звуком и вибрацией
        val isSleep = alarmType == "sleep"

        val stopIntent = Intent(context, StopAlarmReceiver::class.java)
        val stopPending = PendingIntent.getBroadcast(
            context, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openIntent = Intent(context, AlarmActivity::class.java)
        val openPending = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(alarmTitle)
            .setContentText(if (isSleep) "Пора готовиться ко сну" else "Доброе утро!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(!isSleep)  // для сна — обычное уведомление, для подъёма — постоянное
            .setContentIntent(openPending)
            .setVibrate(longArrayOf(0, 500, 500, 500))

        if (!isSleep) {
            // Звук только для будильника пробуждения
            builder.addAction(android.R.drawable.ic_delete, "Выключить", stopPending)
            AlarmSoundManager.play(context)
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Будильник",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления будильника"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}