package com.aap.worldflags.data

/**
 * @property totalQuestions number of questions
 * @property correct number of correct answers
 * @property wrong number of wrong answers
 * @property time timestamp when the game was completed
 */
data class SingleGameSummarizedData(val totalQuestions: Int, val correct: Int, val wrong: Int, val time: Long)
