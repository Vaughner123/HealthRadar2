package com.capstone.healthradar

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.*

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Find views safely using view.findViewById
        val lineChart = view.findViewById<LineChart>(R.id.lineChart)
        val pieChart1 = view.findViewById<PieChart>(R.id.pieChart1)
        val pieChart2 = view.findViewById<PieChart>(R.id.pieChart2)
        val pieChart3 = view.findViewById<PieChart>(R.id.pieChart3)

        // Setup charts
        setupLineChart(lineChart)
        setupPieChart(pieChart1, "Disease A")
        setupPieChart(pieChart2, "Disease B")
        setupPieChart(pieChart3, "Disease C")

        return view
    }

    private fun setupLineChart(chart: LineChart) {
        val entries = listOf(
            Entry(1f, 10f),
            Entry(2f, 20f),
            Entry(3f, 15f),
            Entry(4f, 25f)
        )
        val dataSet = LineDataSet(entries, "Weekly Cases").apply {
            color = Color.RED
            valueTextColor = Color.BLACK
            lineWidth = 2f
            circleRadius = 4f
        }

        val lineData = LineData(dataSet)
        chart.data = lineData
        chart.description = Description().apply { text = "Cases over Time" }
        chart.invalidate() // refresh
    }

    private fun setupPieChart(chart: PieChart, title: String) {
        val entries = listOf(
            PieEntry(40f, "Recovered"),
            PieEntry(30f, "Active"),
            PieEntry(30f, "Critical")
        )
        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(Color.GREEN, Color.YELLOW, Color.RED)
            valueTextColor = Color.BLACK
            valueTextSize = 12f
        }
        val pieData = PieData(dataSet)
        chart.data = pieData
        chart.description = Description().apply { text = title }
        chart.invalidate()
    }
}
