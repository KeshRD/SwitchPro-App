package com.kairaxus.switchpro

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

class ScheduleAdapter(
    private val context: Context,
    private val schedules: MutableList<Schedule>,
    private val onDelete: (Schedule) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = schedules.size
    override fun getItem(position: Int): Any = schedules[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_schedule, parent, false)

        val scheduleText = view.findViewById<TextView>(R.id.tvScheduleText)
        val deleteButton = view.findViewById<ImageButton>(R.id.btnDelete)

        val schedule = schedules[position]
        scheduleText.text = "${schedule.datetime} - ${schedule.command}"

        deleteButton.setOnClickListener {
            onDelete(schedule)
        }

        return view
    }
}
