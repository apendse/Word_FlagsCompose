package com.aap.worldflags.repo

import android.util.Log
import com.aap.worldflags.data.SingleGameSummarizedData
import com.aap.worldflags.room.PastScoreDao
import com.aap.worldflags.room.table.ScoreTableRow
import kotlinx.coroutines.flow.Flow

class PastGameRepositoryImpl(private val pastScoreDao: PastScoreDao): PastGameRepository  {

    override suspend fun insertScoreRow(singleGameSummarizedData: SingleGameSummarizedData) {
        Log.d("YYYY", "PastGameRepositoryImpl: insertScoreRow ${singleGameSummarizedData.totalQuestions}")
        with(singleGameSummarizedData) {
            val scoreTableRow = ScoreTableRow(
                id = 0,
                total = totalQuestions,
                correct = correct,
                wrong = wrong,
                isWinner = winner.toInt(),
                playTime = singleGameSummarizedData.time
            )
            pastScoreDao.insertGameScore(scoreTableRow)
        }
    }

    override fun getPastScoreList(): Flow<List<ScoreTableRow>> {
        return pastScoreDao.getScoreList()
    }

    override fun getPastWinningStreaks(): Flow<List<ScoreTableRow>> {
        return pastScoreDao.getWinnerStreak()
    }

    override suspend fun clearTable() {
        pastScoreDao.clear()
    }
}