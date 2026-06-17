package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.databinding.ActivityPlannerDetailBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.data.DatabaseProvider
import com.example.myapplication.data.PlanUiItem
import com.example.myapplication.data.TravelPlan
import com.google.gson.Gson
import kotlinx.coroutines.launch

class PlannerDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlannerDetailBinding
    private var plannerId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityPlannerDetailBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.rvPlan.layoutManager =
            LinearLayoutManager(this)

        plannerId =
            intent.getIntExtra(
                "plannerId",
                -1
            )

        if (plannerId == -1) {

            finish()

            return
        }

        binding.btnEdit.setOnClickListener {

            val intent =
                Intent(
                    this,
                    ManualPlannerActivity::class.java
                )

            intent.putExtra(
                "plannerId",
                plannerId
            )

            startActivity(intent)
        }

        loadPlanner(plannerId)
    }

    override fun onResume() {
        super.onResume()

        if (plannerId != -1) {
            loadPlanner(plannerId)
        }
    }

    private fun loadPlanner(
        plannerId: Int
    ) {

        lifecycleScope.launch {

            val planner =
                DatabaseProvider
                    .getDatabase(this@PlannerDetailActivity)
                    .plannerDao()
                    .getPlannerById(plannerId)?: return@launch

            binding.tvDestination.text =
                planner.destination

            binding.tvDuration.text =
                planner.duration

            val travelPlan =
                Gson().fromJson(
                    planner.planContent,
                    TravelPlan::class.java
                )

            val uiItems =
                mutableListOf<PlanUiItem>()

            travelPlan.days.forEach { day ->

                uiItems.add(
                    PlanUiItem.DayHeader(day.day)
                )

                day.items.forEach {

                    uiItems.add(
                        PlanUiItem.Schedule(it)
                    )
                }
            }

            binding.rvPlan.adapter =
                PlanAdapter(uiItems)
        }
    }
}