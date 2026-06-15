package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.Album
import com.example.myapplication.data.DatabaseProvider
import com.example.myapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var albumCount = 0 

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            Toast.makeText(this, "사진 선택 완료! ML 분류를 시작합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupButtons()
    }

    private fun setupRecyclerView() {
        lifecycleScope.launch {
            val albums = fetchAlbumsFromDb()
            // 실제 데이터가 있는 앨범 개수 계산
            albumCount = albums.size

            val albumAdapter = AlbumAdapter(
                albums = albums,
                onAddClick = {
                    // 🚀 빈 네모칸 클릭 시 앨범 추가 화면으로 이동
                    val intent = Intent(this@MainActivity, AddAlbumActivity::class.java)
                    startActivity(intent)
                },
                onAlbumClick = { albumTitle ->

                    val intent =
                        Intent(
                            this@MainActivity,
                            AlbumDetailActivity::class.java
                        )

                    intent.putExtra(
                        "ALBUM_NAME",
                        albumTitle
                    )

                    startActivity(intent)
                }
            )

            binding.rvAlbums.adapter = albumAdapter
        }
    }

    override fun onResume()
    {
        super.onResume()
        setupRecyclerView()
    }

    // 🗄️ 테스트를 위해 빈 슬롯("")을 포함한 리스트를 반환합니다.
    private suspend fun fetchAlbumsFromDb():
            List<Album> {

        return DatabaseProvider
            .getDatabase(this)
            .albumDao()
            .getAllAlbums()
    }

    private fun setupButtons() {
        binding.btnViewMap.setOnClickListener {

            val intent =
                Intent(
                    this,
                    MapActivity::class.java
                )

            startActivity(intent)
        }

        binding.btnAiRecommend.setOnClickListener {

            val intent =
                Intent(this, AIRecommendActivity::class.java)

            startActivity(intent)
        }

        binding.btnManualPlan.setOnClickListener {
            Toast.makeText(this, "수동 일정 추가 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
            //Intent(this, ManualPlannerActivity::class.java)
        }

        binding.btnLoadPlanner.setOnClickListener {

            val intent =
                Intent(this, SavedPlannerActivity::class.java)

            startActivity(intent)
        }

        binding.fabAddPhoto.setOnClickListener {
            galleryLauncher.launch("image/*")
        }
    }
}
