package com.example.myapplication

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.data.Content
import com.example.myapplication.data.DatabaseProvider
import com.example.myapplication.data.GeminiRequest
import com.example.myapplication.data.Part
import com.example.myapplication.data.PlanUiItem
import com.example.myapplication.data.Planner
import com.example.myapplication.data.TravelPlan
import com.example.myapplication.databinding.ActivityAiRecommendBinding
import com.example.myapplication.network.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.util.Calendar

class AIRecommendActivity : AppCompatActivity() {

    private var currentTravelPlan: TravelPlan? = null
    private var currentJsonPlan: String = ""
    private lateinit var binding: ActivityAiRecommendBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAiRecommendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvPlan.apply {
            layoutManager = LinearLayoutManager(this@AIRecommendActivity)
            itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
        }

        setupRecommendButton()
        setupSaveButton()
        setupDatePicker()

        binding.btnSavePlanner.isEnabled = false
    }

    private fun setupRecommendButton() {
        binding.btnRecommend.setOnClickListener {
            val destination = binding.etDestination.text.toString().trim()
            val startDate = binding.etStartDate.text.toString().trim()
            val endDate = binding.etEndDate.text.toString().trim()

            if (destination.isBlank()) {
                Toast.makeText(this, "목적지를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (startDate.isBlank() || endDate.isBlank()) {
                Toast.makeText(this, "여행 날짜를 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 시작일과 종료일 비교 로직 추가
            if (startDate > endDate) {
                Toast.makeText(this, "시작일은 종료일보다 빨라야 합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            setLoading(true)
            binding.tvEmpty.text = "AI가 여행 일정을 생성하고 있습니다..."

            lifecycleScope.launch {
                try {
                    val weatherInfo = fetchWeatherInfoOrNull(destination)
                    val aiJson = generateTravelPlan(
                        destination = destination,
                        startDate = startDate,
                        endDate = endDate,
                        weatherInfo = weatherInfo
                    )

                    currentJsonPlan = aiJson
                    val travelPlan = Gson().fromJson(aiJson, TravelPlan::class.java)
                    currentTravelPlan = travelPlan

                    val uiItems = mutableListOf<PlanUiItem>()
                    travelPlan.days.forEach { dayPlan ->
                        uiItems.add(PlanUiItem.DayHeader(dayPlan.day))
                        dayPlan.items.forEach { item ->
                            uiItems.add(PlanUiItem.Schedule(item))
                        }
                    }

                    binding.rvPlan.adapter = PlanAdapter(uiItems)
                    binding.rvPlan.visibility = View.VISIBLE
                    binding.tvEmpty.visibility = View.GONE
                    binding.btnSavePlanner.isEnabled = true
                } catch (e: Exception) {
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.tvEmpty.text = "일정을 생성하지 못했습니다.\n다시 시도해주세요."
                } finally {
                    setLoading(false)
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRecommend.isEnabled = !isLoading
    }

    private suspend fun fetchWeatherInfoOrNull(destination: String): String? {
        return try {
            val response = RetrofitClient.weatherApi.getWeather(
                city = destination,
                apiKey = BuildConfig.API_KEY
            )

            if (!response.isSuccessful) {
                return null
            }

            val weather = response.body() ?: return null
            val description = weather.weather.getOrNull(0)?.description.orEmpty()
            val temperature = weather.main.temp
            val clothingAdvice = when {
                temperature >= 28 -> "반팔, 반바지"
                temperature >= 20 -> "반팔과 얇은 겉옷"
                temperature >= 10 -> "긴팔과 가벼운 겉옷"
                else -> "따뜻한 외투"
            }

            """
            날씨: ${convertWeather(description)}
            기온: ${temperature}°C
            추천 복장: $clothingAdvice
            """.trimIndent()
        } catch (e: Exception) {
            null
        }
    }

    private fun convertWeather(desc: String): String {
        return when (desc.lowercase()) {
            "clear sky" -> "맑음"
            "few clouds" -> "구름 조금"
            "scattered clouds" -> "구름 많음"
            "broken clouds" -> "흐림"
            "rain" -> "비"
            "shower rain" -> "소나기"
            "thunderstorm" -> "천둥번개"
            "snow" -> "눈"
            else -> desc.ifBlank { "정보 없음" }
        }
    }

    private fun setupSaveButton() {
        binding.btnSavePlanner.setOnClickListener {
            savePlanner()
        }
    }

    private fun setupDatePicker() {
        binding.etStartDate.setOnClickListener {
            showDatePicker(binding.etStartDate)
        }

        binding.etEndDate.setOnClickListener {
            showDatePicker(binding.etEndDate)
        }

        binding.etStartDate.keyListener = null
        binding.etEndDate.keyListener = null
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->
                editText.setText(
                    String.format("%04d-%02d-%02d", year, month + 1, day)
                )
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private suspend fun generateTravelPlan(
        destination: String,
        startDate: String,
        endDate: String,
        weatherInfo: String?
    ): String {
        val weatherPrompt = if (weatherInfo != null) {
            """
            참고 가능한 현재 날씨:
            $weatherInfo
            """.trimIndent()
        } else {
            """
            날씨 정보는 사용할 수 없다.
            여행 날짜가 장기 예측 범위를 벗어났거나 날씨 조회에 실패했을 수 있으니, 날씨를 가정하지 말고 계절과 일반적인 여행 동선 중심으로 추천하라.
            """.trimIndent()
        }

        val prompt = """
        여행 목적지: $destination

        여행 기간:
        $startDate ~ $endDate

        $weatherPrompt

        당신은 전문 여행 플래너다.

        반드시 JSON만 반환하라.
        JSON 밖의 설명 텍스트를 출력하지 마라.
        설명문 금지.
        서론 금지.
        결론 금지.
        마크다운 금지.
        코드블록 금지.

        다음 스키마를 정확히 따라라.

        {
          "destination": "여행지명",
          "days": [
            {
              "day": 1,
              "items": [
                {
                  "time": "오전",
                  "title": "일정명",
                  "category": "관광"
                }
              ]
            }
          ]
        }

        category는 반드시 아래 중 하나만 사용한다.

        관광
        음식
        숙소
        쇼핑
        교통
        기타

        여행 기간에 맞춰 day 개수를 생성하라.
        각 day마다 최소 4개의 일정을 생성하라.

        이제 JSON만 출력하라.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                Content(parts = listOf(Part(prompt)))
            )
        )

        val response = RetrofitClient.geminiApi.generateContent(
            BuildConfig.GEMINI_API_KEY,
            request
        )

        if (!response.isSuccessful) {
            throw IllegalStateException("Gemini API call failed: ${response.code()}")
        }

        val body = response.body()
        if (body == null || body.candidates.isEmpty()) {
            throw IllegalStateException("Gemini response is empty")
        }

        return body.candidates[0].content.parts[0].text
    }

    private fun savePlanner() {
        if (currentTravelPlan == null) {
            Toast.makeText(this, "먼저 AI 일정을 생성해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val destination = binding.etDestination.text.toString().trim()
            val startDate = binding.etStartDate.text.toString().trim()
            val endDate = binding.etEndDate.text.toString().trim()

            val planner = Planner(
                id = 0,
                destination = destination,
                startDate = startDate,
                endDate = endDate,
                duration = "$startDate ~ $endDate",
                planContent = currentJsonPlan,
                createdAt = System.currentTimeMillis()
            )

            DatabaseProvider
                .getDatabase(this@AIRecommendActivity)
                .plannerDao()
                .insertPlanner(planner)

            Toast.makeText(this@AIRecommendActivity, "플랜 저장 완료", Toast.LENGTH_SHORT).show()
        }
    }
}
