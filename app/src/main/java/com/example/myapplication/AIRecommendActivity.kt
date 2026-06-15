package com.example.myapplication

import android.os.Bundle
import android.view.View
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
import com.example.myapplication.data.TravelPlan
import com.example.myapplication.network.GeminiApiService
import com.google.gson.Gson
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.data.PlanUiItem
import com.example.myapplication.BuildConfig

class AIRecommendActivity : AppCompatActivity() {

    private var currentTravelPlan: TravelPlan? = null
    private var currentJsonPlan: String = ""
    private lateinit var binding: ActivityAiRecommendBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityAiRecommendBinding.inflate(layoutInflater)

        setContentView(binding.root)
        binding.rvPlan.layoutManager =
            LinearLayoutManager(this)

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

                Toast.makeText(
                    this,
                    "목적지를 입력해주세요.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            lifecycleScope.launch {

                try {

                    binding.progressBar.visibility =
                        View.VISIBLE

                    binding.btnRecommend.isEnabled =
                        false

                    val weatherResponse =
                        RetrofitClient.weatherApi.getWeather(
                            city = destination,
                            apiKey = BuildConfig.API_KEY
                        )

                    if (!weatherResponse.isSuccessful) {

                        Toast.makeText(
                            this@AIRecommendActivity,
                            "날씨 정보를 찾을 수 없습니다.",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@launch
                    }

                    val weather =
                        weatherResponse.body()

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

                    val aiJson =
                        generateTravelPlan(
                            destination,
                            startDate,
                            endDate,
                            weatherInfo
                        )

                    currentJsonPlan =
                        aiJson

                    val travelPlan =
                        Gson().fromJson(
                            aiJson,
                            TravelPlan::class.java
                        )

                    currentTravelPlan =
                        travelPlan

                    val uiItems =
                        mutableListOf<PlanUiItem>()

                    travelPlan.days.forEach { dayPlan ->

                        uiItems.add(
                            PlanUiItem.DayHeader(
                                dayPlan.day
                            )
                        )

                        dayPlan.items.forEach { item ->

                            uiItems.add(
                                PlanUiItem.Schedule(
                                    item
                                )
                            )
                        }
                    }

                    binding.rvPlan.adapter =
                        PlanAdapter(uiItems)

                    binding.rvPlan.visibility =
                        View.VISIBLE

                    binding.tvEmpty.visibility =
                        View.GONE

                }
                catch (e: Exception) {

                    Toast.makeText(
                        this@AIRecommendActivity,
                        e.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
                finally {

                    binding.progressBar.visibility =
                        View.GONE

                    binding.btnRecommend.isEnabled =
                        true
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

        현재 날씨:
        $weatherInfo

        당신은 전문 여행 플래너이다.

        반드시 JSON만 반환하라.

        JSON 외의 어떠한 텍스트도 출력하지 마라.

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
                    BuildConfig.GEMINI_API_KEY,
                    request
                )

        if (!response.isSuccessful) {

            return """
        Gemini API 호출 실패

        code = ${response.code()}

        ${response.errorBody()?.string()}
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

        if (currentTravelPlan == null) {

            Toast.makeText(
                this,
                "먼저 AI 일정을 생성해주세요.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        lifecycleScope.launch {

            val destination =
                binding.etDestination.text.toString()

            val startDate =
                binding.etStartDate.text.toString()

            val endDate =
                binding.etEndDate.text.toString()

            val planner =
                Planner(
                    id = 0,
                    destination = destination,
                    startDate = startDate,
                    endDate = endDate,
                    duration = "$startDate ~ $endDate",

                    planContent =
                        currentJsonPlan,

                    createdAt =
                        System.currentTimeMillis()
                )

            DatabaseProvider
                .getDatabase(
                    this@AIRecommendActivity
                )
                .plannerDao()
                .insertPlanner(
                    planner
                )

            Toast.makeText(
                this@AIRecommendActivity,
                "플래너 저장 완료",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
