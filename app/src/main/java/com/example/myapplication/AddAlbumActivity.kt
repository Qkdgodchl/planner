package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityAddAlbumBinding

class AddAlbumActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAlbumBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAlbumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 저장 버튼 클릭 시
        binding.btnSaveAlbum.setOnClickListener {
            val title = binding.etAlbumTitle.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(this, "제목을 입력해 주세요!", Toast.LENGTH_SHORT).show()
            } else {
                // TODO: 실제 Room DB에 앨범 데이터 저장하는 로직
                Toast.makeText(this, "[${title}] 앨범이 생성되었습니다.", Toast.LENGTH_SHORT).show()
                finish() // 화면 종료하고 메인으로 돌아가기
            }
        }
    }
}