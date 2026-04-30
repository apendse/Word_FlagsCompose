package com.aap.worldflags.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert

import com.aap.worldflags.room.table.COUNTRY_NAME_COLUMN
import com.aap.worldflags.room.table.QUESTION_TABLE
import com.aap.worldflags.room.table.QUESTION_TIMESTAMP
import com.aap.worldflags.room.table.QuestionRow

@Dao
interface PastQuestionsDao {
    /**
     * Get the question row for a given country
     */
    @Query("SELECT * from $QUESTION_TABLE where $COUNTRY_NAME_COLUMN = :countryParam"  )
    suspend fun getQuestionRow(countryParam: String): QuestionRow?

    @Query("SELECT * from $QUESTION_TABLE")
    suspend fun getQuestionRows(): List<QuestionRow>

    @Insert
    suspend fun insertQuestionRow(questionRow: QuestionRow)

    @Upsert
    suspend fun insertOrUpdateQuestionRow(questionRow: QuestionRow)

    @Update
    suspend fun updateQuestionRow(questionRow: QuestionRow)

    @Query("SELECT COUNT(*) from $QUESTION_TABLE"  )
    suspend fun getQuestionCount(): Int

//    @Query("SELECT * from $QUESTION_TABLE WHERE $QUESTION_ID = ( SELECT $QUESTION_ID FROM $QUESTION_TABLE WHERE $COUNTRY_NAME_COLUMN = :countryParam)   "  )
//    suspend fun deleteQuestionForCountry(countryParam: String)

    @Query("DELETE FROM $QUESTION_TABLE WHERE $QUESTION_TIMESTAMP = ( SELECT MIN($QUESTION_TIMESTAMP) FROM $QUESTION_TABLE)" )
    suspend fun deleteOldestQuestion()

}

