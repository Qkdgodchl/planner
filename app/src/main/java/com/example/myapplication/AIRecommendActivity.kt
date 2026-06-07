package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.DatabaseProvider
import com.example.myapplication.data.Planner
import com.example.myapplication.databinding.ActivityAiRecommendBinding
import kotlinx.coroutines.launch

class AIRecommendActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAiRecommendBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityAiRecommendBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setupRecommendButton()
        setupSaveButton()
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

            binding.tvResult.text =
                """
                📍 여행지 : $destination

                📅 기간 : $startDate ~ $endDate

                ==================

                Day 1

                • 대표 관광지 방문
                • 현지 맛집 탐방

                Day 2

                • 자연 명소 방문
                • 사진 촬영

                Day 3

                • 기념품 구매
                • 귀가

                ==================

                (현재는 테스트 데이터입니다)
                """.trimIndent()
        }
    }

    private fun setupSaveButton() {

        binding.btnSavePlanner.setOnClickListener {
            savePlanner()
        }
    }

    private fun savePlanner() {

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