package com.aap.worldflags.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aap.worldflags.data.CurrentGameScore
import com.aap.worldflags.data.CurrentQuestionScore
import com.aap.worldflags.data.Game
import com.aap.worldflags.data.MediaData
import com.aap.worldflags.data.SingleGameSummarizedData
import com.aap.worldflags.repo.GamePlayRepository
import com.aap.worldflags.repo.PastGameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val NUMBER_OF_QUESTIONS_PER_GAME = 3
private const val DELAY_AFTER_ANSWER_TO_NEXT_PAGE = 150L

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

    fun startANewGame(numQuestions: Int = NUMBER_OF_QUESTIONS_PER_GAME) {
        viewModelScope.launch(Dispatchers.Default) {
            val game = createAGame(numQuestions)
            _uiState.value = GameUiState.GameUiStateWithData(game)
        }
    }

    fun Game.isPossibleToMoveToNext() = currentQuestion < (NUMBER_OF_QUESTIONS_PER_GAME - 1)

    private suspend fun moveToNextQuestionIfNotComplete() {
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
                    with(game) {
                        SingleGameSummarizedData(
                            totalQuestions = questions.size,
                            correct = currentGameScore.correct,
                            wrong = currentGameScore.wrong,
                            gamePlayRepository.isWinner(
                                correct = currentGameScore.correct,
                                wrong = currentGameScore.wrong
                            ),
                            time = System.currentTimeMillis()
                        )
                    }
                )
                gamePlayRepository.markGameComplete()
            }
        }
    }

    private fun getNewTitle(gameScore: CurrentGameScore): String {
        return "Current score: ${gameScore.correct} :)  ${gameScore.wrong} :("
    }

    private suspend fun createAGame(numberOfQuestions: Int) =
        gamePlayRepository.createANewGame(numberOfQuestions)

    fun playSoundOnly(answerIndex: Int) {
        val currentState = _uiState.value
        if (currentState is GameUiState.GameUiStateWithData) {
            val currentGame = currentState.game
            val isAnswerCorrect = gamePlayRepository.isAnswerCorrect(currentGame, currentGame.currentQuestion, answerIndex)
            playSound(isAnswerCorrect)
        }
    }

    fun onAnswerClickCompleted(answerIndex: Int) {
        val currentState = _uiState.value
        if (currentState is GameUiState.GameUiStateWithData) {
            val currentGame = currentState.game
            val currentQuestionIndex = currentGame.currentQuestion

            if (currentGame.currentGameScore.answered > currentQuestionIndex) {
                return
            }

            // 1. Update selection locally
            val questionsWithCurrentUpdated = currentGame.questions.toMutableList()
            val answersWithSelectionUpdated = questionsWithCurrentUpdated[currentQuestionIndex].answers.toMutableList()
            answersWithSelectionUpdated.forEachIndexed { i, _ ->
                answersWithSelectionUpdated[i] = answersWithSelectionUpdated[i].copy(isSelected = (i == answerIndex))
            }
            questionsWithCurrentUpdated[currentQuestionIndex] =
                questionsWithCurrentUpdated[currentQuestionIndex].copy(answers = answersWithSelectionUpdated)

            val gameWithSelection = currentGame.copy(questions = questionsWithCurrentUpdated)

            // 2. Process logic and advance
            moveToNextQuestionWithDelay(answerIndex, gameWithSelection)
        }
    }

    private fun moveToNextQuestionWithDelay(answerIndex: Int, gameWithSelection: Game) {
        viewModelScope.launch {
            // Update score and selection state immediately
            updateScore(answerIndex, gameWithSelection)
            
            // Short delay so user can see their selection/score before sliding
            delay(DELAY_AFTER_ANSWER_TO_NEXT_PAGE)
            
            moveToNextQuestionIfNotComplete()
        }
    }

    private fun updateScore(answerIndex: Int, gameState: Game) {
        val updatedGame = gamePlayRepository.processAnswer(gameState, gameState.currentQuestion, answerIndex)
        val isAnswerCorrect = gamePlayRepository.isAnswerCorrect(gameState, gameState.currentQuestion, answerIndex)
        val newTitle = getNewTitle(updatedGame.currentGameScore)

        _scoreUiState.value = CurrentQuestionScore(isAnswerCorrect, newTitle, false)
        _uiState.value = GameUiState.GameUiStateWithData(updatedGame)
    }

    private fun playSound(correct: Boolean, onComplete : (() -> Unit)? = null) {
        val mediaPlayer = if (correct) mediaData.correct else mediaData.wrong
        mediaPlayer.setOnCompletionListener { _ -> onComplete?.invoke() }
        mediaPlayer.start()
    }
}