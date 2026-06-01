package com.example.laba3

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject


class SleepRepository(context: Context) {

    companion object {
        private const val PREFS_NAME = "sleep_diary_prefs"
        private const val KEY_RECORDS = "sleep_records"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── CRUD операции ─────────────────────────────────────────────────────────


    fun getAll(): List<SleepRecord> {
        val raw = prefs.getString(KEY_RECORDS, "[]") ?: "[]"
        val array = JSONArray(raw)
        val list = mutableListOf<SleepRecord>()
        for (i in 0 until array.length()) {
            list.add(fromJson(array.getJSONObject(i)))
        }
        return list.sortedByDescending { it.id }
    }


    fun add(record: SleepRecord) {
        val list = getAll().toMutableList()
        list.add(record)
        save(list)
    }


    fun update(record: SleepRecord) {
        val list = getAll().toMutableList()
        val index = list.indexOfFirst { it.id == record.id }
        if (index >= 0) list[index] = record
        save(list)
    }


    fun delete(id: Long) {
        val list = getAll().filter { it.id != id }
        save(list)
    }

    // ── Статистика ────────────────────────────────────────────────────────────


    fun averageDurationMinutes(): Int {
        val list = getAll().filter { it.durationMinutes() > 0 }
        if (list.isEmpty()) return 0
        return list.sumOf { it.durationMinutes() } / list.size
    }


    fun averageQuality(): Float {
        val list = getAll()
        if (list.isEmpty()) return 0f
        return list.sumOf { it.quality }.toFloat() / list.size
    }


    fun getLastN(n: Int): List<SleepRecord> = getAll().take(n)

    // ── Сериализация ──────────────────────────────────────────────────────────

    private fun save(list: List<SleepRecord>) {
        val array = JSONArray()
        list.forEach { array.put(toJson(it)) }
        prefs.edit().putString(KEY_RECORDS, array.toString()).apply()
    }

    private fun toJson(r: SleepRecord): JSONObject = JSONObject().apply {
        put("id",        r.id)
        put("date",      r.date)
        put("sleepTime", r.sleepTime)
        put("wakeTime",  r.wakeTime)
        put("quality",   r.quality)
        put("note",      r.note)
    }

    private fun fromJson(o: JSONObject) = SleepRecord(
        id        = o.getLong("id"),
        date      = o.getString("date"),
        sleepTime = o.getString("sleepTime"),
        wakeTime  = o.getString("wakeTime"),
        quality   = o.getInt("quality"),
        note      = o.optString("note", "")
    )
}
