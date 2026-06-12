package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityPlannerDetailBinding

class PlannerDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlannerDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityPlannerDetailBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val destination =
            intent.getStringExtra("destination") ?: ""

        val duration =
            intent.getStringExtra("duration") ?: ""

        val content =
            intent.getStringExtra("content") ?: ""

        binding.tvDestination.text = destination
        binding.tvDuration.text = duration
        binding.tvContent.text = content
    }
}