package com.aap.worldflags.room.table

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

const val QUESTION_TABLE = "past_questions_table"
const val COUNTRY_CODE_COLUMN = "country_code"
const val COUNTRY_NAME_COLUMN = "country_name"
const val QUESTION_TIMESTAMP = "question_timestamp"
const val ANSWERED_CORRECTLY = "was_correct"
const val QUESTION_ID = "past_question_id"
@Entity(tableName = QUESTION_TABLE)
data class QuestionRow(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = QUESTION_ID) val id: Int,
    @ColumnInfo(name = COUNTRY_CODE_COLUMN) val countryCode: String,
    @ColumnInfo(name = COUNTRY_NAME_COLUMN) val countryName: String,
    @ColumnInfo(name = QUESTION_TIMESTAMP) val timestamp: Int,
    @ColumnInfo(name = ANSWERED_CORRECTLY) val wasCorrectlyAnswered: Boolean,
    )
