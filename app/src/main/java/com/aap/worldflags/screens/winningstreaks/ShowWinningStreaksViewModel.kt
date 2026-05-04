package com.aap.worldflags.screens.winningstreaks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aap.worldflags.repo.PastGameRepository
import com.aap.worldflags.room.table.ScoreTableRow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ShowWinningStreaksViewModel @Inject constructor(
    pastGameRepository: PastGameRepository
) : ViewModel() {
    enum class WinnerState {
        UNINITIALIZED,
        WINNER,
        NOT_WINNER
    }

    private data class StreakData(val winnerState: WinnerState, val list: List<ScoreTableRow>)

    val WINNER_STREAK_MIN_SIZE = 3

    val flowOfStreaks = pastGameRepository.getPastScoreList()
        .map { rows ->
            // Group into streaks on the list itself, before flatMapping
            rows
                .fold(mutableListOf<StreakData>()) { streaks, row ->
                    val winnerState = if (row.isWinner == 1) WinnerState.WINNER else WinnerState.NOT_WINNER
                    val last = streaks.lastOrNull()
                    if (last != null && last.winnerState == winnerState) {
                        streaks[streaks.lastIndex] = StreakData(winnerState, last.list + row)
                    } else {
                        streaks.add(StreakData(winnerState, listOf(row)))
                    }
                    streaks
                }
                .filter { it.winnerState == WinnerState.WINNER && it.list.size >= WINNER_STREAK_MIN_SIZE }
        }
        .map { winningStreaks ->
            WinningStreakUiState.WinningStreak(winningStreaks.map { it.list })
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, WinningStreakUiState.Loading)

}


sealed class WinningStreakUiState {
    data object Loading : WinningStreakUiState()
    data object NoWinningStreak : WinningStreakUiState()
    data class WinningStreak(val list: List<List<ScoreTableRow>>) : WinningStreakUiState()
}