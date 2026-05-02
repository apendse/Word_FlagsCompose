package com.aap.worldflags.screens.winningstreaks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aap.worldflags.repo.PastGameRepository
import com.aap.worldflags.room.table.ScoreTableRow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ShowWinningStreaksViewModel  @Inject constructor(pastGameRepository: PastGameRepository
): ViewModel() {

    val uiState: StateFlow<WinningStreakUiState> = pastGameRepository.getPastWinningStreaks().map { v ->
        if (v.isEmpty()) {
            WinningStreakUiState.NoWinningStreak
        } else {
            WinningStreakUiState.WinningStreak(v)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WinningStreakUiState.Loading
    )

}

sealed class WinningStreakUiState {
    data object Loading: WinningStreakUiState()
    data object NoWinningStreak: WinningStreakUiState()
    data class WinningStreak(val list: List<ScoreTableRow>): WinningStreakUiState()
}