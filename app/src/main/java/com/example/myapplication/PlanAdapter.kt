package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.PlanUiItem
import com.example.myapplication.databinding.ItemDayHeaderBinding
import com.example.myapplication.databinding.ItemPlanBinding

class PlanAdapter(

    private val items: MutableList<PlanUiItem>,
    private val editable: Boolean = false,
    private val onAddSchedule: (Int) -> Unit = {},
    private val onScheduleClick: (Int) -> Unit = {},
    private val onScheduleLongClick: (Int) -> Unit = {}

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

                val headerHolder = holder as HeaderViewHolder

                headerHolder.binding.tvDayHeader.text =
                    "Day ${item.day}"

                if (editable) {

                    headerHolder.binding.btnAddSchedule.visibility =
                        View.VISIBLE

                    headerHolder.binding.btnAddSchedule.setOnClickListener {
                        onAddSchedule(position)
                    }

                } else {

                    headerHolder.binding.btnAddSchedule.visibility =
                        View.GONE
                }
            }

            is PlanUiItem.Schedule -> {

                val plan =
                    item.item

                val icon = when(plan.time){

                    "오전" -> "🌅"
                    "오후" -> "☀️"
                    "저녁" -> "🌇"
                    "밤" -> "🌙"
                    else -> "🕒"
                }

                (holder as PlanViewHolder)
                    .binding
                    .tvTime
                    .text =
                    plan.time

                holder.binding.tvTime.text =
                    "$icon ${plan.time}"

                holder.binding.tvTitle.text =
                    plan.title

                holder.binding.chipCategory.text =
                    plan.category

                val color = when(plan.category) {

                    "관광" -> android.graphics.Color.parseColor("#3182F6")

                    "음식" -> android.graphics.Color.parseColor("#F97316")

                    "숙소" -> android.graphics.Color.parseColor("#22C55E")

                    "쇼핑" -> android.graphics.Color.parseColor("#A855F7")

                    "교통" -> android.graphics.Color.parseColor("#6B7280")

                    else -> android.graphics.Color.parseColor("#64748B")
                }

                holder.binding.chipCategory.setChipBackgroundColor(
                    android.content.res.ColorStateList.valueOf(color)
                )

                holder.itemView.setOnClickListener {
                    onScheduleClick(position)
                }

                holder.itemView.setOnLongClickListener {
                    onScheduleLongClick(position)

                    true
                }
            }
        }
    }

    override fun getItemCount(): Int =
        items.size
}