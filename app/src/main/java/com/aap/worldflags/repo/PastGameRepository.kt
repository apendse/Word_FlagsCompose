package com.aap.worldflags.repo

import com.aap.worldflags.data.SingleGameSummarizedData
import com.aap.worldflags.room.table.ScoreTableRow
import kotlinx.coroutines.flow.Flow

/**
 * Keeps the record of past games.
 * When was the game played, how many correct, how many wrong, maybe some score (stars out of 5)
 */
interface PastGameRepository {
    suspend fun insertScoreRow(singleGameSummarizedData: SingleGameSummarizedData)
    fun getPastScoreList(): Flow<List<ScoreTableRow>>
}