package com.bignerdranch.android.geoquiz

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlin.math.roundToInt

private const val TAG = "MainActivity"
private const val KEY_INDEX = "index"
private const val KEY_REMAINING_CHEATS = "remaining_cheats"
private const val REQUEST_CODE_CHEAT = 0

class MainActivity : AppCompatActivity() {
    private lateinit var trueButton: Button
    private lateinit var falseButton: Button
    private lateinit var nextButton: ImageButton
    private lateinit var prevButton: ImageButton
    private lateinit var questionTextView: TextView
    private lateinit var cheatButton: Button

    private val quizViewModel: QuizViewModel by lazy {
        ViewModelProviders.of(this).get(QuizViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentIndex = savedInstanceState?.getInt(KEY_INDEX, 0) ?: 0
        quizViewModel.currentIndex = currentIndex

        val cheatsRemaining = savedInstanceState?.getInt(KEY_REMAINING_CHEATS, 3) ?: 3
        quizViewModel.cheatsRemaining = cheatsRemaining

        trueButton = findViewById(R.id.true_button)
        falseButton = findViewById(R.id.false_button)
        nextButton = findViewById(R.id.next_button)
        prevButton = findViewById(R.id.prev_button)
        questionTextView = findViewById(R.id.question_text_view)
        cheatButton = findViewById(R.id.cheat_button)

        trueButton.setOnClickListener { view: View ->
            checkAnswer(true)
            score()
            setButtons(false)
        }

        falseButton.setOnClickListener { view: View ->
            checkAnswer(false)
            score()
            setButtons(false)
        }

        nextButton.setOnClickListener { view: View ->
            quizViewModel.moveToNext()
            updateQuestion()
        }

        prevButton.setOnClickListener { view: View ->
            quizViewModel.moveToPrev()
            updateQuestion()
        }

        questionTextView.setOnClickListener { view: View ->
            quizViewModel.moveToNext()
            updateQuestion()
        }

        cheatButton.setOnClickListener { view: View ->
            val answerIsTrue = quizViewModel.currentQuestionAnswer
            val intent = CheatActivity.newIntent(this@MainActivity, answerIsTrue,
                quizViewModel.cheatsRemaining, quizViewModel.isCheater)
            startActivityForResult(intent, REQUEST_CODE_CHEAT)
        }

        updateQuestion()

    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putInt(KEY_INDEX, quizViewModel.currentIndex)
        savedInstanceState.putInt(KEY_REMAINING_CHEATS, quizViewModel.cheatsRemaining)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK)
            return
        if (requestCode == REQUEST_CODE_CHEAT) {
            val cheated =
                data?.getBooleanExtra(EXTRA_ANSWER_SHOWN, false) ?: false
            if(cheated && !quizViewModel.isCheater)
                quizViewModel.cheatsRemaining -= 1
            quizViewModel.isCheater = cheated
        }
    }

    private fun updateQuestion() {
        val questionTextResId = quizViewModel.currentQuestionText
        questionTextView.setText(questionTextResId)
        setButtons(true)
        if(quizViewModel.isCurrentQuestionAnswered)
            setButtons(false)
    }

    private fun checkAnswer(userAnswer:Boolean) {
        val correctAnswer = quizViewModel.currentQuestionAnswer
        val messageResId = when {
            quizViewModel.isCheater -> {quizViewModel.setCurrentUserAnswer(false); R.string.judgment_toast}
            userAnswer == correctAnswer -> {quizViewModel.setCurrentUserAnswer(true); R.string.correct_toast}
            else -> {quizViewModel.setCurrentUserAnswer(false); R.string.incorrect_toast}
        }

        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show()
    }

    private fun score() {
        if(quizViewModel.allAnswered)
            Toast.makeText(this, "Score: ${quizViewModel.score}%", Toast.LENGTH_SHORT).show()
    }

    private fun setButtons(isEnabled: Boolean) {
        trueButton.isEnabled = isEnabled
        falseButton.isEnabled = isEnabled
    }
}
