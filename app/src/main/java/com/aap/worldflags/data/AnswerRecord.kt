package com.aap.worldflags.data

/**
 * Encapsulates one answer which user has given.
 * Whether the answer was correct and what was the correct answer.
 * Used for showing the detailed score for a game after it's complete.
 */
data class AnswerRecord(val flagDrawable: Int, val correctAnswer: String, val yourAnswer: String, val yourAnswerDrawable: Int?) {
    val isCorrect = (correctAnswer == yourAnswer)
}
