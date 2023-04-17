package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.api.ApiClient
import com.example.myapplication.model.Category
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    // Views
    private lateinit var progressBar: ProgressBar
    private lateinit var categorySpinner: Spinner
    private lateinit var difficultyRadioGroup: RadioGroup
    private lateinit var startQuizButton: Button

    // Data
    private lateinit var spinnerAdapter: ArrayAdapter<String>
    private var selectedCategoryId by Delegates.notNull<Int>()
    private var categoryIds: MutableList<Category> = mutableListOf()
    private var selectedDifficulty = ""


    // API controller
    private lateinit var apiController: ApiController
    private lateinit var apiClient: ApiClient // Didn't get this idea to work

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize ApiController
        apiController = ApiController(this)
        //apiClient = ApiClient()

        // Initialize views
        progressBar = findViewById(R.id.progress_bar)
        categorySpinner = findViewById(R.id.category_spinner)
        difficultyRadioGroup = findViewById<RadioGroup>(R.id.difficulty_radio_group)
        startQuizButton = findViewById(R.id.start_quiz_button)

        // Initialize the spinner and its adapter
        spinnerAdapter = ArrayAdapter(this, R.layout.spinner_item)
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        // Set the adapter for the spinner
        categorySpinner.adapter = spinnerAdapter

        // Hide start button
        startQuizButton.visibility = View.GONE

        // Couldn't get this way of using retrofit to get the API data
        /*apiClient.getCategories(
            onSuccess = { categories ->
                for (category in categories) {
                    spinnerAdapter.add(category.name)
                    categoryIds.add(category)
                }
                toggleViews(false) // hide progress bar and show UI elements
                Log.d("MainActivity", "Categories: $categories")
            },
            onError = { error ->
                Log.e("MainActivity", "Error: $error")
                toggleViews(false) // hide progress bar and show UI elements
            }
        )*/

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = parent?.getItemAtPosition(position)?.toString() ?: ""
                val categoryName = selectedCategory.split(" - ")[0]
                selectedCategoryId = categoryIds.find { it.name == categoryName }?.id ?: -1
                startQuizButton.visibility = View.VISIBLE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }


        findViewById<RadioButton>(R.id.easy_rb).setOnClickListener {
            setDifficultyLevel()
        }
        findViewById<RadioButton>(R.id.medium_rb).setOnClickListener {
            setDifficultyLevel()
        }
        findViewById<RadioButton>(R.id.hard_rb).setOnClickListener {
            setDifficultyLevel()
        }


        // Set click listener for startQuiz button
        startQuizButton.setOnClickListener {
            startQuiz()
        }
    }

    /**
     * Clears and updates the spinner adapter, sets selectedDifficulty based on checked radio button and then calls getCategories() to retrieve categories
     */
    private fun setDifficultyLevel() {
        // Clear the adapter of the spinner
        spinnerAdapter.clear()
        // Notify the adapter that the data has changed
        spinnerAdapter.notifyDataSetChanged()

        selectedDifficulty = when (difficultyRadioGroup.checkedRadioButtonId) {
            R.id.easy_rb -> "easy"
            R.id.medium_rb -> "medium"
            R.id.hard_rb -> "hard"
            else -> "easy"
        }
        getCategories()
    }

    /**
     * Retrieves a list of categories and adds to spinner if questionCount is 20 or above questions
     */
    private fun getCategories() {
        categoryIds.clear()
        toggleViews(true)
        // Make the API call to retrieve the categories
        apiController.getCategories(
            onSuccess = { categories ->
                val categoriesSorted = categories.sortedBy { it.name }
                for (category in categoriesSorted) {
                    apiController.getCategoryCount(
                        category = category.id,
                        onSuccess = { categoryCount ->
                            val totalQuestionCount = when (selectedDifficulty) {
                                "easy" -> categoryCount.totalEasyQuestionCount
                                "medium" -> categoryCount.totalMediumQuestionCount
                                "hard" -> categoryCount.totalHardQuestionCount
                                else -> categoryCount.totalQuestionCount
                            }
                            if(totalQuestionCount >= 20) {
                                spinnerAdapter.add("${category.name} - $totalQuestionCount Questions")
                            }

                        },
                        onError = { error ->
                            Log.e("MainActivity", "An error occurred: ${error.message}")
                        }
                    )
                    categoryIds.add(category)
                }
                toggleViews(false) // hide progress bar and show UI elements
            },
            onError = { error ->
                error.printStackTrace()
                toggleViews(false) // hide progress bar and show UI elements
            }
        )
    }

    /**
     * Starts the quiz by creating an intent to the QuestionActivity and passing the selected category ID and difficulty.
     */
    private fun startQuiz() {
        // Create an Intent to start the QuestionActivity
        val intent = Intent(this, QuestionActivity::class.java)

        // Add the selected category ID and difficulty level as extras to the intent
        intent.putExtra("category_id", selectedCategoryId)
        intent.putExtra("difficulty", selectedDifficulty)

        // Start the QuestionActivity
        startActivity(intent)
    }

    /**
     * Toggles the visibility of views for showing/hiding UI elements
     * @param isLoading True if loading, false if not.
     */
    private fun toggleViews(isLoading: Boolean) {
        val loading: TextView = findViewById(R.id.loading)
        val selectQuizCategory: TextView = findViewById(R.id.select_Quiz_Category)
        val selectQuizDifficulty: TextView = findViewById(R.id.select_Quiz_Difficulty)

        if (isLoading) {
            loading.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
            categorySpinner.visibility = View.GONE
            selectQuizCategory.visibility = View.GONE
            selectQuizDifficulty.visibility = View.GONE
            startQuizButton.visibility = View.GONE
            difficultyRadioGroup.visibility = View.GONE
        } else {
            loading.visibility = View.GONE
            progressBar.visibility = View.GONE
            categorySpinner.visibility = View.VISIBLE
            selectQuizCategory.visibility = View.VISIBLE
            selectQuizDifficulty.visibility = View.VISIBLE
            startQuizButton.visibility = View.VISIBLE
            difficultyRadioGroup.visibility = View.VISIBLE
        }
    }
}


