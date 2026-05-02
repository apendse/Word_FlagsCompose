package com.aap.worldflags.repo

import com.aap.worldflags.data.AnswerRecord
import com.aap.worldflags.data.Game
import com.aap.worldflags.data.SingleGameSummarizedData
import com.aap.worldflags.room.table.QuestionRow
import kotlinx.coroutines.flow.Flow

/**
 * Repository that handles game creation and housekeeping
 */
interface GamePlayRepository {
    suspend fun createANewGame(numQuestionsInAGame: Int): Game
    fun isAnswerCorrect(game: Game, questionIndex: Int, answerIndex: Int): Boolean
    fun processAnswer(game: Game, questionIndex: Int, answerIndex: Int): Game
    fun getDetailedScore(): List<AnswerRecord>?
    fun getScoreSummary(): SingleGameSummarizedData
    fun getPastScores(): Flow<List<SingleGameSummarizedData>>
    suspend fun markGameComplete()
    suspend fun getPastQuestions(): List<QuestionRow>
    fun isWinner(correct: Int, wrong: Int): Boolean


}