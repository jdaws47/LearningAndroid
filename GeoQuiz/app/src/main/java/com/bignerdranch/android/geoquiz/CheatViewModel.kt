package com.bignerdranch.android.geoquiz

import androidx.lifecycle.ViewModel

private const val TAG = "CheatViewModel"

class CheatViewModel : ViewModel()  {
    var answerIsTrue = false
    var isAnswerShown = false

    var cheatsRemaining = 3
    val cheatText: String
        get() = "Cheats Remaining: ${cheatsRemaining}"
}