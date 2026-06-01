package com.example.laba3

/**
 * Модель одной записи в дневнике сна.
 *
 * @param id            уникальный идентификатор (timestamp создания)
 * @param date          дата в формате "dd.MM.yyyy"
 * @param sleepTime     время засыпания в формате "HH:mm"
 * @param wakeTime      время пробуждения в формате "HH:mm"
 * @param quality       качество сна от 1 до 5
 * @param note          произвольная заметка пользователя
 */
data class SleepRecord(
    val id: Long = System.currentTimeMillis(),
    val date: String = "",
    val sleepTime: String = "",
    val wakeTime: String = "",
    val quality: Int = 3,
    val note: String = ""
) {
    /**
     * Вычисляет продолжительность сна в минутах.
     * Корректно обрабатывает переход через полночь (например, 23:00 → 07:00).
     */
    fun durationMinutes(): Int {
        return try {
            val (sh, sm) = sleepTime.split(":").map { it.toInt() }
            val (wh, wm) = wakeTime.split(":").map { it.toInt() }
            val sleepTotal = sh * 60 + sm
            val wakeTotal  = wh * 60 + wm
            if (wakeTotal >= sleepTotal) wakeTotal - sleepTotal
            else (24 * 60 - sleepTotal) + wakeTotal   // переход через полночь
        } catch (e: Exception) {
            0
        }
    }

    /** Возвращает продолжительность сна в формате "Xч Yмин". */
    fun durationFormatted(): String {
        val total = durationMinutes()
        val h = total / 60
        val m = total % 60
        return "${h}ч ${m}мин"
    }

    /** Возвращает эмодзи-оценку качества сна. */
    fun qualityEmoji(): String = when (quality) {
        1 -> "😫"
        2 -> "😕"
        3 -> "😐"
        4 -> "😊"
        5 -> "😴"
        else -> "😐"
    }
}
