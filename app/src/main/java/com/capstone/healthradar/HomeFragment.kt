package com.capstone.healthradar

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var lineChart: LineChart
    private lateinit var pieChart: PieChart
    private lateinit var spinner: Spinner

    private val db = FirebaseFirestore.getInstance()
    private val municipalities = listOf("Liloan", "Consolacion", "Mandaue")
    private val TAG = "HomeFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        lineChart = view.findViewById(R.id.lineChart)
        pieChart = view.findViewById(R.id.pieChart)
        spinner = view.findViewById(R.id.municipalitySpinner)

        setupSpinner()
        // Optionally set default selection (will trigger listener)
        spinner.setSelection(0)

        return view
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, municipalities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedMunicipality = municipalities[position]
                loadPieChart(selectedMunicipality)
                loadLineChart(selectedMunicipality)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    /**
     * Loads pie chart from:
     * UploadedDisease / {municipality} / diseaseEntries
     *
     * Each disease entry document should have:
     * - diseaseName: string
     * - cases: number
     */
    private fun loadPieChart(municipality: String) {
        val subcollection = "diseaseEntries" // make sure this matches your Firestore subcollection name
        db.collection("UploadedDisease")
            .document(municipality)
            .collection(subcollection)
            .get()
            .addOnSuccessListener { snapshot ->
                val diseaseSums = mutableMapOf<String, Float>()
                for (doc in snapshot.documents) {
                    val name = doc.getString("diseaseName") ?: "Unknown"
                    // cases might be stored as Long or Double
                    val cases = (doc.getLong("cases")?.toFloat()
                        ?: doc.getDouble("cases")?.toFloat()
                        ?: 0f)
                    if (cases > 0f) {
                        diseaseSums[name] = (diseaseSums[name] ?: 0f) + cases
                    }
                }

                if (diseaseSums.isNotEmpty()) {
                    val entries = diseaseSums.map { PieEntry(it.value, it.key) }
                    val ds = PieDataSet(ArrayList(entries), "Disease distribution in $municipality")
                    ds.setDrawValues(true)
                    // simple color set; you can use a larger palette if you have more categories
                    ds.colors = listOf(Color.RED, Color.GREEN, Color.MAGENTA, Color.CYAN, Color.YELLOW)
                    ds.valueTextColor = Color.BLACK
                    ds.valueTextSize = 12f

                    val data = PieData(ds)
                    pieChart.data = data
                    pieChart.centerText = municipality
                    pieChart.invalidate()
                    pieChart.animateY(600)
                } else {
                    Log.w(TAG, "No pie entries for $municipality")
                    pieChart.clear()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to fetch pie data", e)
                pieChart.clear()
            }
    }

    /**
     * Loads line chart from:
     * UploadedDisease / {municipality} / diseaseEntries
     *
     * Groups by week-of-month into 4 buckets: Week 1..Week 4
     * Each disease entry document should have:
     * - cases: number
     * - date: Timestamp
     */
    private fun loadLineChart(municipality: String) {
        val subcollection = "diseaseEntries" // same as above
        db.collection("UploadedDisease")
            .document(municipality)
            .collection(subcollection)
            .get()
            .addOnSuccessListener { snapshot ->
                // 4 buckets for Week 1..4
                val weekSums = FloatArray(4) { 0f }
                for (doc in snapshot.documents) {
                    val cases = (doc.getLong("cases")?.toFloat()
                        ?: doc.getDouble("cases")?.toFloat()
                        ?: 0f)
                    val timestamp = doc.getTimestamp("date")
                    val date = timestamp?.toDate()
                    if (date != null) {
                        val cal = Calendar.getInstance()
                        cal.time = date
                        val day = cal.get(Calendar.DAY_OF_MONTH) // 1..31
                        // group into rough 4-week buckets:
                        val weekIndex = ((day - 1) / 7).coerceIn(0, 3) // 0..3
                        weekSums[weekIndex] = weekSums[weekIndex] + cases
                    } else {
                        // if no date, we can place into last bucket or skip; here we skip
                    }
                }

                val weeks = listOf("Week 1", "Week 2", "Week 3", "Week 4")
                val entries = ArrayList<Entry>()
                for (i in weekSums.indices) {
                    entries.add(Entry(i.toFloat(), weekSums[i]))
                }

                val hasData = weekSums.any { it > 0f }
                if (hasData) {
                    val ds = LineDataSet(entries, "Weekly cases in $municipality")
                    ds.color = Color.BLUE
                    ds.valueTextColor = Color.BLACK
                    ds.valueTextSize = 10f
                    ds.setDrawCircles(true)
                    ds.mode = LineDataSet.Mode.CUBIC_BEZIER

                    val lineData = LineData(ds)
                    lineChart.data = lineData

                    lineChart.xAxis.apply {
                        granularity = 1f
                        position = XAxis.XAxisPosition.BOTTOM
                        valueFormatter = IndexAxisValueFormatter(weeks)
                    }
                    lineChart.axisRight.isEnabled = false
                    lineChart.description.isEnabled = false
                    lineChart.invalidate()
                    lineChart.animateX(700)
                } else {
                    Log.w(TAG, "No line entries for $municipality")
                    lineChart.clear()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to fetch line data", e)
                lineChart.clear()
            }
    }
}
