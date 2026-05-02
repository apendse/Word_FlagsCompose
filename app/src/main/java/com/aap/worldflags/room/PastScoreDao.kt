package com.aap.worldflags.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aap.worldflags.room.table.SCORE_TABLE
import com.aap.worldflags.room.table.ScoreTableRow
import kotlinx.coroutines.flow.Flow

@Dao
interface PastScoreDao {
    @Query("SELECT * FROM $SCORE_TABLE")
    fun getScoreList(): Flow<List<ScoreTableRow>>

    @Query("SELECT * FROM $SCORE_TABLE WHERE isWinner = 1")
    fun getWinnerStreak(): Flow<List<ScoreTableRow>>

    @Insert
    suspend fun insertGameScore(scoreTableRow: ScoreTableRow)

}