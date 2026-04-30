package com.aap.worldflags.screens.score

import androidx.lifecycle.ViewModel
import com.aap.worldflags.data.SingleGameSummarizedData
import com.aap.worldflags.repo.GamePlayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ShowScoreViewModel @Inject constructor(private val gamePlayRepository: GamePlayRepository): ViewModel() {
    private val _uiState = MutableStateFlow<SingleGameSummarizedData?>(null)
    val uiState: StateFlow<SingleGameSummarizedData?> = _uiState.asStateFlow()

    fun fetchScore() {
        _uiState.value = gamePlayRepository.getScoreSummary()
    }
}

