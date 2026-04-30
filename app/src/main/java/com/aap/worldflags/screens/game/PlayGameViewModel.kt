package com.aap.worldflags.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aap.worldflags.repo.GamePlayRepository
import com.aap.worldflags.repo.PastGameRepository
import com.aap.worldflags.data.CurrentGameScore
import com.aap.worldflags.data.CurrentQuestionScore
import com.aap.worldflags.data.Game
import com.aap.worldflags.data.MediaData
import com.aap.worldflags.data.SingleGameSummarizedData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val DEFAULT_NUM_QUESTIONS = 2
private const val DELAY_AFTER_ANSWER_TO_NEXT_PAGE = 1L

@HiltViewModel
class PlayGameViewModel @Inject constructor(
    private val gamePlayRepository: GamePlayRepository,
    private val pastGameRepository: PastGameRepository,
    private val mediaData: MediaData
) : ViewModel() {
    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Empty)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _scoreUiState = MutableStateFlow(CurrentQuestionScore(false, "", true))
    val scoreUiState: StateFlow<CurrentQuestionScore> = _scoreUiState.asStateFlow()

    fun startANewGame(numQuestions: Int = DEFAULT_NUM_QUESTIONS) {
        viewModelScope.launch(Dispatchers.Default) {
            val game = createAGame(numQuestions)
            _uiState.value = GameUiState.GameUiStateWithData(game)
        }
    }

    fun Game.isPossibleToMoveToNext() = currentQuestion < (DEFAULT_NUM_QUESTIONS - 1)

    private fun moveToNextQuestionIfNotComplete() {
        viewModelScope.launch {
            delay(DELAY_AFTER_ANSWER_TO_NEXT_PAGE)
            val stateValue = _uiState.value
            if (stateValue is GameUiState.GameUiStateWithData) {

                val game = stateValue.game
                if (game.isPossibleToMoveToNext()) {
                    val gameWithQuestionAdvancedByOne =
                        game.copy(currentQuestion = game.currentQuestion + 1)
                    _uiState.value = GameUiState.GameUiStateWithData(gameWithQuestionAdvancedByOne)
                } else {
                    _uiState.value = GameUiState.GameComplete

                    pastGameRepository.insertScoreRow(
                        SingleGameSummarizedData(
                            totalQuestions = game.questions.size,
                            correct = game.currentGameScore.correct,
                            wrong = game.currentGameScore.wrong,
                            time = System.currentTimeMillis()
                        )
                    )
                    gamePlayRepository.markGameComplete()
                }
            }
        }
    }

    private fun getNewTitle(gameScore: CurrentGameScore): String {
        return "Current score: ${gameScore.correct} :)  ${gameScore.wrong} :("
    }

    private suspend fun createAGame(numberOfQuestions: Int) =
        gamePlayRepository.createANewGame(numberOfQuestions)

    fun playSoundOnly(answerIndex: Int) {
        val currentGame = (_uiState.value as GameUiState.GameUiStateWithData).game
        val currentQuestionIndex = currentGame.currentQuestion
        val isAnswerCorrect =
            gamePlayRepository.isAnswerCorrect(currentGame, currentQuestionIndex, answerIndex)
        playSound(isAnswerCorrect)
    }

    fun onAnswerClickCompleted(answerIndex: Int) {
        val currentGame = (_uiState.value as GameUiState.GameUiStateWithData).game
        val currentQuestionIndex = currentGame.currentQuestion
        val questionsWithCurrentUpdated = currentGame.questions.toMutableList()
        val answersWithSelectionUpdated = questionsWithCurrentUpdated[currentQuestionIndex].answers.toMutableList()
        answersWithSelectionUpdated.forEachIndexed { i, answer ->
            if (i == answerIndex) {
                answersWithSelectionUpdated[i] = answersWithSelectionUpdated[i].copy(isSelected = true)
            } else {
                answersWithSelectionUpdated[i] = answersWithSelectionUpdated[i].copy(isSelected = false)
            }
        }
        questionsWithCurrentUpdated[currentQuestionIndex] =
            questionsWithCurrentUpdated[currentQuestionIndex].copy(answers = answersWithSelectionUpdated)
        val intermediateGameState =
            GameUiState.GameUiStateWithData(currentGame.copy(questions = questionsWithCurrentUpdated))
        moveToNextQuestionWithDelay(answerIndex, intermediateGameState)
    }

    private fun moveToNextQuestionWithDelay(answerIndex: Int, gameState: GameUiState.GameUiStateWithData) {
        updateScore(answerIndex, gameState)
        viewModelScope.launch {
            delay(DELAY_AFTER_ANSWER_TO_NEXT_PAGE)
            moveToNextQuestionIfNotComplete()
        }

    }

    private fun updateScore(answerIndex: Int, gameStateWithData: GameUiState.GameUiStateWithData) {

        val gameState = gameStateWithData.game
        val questionIndex = gameState.currentQuestion

        val updatedGame = gamePlayRepository.processAnswer(gameState, questionIndex, answerIndex)
        val isAnswerCorrect =
            gamePlayRepository.isAnswerCorrect(gameState, questionIndex, answerIndex)
        val newTitle = getNewTitle(updatedGame.currentGameScore)

        _scoreUiState.value = CurrentQuestionScore(isAnswerCorrect, newTitle, false)
        _uiState.value = GameUiState.GameUiStateWithData(updatedGame)
    }

    private fun playSound(correct: Boolean, onComplete : (() -> Unit)? = null) {
        val mediaPlayer = if (correct) {
            mediaData.correct
        } else {
            mediaData.wrong
        }
        mediaPlayer.setOnCompletionListener { _ ->
            onComplete?.invoke()
        }
        mediaPlayer.start()
    }

    fun markGameComplete() {
        viewModelScope.launch(Dispatchers.IO) {
            gamePlayRepository.markGameComplete()
            gamePlayRepository.markGameComplete()
        }
    }
}
