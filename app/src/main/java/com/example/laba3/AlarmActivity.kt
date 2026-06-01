package com.example.laba3

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

/**
 * Экран установки умного будильника.
 * Два будильника: напоминание об отходе ко сну и звонок на пробуждение.
 */
class AlarmActivity : AppCompatActivity() {

    companion object {
        private const val PREFS_NAME         = "alarm_prefs"
        private const val KEY_SLEEP_HOUR     = "sleep_hour"
        private const val KEY_SLEEP_MINUTE   = "sleep_minute"
        private const val KEY_WAKE_HOUR      = "wake_hour"
        private const val KEY_WAKE_MINUTE    = "wake_minute"
        private const val KEY_ENABLED        = "alarm_enabled"
        private const val REQUEST_CODE_SLEEP = 2001  // будильник отхода ко сну
        private const val REQUEST_CODE_WAKE  = 2002  // будильник пробуждения
    }

    private lateinit var timePickerSleep: TimePicker
    private lateinit var timePickerWake:  TimePicker
    private lateinit var btnSet:          Button
    private lateinit var btnCancel:       Button
    private lateinit var tvStatus:        TextView
    private lateinit var btnBack:         Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        timePickerSleep = findViewById(R.id.timePickerSleep)
        timePickerWake  = findViewById(R.id.timePickerWake)
        btnSet          = findViewById(R.id.btnSetAlarm)
        btnCancel       = findViewById(R.id.btnCancelAlarm)
        tvStatus        = findViewById(R.id.tvAlarmStatus)
        btnBack         = findViewById(R.id.btnBack)

        timePickerSleep.setIs24HourView(true)
        timePickerWake.setIs24HourView(true)

        loadSavedAlarm()

        btnSet.setOnClickListener    { setAlarms() }
        btnCancel.setOnClickListener { cancelAlarms() }
        btnBack.setOnClickListener   {
            AlarmSoundManager.stop()
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(AlarmReceiver.NOTIFICATION_ID)
            finish()
        }
    }

    private fun setAlarms() {
        val sleepHour   = timePickerSleep.hour
        val sleepMinute = timePickerSleep.minute
        val wakeHour    = timePickerWake.hour
        val wakeMinute  = timePickerWake.minute

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // ── Будильник 1: отход ко сну ─────────────────────────────────────────
        val sleepCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, sleepHour)
            set(Calendar.MINUTE,      sleepMinute)
            set(Calendar.SECOND,      0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }

        val sleepIntent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("alarm_type", "sleep")
            putExtra("alarm_title", "🌙 Время ложиться спать!")
        }
        val sleepPending = PendingIntent.getBroadcast(
            this, REQUEST_CODE_SLEEP, sleepIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            sleepCalendar.timeInMillis,
            sleepPending
        )

        // ── Будильник 2: пробуждение ──────────────────────────────────────────
        val wakeCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, wakeHour)
            set(Calendar.MINUTE,      wakeMinute)
            set(Calendar.SECOND,      0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }

        val wakeIntent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("alarm_type", "wake")
            putExtra("alarm_title", "☀️ Время просыпаться!")
        }
        val wakePending = PendingIntent.getBroadcast(
            this, REQUEST_CODE_WAKE, wakeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            wakeCalendar.timeInMillis,
            wakePending
        )

        // Сохранить настройки
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_SLEEP_HOUR,   sleepHour)
            .putInt(KEY_SLEEP_MINUTE, sleepMinute)
            .putInt(KEY_WAKE_HOUR,    wakeHour)
            .putInt(KEY_WAKE_MINUTE,  wakeMinute)
            .putBoolean(KEY_ENABLED,  true)
            .apply()

        tvStatus.text = "✅ Сон: %02d:%02d  |  Подъём: %02d:%02d".format(
            sleepHour, sleepMinute, wakeHour, wakeMinute)

        Toast.makeText(this,
            "Будильники установлены: сон %02d:%02d, подъём %02d:%02d".format(
                sleepHour, sleepMinute, wakeHour, wakeMinute),
            Toast.LENGTH_LONG).show()
    }

    private fun cancelAlarms() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Отменяем оба будильника
        val sleepPending = PendingIntent.getBroadcast(
            this, REQUEST_CODE_SLEEP,
            Intent(this, AlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val wakePending = PendingIntent.getBroadcast(
            this, REQUEST_CODE_WAKE,
            Intent(this, AlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(sleepPending)
        alarmManager.cancel(wakePending)

        AlarmSoundManager.stop()
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(AlarmReceiver.NOTIFICATION_ID)

        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_ENABLED, false)
            .apply()

        tvStatus.text = "❌ Будильники отключены"
        Toast.makeText(this, "Будильники отключены", Toast.LENGTH_SHORT).show()
    }

    private fun loadSavedAlarm() {
        val prefs       = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val sleepHour   = prefs.getInt(KEY_SLEEP_HOUR,   23)
        val sleepMinute = prefs.getInt(KEY_SLEEP_MINUTE,  0)
        val wakeHour    = prefs.getInt(KEY_WAKE_HOUR,     7)
        val wakeMinute  = prefs.getInt(KEY_WAKE_MINUTE,   0)
        val enabled     = prefs.getBoolean(KEY_ENABLED, false)

        timePickerSleep.hour   = sleepHour
        timePickerSleep.minute = sleepMinute
        timePickerWake.hour    = wakeHour
        timePickerWake.minute  = wakeMinute

        tvStatus.text = if (enabled)
            "✅ Сон: %02d:%02d  |  Подъём: %02d:%02d".format(
                sleepHour, sleepMinute, wakeHour, wakeMinute)
        else
            "❌ Будильники не установлены"
    }
}