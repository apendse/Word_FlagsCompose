package com.aap.worldflags.room.table

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Past score records
 */
const val SCORE_TABLE = "score_table"
@Entity(tableName = SCORE_TABLE)
data class ScoreTableRow(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "total_questions") val total: Int,
    @ColumnInfo(name = "correct") val correct: Int,
    @ColumnInfo(name = "wrong") val wrong: Int,
    @ColumnInfo(name = "isWinner") val isWinner: Int,
    @ColumnInfo(name = "date_played") val playTime: Long,
)
