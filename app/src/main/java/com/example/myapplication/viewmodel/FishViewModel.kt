package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FishViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FishRepository

    // ===== 观察数据 =====
    val allSpecies: StateFlow<List<FishSpecies>>
    val allRecords: StateFlow<List<FishRecord>>
    val totalCount: StateFlow<Int>
    val speciesCounts: StateFlow<List<SpeciesCount>>
    val speciesWeightSum: StateFlow<List<SpeciesWeightSum>>
    val topHeavyRecords: StateFlow<List<FishRecord>>

    // 今天零点的毫秒数
    private val todayStart: Long
        get() {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

    // 本周一的零点
    private val weekStart: Long
        get() {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

    // 本月1号的零点
    private val monthStart: Long
        get() {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

    // 今天、本周、本月的鱼获数
    val todayCount: StateFlow<Int>
    val weekCount: StateFlow<Int>
    val monthCount: StateFlow<Int>

    // 本周、本月的分类统计
    val weekSpeciesCounts: StateFlow<List<SpeciesCount>>
    val monthSpeciesCounts: StateFlow<List<SpeciesCount>>

    // 最近一次鱼获 - 使用 Flow 自动刷新
    val latestRecord: StateFlow<FishRecord?>
    val latestSpecies: StateFlow<FishSpecies?>

    // species name map for quick lookup
    private val speciesMap = MutableStateFlow<Map<Long, FishSpecies>>(emptyMap())

    init {
        val dao = FishDatabase.getDatabase(application).fishDao()
        repository = FishRepository(dao)

        allSpecies = repository.allSpecies.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        allRecords = repository.allRecords.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        totalCount = repository.totalCount.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), 0
        )
        speciesCounts = repository.speciesCounts.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        speciesWeightSum = repository.speciesWeightSum.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        topHeavyRecords = repository.topHeavyRecords.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        todayCount = repository.getCountSince(todayStart).stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), 0
        )
        weekCount = repository.getCountSince(weekStart).stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), 0
        )
        monthCount = repository.getCountSince(monthStart).stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), 0
        )
        weekSpeciesCounts = repository.getSpeciesCountsSince(weekStart).stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        monthSpeciesCounts = repository.getSpeciesCountsSince(monthStart).stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

        // 最新的鱼获 + 对应的鱼种信息 — 使用 Flow 实时更新
        val latestRecordFlow = repository.getLatestRecordFlow().stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), null
        )
        latestRecord = latestRecordFlow

        // 根据最新鱼获自动更新对应的鱼种信息
        val latestSp = MutableStateFlow<FishSpecies?>(null)
        viewModelScope.launch {
            latestRecordFlow.collect { record ->
                latestSp.value = if (record != null) {
                    repository.getSpeciesById(record.speciesId)
                } else null
            }
        }
        latestSpecies = latestSp.asStateFlow()

        // 构建鱼种映射表
        viewModelScope.launch {
            allSpecies.collect { speciesList ->
                speciesMap.value = speciesList.associateBy { it.id }
            }
        }
    }

    fun getSpeciesName(speciesId: Long): String {
        return speciesMap.value[speciesId]?.name ?: "未知"
    }

    fun getSpeciesEmoji(speciesId: Long): String {
        return speciesMap.value[speciesId]?.emoji ?: "🐟"
    }

    // ===== 操作函数 =====
    fun addRecord(record: FishRecord) {
        viewModelScope.launch {
            repository.addRecord(record)
        }
    }

    fun updateRecord(record: FishRecord) {
        viewModelScope.launch {
            repository.updateRecord(record)
        }
    }

    fun deleteRecord(record: FishRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record)
        }
    }

    fun getRecordById(id: Long, callback: (FishRecord?) -> Unit) {
        viewModelScope.launch {
            callback(repository.getRecordById(id))
        }
    }

    fun getFormattedDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA)
        return sdf.format(Date(timestamp))
    }

    fun getFormattedDateShort(timestamp: Long): String {
        val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.CHINA)
        return sdf.format(Date(timestamp))
    }

    fun getFormattedWeight(weight: Double?): String {
        return if (weight != null) String.format("%.2f kg", weight) else "未记录"
    }

    fun getFormattedLength(length: Double?): String {
        return if (length != null) String.format("%.1f cm", length) else "未记录"
    }
}
