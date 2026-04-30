package com.aap.worldflags.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aap.worldflags.room.table.GameRow
import com.aap.worldflags.room.table.QuestionRow
import com.aap.worldflags.room.table.ScoreTableRow


@Database(
    version = 4,
    entities = [GameRow::class, ScoreTableRow::class, QuestionRow::class],
    )
abstract class GameDatabase: RoomDatabase() {
    abstract fun pastGamesDao(): GameDao
    abstract fun pastScoresDao(): PastScoreDao
    abstract fun pastQuestionsDao(): PastQuestionsDao
}





