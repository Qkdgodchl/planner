package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.data.DatabaseProvider
import com.example.myapplication.data.Planner
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

                planners = emptyList(),

                onPlannerClick = { planner ->

                    val intent =
                        Intent(
                            this,
                            PlannerDetailActivity::class.java
                        )

                    intent.putExtra(
                        "plannerId",
                        planner.id
                    )

                    startActivity(intent)
                },

                onPlannerLongClick = { planner ->

                    showDeleteDialog(planner)
                }
            )

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

    private fun showDeleteDialog(
        planner: Planner
    ) {

        androidx.appcompat.app.AlertDialog.Builder(this)

            .setTitle("플래너 삭제")

            .setMessage(
                "'${planner.destination}' 여행 일정을 삭제하시겠습니까?"
            )

            .setPositiveButton("삭제") { _, _ ->

                lifecycleScope.launch {

                    DatabaseProvider
                        .getDatabase(this@SavedPlannerActivity)
                        .plannerDao()
                        .deletePlanner(planner.id)

                    loadPlanners()
                }
            }

            .setNegativeButton("취소", null)

            .show()
    }
}