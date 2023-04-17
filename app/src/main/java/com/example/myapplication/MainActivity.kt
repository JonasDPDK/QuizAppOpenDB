package com.example.myapplication

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.model.Category
import com.example.myapplication.model.Question
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var spinnerAdapter: ArrayAdapter<String>
    private lateinit var apiController: ApiController
    private var selectedCategoryId by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.progress_bar)
        apiController = ApiController(this)

        // Initialize the spinner and its adapter
        val categorySpinner: Spinner = findViewById(R.id.category_spinner)
        spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Set the adapter for the spinner
        categorySpinner.adapter = spinnerAdapter

        val categoryIds = mutableListOf<Category>()

        toggleViews(true) // show progress bar and hide UI elements
        // Make the API call to retrieve the categories
        apiController.getCategories(
            onSuccess = { categories ->
                for (category in categories) {
                    spinnerAdapter.add(category.name)
                    categoryIds.add(category)
                }
                toggleViews(false) // hide progress bar and show UI elements
            },
            onError = { error ->
                error.printStackTrace()
                toggleViews(false) // hide progress bar and show UI elements
            }
        )

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = parent?.getItemAtPosition(position)?.toString() ?: ""
                selectedCategoryId = categoryIds.find { it.name == selectedCategory }?.id ?: -1
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        val difficultyRadioGroup = findViewById<RadioGroup>(R.id.difficulty_radio_group)
        val selectedDifficulty = when (difficultyRadioGroup.checkedRadioButtonId) {
            R.id.easy_radio_button -> "easy"
            R.id.medium_radio_button -> "medium"
            R.id.hard_radio_button -> "hard"
            else -> "easy" // fallback to default value
        }

        val startQuizButton: Button = findViewById(R.id.start_quiz_button)
        startQuizButton.setOnClickListener {
            // Create an Intent to start the QuestionActivity
            val intent = Intent(this, QuestionActivity::class.java)

            // Add the selected category ID and difficulty level as extras to the intent
            intent.putExtra("category_id", selectedCategoryId)
            intent.putExtra("difficulty", selectedDifficulty)

            // Start the QuestionActivity
            startActivity(intent)
        }

    }

    private fun toggleViews(isLoading: Boolean) {
        val loading: TextView = findViewById(R.id.loading)
        val selectQuizCategory: TextView = findViewById(R.id.select_Quiz_Category)
        val selectQuizDifficulty: TextView = findViewById(R.id.select_Quiz_Difficulty)
        val startQuizButton: Button = findViewById(R.id.start_quiz_button)
        val difficultyRadioGroup: RadioGroup = findViewById(R.id.difficulty_radio_group)
        val categorySpinner: Spinner = findViewById(R.id.category_spinner)

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


