package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.data.DatabaseProvider
import com.example.myapplication.databinding.ActivitySavedPlannerBinding
import kotlinx.coroutines.launch

class SavedPlannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySavedPlannerBinding

    private lateinit var plannerAdapter: PlannerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySavedPlannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        plannerAdapter =
            PlannerAdapter(
                emptyList()
            ) { planner ->

                val intent =
                    Intent(
                        this,
                        PlannerDetailActivity::class.java
                    )

                intent.putExtra(
                    "destination",
                    planner.destination
                )

                intent.putExtra(
                    "duration",
                    planner.duration
                )

                intent.putExtra(
                    "planContent",
                    planner.planContent
                )

                startActivity(intent)
            }

        binding.rvPlanner.apply {
            layoutManager = LinearLayoutManager(this@SavedPlannerActivity)
            adapter = plannerAdapter
        }

        loadPlanners()
    }

    private fun loadPlanners() {

        lifecycleScope.launch {

            val planners =
                DatabaseProvider
                    .getDatabase(this@SavedPlannerActivity)
                    .plannerDao()
                    .getAllPlanners()

            plannerAdapter.updateData(planners)
        }
    }
}