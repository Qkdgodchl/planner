package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.myapplication.data.Album
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

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                savePhotoToAlbum(it)
            } catch (e: Exception) {
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

        // 제목 영역을 클릭하면 제목 수정 다이얼로그를 띄웁니다.
        binding.collapsingToolbar.setOnClickListener {
            showEditTitleDialog()
        }

        binding.fabAddPhoto.setOnClickListener {
            galleryLauncher.launch(arrayOf("image/*"))
        }

        loadPhotos()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_album_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit_title -> {
                showEditTitleDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showEditTitleDialog() {
        val editText = EditText(this)
        editText.setText(binding.collapsingToolbar.title)
        editText.setSelection(editText.text?.length ?: 0)

        // 다이얼로그 입력창에 여백(Padding) 추가를 위한 컨테이너 설정
        val container = FrameLayout(this)
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        val margin = (20 * resources.displayMetrics.density).toInt()
        params.setMargins(margin, margin / 2, margin, 0)
        editText.layoutParams = params
        container.addView(editText)

        AlertDialog.Builder(this)
            .setTitle("앨범 제목 수정")
            .setView(container)
            .setPositiveButton("수정") { _, _ ->
                val newTitle = editText.text.toString().trim()
                if (newTitle.isNotEmpty()) {
                    updateAlbumTitle(newTitle)
                } else {
                    Toast.makeText(this, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun updateAlbumTitle(newTitle: String) {
        lifecycleScope.launch {
            val db = DatabaseProvider.getDatabase(this@AlbumDetailActivity)
            val albumDao = db.albumDao()
            val album = albumDao.getAlbumById(albumId) ?: return@launch
            
            val updatedAlbum = album.copy(title = newTitle)
            albumDao.updateAlbum(updatedAlbum)
            
            binding.collapsingToolbar.title = newTitle
            Toast.makeText(this@AlbumDetailActivity, "제목이 수정되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadPhotos() {
        lifecycleScope.launch {
            val db = DatabaseProvider.getDatabase(this@AlbumDetailActivity)
            val album = db.albumDao().getAlbumById(albumId)
            val allPhotos = db.photoDao().getPhotosByAlbumId(albumId)

            album?.let { 
                bindAlbumHeader(it)
                binding.collapsingToolbar.title = it.title
            }
            bindPhotoSection(allPhotos.filter { it.category == CATEGORY_LANDMARK }, binding.rvTouristAttractions)
            bindPhotoSection(allPhotos.filter { it.category == CATEGORY_FOOD }, binding.rvFood)
            bindPhotoSection(allPhotos.filter { it.category == CATEGORY_NATURE }, binding.rvLandscape)

            binding.tvTouristCount.text = allPhotos.count { it.category == CATEGORY_LANDMARK }.toString()
            binding.tvFoodCount.text = allPhotos.count { it.category == CATEGORY_FOOD }.toString()
            binding.tvLandscapeCount.text = allPhotos.count { it.category == CATEGORY_NATURE }.toString()
        }
    }

    private fun bindAlbumHeader(album: Album) {
        if (album.coverImagePath.isNotEmpty()) {
            Glide.with(binding.ivAlbumHeader.context)
                .load(album.coverImagePath)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(binding.ivAlbumHeader)
        } else {
            binding.ivAlbumHeader.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    private fun bindPhotoSection(photos: List<Photo>, recyclerView: androidx.recyclerview.widget.RecyclerView) {
        recyclerView.apply {
            layoutManager = GridLayoutManager(this@AlbumDetailActivity, 3)
            adapter = PhotoAdapter(
                photos = photos,
                onDeleteClick = { photo -> showDeletePhotoDialog(photo) },
                onSetCoverClick = { photo -> setCoverPhoto(photo) }
            )
        }
    }

    private fun showDeletePhotoDialog(photo: Photo) {
        AlertDialog.Builder(this)
            .setTitle("사진 삭제")
            .setMessage("이 사진을 앨범에서 삭제할까요?")
            .setPositiveButton("삭제") { _, _ -> deletePhoto(photo) }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun deletePhoto(photo: Photo) {
        lifecycleScope.launch {
            val db = DatabaseProvider.getDatabase(this@AlbumDetailActivity)
            val albumDao = db.albumDao()
            val photoDao = db.photoDao()
            val album = albumDao.getAlbumById(albumId)

            photoDao.deletePhoto(photo)

            if (album?.coverImagePath == photo.uri) {
                val nextCover = photoDao.getFirstPhotoByAlbumId(albumId)?.uri.orEmpty()
                albumDao.updateAlbum(album.copy(coverImagePath = nextCover))
            }

            Toast.makeText(this@AlbumDetailActivity, "사진을 삭제했습니다.", Toast.LENGTH_SHORT).show()
            loadPhotos()
        }
    }

    private fun setCoverPhoto(photo: Photo) {
        lifecycleScope.launch {
            val albumDao = DatabaseProvider.getDatabase(this@AlbumDetailActivity).albumDao()
            val album = albumDao.getAlbumById(albumId) ?: return@launch
            val updatedAlbum = album.copy(coverImagePath = photo.uri)

            albumDao.updateAlbum(updatedAlbum)
            bindAlbumHeader(updatedAlbum)
            Toast.makeText(this@AlbumDetailActivity, "대표 사진을 변경했습니다.", Toast.LENGTH_SHORT).show()
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
                albumDao.updateAlbum(album.copy(coverImagePath = uri.toString()))
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

    companion object {
        private const val CATEGORY_FOOD = "음식"
        private const val CATEGORY_LANDMARK = "관광지"
        private const val CATEGORY_NATURE = "자연경관"
    }
}
