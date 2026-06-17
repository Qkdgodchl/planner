package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.Planner
import com.example.myapplication.data.TravelPlan
import com.example.myapplication.databinding.ItemPlannerBinding
import com.google.gson.Gson

class PlannerAdapter(
    private var planners: List<Planner>,
    private val onPlannerClick: (Planner) -> Unit,
    private val onPlannerLongClick: (Planner) -> Unit
) : RecyclerView.Adapter<PlannerAdapter.PlannerViewHolder>() {

    inner class PlannerViewHolder(
        val binding: ItemPlannerBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlannerViewHolder {

        val binding = ItemPlannerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return PlannerViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: PlannerViewHolder,
        position: Int
    ) {

        val planner = planners[position]

        holder.binding.tvDestination.text =
            planner.destination

        holder.binding.tvDate.text =
            planner.duration

        try {

            val travelPlan =
                Gson().fromJson(
                    planner.planContent,
                    TravelPlan::class.java
                )

            val firstDay =
                travelPlan.days.firstOrNull()

            val firstItem =
                firstDay?.items?.firstOrNull()

            holder.binding.tvPlanPreview.text =
                if (firstItem != null) {

                    "Day ${firstDay.day} · ${firstItem.title}"

                } else {

                    "일정 없음"
                }

        } catch (e: Exception) {

            holder.binding.tvPlanPreview.text =
                "저장된 여행 일정"
        }

        holder.itemView.setOnClickListener {
            onPlannerClick(planner)
        }

        holder.itemView.setOnLongClickListener {

            onPlannerLongClick(planner)

            true
        }
    }

    override fun getItemCount(): Int =
        planners.size

    fun updateData(newList: List<Planner>) {
        planners = newList
        notifyDataSetChanged()
    }
}