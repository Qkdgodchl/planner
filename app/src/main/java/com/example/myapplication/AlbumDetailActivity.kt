package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.data.DatabaseProvider
import com.example.myapplication.data.Photo
import com.example.myapplication.databinding.ActivityAlbumDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlbumDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlbumDetailBinding
    private var albumId: Int = -1
    private lateinit var classifier: TravelClassifier

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            savePhotoToAlbum(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlbumDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        albumId = intent.getIntExtra("ALBUM_ID", -1)
        val albumName = intent.getStringExtra("ALBUM_NAME") ?: "나의 여행"
        
        if (albumId == -1) {
            Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 분류기 초기화
        classifier = TravelClassifier(this)

        binding.collapsingToolbar.title = albumName
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.fabAddPhoto.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        loadPhotos()
    }

    private fun loadPhotos() {
        lifecycleScope.launch {
            val photoDao = DatabaseProvider.getDatabase(this@AlbumDetailActivity).photoDao()
            val allPhotos = photoDao.getPhotosByAlbumId(albumId)

            // 관광지
            val touristPhotos = allPhotos.filter { it.category == "관광지" }
            binding.rvTouristAttractions.apply {
                layoutManager = GridLayoutManager(this@AlbumDetailActivity, 3)
                adapter = PhotoAdapter(touristPhotos)
            }
            binding.tvTouristCount.text = touristPhotos.size.toString()

            // 음식
            val foodPhotos = allPhotos.filter { it.category == "음식" }
            binding.rvFood.apply {
                layoutManager = GridLayoutManager(this@AlbumDetailActivity, 3)
                adapter = PhotoAdapter(foodPhotos)
            }
            binding.tvFoodCount.text = foodPhotos.size.toString()

            // 자연경관
            val landscapePhotos = allPhotos.filter { it.category == "자연경관" }
            binding.rvLandscape.apply {
                layoutManager = GridLayoutManager(this@AlbumDetailActivity, 3)
                adapter = PhotoAdapter(landscapePhotos)
            }
            binding.tvLandscapeCount.text = landscapePhotos.size.toString()
        }
    }

    private fun savePhotoToAlbum(uri: Uri) {
        lifecycleScope.launch {
            // ML 모델을 사용하여 이미지 분류 수행
            val category = withContext(Dispatchers.Default) {
                classifier.classify(uri)
            }

            val photo = Photo(
                albumId = albumId,
                uri = uri.toString(),
                category = category
            )

            DatabaseProvider.getDatabase(this@AlbumDetailActivity).photoDao().insertPhoto(photo)
            
            Toast.makeText(this@AlbumDetailActivity, "사진이 '${category}'(으)로 분류되어 저장되었습니다.", Toast.LENGTH_SHORT).show()
            loadPhotos()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::classifier.isInitialized) {
            classifier.close()
        }
    }
}