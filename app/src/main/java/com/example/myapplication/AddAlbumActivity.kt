package com.example.myapplication

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.Album
import com.example.myapplication.data.DatabaseProvider
import com.example.myapplication.databinding.ActivityAddAlbumBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

class AddAlbumActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityAddAlbumBinding
    private var googleMap: GoogleMap? = null
    
    // 세련된 선택을 위한 여행지 데이터 리스트
    private val locations = listOf(
        LocationData("부산 (Busan)", 35.1796, 129.0756),
        LocationData("서울 (Seoul)", 37.5665, 126.9780),
        LocationData("제주도 (Jeju)", 33.4996, 126.5312),
        LocationData("강릉 (Gangneung)", 37.7519, 128.8761),
        LocationData("경주 (Gyeongju)", 35.8562, 129.2247),
        LocationData("인천 (Incheon)", 37.4563, 126.7052),
        LocationData("전주 (Jeonju)", 35.8242, 127.1480)
    )

    private var selectedLatLng: LatLng = LatLng(35.1796, 129.0756) // 기본값 부산

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAlbumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupLocationDropdown()
        
        // 지도 초기화 (디자인을 위해 CardView 안에 배치됨)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnSaveAlbum.setOnClickListener {
            saveAlbum()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupLocationDropdown() {
        // Material Design Exposed Dropdown Menu 설정
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            locations.map { it.name }
        )
        binding.autoCompleteLocation.setAdapter(adapter)

        // 아이템 선택 시 자동 좌표 설정 및 지도 이동
        binding.autoCompleteLocation.setOnItemClickListener { _, _, position, _ ->
            val location = locations[position]
            selectedLatLng = LatLng(location.latitude, location.longitude)
            updateMapPosition(location.name)
        }
        
        // 초기값 설정
        binding.autoCompleteLocation.setText(locations[0].name, false)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        // 지도 UI 설정 (줌 버튼 비활성화 등 깔끔하게)
        map.uiSettings.isZoomControlsEnabled = false
        map.uiSettings.isMapToolbarEnabled = false
        updateMapPosition(locations[0].name)
    }

    private fun updateMapPosition(title: String) {
        googleMap?.let { map ->
            map.clear()
            map.addMarker(MarkerOptions()
                .position(selectedLatLng)
                .title(title))
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 13f))
        }
    }

    private fun saveAlbum() {
        val title = binding.etAlbumTitle.text.toString().trim()

        if (title.isBlank()) {
            Toast.makeText(this, "앨범 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            // DB 저장 로직 (선택된 위도/경도가 자동으로 저장됨)
            DatabaseProvider.getDatabase(this@AddAlbumActivity).albumDao().insertAlbum(
                Album(
                    title = title,
                    latitude = selectedLatLng.latitude,
                    longitude = selectedLatLng.longitude
                )
            )
            Toast.makeText(this@AddAlbumActivity, "✨ 멋진 앨범이 생성되었습니다!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    data class LocationData(val name: String, val latitude: Double, val longitude: Double)
}