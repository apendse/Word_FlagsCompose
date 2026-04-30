package com.aap.worldflags.room.table

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Past game records
 */
@Entity(tableName = "game_table")
data class GameRow(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "question_flag") val total: Int,
    @ColumnInfo(name = "answer1") val correct: Int,
    @ColumnInfo(name = "answer2") val wrong: Int,
    @ColumnInfo(name = "date_played") val date: String,
)
