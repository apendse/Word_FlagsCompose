package com.aap.worldflags.screens.score

import androidx.lifecycle.ViewModel
import com.aap.worldflags.data.AnswerRecord
import com.aap.worldflags.repo.GamePlayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ShowDetailedScoreViewModel @Inject constructor(private val gamePlayRepository: GamePlayRepository): ViewModel() {
    private val _uiState = MutableStateFlow<List<AnswerRecord>?>(null)
    val uiState: StateFlow<List<AnswerRecord>?> = _uiState.asStateFlow()

    fun fetchScore() {
        _uiState.value = gamePlayRepository.getDetailedScore()
    }
}