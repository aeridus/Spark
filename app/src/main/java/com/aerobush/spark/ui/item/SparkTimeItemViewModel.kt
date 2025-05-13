package com.aerobush.spark.ui.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aerobush.spark.data.SparkTimeItem
import com.aerobush.spark.data.SparkTimeItemsRepository
import com.aerobush.spark.data.SparkConstants
import com.aerobush.spark.data.ThemeMode
import com.aerobush.spark.data.TimeUtils
import com.aerobush.spark.data.UserPreferencesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Duration
import java.time.OffsetDateTime

/**
 * ViewModel to manage spark items in the Room database.
 */
class SparkTimeItemViewModel(
    private val sparkTimeItemsRepository: SparkTimeItemsRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    /**
     * Changes based on user input
     */
    val themeUiState: StateFlow<ThemeUiState> =
        userPreferencesRepository.themeMode.map { themeMode ->
            ThemeUiState(
                themeMode = themeMode
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = runBlocking {
                ThemeUiState(
                    themeMode = userPreferencesRepository.themeMode.first()
                )
            }
        )

    fun cycleThemeMode(currentThemeMode : ThemeMode) {
        viewModelScope.launch {
            val newThemeModeIndex = (currentThemeMode.ordinal + 1) % ThemeMode.entries.size

            userPreferencesRepository.saveThemeMode(ThemeMode.entries[newThemeModeIndex])
        }
    }

    /**
     * Changes frequently
     */
    private val _currentTimeState = MutableStateFlow<OffsetDateTime>(TimeUtils.getCurrentTime())
    private val currentTimeState: StateFlow<OffsetDateTime> = _currentTimeState

    init {
        viewModelScope.launch {
            while (isActive) {
                _currentTimeState.value = TimeUtils.getCurrentTime()
                delay(REFRESH_MILLIS)
            }
        }
    }

    /**
     * Changes based on user input
     */
    private val dayThresholdHourState: StateFlow<Int> =
        userPreferencesRepository.dayThresholdHour.map { dayThresholdHour ->
            dayThresholdHour
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = runBlocking {
                userPreferencesRepository.dayThresholdHour.first()
            }
        )

    fun updateDayThresholdHour(newHour : Int) {
        viewModelScope.launch {
            userPreferencesRepository.saveDayThresholdHour(newHour)
        }
    }


    /**
     * Changes less often
     */
    private val sparkTimeItemsState: StateFlow<List<SparkTimeItem>> = sparkTimeItemsRepository
        .getAllItemsStream()
        .filterNotNull()
        .map { sparkTimeItems ->
            sparkTimeItems
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = listOf<SparkTimeItem>()
        )

    /**
     * We want this to update with time changes, user input, or database changes
     */
    val uiState: StateFlow<SparkUiState> = combine(
        currentTimeState, dayThresholdHourState, sparkTimeItemsState
    ) { currentTime, dayThresholdHour, sparkTimeItems ->
            val dayThreshold = TimeUtils.getDayThresholdEpochMilli(
                dayThresholdHour
            )
            val weekAgoThreshold = dayThreshold - Duration.ofDays(7).toMillis()
            val sparkWeekItems = sparkTimeItems.filter { it.time >= weekAgoThreshold }

            if (sparkWeekItems.isEmpty())
            {
                SparkUiState(dayThresholdHour = dayThresholdHour)
            }
            else {
                val lastSparkTime = TimeUtils.toOffsetDateTime(sparkWeekItems.last().time)

                var totalHours = 24
                var totalMinutes = 0
                TimeUtils.getDurationParts(
                    startTime = lastSparkTime,
                    endTime = currentTime,
                    output =  { hours, minutes ->
                        totalHours = hours.toInt()
                        totalMinutes = minutes.toInt()
                    }
                )

                SparkUiState(
                    dayThresholdHour = dayThresholdHour,
                    totalHours = totalHours,
                    totalMinutes = totalMinutes,
                    sparkChoices = sparkWeekItems.map { it.sparkChoice }
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = SparkUiState()
        )

    /**
     * Inserts a [SparkTimeItem] in the Room database
     */
    suspend fun saveSparkTimeItem(sparkChoice: String) {
        val sparkTimeItem = SparkTimeItem(
            time = TimeUtils.toEpochMilli(TimeUtils.getCurrentTime()),
            sparkChoice = sparkChoice,
            sentFirstReminder = false,
            sentSecondReminder = false
        )
        sparkTimeItemsRepository.insertItem(sparkTimeItem)
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
        private const val REFRESH_MILLIS = 15_000L
    }
}

/**
 * UI state for Spark
 */
data class ThemeUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)

data class SparkUiState(
    val dayThresholdHour: Int = SparkConstants.DEFAULT_DAY_THRESHOLD_HOUR,
    val totalHours: Int = 24,
    val totalMinutes: Int = 0,
    val sparkChoices: List<String> = emptyList<String>(),
)