package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.Album
import com.example.myapplication.data.DatabaseProvider
import com.example.myapplication.data.Photo
import com.example.myapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var albumCount = 0

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            showAlbumSelectionDialog(it)
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

    private fun showAlbumSelectionDialog(uri: Uri) {
        lifecycleScope.launch {
            val albums = fetchAlbumsFromDb()
            if (albums.isEmpty()) {
                Toast.makeText(this@MainActivity, "저장할 앨범이 없습니다. 먼저 앨범을 생성해주세요.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val albumTitles = albums.map { it.title }.toTypedArray()
            AlertDialog.Builder(this@MainActivity)
                .setTitle("사진을 저장할 앨범 선택")
                .setItems(albumTitles) { _, which ->
                    val selectedAlbum = albums[which]
                    processAndSavePhoto(uri, selectedAlbum.id)
                }
                .setNegativeButton("취소", null)
                .show()
        }
    }

    private fun processAndSavePhoto(uri: Uri, albumId: Int) {
        lifecycleScope.launch {
            Toast.makeText(this@MainActivity, "ML 분류를 시작합니다...", Toast.LENGTH_SHORT).show()

            val category = withContext(Dispatchers.Default) {
                val classifier = TravelClassifier(this@MainActivity)
                val result = classifier.classify(uri)
                classifier.close()
                result
            }

            val photo = Photo(
                albumId = albumId,
                uri = uri.toString(),
                category = category
            )

            withContext(Dispatchers.IO) {
                DatabaseProvider.getDatabase(this@MainActivity).photoDao().insertPhoto(photo)
            }

            Toast.makeText(this@MainActivity, "'${category}'(으)로 분류되어 앨범에 저장되었습니다.", Toast.LENGTH_SHORT).show()
            setupRecyclerView() // 커버 사진 등이 변경될 수 있으므로 갱신
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
