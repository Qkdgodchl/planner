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

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // 마커 클릭 리스너 설정
        googleMap.setOnMarkerClickListener { marker ->
            val albumId = marker.tag as? Int ?: return@setOnMarkerClickListener false
            val albumTitle = marker.title

            val intent = Intent(this@MapActivity, AlbumDetailActivity::class.java).apply {
                putExtra("ALBUM_ID", albumId)
                putExtra("ALBUM_NAME", albumTitle)
            }
            startActivity(intent)
            true
        }

        lifecycleScope.launch {
            val albums = DatabaseProvider.getDatabase(this@MapActivity).albumDao().getAllAlbums()

            if (albums.isEmpty()) return@launch

            val boundsBuilder = LatLngBounds.Builder()

            for (album in albums) {
                val location = LatLng(album.latitude, album.longitude)
                val marker = googleMap.addMarker(
                    MarkerOptions()
                        .position(location)
                        .title(album.title)
                )
                marker?.tag = album.id
                boundsBuilder.include(location)
            }

            // 모든 마커가 보이도록 카메라 이동
            if (albums.size == 1) {
                // 앨범이 하나일 때는 적당한 줌 레벨로 이동
                val firstLocation = LatLng(albums[0].latitude, albums[0].longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 12f))
            } else {
                // 여러 개일 때는 모든 핀을 포함하는 경계로 이동
                try {
                    val bounds = boundsBuilder.build()
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
