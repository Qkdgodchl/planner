package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var albumCount = 0 // 앨범 존재 여부를 확인하기 위한 변수

    // 📸 갤러리 앱 연동 (평가기준: 외부 APP 연동 20점)
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            // TODO: 1. 사진을 어느 앨범에 추가할지 묻는 다이얼로그 띄우기
            // TODO: 2. TensorFlow Lite ML 모델에 이미지 URI 전달하여 카테고리(음식/자연/랜드마크) 판독 (평가기준: 머신러닝 50점)
            Toast.makeText(this, "사진 선택 완료! ML 분류를 시작합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding 초기화 (xml 레이아웃과 코드 연결)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 화면 로딩 시 실행할 설정 함수들
        setupRecyclerView()
        setupButtons()
    }

    // 🖼️ 앨범 스크롤 뷰(RecyclerView) 설정
    private fun setupRecyclerView() {
        // 코루틴을 사용한 백그라운드 데이터 로딩 (평가기준: Coroutine 20점)
        lifecycleScope.launch {
            // DB에서 앨범 목록을 가져올 때까지 기다림
            val albums = fetchAlbumsFromDb()
            albumCount = albums.size

            // 만들어둔 어댑터를 스크롤 뷰에 연결
            val albumAdapter = AlbumAdapter(
                albums = albums,
                onAddClick = {
                    // 마지막 '+ 앨범 추가' 카드를 터치했을 때 실행할 코드
                    Toast.makeText(this@MainActivity, "새로운 앨범 추가 화면으로 이동!", Toast.LENGTH_SHORT).show()
                    // TODO: 앨범 추가 Fragment 또는 Activity로 이동하는 로직
                },
                onAlbumClick = { albumName ->
                    // 일반 여행 앨범 카드를 터치했을 때 실행할 코드
                    Toast.makeText(this@MainActivity, "[${albumName}] 앨범으로 이동!", Toast.LENGTH_SHORT).show()
                    // TODO: 상세 앨범 Fragment 또는 Activity로 이동하는 로직
                }
            )

            binding.rvAlbums.adapter = albumAdapter
        }
    }

    // 🗄️ Room DB에서 데이터를 가져오는 가상의 코루틴 함수 (평가기준: DB 30점)
    private suspend fun fetchAlbumsFromDb(): List<String> = withContext(Dispatchers.IO) {
        // 나중에 이 부분을 실제 Room DB DAO를 호출하는 코드로 바꾸면 됩니다.
        return@withContext listOf("부산 여행", "제주도 여행")
    }

    // 🖱️ 화면에 있는 각종 버튼들 클릭 이벤트 설정
    private fun setupButtons() {

        // 1. 하단 지도 보기 버튼 (앨범이 없으면 접근 불가)
        binding.btnViewMap.setOnClickListener {
            if (albumCount == 0) {
                Toast.makeText(this, "앨범을 먼저 생성해주세요!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "전체 지도 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
                // TODO: 구글 맵 화면으로 이동 (평가기준: 외부 API 30점)
            }
        }

        // 2. 중간 AI 일정 추천 버튼
        binding.btnAiRecommend.setOnClickListener {
            Toast.makeText(this, "AI 여행 추천 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
            // TODO: ChatGPT API 통신 화면으로 이동 (평가기준: 외부 API + Retrofit 20점)
        }

        // 3. 중간 수동 일정 추가 버튼
        binding.btnManualPlan.setOnClickListener {
            Toast.makeText(this, "수동 일정 추가 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
        }

        // 4. 중간 보관된 플래너 불러오기 버튼
        binding.btnLoadPlanner.setOnClickListener {
            Toast.makeText(this, "보관된 플래너 목록을 엽니다.", Toast.LENGTH_SHORT).show()
        }

        // 5. 우측 하단 플로팅 버튼 (사진 추가)
        binding.fabAddPhoto.setOnClickListener {
            // 등록해둔 갤러리 런처 실행 (이미지 파일만 선택 가능하도록)
            galleryLauncher.launch("image/*")
        }
    }
}