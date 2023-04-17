package com.example.myapplication

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.myapplication.model.Question

class QuestionActivity : AppCompatActivity() {

    // Views
    private lateinit var scoreTextView: TextView
    private lateinit var questionCountTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var questionTextView: TextView
    private lateinit var answerButton1: Button
    private lateinit var answerButton2: Button
    private lateinit var answerButton3: Button
    private lateinit var answerButton4: Button
    private lateinit var answerButtons: List<Button>
    private lateinit var nextQuestionButton: Button

    // Data
    private var currentQuestionIndex = 0
    private lateinit var questions: List<Question>
    private var score = 0
    private var scoreText = "Score:"
    private var nextButtonText = "Next"
    private var gamePlus = false;

    // API controller
    private lateinit var apiController: ApiController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question)

        // Initialize ApiController
        apiController = ApiController(this)

        // Initialize views
        scoreTextView = findViewById(R.id.score_text_view)
        questionCountTextView = findViewById(R.id.question_count_text)
        progressBar = findViewById(R.id.progress_bar)
        questionTextView = findViewById(R.id.question_text_view)
        answerButton1 = findViewById(R.id.answer_1_button)
        answerButton2 = findViewById(R.id.answer_2_button)
        answerButton3 = findViewById(R.id.answer_3_button)
        answerButton4 = findViewById(R.id.answer_4_button)
        answerButtons = listOf(answerButton1, answerButton2, answerButton3, answerButton4)
        nextQuestionButton = findViewById(R.id.next_question_button)

        // Initialize the questions variable to an empty list
        questions = emptyList()

        // Get questions from API
        getQuestions()

        // Set click listeners for answer buttons &
        // Force background color of all answer buttons to default color
        for (i in answerButtons.indices) {
            answerButtons[i].setOnClickListener {
                checkAnswer(answerButtons[i])
            }
            answerButtons[i].backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.my_primary))
        }

        // Set click listener for next button
        nextQuestionButton.setOnClickListener {
            nextQuestion()
        }
    }

    /**
     * Fetches questions from the API based on the selected category ID and difficulty level from the intent extras.
     */
    private fun getQuestions() {
        // Get the selected category ID and difficulty level from the intent extras
        val selectedCategoryId = intent.getIntExtra("category_id", 9)
        val selectedDifficulty = intent.getStringExtra("difficulty")
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

                    Log.e("Question", "selectedCategoryId: $selectedCategoryId")
                    Log.e("Question", "selectedDifficulty: $selectedDifficulty")
                    Log.e("Question", "Questions: $fetchedQuestions")
                },
                onError = { error ->
                    Log.e(TAG, "An error occurred: ${error.message}")
                }
            )
        }
    }

    /**
     * Displays the question and answer options for the specified index.
     * @param index the index of the question to be displayed
     */
    private fun displayQuestion(index: Int) {
        // Hide the next button
        nextQuestionButton.visibility = View.GONE

        val questionCountText = "Question: ${if (gamePlus) currentQuestionIndex + 11 else currentQuestionIndex + 1}/${if (gamePlus) (questions.size + 10) else questions.size}"
        questionCountTextView.text = questionCountText
        progressBar.progress = (currentQuestionIndex + 1).let { if (gamePlus) it + 10 else it }
        progressBar.max = questions.size.let { if (gamePlus) it + 10 else it }

        // Get current question
        val currentQuestion = questions[index]
        questionTextView.text = currentQuestion.question
        // Adds the correct question and incorrect ones into a list and shuffle's it
        val answers = mutableListOf<String>().apply {
            add(currentQuestion.correctAnswer)
            addAll(currentQuestion.incorrectAnswers)
            shuffle()
        }
        // Assign each question to a button after being shuffled
        for (i in answerButtons.indices) {
            answerButtons[i].text = answers[i]
        }
    }

    /**
     * Displays the next question in the quiz, or shows a completion dialog if there are no more questions.
     */
    private fun nextQuestion() {
        isEnabledAnswerButtons()
        // Reset background color of all answer buttons to default color
        for (i in answerButtons.indices) {
            answerButtons[i].backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.my_primary))
        }
        currentQuestionIndex++
        Log.d("NextQuestion", "currentQuestionIndex: $currentQuestionIndex")
        Log.d("NextQuestion", "questions.size: ${questions.size}")
        if (currentQuestionIndex < questions.size) {
            displayQuestion(currentQuestionIndex)
        } else {
            showQuizCompletedDialog()
        }

        nextButtonText = if (currentQuestionIndex == 9) "Finish" else "Next"
        nextQuestionButton.text = nextButtonText
    }

    /**
     * Checks the user's answer, updates the score, and highlights the correct answer if the user is wrong.
     * @param answerButton the button representing the user's selected answer
     */
    private fun checkAnswer(answerButton: Button) {
        // Disable all answer buttons
        isEnabledAnswerButtons()

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
            for (i in answerButtons.indices) {
                if(answerButtons[i].text.equals(currentQuestion.correctAnswer)) {
                    answerButtons[i].backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green))
                }
            }
        }

        // Show the next button
        nextQuestionButton.visibility = View.VISIBLE

        // Update score text
        scoreText = "Score: $score/${if (gamePlus) (questions.size + 10) else questions.size}"
        scoreTextView.text = scoreText
    }

    /**
     * Enable or disable the buttons, this is so prevent user input after selected answer
     */
    private fun isEnabledAnswerButtons() {
        for (i in answerButtons.indices) {
            answerButtons[i].isEnabled = !answerButtons[i].isEnabled
        }
    }

    /**
     * Displays a dialog box showing the quiz score and options to either exit the quiz
     * or get 10 more questions (if available).
     */
    private fun showQuizCompletedDialog() {
        nextQuestionButton.visibility = View.GONE // Hide the button
        val builder = AlertDialog.Builder(this)
            .setTitle("Quiz Completed")
            .setMessage("Your score is $score out of ${if (gamePlus) (questions.size + 10) else questions.size}")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        // if user already received the 10 more questions, then don't show this part
        if(!gamePlus) {
            builder.setNegativeButton("10 More!") { dialog, _ ->
                gamePlus = true
                dialog.dismiss()
                // Get 10 new questions
                getQuestions()
                // Update score text
                scoreText = "Score: $score/${questions.size + 10}"
                scoreTextView.text = scoreText
                currentQuestionIndex = -1
                nextQuestion()
                isEnabledAnswerButtons()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }
}
