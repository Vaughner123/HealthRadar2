package com.capstone.healthradar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class MapFragment : Fragment() {

    private lateinit var mapView: MapView
    private val areas = mutableListOf<AreaData>()

    data class AreaData(
        val name: String,
        val location: GeoPoint,
        var cases: Int,
        var overlay: Polygon? = null
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val context = requireContext() // ✅ Correct requireContext()
        Configuration.getInstance().load(context, context.getSharedPreferences("prefs", 0))

        val rootView = inflater.inflate(R.layout.fragment_map, container, false)
        val mapContainer = rootView.findViewById<FrameLayout>(R.id.map_container)
        val formContainer = rootView.findViewById<LinearLayout>(R.id.input_container)

        // Initialize map
        mapView = MapView(context)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(12.5)
        mapView.controller.setCenter(GeoPoint(10.37, 123.965))

        mapContainer.addView(mapView)

        // Initial demo data
        val initialAreas = listOf(
            AreaData("Liloan", GeoPoint(10.4018, 123.9959), 20),
            AreaData("Consolacion", GeoPoint(10.3733, 123.9496), 60),
            AreaData("Mandaue", GeoPoint(10.3167, 123.9456), 150)
        )
        areas.addAll(initialAreas)

        for (area in areas) {
            val row = inflater.inflate(R.layout.item_case_input, formContainer, false)
            val areaName = row.findViewById<TextView>(R.id.area_name)
            val caseInput = row.findViewById<EditText>(R.id.case_input)
            val updateButton = row.findViewById<Button>(R.id.update_button)

            areaName.text = area.name
            caseInput.setText(area.cases.toString())

            drawCircle(area)

            updateButton.setOnClickListener {
                val newCases = caseInput.text.toString().toIntOrNull()
                if (newCases != null && newCases != area.cases) {
                    val oldCases = area.cases
                    area.cases = newCases

                    // Remove old circle
                    area.overlay?.let { mapView.overlays.remove(it) }
                    drawCircle(area)

                    mapView.controller.animateTo(area.location)
                    mapView.invalidate()
                }
            }

            formContainer.addView(row)
        }

        return rootView
    }

    private fun drawCircle(area: AreaData) {
        val radius = 0.009
        val (fillColor, strokeColor) = getColorForCases(area.cases)
        val overlay = Polygon().apply {
            points = createCirclePoints(area.location, radius)
            this.fillColor = fillColor
            this.strokeColor = strokeColor
            this.strokeWidth = 5f
        }
        area.overlay = overlay
        mapView.overlays.add(overlay)
    }

    private fun getColorForCases(cases: Int): Pair<Int, Int> {
        return when {
            cases > 100 -> 0x30FF0000.toInt() to 0xFFFF0000.toInt() // Red
            cases > 50 -> 0x300000FF.toInt() to 0xFF0000FF.toInt()   // Blue
            else -> 0x3000FF00.toInt() to 0xFF00FF00.toInt()         // Green
        }
    }

    private fun createCirclePoints(center: GeoPoint, radius: Double, segments: Int = 36): List<GeoPoint> {
        val coords = mutableListOf<GeoPoint>()
        for (i in 0 until segments) {
            val angle = 2.0 * Math.PI * i / segments
            val lat = center.latitude + radius * sin(angle)
            val lon = center.longitude + radius * cos(angle)
            coords.add(GeoPoint(lat, lon))
        }
        return coords
    }
}
