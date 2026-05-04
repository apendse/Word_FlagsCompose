package com.aap.worldflags.screens.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aap.worldflags.data.SingleGameSummarizedData
import com.aap.worldflags.repo.FlagImages
import com.aap.worldflags.repo.GameDataCreationRepository
import com.aap.worldflags.repo.PastGameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random

const val HORIZONTAL_ROW_COUNT = 20
@HiltViewModel
class HomeViewModel @Inject constructor(
    pastGameRepository: PastGameRepository,
    private val gameCreationRepository: GameDataCreationRepository,
    private val application: Application) : ViewModel() {

    private val _uiState = MutableStateFlow<List<Int>>(emptyList())
    val uiState: StateFlow<List<Int>> = _uiState.asStateFlow()

    private val _scrollState = MutableStateFlow<Int>(0)
    val scrollPos: StateFlow<Int> = _scrollState.asStateFlow()

    private val _startOffsets = MutableStateFlow<List<Int>>(emptyList())
    val startOffsets: StateFlow<List<Int>> = _startOffsets.asStateFlow()



    init {
        initializeGameData()
        initializeGrid()
        startScrollPos()
        //insertTestData(pastGameRepository)
    }

    private fun startScrollPos() {
        viewModelScope.launch(Dispatchers.IO) {
            while(true) {
                delay(5000)
                _scrollState.value = _scrollState.value + 1
            }
        }
    }

    private fun initializeGrid() {
        viewModelScope.launch {
            _uiState.value = FlagImages.images.values.toList()
            val list = mutableListOf<Int>()
            repeat(20) {
                // Create a starting point which can render completely so subtract a small number from the last
                list.add(Random.nextInt(0, FlagImages.images.values.size - 5))
            }
            _startOffsets.value = list
        }
    }


    private fun initializeGameData() {
        viewModelScope.launch {
            gameCreationRepository.readAssets()
        }
    }

    fun insertTestData(repo: PastGameRepository) {
        // To test winning streaks insert a few rows
        viewModelScope.launch {
            val job = async(Dispatchers.IO) {
                repo.clearTable()
                val formatter = SimpleDateFormat("MMMM d, yyyy h:mma", Locale.getDefault())

                val list = listOf(
                    SingleGameSummarizedData(
                        20, 19, 1, true,
                        formatter.getTimeStamp("April 24, 2026 2:30pm")
                    ),

                    SingleGameSummarizedData(
                        20, 20, 0, true,
                        formatter.getTimeStamp("April 24, 2026 3:30pm")
                    ),

                    SingleGameSummarizedData(
                        20, 19, 1, true,
                        formatter.getTimeStamp("April 24, 2026 4:30pm")
                    ),

                    SingleGameSummarizedData(
                        20, 20, 0, true,
                        formatter.getTimeStamp("April 24, 2026 5:30pm")
                    ),

                    SingleGameSummarizedData(
                        20, 20, 0, true,
                        formatter.getTimeStamp("April 27, 2026 1:30pm")
                    ),

                    SingleGameSummarizedData(
                        20, 15, 5, false,
                        formatter.getTimeStamp("April 27, 2026 2:30pm")
                    ),
                    SingleGameSummarizedData(
                        20, 15, 5, false,
                        formatter.getTimeStamp("April 27, 2026 3:30pm")
                    ),

                    SingleGameSummarizedData(
                        20, 18, 2, true,
                        formatter.getTimeStamp("April 28, 2026 2:30pm")
                    ),
                    SingleGameSummarizedData(
                        20, 19, 1, true,
                        formatter.getTimeStamp("April 28, 2026 3:15pm")
                    ),
                    SingleGameSummarizedData(
                        20, 20, 0, true,
                        formatter.getTimeStamp("April 28, 2026 4:30pm")
                    ),
                )
                list.forEach { row ->
                    repo.insertScoreRow(row)
                }
            }
            job.await()
        }
    }

    fun SimpleDateFormat.getTimeStamp(timeStr: String): Long {
        return parse(timeStr)?.time ?: 0L
    }
}