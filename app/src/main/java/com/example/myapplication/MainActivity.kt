import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.myapplication.databinding.ActivityMainBinding // ViewBinding 사용 가정

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var albumCount = 0 // 실제로는 Room DB에서 가져와야 함

    // 갤러리 앱 연동 (평가기준: 외부 APP 연동 20점)
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            // TODO: 1. 사진을 어느 앨범에 추가할지 묻는 다이얼로그 띄우기
            // TODO: 2. TensorFlow Lite ML 모델에 이미지 URI 전달하여 카테고리(음식/자연/랜드마크) 판독 (평가기준: 머신러닝 50점)
            Toast.makeText(this, "사진 선택됨. ML 분석을 시작합니다.", Toast.LENGTH_SHORT).show()
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
        // 코루틴을 사용한 백그라운드 데이터 로딩 (평가기준: Coroutine 20점)
        lifecycleScope.launch {
            val albums = fetchAlbumsFromDb()
            albumCount = albums.size
            // TODO: RecyclerView Adapter 연결 및 가로 스크롤 구현 (마지막 아이템은 '추가하기' 버튼)
        }
    }

    // Room DB에서 데이터를 가져오는 가상의 코루틴 함수
    private suspend fun fetchAlbumsFromDb(): List<String> = withContext(Dispatchers.IO) {
        // 실제로는 Room Database DAO 호출
        return@withContext listOf("부산 여행", "제주도 여행")
    }

    private fun setupButtons() {
        binding.btnViewMap.setOnClickListener {
            if (albumCount == 0) {
                Toast.makeText(this, "앨범을 먼저 생성해주세요!", Toast.LENGTH_SHORT).show()
            } else {
                // TODO: 구글 맵 화면(Fragment 또는 Activity)으로 이동 (평가기준: API 30점)
            }
        }

        binding.btnAiRecommend.setOnClickListener {
            // TODO: ChatGPT API 통신 화면으로 이동 (평가기준: API + Retrofit 다운로드 매니저)
        }

        binding.fabAddPhoto.setOnClickListener {
            // 외부 갤러리 앱 호출
            galleryLauncher.launch("image/*")
        }
    }
}