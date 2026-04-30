package com.aap.worldflags.repo

const val INSTRINSIC_WEIGHT_FOR_FAMOUS_COUNTRIES = 12
const val INSTRINSIC_WEIGHT_FOR_NON_FAMOUS_COUNTRIES = 2
private const val INCREASED_WEIGHT_FOR_FAMOUS_COUNTRIES = INSTRINSIC_WEIGHT_FOR_FAMOUS_COUNTRIES + 5
private const val DECREASED_WEIGHT_FOR_FAMOUS_COUNTRIES = 4
private const val INCREASED_WEIGHT_FOR_NON_FAMOUS_COUNTRIES = 3
private const val DECREASED_WEIGHT_FOR_NON_FAMOUS_COUNTRIES = 1

enum class AnswerQualifier {
    CORRECT, WRONG,
}

// The weight adjustment done for correct and wrong questions.
// Questions answered correctly lose weight and are asked with less likelihood
// Questions answered incorrectly gain weight and are asked with more likelihood
// If the country is "famous" then the weight gain and loss is more.
// If the country is not famous then less gain and loss.
fun Int.getNewWeightFor(answerQualifier: AnswerQualifier): Int {
    if (this == INSTRINSIC_WEIGHT_FOR_FAMOUS_COUNTRIES) {
        return when(answerQualifier) {
            AnswerQualifier.CORRECT -> DECREASED_WEIGHT_FOR_FAMOUS_COUNTRIES
            AnswerQualifier.WRONG -> INCREASED_WEIGHT_FOR_FAMOUS_COUNTRIES
        }
    } else {
        return when(answerQualifier) {
            AnswerQualifier.CORRECT -> DECREASED_WEIGHT_FOR_NON_FAMOUS_COUNTRIES
            AnswerQualifier.WRONG -> INCREASED_WEIGHT_FOR_NON_FAMOUS_COUNTRIES
        }

    }
}