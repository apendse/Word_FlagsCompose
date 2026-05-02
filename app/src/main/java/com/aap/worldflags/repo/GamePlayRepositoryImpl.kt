package com.aap.worldflags.repo

import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import com.aap.worldflags.data.Answer
import com.aap.worldflags.data.AnswerRecord
import com.aap.worldflags.data.CountryFlagDataWithWeight
import com.aap.worldflags.data.CountryType
import com.aap.worldflags.data.CurrentGameScore
import com.aap.worldflags.data.FlagData
import com.aap.worldflags.data.Game
import com.aap.worldflags.data.QuestionOptions
import com.aap.worldflags.data.SingleGameSummarizedData
import com.aap.worldflags.room.GameDatabase
import com.aap.worldflags.room.table.QuestionRow
import com.aap.worldflags.room.table.ScoreTableRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random

private const val WRONG_ANSWER_COUNT = 3
private const val GAME_COUNTER = "GameCounter"
private const val QUESTION_REPEAT_GAP = 4
private const val MAX_OLD_QUESTIONS = 40

interface QuestionPicker {
    fun pick(): FlagData
}

private const val WINNING_THRESHOLD = 0.75f
class GamePlayRepositoryImpl @Inject constructor(
    private val gameDatabase: GameDatabase,
    private val gameCreationRepository: GameDataCreationRepository,
    private val dataStore: DataStore<Preferences>
) : GamePlayRepository {
    /**
     * The [List] of [AnswerRecord] that represents the answers in the current game.
     * To be used to display the concise and detailed score.
     */
    private val answerRecords = mutableListOf<AnswerRecord>()
    val gameCounter = intPreferencesKey(GAME_COUNTER)
    override fun getDetailedScore() = answerRecords
    private val countryFlagDataWithWeight: MutableMap<String, CountryFlagDataWithWeight> = emptyMap<String, CountryFlagDataWithWeight>().toMutableMap()
    private val countriesWithDuplicateFlag: Map<String, Set<String>>

    init {
        // read the country data
        countryFlagDataWithWeight.putAll(
            gameCreationRepository.getFlags(CountryType.COUNTRY_FAMOUS).map {
            it.countryCode to CountryFlagDataWithWeight(it, true, INSTRINSIC_WEIGHT_FOR_FAMOUS_COUNTRIES)
            }
        )

        countryFlagDataWithWeight.putAll(
            gameCreationRepository.getFlags(CountryType.COUNTRY_NOT_FAMOUS).map {
                it.countryCode to CountryFlagDataWithWeight(it, false, INSTRINSIC_WEIGHT_FOR_NON_FAMOUS_COUNTRIES)
        })

        val set1 = setOf("TD", "RO")
        countriesWithDuplicateFlag = mapOf("TD" to set1, "RO" to set1)
    }


    override fun getScoreSummary(): SingleGameSummarizedData {
        val correct = answerRecords.filter { it.isCorrect }.size
        val wrong = answerRecords.size - correct
        return SingleGameSummarizedData(
            totalQuestions = answerRecords.size,
            correct = correct,
            wrong = wrong,
            winner = isWinner(correct, wrong),
            time = System.currentTimeMillis(), )
    }

    override fun getPastScores(): Flow<List<SingleGameSummarizedData>> {
        return gameDatabase.pastScoresDao().getScoreList().map {
            it.map { row ->
                with(row) {
                    SingleGameSummarizedData(
                        totalQuestions = total,
                        correct = correct,
                        wrong = wrong,
                        winner = isWinner(correct, wrong),
                        time = playTime
                    )
                }
            }
        }
    }


    private suspend fun getCurrentGameCounter(): Int {
        val v = dataStore.data.lastOrNull()
        return (v?.get(gameCounter) ?: 0) + 1
    }

    override suspend fun markGameComplete() {
        insertGameInPastGamesDbTable()
        recordLastNQuestionWithAnswersInDb()
    }

    override suspend fun getPastQuestions(): List<QuestionRow> {
        return gameDatabase.pastQuestionsDao().getQuestionRows()
    }

    override fun isWinner(correct: Int, wrong: Int): Boolean {
        return correct.toFloat() / (correct + wrong) > WINNING_THRESHOLD
    }

    // Remember which questions were asked. Then clean up so that only the last recent 10 questions are kept
    // Record the ones which the user got wrong. No need to add correct ones.
    private suspend fun recordLastNQuestionWithAnswersInDb() {
        val timeStamp = System.currentTimeMillis()
        val pastQuestionsDao = gameDatabase.pastQuestionsDao()
        answerRecords.forEach { answerRecord ->
            pastQuestionsDao.insertOrUpdateQuestionRow(
                QuestionRow(
                    0,
                    "",
                    answerRecord.correctAnswer,
                    timeStamp.toInt(),
                    answerRecord.isCorrect)
            )
        }
        deleteOldQuestions()
    }


    private suspend fun deleteOldQuestions() {
        val pastQuestionsDao = gameDatabase.pastQuestionsDao()
        while(pastQuestionsDao.getQuestionCount() > MAX_OLD_QUESTIONS) {
            pastQuestionsDao.deleteOldestQuestion()
        }
    }

    private suspend fun insertGameInPastGamesDbTable() {
        val summary = getScoreSummary()
        with(summary) {
            val scoreTableRow =
                ScoreTableRow(
                    0,
                    totalQuestions,
                    correct,
                    wrong,
                    isWinner(correct, wrong).toInt(),
                    System . currentTimeMillis ()
                )
            gameDatabase.pastScoresDao().insertGameScore(scoreTableRow)
        }

    }

    override fun isAnswerCorrect(game: Game, questionIndex: Int, answerIndex: Int) = game.questions[questionIndex].answers[answerIndex].isCorrect
    override fun processAnswer(game: Game, questionIndex: Int, answerIndex: Int): Game {
        val isCorrect = isAnswerCorrect(game, questionIndex, answerIndex)
        val score = game.currentGameScore.copy(
            answered = game.currentGameScore.answered + 1,
            correct = game.currentGameScore.correct + isCorrect.toInt(),
            wrong = game.currentGameScore.wrong + (!isCorrect).toInt(),
        )
        recordAnswer(game.questions[questionIndex], answerIndex)
        return game.copy(currentGameScore = score)
    }

    private fun recordAnswer(questionOptions: QuestionOptions, answerIndex: Int) {
        val isCorrect = questionOptions.answers[answerIndex].isCorrect
        val answerRecord = AnswerRecord(
            questionOptions.flagImage,
            questionOptions.answers.first { it.isCorrect }.countryName,
            questionOptions.answers[answerIndex].countryName,
            if (isCorrect) { null } else { questionOptions.answers[answerIndex].answerDrawable },

        )
        answerRecords.add(answerRecord)

    }

    suspend fun chooseAQuestionCountryFilterOutRepeat(questionPicker: QuestionPicker): FlagData {
        var flagData = questionPicker.pick()
        withContext(Dispatchers.IO) {
            repeat(10) {
                val row = gameDatabase.pastQuestionsDao().getQuestionRow(flagData.countryName) ?: return@withContext flagData
                if (-1 + QUESTION_REPEAT_GAP < getCurrentGameCounter()) {
                    flagData = questionPicker.pick()
                }
            }
        }
        return flagData
    }



    private fun checkIfCountryIsDuplicate(questionCountryCodeSet: Set<String>, questionFlagData: FlagData): Boolean {
        return questionCountryCodeSet.contains(questionFlagData.countryCode)

    }




    override suspend fun createANewGame(numQuestionsInAGame: Int): Game {
        // read all the questions with their intrinsic weights
        val questions = mutableListOf<QuestionOptions>()
        val currentCountryFlagData = this.countryFlagDataWithWeight.toMutableMap()
        answerRecords.clear()
        // read the last answer records. increase the weight for wrong answers. Decrease for correct answers
        //
        getPastQuestions().forEach { question ->
            val v = currentCountryFlagData[question.countryCode] ?: throw RuntimeException("invalid country code ${question.countryCode}")
            currentCountryFlagData[question.countryCode] = v.copy(weight = v.weight.getNewWeightFor( if (question.wasCorrectlyAnswered ) AnswerQualifier.CORRECT else AnswerQualifier.WRONG))
        }

        val listOfQuestionData = pickWithWeight(numQuestionsInAGame, currentCountryFlagData).map {
            it.flagData
        }
        listOfQuestionData.forEach { questionFlagData ->
            val answers = mutableListOf<Answer>()
            // add the correct answer
            answers.add(Answer(questionFlagData.countryName, questionFlagData.flagDrawable, true))
            // get wrong options
            val wrongAnswers = getFillerAnswers(questionFlagData).map {
                Answer(
                    it.countryName,
                    it.flagDrawable,
                    false
                )
            }
            answers.addAll(wrongAnswers)
            answers.shuffle()
            questions.add(QuestionOptions(questionFlagData.flagDrawable, answers))
        }
        val str = listOfQuestionData.map { it.countryName }.joinToString(",")
        Log.d("YYYY", "game created $str");
        return Game(questions, CurrentGameScore(0, questions.size, 0, 0), currentQuestion = 0)
        // Use the random number generator to create a new game


    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun pickWithWeight(numQuestionsInAGame: Int, currentCountryFlagDataMap: Map<String, CountryFlagDataWithWeight>): List<CountryFlagDataWithWeight> {
        var currentWeight = 0
        val weightList = listOf<Int>().toMutableList()
        val countryList = listOf<CountryFlagDataWithWeight>().toMutableList()
        currentCountryFlagDataMap.forEach { code, countryFlagDataWithWeight ->
            weightList.add(currentWeight + countryFlagDataWithWeight.weight)
            countryList.add(countryFlagDataWithWeight)
            currentWeight += countryFlagDataWithWeight.weight
        }
        // find the matching number
        val listOfIndexes = mutableListOf<Int>()
        while (listOfIndexes.size < numQuestionsInAGame) {
            val rand = Random.nextInt(currentWeight+1)
            val index = weightList.findIndex(rand)
            if (!listOfIndexes.contains(index)) {
                listOfIndexes.add(index)
            }

        }

        val returnList = listOf<CountryFlagDataWithWeight>().toMutableList()
        listOfIndexes.forEach { index ->
            currentCountryFlagDataMap[countryList[index].flagData.countryCode]?.let {
                returnList.add(it)
            }
        }
        return returnList.toList()

    }

    fun List<Int>.findIndex(v: Int): Int {
        for (i in this.lastIndex downTo 0) {
            if (this[i] <= v) {
                return i
            }
        }
        return this.lastIndex
    }

    suspend fun createANewGameOld(numQuestionsInAGame: Int): Game {
        val questions = mutableListOf<QuestionOptions>()
        answerRecords.clear()
        // pick QUESTIONS_IN_A_GAME unique countries
        val questionCountryCodeSet = mutableSetOf<String>()
        val questionPicker: QuestionPicker = DefaultQuestionPicker(gameCreationRepository)
        while (questionCountryCodeSet.size < numQuestionsInAGame) {
            val questionFlagData = chooseAQuestionCountryFilterOutRepeat(questionPicker)
            if (!checkIfCountryIsDuplicate(questionCountryCodeSet, questionFlagData)) {
                //
                questionCountryCodeSet.add(questionFlagData.countryCode)
                // pick 3 wrong answers

                val answers = mutableListOf<Answer>()
                // add the correct answer
                answers.add(Answer(questionFlagData.countryName, questionFlagData.flagDrawable, true))
                // get wrong options
                val wrongAnswers = getFillerAnswers(questionFlagData).map { Answer(it.countryName, it.flagDrawable, false) }
                answers.addAll(wrongAnswers)
                answers.shuffle()
                questions.add(QuestionOptions(questionFlagData.flagDrawable, answers))
            }
        }
        return Game(questions, CurrentGameScore(0, questions.size, 0, 0), currentQuestion = 0)
    }

    private fun getFillerAnswers(countryData: FlagData): List<FlagData> {
        val set = mutableSetOf<FlagData>()

        set.add(countryData) // add the question so that it does not get picked again

        val retList = mutableListOf<FlagData>()
        while (set.size < WRONG_ANSWER_COUNT + 1) {

            val tempCountry = selectWrongAnswer(countryData)
            if (!set.contains(tempCountry)) {
                set.add(tempCountry)
                retList.add(tempCountry)
            }
        }
        return retList
    }

    private fun selectWrongAnswer(countryData: FlagData): FlagData {
        val confusingAnswers = gameCreationRepository.getConfusingAnswers(countryData.countryCode)
        if (confusingAnswers.isNotEmpty()) {
            if (Random.nextInt(0, 100) < 90) {
                return confusingAnswers.randomElement()
            }
        }
        val countryFlagListCombined =
            gameCreationRepository.getFlags(CountryType.COUNTRY_FULL_LIST)

        var randomlyPickedCountry = countryFlagListCombined.randomElement()
        var done = false
        while(!done) {
            if (areCountriesWithDuplicateFlags(randomlyPickedCountry.countryCode, countryData.countryCode)) {
                    randomlyPickedCountry = countryFlagListCombined.randomElement()
                } else {
                    done = true
                }
        }
        return randomlyPickedCountry
    }

    fun areCountriesWithDuplicateFlags(countryCode1: String, countryCode2: String): Boolean {
        val set = countriesWithDuplicateFlag[countryCode1] ?: return false
        return set.contains(countryCode2)
    }
}

fun <T> List<T>.randomElement(): T {
    return this[Random.nextInt(0, this.size)]
}


class DefaultQuestionPicker(gameCreationRepository: GameDataCreationRepository): QuestionPicker {

    private val countryFlagListImportant = gameCreationRepository.getFlags(CountryType.COUNTRY_FAMOUS)
    private val countryFlagListLessKnown =
        gameCreationRepository.getFlags(CountryType.COUNTRY_NOT_FAMOUS)

    override fun pick(): FlagData {
        return when (Random.nextInt(0, 10)) {
            in 0..8 -> {
                countryFlagListImportant.randomElement()
            }

            else -> {
                countryFlagListLessKnown.randomElement()
            }
        }
    }
}