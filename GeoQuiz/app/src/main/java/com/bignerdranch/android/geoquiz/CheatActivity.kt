package com.bignerdranch.android.geoquiz

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import org.w3c.dom.Text

const val EXTRA_ANSWER_SHOWN = "com.bignerdranch.android.geoquiz.answer_shown"
private const val EXTRA_ANSWER_IS_TRUE =
    "com.bignerdranch.android.geoquiz.answer_is_true"
private const val EXTRA_CHEATS_REMAINING =
    "com.bignerdranch.android.geoquiz.cheats_remaining"
private const val EXTRA_HAS_CHEATED_PREVIOUSLY =
    "com.bignerdranch.android.geoquiz.has_cheated_previously"
private const val KEY_INDEX = "index"
private const val KEY_REMAINING_CHEATS = "remaining_cheats"


class CheatActivity : AppCompatActivity() {

    private lateinit var answerTextView: TextView
    private lateinit var showAnswerButton: Button
    private lateinit var cheatsTextView: TextView

    private val cheatViewModel: CheatViewModel by lazy {
        ViewModelProviders.of(this).get(CheatViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cheat)

        val wasShownPreviously = savedInstanceState?.getBoolean(KEY_INDEX, false) ?: false
        cheatViewModel.isAnswerShown = wasShownPreviously

        val cheatsPreviouslyRemaining = savedInstanceState?.getInt(KEY_REMAINING_CHEATS, 3) ?: 3
        cheatViewModel.cheatsRemaining = cheatsPreviouslyRemaining

        cheatViewModel.answerIsTrue = intent.getBooleanExtra(EXTRA_ANSWER_IS_TRUE, false)
        cheatViewModel.cheatsRemaining= intent.getIntExtra(EXTRA_CHEATS_REMAINING, 3)
        val hasCheatedPreviously = intent.getBooleanExtra(EXTRA_HAS_CHEATED_PREVIOUSLY, false)
        if(hasCheatedPreviously)
            cheatViewModel.isAnswerShown = true

        answerTextView = findViewById(R.id.answer_text_view)
        cheatsTextView = findViewById(R.id.cheat_text_view)
        showAnswerButton = findViewById(R.id.show_answer_button)
        showAnswerButton.setOnClickListener {
            setAnswerText()
            cheatViewModel.isAnswerShown = true
            cheatViewModel.cheatsRemaining -= 1
            cheatsTextView.setText(cheatViewModel.cheatText)
            setAnswerShownResult()
            showAnswerButton.isEnabled = false
        }

        if(wasShownPreviously || hasCheatedPreviously) {
            setAnswerText()
            setAnswerShownResult()
            showAnswerButton.isEnabled = false
        }

        cheatsTextView.setText(cheatViewModel.cheatText)

        if(cheatViewModel.cheatsRemaining <= 0)
            showAnswerButton.isEnabled = false
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putBoolean(KEY_INDEX, cheatViewModel.isAnswerShown)
        savedInstanceState.putInt(KEY_REMAINING_CHEATS, cheatViewModel.cheatsRemaining)
    }

    private fun setAnswerShownResult() {
        val data = Intent().apply {
            putExtra(EXTRA_ANSWER_SHOWN, cheatViewModel.isAnswerShown)
        }
        setResult(Activity.RESULT_OK, data)
    }

    private fun setAnswerText() {
        /*val answerText = when {
                cheatViewModel.answerIsTrue -> R.string.true_button
                else -> R.string.false_button
            }*/
        val answerText = "No cheating, loser"
        answerTextView.setText(answerText)
    }

    companion object {
        fun newIntent(packageContext: Context, answerIsTrue: Boolean, cheatsRemaining: Int, hasCheatedPreviously: Boolean): Intent {
            return Intent(packageContext, CheatActivity::class.java).apply {
                putExtra(EXTRA_ANSWER_IS_TRUE, answerIsTrue)
                putExtra(EXTRA_CHEATS_REMAINING, cheatsRemaining)
                putExtra(EXTRA_HAS_CHEATED_PREVIOUSLY, hasCheatedPreviously)
            }
        }
    }

}
