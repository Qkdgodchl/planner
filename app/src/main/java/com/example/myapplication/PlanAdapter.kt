package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.PlanUiItem
import com.example.myapplication.databinding.ItemDayHeaderBinding
import com.example.myapplication.databinding.ItemPlanBinding

class PlanAdapter(
    private val items: List<PlanUiItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {

        private const val TYPE_HEADER = 0
        private const val TYPE_PLAN = 1
    }

    inner class HeaderViewHolder(
        val binding: ItemDayHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root)

    inner class PlanViewHolder(
        val binding: ItemPlanBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(
        position: Int
    ): Int {

        return when(items[position]) {

            is PlanUiItem.DayHeader ->
                TYPE_HEADER

            is PlanUiItem.Schedule ->
                TYPE_PLAN
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        return if (viewType == TYPE_HEADER) {

            HeaderViewHolder(
                ItemDayHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

        } else {

            PlanViewHolder(
                ItemPlanBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {

        when(val item = items[position]) {

            is PlanUiItem.DayHeader -> {

                (holder as HeaderViewHolder)
                    .binding
                    .tvDayHeader
                    .text =
                    "Day ${item.day}"
            }

            is PlanUiItem.Schedule -> {

                val plan =
                    item.item

                val icon =
                    when(plan.category) {

                        "관광" -> "📸"
                        "음식" -> "🍴"
                        "숙소" -> "🏨"
                        "쇼핑" -> "🛍"
                        "교통" -> "🚗"

                        else -> "📍"
                    }

                (holder as PlanViewHolder)
                    .binding
                    .tvTime
                    .text =
                    plan.time

                holder.binding.tvTitle.text =
                    plan.title

                holder.binding.tvCategory.text =
                    "$icon ${plan.category}"
            }
        }
    }

    override fun getItemCount(): Int =
        items.size
}