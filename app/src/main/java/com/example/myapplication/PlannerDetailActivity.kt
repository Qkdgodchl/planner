package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityPlannerDetailBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.data.PlanUiItem
import com.example.myapplication.data.TravelPlan
import com.google.gson.Gson

class PlannerDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlannerDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityPlannerDetailBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        val destination =
            intent.getStringExtra(
                "destination"
            ) ?: ""

        val duration =
            intent.getStringExtra(
                "duration"
            ) ?: ""

        val planContent =
            intent.getStringExtra(
                "planContent"
            ) ?: ""

        binding.tvDestination.text =
            destination

        binding.tvDuration.text =
            duration

        binding.rvPlan.layoutManager =
            LinearLayoutManager(this)

        try {

            val travelPlan =
                Gson().fromJson(
                    planContent,
                    TravelPlan::class.java
                )

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

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }
}