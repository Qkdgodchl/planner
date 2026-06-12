package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.DatabaseProvider
import com.example.myapplication.data.Planner
import com.example.myapplication.databinding.ActivityAiRecommendBinding
import com.example.myapplication.network.RetrofitClient
import kotlinx.coroutines.launch
import com.example.myapplication.data.GeminiRequest
import com.example.myapplication.data.Content
import com.example.myapplication.data.Part
import com.example.myapplication.network.GeminiApiService

class AIRecommendActivity : AppCompatActivity() {

    private val API_KEY = "0941f92cd5f67bfc18f4780b76026d65"
    private val GEMINI_API_KEY = "AIzaSyBLeo6RxRr6bzEIDRj6MdW6FLDgTNdAoxU"
    private lateinit var binding: ActivityAiRecommendBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityAiRecommendBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setupRecommendButton()
        setupSaveButton()
    }

    private fun convertWeather(desc: String): String {

        return when(desc.lowercase()) {

            "clear sky" -> "맑음"
            "few clouds" -> "구름 조금"
            "scattered clouds" -> "구름 많음"
            "broken clouds" -> "흐림"
            "rain" -> "비"
            "shower rain" -> "소나기"
            "thunderstorm" -> "천둥번개"
            "snow" -> "눈"

            else -> desc
        }
    }

    private fun setupRecommendButton() {

        binding.btnRecommend.setOnClickListener {

            val destination =
                binding.etDestination.text.toString()

            val startDate =
                binding.etStartDate.text.toString()

            val endDate =
                binding.etEndDate.text.toString()

            if (destination.isBlank()) {

                binding.tvResult.text =
                    "목적지를 입력해주세요."

                return@setOnClickListener
            }

            lifecycleScope.launch {

                try {

                    binding.tvResult.text =
                        "날씨 정보를 불러오는 중..."

                    val response =
                        RetrofitClient.weatherApi.getWeather(
                            city = destination,
                            apiKey = API_KEY
                        )

                    if (response.isSuccessful) {

                        val weather =
                            response.body()

                        val weatherText =
                            convertWeather(
                                weather?.weather
                                    ?.getOrNull(0)
                                    ?.description ?: ""
                            )

                        val temperature =
                            weather?.main?.temp ?: 0.0

                        val clothingAdvice =
                            when {

                                temperature >= 28 ->
                                    "반팔, 반바지"

                                temperature >= 20 ->
                                    "반팔 + 얇은 겉옷"

                                temperature >= 10 ->
                                    "긴팔 + 가디건"

                                else ->
                                    "외투 준비"
                            }

                        val weatherInfo =
                            """
                            날씨: $weatherText
                            기온: ${temperature}°C
                            추천 복장: $clothingAdvice
                            """.trimIndent()

                        binding.tvResult.text =
                            "AI가 여행 일정을 생성 중입니다..."

                        val aiPlan =
                            generateTravelPlan(
                                destination,
                                startDate,
                                endDate,
                                weatherInfo
                            )

                        savePlannerAutomatically(
                            destination,
                            startDate,
                            endDate,
                            aiPlan
                        )

                        binding.tvResult.text =
                            aiPlan
                    }
                    else {

                        binding.tvResult.text =
                            """
                        도시 정보를 찾을 수 없습니다.

                        Error Code:
                        ${response.code()}
                        """.trimIndent()
                    }
                }
                catch (e: Exception) {

                    e.printStackTrace()

                    binding.tvResult.text =
                        """
                        네트워크 오류

                        ${e.javaClass.simpleName}

                        ${e.message}
                        """.trimIndent()
                }
            }
        }
    }

    private suspend fun savePlannerAutomatically(
        destination: String,
        startDate: String,
        endDate: String,
        plan: String
    ) {

        val planner =
            Planner(
                destination = destination,
                startDate = startDate,
                endDate = endDate,
                duration = "$startDate ~ $endDate",
                planContent = plan
            )

        DatabaseProvider
            .getDatabase(this)
            .plannerDao()
            .insertPlanner(planner)
    }

    private fun setupSaveButton() {

        binding.btnSavePlanner.setOnClickListener {
            savePlanner()
        }
    }

    private suspend fun generateTravelPlan(
        destination: String,
        startDate: String,
        endDate: String,
        weatherInfo: String
    ): String {

        val prompt =
            """
        여행 목적지: $destination
        
        여행 기간:
        $startDate ~ $endDate
        
        현재 날씨 정보:
        $weatherInfo
        
        위 정보를 고려하여
        실제 여행 일정처럼 작성해줘.
        
        형식:
        
        Day 1
        ...
        
        Day 2
        ...
        
        Day 3
        ...
        
        맛집과 관광지도 포함해줘.
        """.trimIndent()

        val request =
            GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(prompt)
                        )
                    )
                )
            )

        val response =
            RetrofitClient
                .geminiApi
                .generateContent(
                    GEMINI_API_KEY,
                    request
                )

        if (!response.isSuccessful) {

            return """
            Gemini API 호출 실패
            
            Error Code:
            ${response.code()}
        """.trimIndent()
        }

        val body =
            response.body()

        if (
            body == null ||
            body.candidates.isEmpty()
        ) {

            return "AI 응답이 비어 있습니다."
        }

        return body
            .candidates[0]
            .content
            .parts[0]
            .text
    }

    private fun savePlanner() {

        if (binding.tvResult.text.isBlank()) {

            Toast.makeText(
                this,
                "먼저 AI 일정을 생성해주세요.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        lifecycleScope.launch {

            val destination = binding.etDestination.text.toString()
            val startDate = binding.etStartDate.text.toString()
            val endDate = binding.etEndDate.text.toString()

            val planner = Planner(
                id = 0, // Room이 자동 생성
                destination = destination,
                startDate = startDate,
                endDate = endDate,
                duration = "$startDate ~ $endDate",
                planContent = binding.tvResult.text.toString(),
                createdAt = System.currentTimeMillis()
            )

            DatabaseProvider
                .getDatabase(this@AIRecommendActivity)
                .plannerDao()
                .insertPlanner(planner)

            Toast.makeText(
                this@AIRecommendActivity,
                "플래너 저장 완료",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}