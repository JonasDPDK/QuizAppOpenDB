package com.example.myapplication

import android.content.ContentValues.TAG
import android.content.Context
import android.provider.Settings.Global.getString
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.myapplication.model.Category
import com.example.myapplication.model.Question
import org.json.JSONException

class ApiController(private val context: Context) {

    private val requestQueue = Volley.newRequestQueue(context)

    fun getCategories(onSuccess: (List<Category>) -> Unit, onError: (VolleyError) -> Unit) {
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

    fun fetchQuestions(category: Int, difficulty: String, onSuccess: (List<Question>) -> Unit, onError: (VolleyError) -> Unit) {
        val url = "https://opentdb.com/api.php?amount=10&category=$category&difficulty=$difficulty&type=multiple"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val results = response.getJSONArray("results")
                val questions = mutableListOf<Question>()
                for (i in 0 until results.length()) {
                    val result = results.getJSONObject(i)
                    var question = result.getString("question")
                    question = question.replace("&quot;", "\"")
                    question = question.replace("&#039;", "'")
                    question = question.replace("&amp;", "&")
                    var correctAnswer = result.getString("correct_answer")
                    correctAnswer = correctAnswer.replace("&amp;", "&")
                    correctAnswer = correctAnswer.replace("&rsquo;", "'")
                    correctAnswer = correctAnswer.replace("&#039;", "'")
                    val incorrectAnswers = mutableListOf<String>()
                    val incorrectAnswersArray = result.getJSONArray("incorrect_answers")
                    for (j in 0 until incorrectAnswersArray.length()) {
                        var incorrectAnswer = incorrectAnswersArray.getString(j)
                        incorrectAnswer = incorrectAnswer.replace("&amp;", "&")
                        incorrectAnswer = incorrectAnswer.replace("&rsquo;", "'")
                        incorrectAnswer = incorrectAnswer.replace("&#039;", "'")
                        incorrectAnswers.add(incorrectAnswer)
                    }
                    questions.add(Question(question, correctAnswer, incorrectAnswers))
                }
                onSuccess(questions)
            },
            { error ->
                Log.e("QuestionActivity", "Error fetching questions: ${error.message}")
            })

        requestQueue.add(request)
    }

}
