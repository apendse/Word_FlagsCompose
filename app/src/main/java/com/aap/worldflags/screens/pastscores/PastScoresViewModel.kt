package com.aap.worldflags.screens.pastscores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aap.worldflags.data.SingleGameSummarizedData
import com.aap.worldflags.repo.PastGameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PastScoresViewModel @Inject constructor(pastGameRepository: PastGameRepository): ViewModel() {
    val scoreUiState: StateFlow<List<SingleGameSummarizedData>> = pastGameRepository.getPastScoreList().map {
        it.map {
            SingleGameSummarizedData(it.total, it.correct, it.wrong, it.playTime)
        }
    }.stateIn(
        scope = viewModelScope, started =  SharingStarted.Lazily, initialValue = emptyList())
}