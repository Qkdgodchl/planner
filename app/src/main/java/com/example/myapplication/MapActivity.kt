package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.DatabaseProvider
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.launch

class MapActivity :
    AppCompatActivity(),
    OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_map)

        val mapFragment =
            supportFragmentManager
                .findFragmentById(R.id.map)
                    as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {

        googleMap = map

        lifecycleScope.launch {

            val albums =
                DatabaseProvider
                    .getDatabase(this@MapActivity)
                    .albumDao()
                    .getAllAlbums()

            if (albums.isEmpty()) {
                return@launch
            }

            val boundsBuilder =
                LatLngBounds.Builder()

            for (album in albums) {

                val location =
                    com.google.android.gms.maps.model.LatLng(
                        album.latitude,
                        album.longitude
                    )

                val marker =
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(location)
                            .title(album.title)
                    )

                marker?.tag = album.id

                googleMap.setOnMarkerClickListener { marker ->

                    val albumId =
                        marker.tag as Int

                    val intent =
                        Intent(
                            this@MapActivity,
                            AlbumDetailActivity::class.java
                        )

                    intent.putExtra(
                        "ALBUM_ID",
                        albumId
                    )

                    startActivity(intent)

                    true
                }

                boundsBuilder.include(location)
            }

            val bounds =
                boundsBuilder.build()

            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    bounds,
                    150
                )
            )
        }
    }
}