package com.aap.worldflags.data

import androidx.annotation.DrawableRes

data class Game(val questions: List<QuestionOptions>, val currentGameScore: CurrentGameScore, val currentQuestion: Int = 0)

data class FlagData(val countryCode: String,  val countryName: String, val flagDrawable: Int,)
data class QuestionOptions(@DrawableRes val flagImage: Int, val questionCountryCode: String, val answers: List<Answer>)
data class CurrentQuestionScore(val isCorrect: Boolean, val title: String, val ignore: Boolean)