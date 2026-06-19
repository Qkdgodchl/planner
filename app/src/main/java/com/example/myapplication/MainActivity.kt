package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.Album
import com.example.myapplication.data.DatabaseProvider
import com.example.myapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

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
            albumCount = albums.size

            val albumAdapter = AlbumAdapter(
                albums = albums,
                onAddClick = {
                    val intent = Intent(this@MainActivity, AddAlbumActivity::class.java)
                    startActivity(intent)
                },
                onAlbumClick = { albumId, albumTitle ->
                    val intent = Intent(this@MainActivity, AlbumDetailActivity::class.java).apply {
                        putExtra("ALBUM_ID", albumId)
                        putExtra("ALBUM_NAME", albumTitle)
                    }
                    startActivity(intent)
                },
                onDeleteClick = { album ->
                    showDeleteConfirmDialog(album)
                }
            )

            binding.rvAlbums.adapter = albumAdapter
        }
    }

    private fun showDeleteConfirmDialog(album: Album) {
        AlertDialog.Builder(this)
            .setTitle("앨범 삭제")
            .setMessage("'${album.title}' 앨범을 삭제하시겠습니까? 앨범 내 사진 데이터는 유지되지만 앨범 목록에서는 사라집니다.")
            .setPositiveButton("삭제") { _, _ ->
                deleteAlbum(album)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun deleteAlbum(album: Album) {
        lifecycleScope.launch {
            DatabaseProvider.getDatabase(this@MainActivity).albumDao().deleteAlbum(album)
            Toast.makeText(this@MainActivity, "앨범이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            setupRecyclerView() // 목록 갱신
        }
    }

    override fun onResume() {
        super.onResume()
        setupRecyclerView()
    }

    private suspend fun fetchAlbumsFromDb(): List<Album> {
        return DatabaseProvider.getDatabase(this).albumDao().getAllAlbums()
    }

    private fun setupButtons() {
        binding.btnViewMap.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

        binding.btnAiRecommend.setOnClickListener {
            val intent = Intent(this, AIRecommendActivity::class.java)
            startActivity(intent)
        }

        binding.btnManualPlan.setOnClickListener {
            val intent = Intent(this, ManualPlannerActivity::class.java)
            startActivity(intent)
        }

        binding.btnLoadPlanner.setOnClickListener {
            val intent = Intent(this, SavedPlannerActivity::class.java)
            startActivity(intent)
        }

        binding.fabAddPhoto.setOnClickListener {
            galleryLauncher.launch("image/*")
        }
    }
}
