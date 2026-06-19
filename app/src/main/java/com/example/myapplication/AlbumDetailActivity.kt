package com.example.myapplication

import android.content.Intent
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

    // 썸네일 문제를 해결하기 위해 OpenDocument를 사용하고 권한을 유지합니다.
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                // URI 권한을 영구적으로 유지하도록 설정 (앱 재시작 후에도 썸네일이 보이게 함)
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                savePhotoToAlbum(it)
            } catch (e: Exception) {
                e.printStackTrace()
                // 일부 기기나 상황에서 권한 확보 실패 시에도 일단 시도
                savePhotoToAlbum(it)
            }
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

        classifier = TravelClassifier(this)
        binding.collapsingToolbar.title = albumName
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.fabAddPhoto.setOnClickListener {
            // OpenDocument용 타입 지정
            galleryLauncher.launch(arrayOf("image/*"))
        }

        loadPhotos()
    }

    private fun loadPhotos() {
        lifecycleScope.launch {
            val photoDao = DatabaseProvider.getDatabase(this@AlbumDetailActivity).photoDao()
            val allPhotos = photoDao.getPhotosByAlbumId(albumId)

            val touristPhotos = allPhotos.filter { it.category == "관광지" }
            binding.rvTouristAttractions.apply {
                layoutManager = GridLayoutManager(this@AlbumDetailActivity, 3)
                adapter = PhotoAdapter(touristPhotos)
            }
            binding.tvTouristCount.text = touristPhotos.size.toString()

            val foodPhotos = allPhotos.filter { it.category == "음식" }
            binding.rvFood.apply {
                layoutManager = GridLayoutManager(this@AlbumDetailActivity, 3)
                adapter = PhotoAdapter(foodPhotos)
            }
            binding.tvFoodCount.text = foodPhotos.size.toString()

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
            val category = withContext(Dispatchers.Default) {
                classifier.classify(uri)
            }

            val db = DatabaseProvider.getDatabase(this@AlbumDetailActivity)
            val photo = Photo(
                albumId = albumId,
                uri = uri.toString(),
                category = category
            )
            db.photoDao().insertPhoto(photo)

            val albumDao = db.albumDao()
            val album = albumDao.getAlbumById(albumId)
            if (album != null && album.coverImagePath.isEmpty()) {
                val updatedAlbum = album.copy(coverImagePath = uri.toString())
                albumDao.updateAlbum(updatedAlbum)
            }
            
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
