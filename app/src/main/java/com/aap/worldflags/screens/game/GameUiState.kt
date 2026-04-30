package com.aap.worldflags.screens.game

import com.aap.worldflags.data.Game

sealed class GameUiState {
    data object Empty: GameUiState()
    data class GameUiStateWithData(val game: Game): GameUiState()
    object GameComplete: GameUiState()
}



