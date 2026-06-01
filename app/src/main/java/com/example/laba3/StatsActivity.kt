package com.example.laba3

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StatsActivity : AppCompatActivity() {

    private lateinit var repository: SleepRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        repository = SleepRepository(this)

        val avgDur     = repository.averageDurationMinutes()
        val avgQuality = repository.averageQuality()
        val records    = repository.getLastN(7)

        // Средняя продолжительность
        val h = avgDur / 60
        val m = avgDur % 60
        findViewById<TextView>(R.id.tvAvgDuration).text = "${h}ч ${m}мин"

        // Среднее качество
        findViewById<TextView>(R.id.tvAvgQuality).text =
            "%.1f / 5".format(avgQuality)

        // Лучший сон
        val best = records.maxByOrNull { it.durationMinutes() }
        findViewById<TextView>(R.id.tvBestSleep).text =
            best?.let { "${it.durationFormatted()} (${it.date})" } ?: "—"

        // Всего записей
        findViewById<TextView>(R.id.tvTotalRecords).text =
            repository.getAll().size.toString()

        // График
        val chartView = findViewById<SleepBarChart>(R.id.sleepChart)
        chartView.setData(records)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
    }
}


class SleepBarChart @JvmOverloads constructor(
    context: android.content.Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var records: List<SleepRecord> = emptyList()

    private val paintBar   = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1565C0") }
    private val paintText  = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color     = Color.parseColor("#CCCCCC")
        textSize  = 28f
        textAlign = Paint.Align.CENTER
    }
    private val paintLabel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color     = Color.parseColor("#AAAAAA")
        textSize  = 24f
        textAlign = Paint.Align.CENTER
    }
    private val paintLine  = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color       = Color.parseColor("#333333")
        strokeWidth = 1f
    }

    fun setData(records: List<SleepRecord>) {
        this.records = records
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (records.isEmpty()) {
            paintText.textSize = 36f
            canvas.drawText("Нет данных", width / 2f, height / 2f, paintText)
            return
        }

        val paddingLeft   = 60f
        val paddingRight  = 20f
        val paddingTop    = 20f
        val paddingBottom = 60f

        val chartWidth  = width  - paddingLeft - paddingRight
        val chartHeight = height - paddingTop  - paddingBottom

        val maxDuration = records.maxOfOrNull { it.durationMinutes() }?.toFloat() ?: 1f
        val barWidth    = chartWidth / (records.size * 1.5f)
        val gap         = barWidth * 0.5f

        // Горизонтальные линии сетки (8ч рекомендуемый сон)
        val recommendedY = paddingTop + chartHeight * (1f - 480f / maxDuration)
        if (recommendedY > paddingTop) {
            paintLine.color = Color.parseColor("#1565C0")
            paintLine.alpha = 80
            canvas.drawLine(paddingLeft, recommendedY, width - paddingRight, recommendedY, paintLine)
            paintLine.alpha = 255
            paintLine.color = Color.parseColor("#333333")
        }

        records.reversed().forEachIndexed { index, record ->
            val dur = record.durationMinutes().toFloat()
            val barHeight = if (maxDuration > 0) (dur / maxDuration) * chartHeight else 0f

            val left  = paddingLeft + index * (barWidth + gap)
            val right = left + barWidth
            val top   = paddingTop + chartHeight - barHeight
            val bottom = paddingTop + chartHeight

            // Цвет столбца по качеству сна
            paintBar.color = when (record.quality) {
                5    -> Color.parseColor("#1B5E20")
                4    -> Color.parseColor("#2E7D32")
                3    -> Color.parseColor("#1565C0")
                2    -> Color.parseColor("#E65100")
                else -> Color.parseColor("#B71C1C")
            }

            canvas.drawRoundRect(left, top, right, bottom, 8f, 8f, paintBar)

            // Подпись продолжительности над столбцом
            val h = record.durationMinutes() / 60
            canvas.drawText("${h}ч", left + barWidth / 2, top - 8f, paintText)

            // Подпись даты под столбцом
            val shortDate = record.date.substring(0, 5) // dd.MM
            canvas.drawText(shortDate, left + barWidth / 2, height - 10f, paintLabel)
        }

        // Ось Y
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft, paddingTop + chartHeight, paintLine)
        canvas.drawLine(paddingLeft, paddingTop + chartHeight,
            width - paddingRight, paddingTop + chartHeight, paintLine)
    }
}
