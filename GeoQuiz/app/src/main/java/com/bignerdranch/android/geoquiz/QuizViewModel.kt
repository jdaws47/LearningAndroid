package com.bignerdranch.android.geoquiz

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlin.math.roundToInt

private const val TAG = "QuizViewModel"

class QuizViewModel : ViewModel () {
    private val questionBank = listOf(
        Question(R.string.question_australia, true),
        Question(R.string.question_oceans, true),
        Question(R.string.question_mideast, false),
        Question(R.string.question_africa, false),
        Question(R.string.question_americas, true),
        Question(R.string.question_asia, true))

    // -1 is unanswered, 0 was incorrect, 1 was correct
    var answers = IntArray(questionBank.size) {-1}

    var currentIndex = 0

    var cheatsRemaining = 3

    private var isCheaterArr = BooleanArray(questionBank.size) {false}

    val score : Int
        get() = (answers.sum().toFloat() / answers.size * 100).roundToInt()

    val allAnswered: Boolean
        get() = answers.indexOf(-1) == -1

    val isCurrentQuestionAnswered: Boolean
        get() = answers[currentIndex] != -1

    val currentQuestionAnswer: Boolean
        get() = questionBank[currentIndex].answer

    val currentQuestionText: Int
        get() = questionBank[currentIndex].textResId

    var isCheater: Boolean
        get() = isCheaterArr[currentIndex]
        set(cheated: Boolean) { isCheaterArr[currentIndex] = cheated }

    fun moveToNext() {
        currentIndex = (currentIndex + 1) % questionBank.size
    }

    fun moveToPrev() {
        currentIndex = (currentIndex - 1)
        if (currentIndex < 0)
            currentIndex = questionBank.size - 1
    }

    fun setCurrentUserAnswer(correct: Boolean) {
        answers[currentIndex] = if (correct) 1 else 0
    }

}