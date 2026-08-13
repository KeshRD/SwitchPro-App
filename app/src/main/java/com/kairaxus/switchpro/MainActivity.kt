package com.kairaxus.switchpro
import android.Manifest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kairaxus.switchpro.ui.theme.SWITCHPROTheme
import android.provider.Settings

import android.app.Activity
import android.app.AlarmManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import java.io.IOException
import java.io.OutputStream
import java.util.*
import kotlin.concurrent.thread
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothSocket: BluetoothSocket? = null
    private var scheduleMonitor: Timer? = null
    private val BLUETOOTH_PERMISSION_REQUEST = 1001
    private var outputStream: OutputStream? = null


    private val DEVICE_NAME = "HC-05"
    private val UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT), BLUETOOTH_PERMISSION_REQUEST)
                return // Wait for permission result
            }
        }


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        findViewById<Button>(R.id.btnConnect).setOnClickListener {
            connectToDevice()
        }

        findViewById<Button>(R.id.btnOn).setOnClickListener {
            sendCommand("ON")
        }

        findViewById<Button>(R.id.btnOff).setOnClickListener {
            sendCommand("OFF")
        }

        findViewById<Button>(R.id.btnAuto).setOnClickListener {
            sendCommand("AUTO")
            startScheduleMonitor()
        }

        findViewById<Button>(R.id.btnOpenSchedule).setOnClickListener {
            val intent = Intent(this, ScheduleActivity::class.java)
            startActivity(intent)
        }
        val editTempThreshold = findViewById<EditText>(R.id.editTempThreshold)
        val btnSendThreshold = findViewById<Button>(R.id.btnSendThreshold)

        btnSendThreshold.setOnClickListener {
            val thresholdValue = editTempThreshold.text.toString().trim()
            if (thresholdValue.isNotEmpty()) {
                sendData("TEMP:$thresholdValue") // Prefix so Arduino knows it's a threshold
                Toast.makeText(this, "Sent: TEMP:$thresholdValue", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Set a threshold value", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun connectToDevice() {
        val pairedDevices = bluetoothAdapter.bondedDevices
        val device = pairedDevices.find { it.name == DEVICE_NAME }

        device?.let {
            thread {
                try {
                    bluetoothSocket = it.createRfcommSocketToServiceRecord(UUID_INSECURE)
                    bluetoothSocket?.connect()
                    BluetoothHelper.bluetoothSocket = bluetoothSocket
                    runOnUiThread {
                        Toast.makeText(this, "Connected to HC-05", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun sendCommand(command: String) {
        bluetoothSocket?.outputStream?.write(command.toByteArray())
    }
    private fun sendData(data: String) {
        try {
            outputStream?.write(data.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error sending data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startScheduleMonitor() {
        if (scheduleMonitor != null) return  // Already running

        scheduleMonitor = Timer()
        scheduleMonitor?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val now = LocalDateTime.now().withSecond(0).withNano(0)

                val prefs = getSharedPreferences("SCHEDULES", MODE_PRIVATE)
                val savedData = prefs.getString("schedule_list", "[]")
                val schedules = Gson().fromJson(savedData, Array<Schedule>::class.java)

                for (schedule in schedules) {
                    val scheduleTime = LocalDateTime.parse(schedule.datetime)
                    if (scheduleTime == now) {
                        sendCommand(schedule.command)
                    }
                }
            }
        }, 0, 60 * 1000) // Every minute
    }

    override fun onDestroy() {
        super.onDestroy()
        scheduleMonitor?.cancel()
    }
}
