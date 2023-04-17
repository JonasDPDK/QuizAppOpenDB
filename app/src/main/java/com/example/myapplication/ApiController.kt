package com.example.myapplication

import android.content.Context
import androidx.core.text.HtmlCompat
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.myapplication.model.Category
import com.example.myapplication.model.CategoryCount
import com.example.myapplication.model.Question
import org.json.JSONException

class ApiController(context: Context) {

    private val requestQueue = Volley.newRequestQueue(context)

    fun getCategories(
        onSuccess: (List<Category>) -> Unit,
        onError: (VolleyError) -> Unit
    ) {
        val url = "https://opentdb.com/api_category.php"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val categories = response.getJSONArray("trivia_categories")
                    val categoryList = mutableListOf<Category>()
                    for (i in 0 until categories.length()) {
                        val categoryObject = categories.getJSONObject(i)
                        val categoryId = categoryObject.getInt("id")
                        val categoryName = categoryObject.getString("name")
                        val category = Category(categoryId, categoryName)
                        categoryList.add(category)
                    }
                    onSuccess(categoryList)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                onError(error)
            })

        requestQueue.add(request)
    }

    fun getCategoryCount(
        category: Int,
        onSuccess: (CategoryCount) -> Unit,
        onError: (VolleyError) -> Unit
    ) {
        val url = "https://opentdb.com/api_count.php?category=$category"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val categoryCount = response.getJSONObject("category_question_count")
                    val totalQuestionCount = categoryCount.getInt("total_question_count")
                    val totalEasyQuestionCount = categoryCount.getInt("total_easy_question_count")
                    val totalMediumQuestionCount = categoryCount.getInt("total_medium_question_count")
                    val totalHardQuestionCount = categoryCount.getInt("total_hard_question_count")
                    val categoryCountModel = CategoryCount(category, totalQuestionCount, totalEasyQuestionCount, totalMediumQuestionCount, totalHardQuestionCount)
                    onSuccess(categoryCountModel)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                onError(error)
            }
        )

        requestQueue.add(request)
    }

    fun fetchQuestions(
        category: Int,
        difficulty: String,
        onSuccess: (List<Question>) -> Unit,
        onError: (VolleyError) -> Unit
    ) {
        val url = "https://opentdb.com/api.php?amount=10&category=$category&difficulty=$difficulty&type=multiple"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val results = response.getJSONArray("results")
                val questions = mutableListOf<Question>()
                for (i in 0 until results.length()) {
                    val result = results.getJSONObject(i)
                    // Get question
                    val question = result.getString("question")
                    val questionDecoded = HtmlCompat.fromHtml(question, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                    // Get correctAnswer
                    val correctAnswer = result.getString("correct_answer")
                    val correctAnswerDecoded = HtmlCompat.fromHtml(correctAnswer, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                    // Get incorrectAnswers
                    val incorrectAnswersArray = result.getJSONArray("incorrect_answers")
                    val incorrectAnswersDecoded = mutableListOf<String>()
                    for (j in 0 until incorrectAnswersArray.length()) {
                        val incorrectAnswer = incorrectAnswersArray.getString(j)
                        val incorrectAnswerDecoded = HtmlCompat.fromHtml(incorrectAnswer, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                        incorrectAnswersDecoded.add(incorrectAnswerDecoded)
                    }
                    questions.add(Question(questionDecoded, correctAnswerDecoded, incorrectAnswersDecoded))
                }
                onSuccess(questions)
            },
            { error ->
                onError(error)
            })
        requestQueue.add(request)
    }
}
