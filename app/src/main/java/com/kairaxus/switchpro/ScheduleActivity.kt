package com.kairaxus.switchpro
import android.app.DatePickerDialog
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import java.io.IOException
import java.util.*
import kotlin.concurrent.thread
import java.time.LocalDateTime
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kairaxus.switchpro.ui.theme.SWITCHPROTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class ScheduleActivity : AppCompatActivity() {

    private val schedules = mutableListOf<Schedule>()
    private lateinit var adapter: ArrayAdapter<String>
    private var selectedDateTime: LocalDateTime? = null
    private var selectedCommand: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        loadSchedules()
        refreshList()


        findViewById<Button>(R.id.btnPickDate).setOnClickListener {
            pickDate()
        }

        findViewById<Button>(R.id.btnPickTime).setOnClickListener {
            pickTime()
        }

        findViewById<Button>(R.id.btnOn).setOnClickListener {
            selectedCommand = "ON"
        }

        findViewById<Button>(R.id.btnOff).setOnClickListener {
            selectedCommand = "OFF"
        }
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish() // Go back to MainActivity
        }

        findViewById<Button>(R.id.btnSaveSchedule).setOnClickListener {
            if (selectedDateTime != null && selectedCommand != null) {
                val newSchedule = Schedule(selectedDateTime.toString(), selectedCommand!!)
                schedules.add(newSchedule)
                saveSchedules()
                refreshList()
                scheduleAlarm(newSchedule)
                Toast.makeText(this, "Schedule Saved", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickDate() {
        val now = LocalDate.now()
        DatePickerDialog(this, { _, y, m, d ->
            selectedDateTime = LocalDateTime.of(y, m + 1, d, 0, 0)
            updateSelectedText()
        }, now.year, now.monthValue - 1, now.dayOfMonth).show()
    }

    private fun pickTime() {
        val now = LocalTime.now()
        TimePickerDialog(this, { _, h, m ->
            selectedDateTime = selectedDateTime?.withHour(h)?.withMinute(m)
            updateSelectedText()
        }, now.hour, now.minute, true).show()
    }

    private fun updateSelectedText() {
        val display = if (selectedDateTime != null) {
            "Selected: ${selectedDateTime.toString()}"
        } else "Selected: None"

        findViewById<TextView>(R.id.tvSelected).text = display
    }

    private fun refreshList() {
        val listView = findViewById<ListView>(R.id.scheduleListView)

        val adapter = ScheduleAdapter(this, schedules) { toDelete ->
            schedules.remove(toDelete)
            saveSchedules()
            refreshList()
            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
        }

        listView.adapter = adapter
    }


    private fun saveSchedules() {
        val prefs = getSharedPreferences("SCHEDULES", MODE_PRIVATE)
        val editor = prefs.edit()
        val json = Gson().toJson(schedules)
        editor.putString("schedule_list", json)
        editor.apply()
    }
    private fun scheduleAlarm(schedule: Schedule) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, ScheduleReceiver::class.java)
        intent.putExtra("command", schedule.command)

        val requestCode = schedule.datetime.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val scheduleTime = LocalDateTime.parse(schedule.datetime)
        val millis = scheduleTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
    }

    private fun loadSchedules() {
        val prefs = getSharedPreferences("SCHEDULES", MODE_PRIVATE)
        val savedData = prefs.getString("schedule_list", "[]")
        val loaded = Gson().fromJson(savedData, Array<Schedule>::class.java).toList()
        schedules.clear()
        schedules.addAll(loaded)
    }

}
