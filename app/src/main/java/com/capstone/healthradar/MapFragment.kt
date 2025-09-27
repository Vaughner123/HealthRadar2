package com.capstone.healthradar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon
import java.io.BufferedReader
import java.io.InputStreamReader

class MapFragment : Fragment() {

    private lateinit var mapView: MapView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val context = requireContext()
        Configuration.getInstance().load(context, context.getSharedPreferences("prefs", 0))

        val rootView = inflater.inflate(R.layout.fragment_map, container, false)

        // Setup map
        mapView = rootView.findViewById(R.id.map_view)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(11.5)
        mapView.controller.setCenter(GeoPoint(10.37, 123.965))

        // Load polygons from assets JSON
        loadGeoJsonPolygons("geoshapes.json")

        return rootView
    }

    private fun loadGeoJsonPolygons(fileName: String) {
        try {
            val inputStream = requireContext().assets.open(fileName)
            val json = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }

            val jsonObj = JSONObject(json)
            val features = jsonObj.getJSONArray("features")

            for (i in 0 until features.length()) {
                val feature = features.getJSONObject(i)
                val geometry = feature.getJSONObject("geometry")
                val type = geometry.getString("type")

                if (type == "Polygon") {
                    val coords = geometry.getJSONArray("coordinates").getJSONArray(0)
                    val points = mutableListOf<GeoPoint>()

                    for (j in 0 until coords.length()) {
                        val coord = coords.getJSONArray(j)
                        val lon = coord.getDouble(0)
                        val lat = coord.getDouble(1)
                        points.add(GeoPoint(lat, lon))
                    }

                    val polygon = Polygon().apply {
                        this.points = points
                        this.fillColor = 0x3000FF00   // semi-transparent green
                        this.strokeColor = 0xFF00AA00.toInt()
                        this.strokeWidth = 4f
                    }

                    mapView.overlays.add(polygon)
                }
            }

            mapView.invalidate()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
