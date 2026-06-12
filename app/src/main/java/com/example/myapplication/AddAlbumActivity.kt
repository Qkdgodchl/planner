package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.Album
import com.example.myapplication.data.DatabaseProvider
import com.example.myapplication.databinding.ActivityAddAlbumBinding
import kotlinx.coroutines.launch

class AddAlbumActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAlbumBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityAddAlbumBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.btnSaveAlbum.setOnClickListener {

            saveAlbum()
        }
    }

    private fun saveAlbum() {

        val title =
            binding.etAlbumTitle.text.toString()

        val latitude =
            binding.etLatitude.text.toString()

        val longitude =
            binding.etLongitude.text.toString()

        if (title.isBlank()) {

            Toast.makeText(
                this,
                "앨범 이름을 입력해주세요",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        lifecycleScope.launch {

            DatabaseProvider
                .getDatabase(this@AddAlbumActivity)
                .albumDao()
                .insertAlbum(
                    Album(
                        title = title,
                        latitude = latitude.toDoubleOrNull() ?: 0.0,
                        longitude = longitude.toDoubleOrNull() ?: 0.0
                    )
                )

            Toast.makeText(
                this@AddAlbumActivity,
                "앨범 생성 완료",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }
}