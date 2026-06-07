package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.databinding.ActivityAlbumDetailBinding

class AlbumDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlbumDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlbumDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val albumName = intent.getStringExtra("ALBUM_NAME") ?: "나의 여행"
        
        // CollapsingToolbarLayout에 제목 설정
        binding.collapsingToolbar.title = albumName
        
        // 툴바 설정 (뒤로가기 버튼 등)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupCategoryRecyclerViews()
    }

    private fun setupCategoryRecyclerViews() {
        // 샘플 데이터 생성 (나중에 실제 데이터로 대체)
        val dummyPhotos = listOf(
            PhotoItem("https://images.unsplash.com/photo-1500648767791-00dcc994a43e", "관광지"),
            PhotoItem("https://images.unsplash.com/photo-1504674900247-0877df9cc836", "음식"),
            PhotoItem("https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05", "자연경관")
        )

        // 1. 관광지 Grid (3줄)
        binding.rvTouristAttractions.apply {
            layoutManager = GridLayoutManager(this@AlbumDetailActivity, 3)
            adapter = PhotoAdapter(dummyPhotos.filter { it.category == "관광지" })
        }
        binding.tvTouristCount.text = "${dummyPhotos.count { it.category == "관광지" }}"

        // 2. 음식 Grid (3줄)
        binding.rvFood.apply {
            layoutManager = GridLayoutManager(this@AlbumDetailActivity, 3)
            adapter = PhotoAdapter(dummyPhotos.filter { it.category == "음식" })
        }
        binding.tvFoodCount.text = "${dummyPhotos.count { it.category == "음식" }}"

        // 3. 자연경관 Grid (3줄)
        binding.rvLandscape.apply {
            layoutManager = GridLayoutManager(this@AlbumDetailActivity, 3)
            adapter = PhotoAdapter(dummyPhotos.filter { it.category == "자연경관" })
        }
        binding.tvLandscapeCount.text = "${dummyPhotos.count { it.category == "자연경관" }}"
    }
}