package com.aap.worldflags.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aap.worldflags.room.table.PAST_SCORE_TABLE
import com.aap.worldflags.room.table.ScoreTableRow
import kotlinx.coroutines.flow.Flow

@Dao
interface PastScoreDao {
    @Query("SELECT * FROM $PAST_SCORE_TABLE ORDER BY date_played")
    fun getScoreList(): Flow<List<ScoreTableRow>>

    @Query("SELECT * FROM $PAST_SCORE_TABLE WHERE isWinner = 1")
    fun getWinnerStreak(): Flow<List<ScoreTableRow>>

    @Insert
    suspend fun insertGameScore(scoreTableRow: ScoreTableRow)

    @Query("DELETE FROM $PAST_SCORE_TABLE")
    suspend fun clear()
}