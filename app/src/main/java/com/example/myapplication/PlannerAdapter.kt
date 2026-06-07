package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.Planner
import com.example.myapplication.databinding.ItemPlannerBinding

class PlannerAdapter(
    private var planners: List<Planner>
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

        holder.binding.tvPlanPreview.text =
            planner.planContent.take(80)
    }

    override fun getItemCount(): Int =
        planners.size

    fun updateData(newList: List<Planner>) {
        planners = newList
        notifyDataSetChanged()
    }
}