package com.example.laba3

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class AddSleepActivity : AppCompatActivity() {

    private lateinit var repository: SleepRepository
    private lateinit var tvDate:      TextView
    private lateinit var tvSleepTime: TextView
    private lateinit var tvWakeTime:  TextView
    private lateinit var ratingBar:   RatingBar
    private lateinit var etNote:      EditText
    private lateinit var btnSave:     Button

    private var editingId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_sleep)

        repository   = SleepRepository(this)
        tvDate       = findViewById(R.id.tvDate)
        tvSleepTime  = findViewById(R.id.tvSleepTime)
        tvWakeTime   = findViewById(R.id.tvWakeTime)
        ratingBar    = findViewById(R.id.ratingBar)
        etNote       = findViewById(R.id.etNote)
        btnSave      = findViewById(R.id.btnSave)

        // Установить сегодняшнюю дату по умолчанию
        val cal = Calendar.getInstance()
        tvDate.text = formatDate(cal.get(Calendar.DAY_OF_MONTH),
            cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))

        // Установить текущее время по умолчанию
        tvSleepTime.text = formatTime(23, 0)
        tvWakeTime.text  = formatTime(7,  0)

        // Загрузить данные если редактируем
        editingId = intent.getLongExtra(MainActivity.EXTRA_RECORD_ID, -1L)
            .takeIf { it != -1L }
        editingId?.let { loadForEdit(it) }

        tvDate.setOnClickListener      { pickDate() }
        tvSleepTime.setOnClickListener { pickTime(isSleep = true) }
        tvWakeTime.setOnClickListener  { pickTime(isSleep = false) }
        btnSave.setOnClickListener     { saveRecord() }

        findViewById<android.view.View>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun loadForEdit(id: Long) {
        val record = repository.getAll().find { it.id == id } ?: return
        tvDate.text      = record.date
        tvSleepTime.text = record.sleepTime
        tvWakeTime.text  = record.wakeTime
        ratingBar.rating = record.quality.toFloat()
        etNote.setText(record.note)
        btnSave.text = "Обновить"
    }

    private fun pickDate() {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            tvDate.text = formatDate(d, m + 1, y)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun pickTime(isSleep: Boolean) {
        val cal = Calendar.getInstance()
        TimePickerDialog(this, { _, h, m ->
            val time = formatTime(h, m)
            if (isSleep) tvSleepTime.text = time else tvWakeTime.text = time
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }

    private fun saveRecord() {
        val date      = tvDate.text.toString()
        val sleepTime = tvSleepTime.text.toString()
        val wakeTime  = tvWakeTime.text.toString()
        val quality   = ratingBar.rating.toInt().coerceIn(1, 5)
        val note      = etNote.text.toString().trim()

        if (date.isBlank() || sleepTime.isBlank() || wakeTime.isBlank()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        if (editingId != null) {
            repository.update(SleepRecord(editingId!!, date, sleepTime, wakeTime, quality, note))
            Toast.makeText(this, "Запись обновлена", Toast.LENGTH_SHORT).show()
        } else {
            repository.add(SleepRecord(date = date, sleepTime = sleepTime,
                wakeTime = wakeTime, quality = quality, note = note))
            Toast.makeText(this, "Запись сохранена", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    private fun formatDate(d: Int, m: Int, y: Int) =
        "%02d.%02d.%04d".format(d, m, y)

    private fun formatTime(h: Int, m: Int) =
        "%02d:%02d".format(h, m)
}
