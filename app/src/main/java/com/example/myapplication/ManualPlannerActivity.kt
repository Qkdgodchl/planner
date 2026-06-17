package com.example.myapplication

import android.R
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.data.DatabaseProvider
import com.example.myapplication.data.DayPlan
import com.example.myapplication.data.PlanItem
import com.example.myapplication.data.PlanUiItem
import com.example.myapplication.data.Planner
import com.example.myapplication.data.TravelPlan
import com.example.myapplication.databinding.ActivityManualPlannerBinding
import com.example.myapplication.databinding.DialogAddScheduleBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class ManualPlannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManualPlannerBinding

    private val items = mutableListOf<PlanUiItem>()

    private lateinit var adapter: PlanAdapter

    private var dayCount = 0
    private var plannerId = -1
    private var editMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityManualPlannerBinding.inflate(layoutInflater)

        setContentView(binding.root)

        adapter = PlanAdapter(

            items,
            true,

            onAddSchedule = { headerPosition ->
                showScheduleDialog(headerPosition)
            },

            onScheduleClick = { position ->
                editSchedule(position)
            },

            onScheduleLongClick = { position ->
                deleteSchedule(position)
            }
        )

        binding.rvPlan.layoutManager =
            LinearLayoutManager(this)

        binding.rvPlan.adapter =
            adapter

        setupDatePicker()

        plannerId =
            intent.getIntExtra(
                "plannerId",
                -1
            )

        editMode =
            plannerId != -1

        if(editMode){

            binding.btnSave.text = "수정 완료"

            loadPlanner()
        }

        binding.btnAddDay.setOnClickListener {

            addDay()
        }

        binding.btnSave.setOnClickListener {

            savePlanner()
        }
    }

    override fun onResume() {
        super.onResume()
        loadPlanner()
    }

    private fun loadPlanner() {

        lifecycleScope.launch {

            val planner =
                DatabaseProvider
                    .getDatabase(this@ManualPlannerActivity)
                    .plannerDao()
                    .getPlannerById(plannerId)
                    ?: run {
                        Toast.makeText(
                            this@ManualPlannerActivity,
                            "플래너를 찾을 수 없습니다.",
                            Toast.LENGTH_SHORT
                        ).show()

                        finish()
                        return@launch
                    }

            binding.etDestination.setText(planner.destination)
            binding.etStartDate.setText(planner.startDate)
            binding.etEndDate.setText(planner.endDate)

            val travelPlan =
                Gson().fromJson(
                    planner?.planContent,
                    TravelPlan::class.java
                )

            items.clear()

            travelPlan.days.forEach { day ->

                items.add(
                    PlanUiItem.DayHeader(day.day)
                )

                day.items.forEach { schedule ->

                    items.add(
                        PlanUiItem.Schedule(schedule)
                    )
                }
            }

            dayCount =
                travelPlan.days.size

            adapter.notifyDataSetChanged()
        }
    }

    private fun addDay() {

        dayCount++

        items.add(
            PlanUiItem.DayHeader(dayCount)
        )

        adapter.notifyItemInserted(items.lastIndex)
    }

    private fun setupDatePicker() {

        binding.etStartDate.setOnClickListener {

            showDatePicker(binding.etStartDate)
        }

        binding.etEndDate.setOnClickListener {

            showDatePicker(binding.etEndDate)
        }

        binding.etStartDate.keyListener = null
        binding.etEndDate.keyListener = null
    }

    private fun showDatePicker(
        editText: android.widget.EditText
    ) {

        val calendar =
            Calendar.getInstance()

        DatePickerDialog(

            this,

            { _, year, month, day ->

                editText.setText(

                    String.format(
                        "%04d-%02d-%02d",
                        year,
                        month + 1,
                        day
                    )

                )
            },

            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)

        ).show()
    }

    private fun editSchedule(position: Int) {

        var headerPosition = position
        while (headerPosition >= 0) {
            if (items[headerPosition] is PlanUiItem.DayHeader) {
                break
            }
            headerPosition--
        }

        if (headerPosition >= 0) {
            showScheduleDialog(
                headerPosition = headerPosition,
                schedulePosition = position
            )
        }
    }

    private fun deleteSchedule(position: Int) {

        MaterialAlertDialogBuilder(this)
            .setTitle("일정 삭제")
            .setMessage("이 일정을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                items.removeAt(position)
                adapter.notifyItemRemoved(position)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showScheduleDialog(
        headerPosition: Int,
        schedulePosition: Int? = null
    ) {

        val dialogBinding =
            DialogAddScheduleBinding.inflate(layoutInflater)

        val timeList = listOf(
            "오전",
            "오후",
            "저녁",
            "밤"
        )

        val categoryList = listOf(
            "관광",
            "음식",
            "숙소",
            "쇼핑",
            "교통",
            "기타"
        )

        dialogBinding.spTime.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                timeList
            )

        dialogBinding.spCategory.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categoryList
            )

        // ===== 수정 모드인 경우 기존 값 채우기 =====
        if (schedulePosition != null) {

            val schedule =
                (items[schedulePosition] as PlanUiItem.Schedule).item

            dialogBinding.etTitle.setText(schedule.title)

            dialogBinding.spTime.setSelection(
                timeList.indexOf(schedule.time)
                    .coerceAtLeast(0)
            )

            dialogBinding.spCategory.setSelection(
                categoryList.indexOf(schedule.category)
                    .coerceAtLeast(0)
            )
        }

        AlertDialog.Builder(this)

            .setTitle(
                if (schedulePosition == null)
                    "일정 추가"
                else
                    "일정 수정"
            )

            .setView(dialogBinding.root)

            .setPositiveButton(
                if (schedulePosition == null)
                    "추가"
                else
                    "수정"
            ) { _, _ ->

                val time =
                    dialogBinding.spTime.selectedItem.toString()

                val title =
                    dialogBinding.etTitle.text.toString()

                val category =
                    dialogBinding.spCategory.selectedItem.toString()

                if (title.isBlank())
                    return@setPositiveButton

                val schedule = PlanUiItem.Schedule(
                    PlanItem(
                        time,
                        title,
                        category
                    )
                )

                if(schedulePosition == null) {

                    val insertPosition =
                        findInsertPosition(headerPosition)
                    items.add(
                        insertPosition,
                        PlanUiItem.Schedule(

                            PlanItem(
                                time,
                                title,
                                category
                            )
                        )
                    )
                    adapter.notifyItemInserted(insertPosition)

                }else{

                    items[schedulePosition] =
                        PlanUiItem.Schedule(
                            PlanItem(
                                time,
                                title,
                                category
                            )
                        )
                    adapter.notifyItemChanged(schedulePosition)
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun findInsertPosition(
        headerPosition: Int
    ): Int {

        var index = headerPosition + 1

        while (
            index < items.size &&
            items[index] is PlanUiItem.Schedule
        ) {
            index++
        }

        return index
    }

    private fun buildTravelPlan(): TravelPlan {

        val dayPlans = mutableListOf<DayPlan>()

        var currentDay = 0
        var currentItems = mutableListOf<PlanItem>()

        items.forEach { ui ->
            when (ui) {
                is PlanUiItem.DayHeader -> {
                    if (currentDay != 0) {

                        dayPlans.add(
                            DayPlan(
                                currentDay,
                                currentItems.toList()
                            )
                        )
                    }

                    currentDay = ui.day
                    currentItems = mutableListOf()
                }

                is PlanUiItem.Schedule -> {

                    currentItems.add(ui.item)
                }
            }
        }

        if (currentDay != 0) {

            dayPlans.add(
                DayPlan(
                    currentDay,
                    currentItems.toList()
                )
            )
        }

        return TravelPlan(

            destination =
                binding.etDestination.text.toString(),

            days = dayPlans
        )
    }

    private fun savePlanner() {

        if(!validatePlanner())
            return

        if(hasEmptyDay()){

            Toast.makeText(

                this,

                "일정이 없는 Day가 있습니다.",

                Toast.LENGTH_SHORT

            ).show()

            return
        }

        val destination =
            binding.etDestination.text.toString()

        val startDate =
            binding.etStartDate.text.toString()

        val endDate =
            binding.etEndDate.text.toString()

        if (
            destination.isBlank() ||
            startDate.isBlank() ||
            endDate.isBlank()
        ) {

            Toast.makeText(
                this,
                "여행 정보를 입력하세요.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val json =
            Gson().toJson(
                buildTravelPlan()
            )

        lifecycleScope.launch {

            val plannerDao =
                DatabaseProvider
                    .getDatabase(this@ManualPlannerActivity)
                    .plannerDao()

            if(editMode){

                plannerDao.updatePlanner(

                    Planner(

                        id = plannerId,

                        destination = destination,

                        startDate = startDate,

                        endDate = endDate,

                        duration = "$startDate ~ $endDate",

                        planContent = json

                    )

                )

            }else{

                plannerDao.insertPlanner(

                    Planner(

                        destination = destination,

                        startDate = startDate,

                        endDate = endDate,

                        duration = "$startDate ~ $endDate",

                        planContent = json

                    )

                )
            }

            Toast.makeText(
                this@ManualPlannerActivity,
                if (editMode) "플래너가 수정되었습니다."
                else "플래너가 저장되었습니다.",
                Toast.LENGTH_SHORT
            ).show()

            if (editMode) {

                finish()

            } else {

                startActivity(
                    Intent(
                        this@ManualPlannerActivity,
                        SavedPlannerActivity::class.java
                    )
                )

                finish()
            }
        }
    }

    private fun validatePlanner(): Boolean {

        val destination =
            binding.etDestination.text.toString().trim()

        val startDate =
            binding.etStartDate.text.toString().trim()

        val endDate =
            binding.etEndDate.text.toString().trim()

        if (destination.isBlank()) {

            Toast.makeText(
                this,
                "목적지를 입력하세요.",
                Toast.LENGTH_SHORT
            ).show()

            return false
        }

        if (startDate.isBlank()) {

            Toast.makeText(
                this,
                "시작일을 선택하세요.",
                Toast.LENGTH_SHORT
            ).show()

            return false
        }

        if (endDate.isBlank()) {

            Toast.makeText(
                this,
                "종료일을 선택하세요.",
                Toast.LENGTH_SHORT
            ).show()

            return false
        }

        if (dayCount == 0) {

            Toast.makeText(
                this,
                "Day를 하나 이상 추가하세요.",
                Toast.LENGTH_SHORT
            ).show()

            return false
        }

        return true
    }

    private fun hasEmptyDay(): Boolean {

        var hasSchedule = false

        items.forEachIndexed { index, item ->

            when(item){

                is PlanUiItem.DayHeader ->{

                    if(index != 0 && !hasSchedule){
                        return true
                    }

                    hasSchedule = false
                }

                is PlanUiItem.Schedule ->{

                    hasSchedule = true
                }
            }
        }

        return !hasSchedule
    }
}