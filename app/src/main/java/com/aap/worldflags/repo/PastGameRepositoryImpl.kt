package com.aap.worldflags.repo

import android.util.Log
import com.aap.worldflags.data.SingleGameSummarizedData
import com.aap.worldflags.room.PastScoreDao
import com.aap.worldflags.room.table.ScoreTableRow
import kotlinx.coroutines.flow.Flow

class PastGameRepositoryImpl(private val pastScoreDao: PastScoreDao): PastGameRepository  {

    override suspend fun insertScoreRow(singleGameSummarizedData: SingleGameSummarizedData) {
        Log.d("YYYY", "PastGameRepositoryImpl: insertScoreRow ${singleGameSummarizedData.totalQuestions}");
        val scoreTableRow = ScoreTableRow(
            id = 0,
            total = singleGameSummarizedData.totalQuestions,
            correct = singleGameSummarizedData.correct,
            wrong = singleGameSummarizedData.wrong,
            playTime = singleGameSummarizedData.time
        )
        pastScoreDao.insertGameScore(scoreTableRow)
    }

    override fun getPastScoreList(): Flow<List<ScoreTableRow>> {
        return pastScoreDao.getScoreList()
    }

}