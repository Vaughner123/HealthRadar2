package com.capstone.healthradar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.capstone.healthradar.RecordFragment.Companion.editHistory

class RecordFragment : Fragment() {

    companion object {
        private val editHistory = mutableListOf<EditRecord>()

        fun addRecord(record: EditRecord) {
            editHistory.add(0, record) // Add newest on top
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_record, container, false)
        val historyTable = view.findViewById<LinearLayout>(R.id.history_table)
        historyTable.removeAllViews()

        for (record in editHistory) {
            val textView = TextView(requireContext()).apply {
                text = "${record.areaName}: ${record.oldValue} → ${record.newValue} (${record.timestamp})"
                textSize = 16f
                setPadding(0, 8, 0, 8)
            }
            historyTable.addView(textView)
        }

        return view
    }

    data class EditRecord(
        val areaName: String,
        val oldValue: Int,
        val newValue: Int,
        val timestamp: String
    )
}
