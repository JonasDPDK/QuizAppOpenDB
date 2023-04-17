package com.example.myapplication

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.myapplication.model.Question

class QuestionActivity : AppCompatActivity() {

    // Views
    private lateinit var nextQuestionButton: Button
    private lateinit var questionTextView: TextView
    private lateinit var answerButton1: Button
    private lateinit var answerButton2: Button
    private lateinit var answerButton3: Button
    private lateinit var answerButton4: Button
    private lateinit var scoreTextView: TextView
    private lateinit var answerButtons: List<Button>

    // Data
    private var currentQuestionIndex = 0
    private lateinit var questions: List<Question>
    private var score = 0

    // API controller
    private lateinit var apiController: ApiController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question)
        apiController = ApiController(this)

        // Initialize views
        questionTextView = findViewById(R.id.question_text_view)
        answerButton1 = findViewById(R.id.answer_1_button)
        answerButton2 = findViewById(R.id.answer_2_button)
        answerButton3 = findViewById(R.id.answer_3_button)
        answerButton4 = findViewById(R.id.answer_4_button)
        scoreTextView = findViewById(R.id.score_text_view)

        // Get the selected category ID and difficulty level from the intent extras
        val selectedCategoryId = intent.getIntExtra("category_id", 9)
        val selectedDifficulty = intent.getStringExtra("difficulty")

        // Initialize the questions variable to an empty list
        questions = emptyList()

        // Get questions from API
        if (selectedDifficulty != null) {
            apiController.fetchQuestions(
                category = selectedCategoryId,
                difficulty = selectedDifficulty,
                onSuccess = { fetchedQuestions ->
                    // Assign the fetched questions to the questions variable
                    questions = fetchedQuestions

                    // Display first question if questions list is not empty
                    if (questions.isNotEmpty()) {
                        displayQuestion(currentQuestionIndex)
                    }
                },
                onError = { error ->
                    // Handle the error
                }
            )
        }

        // Set click listeners for answer buttons
        answerButton1.setOnClickListener {
            checkAnswer(answerButton1)
        }
        answerButton2.setOnClickListener {
            checkAnswer(answerButton2)
        }
        answerButton3.setOnClickListener {
            checkAnswer(answerButton3)
        }
        answerButton4.setOnClickListener {
            checkAnswer(answerButton4)
        }

        // Force background color of all answer buttons to default color
        answerButton1.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.my_primary))
        answerButton2.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.my_primary))
        answerButton3.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.my_primary))
        answerButton4.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.my_primary))

        // Set click listener for next button
        nextQuestionButton = findViewById(R.id.next_question_button)
        nextQuestionButton.setOnClickListener {
            enableAnswerButtons()
            // Reset background color of all answer buttons to default color
            answerButton1.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.my_primary))
            answerButton2.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.my_primary))
            answerButton3.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.my_primary))
            answerButton4.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.my_primary))
            currentQuestionIndex++

            if (currentQuestionIndex < questions.size) {
                displayQuestion(currentQuestionIndex)
            } else {
                showQuizCompletedDialog()
            }
        }
    }

    private fun displayQuestion(index: Int) {
        // Get current question
        val currentQuestion = questions[index]
        questionTextView.text = currentQuestion.question
        answerButtons = listOf(answerButton1, answerButton2, answerButton3, answerButton4)
        val answers = mutableListOf<String>().apply {
            add(currentQuestion.correctAnswer)
            addAll(currentQuestion.incorrectAnswers)
            shuffle()
        }
        for (i in answerButtons.indices) {
            answerButtons[i].text = answers[i]
        }
    }

    private fun checkAnswer(answerButton: Button) {
        // Get current question
        val currentQuestion = questions[currentQuestionIndex]

        // Check if answer is correct
        val isCorrect = answerButton.text == currentQuestion.correctAnswer

        // Set button colors based on whether answer is correct or incorrect & Increment score if answer is correct
        if (isCorrect) {
            score++
            answerButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green))
        } else {
            answerButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red))

            // Find the correct answer button and change its color to green
            when (currentQuestion.correctAnswer) {
                answerButton1.text -> answerButton1.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green))
                answerButton2.text -> answerButton2.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green))
                answerButton3.text -> answerButton3.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green))
                answerButton4.text -> answerButton4.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green))
            }
        }

        // Disable all answer buttons
        disableAnswerButtons()

        // Show the next button
        nextQuestionButton.visibility = View.VISIBLE

        // Update score text
        val scoreText = "Score: $score/${questions.size}"
        findViewById<TextView>(R.id.score_text_view).text = scoreText
    }

    private fun enableAnswerButtons() {
        answerButton1.isEnabled = true
        answerButton2.isEnabled = true
        answerButton3.isEnabled = true
        answerButton4.isEnabled = true
    }

    private fun disableAnswerButtons() {
        answerButton1.isEnabled = false
        answerButton2.isEnabled = false
        answerButton3.isEnabled = false
        answerButton4.isEnabled = false
    }
    private fun showQuizCompletedDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Quiz Completed")
        builder.setMessage("Your score is $score out of ${questions.size}")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        val dialog = builder.create()
        dialog.show()
    }
}
