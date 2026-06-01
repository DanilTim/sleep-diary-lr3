package com.example.laba3

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_RECORD_ID = "record_id"
    }

    private lateinit var repository: SleepRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: SleepAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repository   = SleepRepository(this)
        recyclerView = findViewById(R.id.recyclerView)
        tvEmpty      = findViewById(R.id.tvEmpty)

        adapter = SleepAdapter(
            onEdit   = { record -> openEdit(record) },
            onDelete = { record -> confirmDelete(record) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, AddSleepActivity::class.java))
        }

        findViewById<View>(R.id.btnStats).setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }

        findViewById<View>(R.id.btnAlarm).setOnClickListener {
            startActivity(Intent(this, AlarmActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadRecords()
    }

    private fun loadRecords() {
        val records = repository.getAll()
        adapter.submitList(records)
        tvEmpty.visibility      = if (records.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (records.isEmpty()) View.GONE   else View.VISIBLE
    }

    private fun openEdit(record: SleepRecord) {
        val intent = Intent(this, AddSleepActivity::class.java)
        intent.putExtra(EXTRA_RECORD_ID, record.id)
        startActivity(intent)
    }

    private fun confirmDelete(record: SleepRecord) {
        AlertDialog.Builder(this)
            .setTitle("Удалить запись?")
            .setMessage("Запись за ${record.date} будет удалена безвозвратно.")
            .setPositiveButton("Удалить") { _, _ ->
                repository.delete(record.id)
                loadRecords()
                Toast.makeText(this, "Запись удалена", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}

// ── RecyclerView Adapter ──────────────────────────────────────────────────────

class SleepAdapter(
    private val onEdit:   (SleepRecord) -> Unit,
    private val onDelete: (SleepRecord) -> Unit
) : RecyclerView.Adapter<SleepAdapter.ViewHolder>() {

    private var items: List<SleepRecord> = emptyList()

    fun submitList(list: List<SleepRecord>) {
        items = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate:     TextView = view.findViewById(R.id.tvDate)
        val tvDuration: TextView = view.findViewById(R.id.tvDuration)
        val tvTimes:    TextView = view.findViewById(R.id.tvTimes)
        val tvQuality:  TextView = view.findViewById(R.id.tvQuality)
        val tvNote:     TextView = view.findViewById(R.id.tvNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sleep, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = items[position]
        holder.tvDate.text     = record.date
        holder.tvDuration.text = record.durationFormatted()
        holder.tvTimes.text    = "${record.sleepTime} → ${record.wakeTime}"
        holder.tvQuality.text  = "${record.qualityEmoji()} ${record.quality}/5"
        holder.tvNote.text     = record.note
        holder.tvNote.visibility = if (record.note.isBlank()) View.GONE else View.VISIBLE

        holder.itemView.setOnClickListener      { onEdit(record) }
        holder.itemView.setOnLongClickListener  { onDelete(record); true }
    }

    override fun getItemCount() = items.size
}