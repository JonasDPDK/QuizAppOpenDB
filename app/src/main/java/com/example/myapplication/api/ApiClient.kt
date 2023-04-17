package com.example.myapplication.api

import android.util.Log
import androidx.core.text.HtmlCompat
import com.example.myapplication.model.Category
import com.example.myapplication.model.Question
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiClient {

    private val apiService: ApiService

    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://opentdb.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

    }

    fun getCategories(onSuccess: (List<Category>) -> Unit, onError: (Throwable) -> Unit) {
        apiService.getCategories().enqueue(object : Callback<ApiResponse<CategoryResponse>> {
            override fun onResponse(
                call: Call<ApiResponse<CategoryResponse>>,
                response: Response<ApiResponse<CategoryResponse>>
            ) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.responseCode == 0) {
                        val results = apiResponse.results
                        val categoryList = results?.map {
                            Category(it.id, it.name)
                        } ?: emptyList()
                        Log.d("ApiClient", "API response: ${response.body()}")

                        Log.d("ApiClient", "Categories: $categoryList")
                        onSuccess(categoryList)
                    } else {
                        onError(Exception("Failed to fetch categories"))
                    }
                } else {
                    onError(Exception("Failed to fetch categories"))
                }
            }

            override fun onFailure(call: Call<ApiResponse<CategoryResponse>>, t: Throwable) {
                onError(t)
            }
        })
    }

    fun fetchQuestions(
        category: Int,
        difficulty: String,
        onSuccess: (List<Question>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        apiService.getQuestions(10, category, difficulty)
            .enqueue(object : Callback<ApiResponse<QuestionResponse>> {
                override fun onResponse(
                    call: Call<ApiResponse<QuestionResponse>>,
                    response: Response<ApiResponse<QuestionResponse>>
                ) {
                    if (response.isSuccessful) {
                        val questions = response.body()?.results?.map {
                            val questionDecoded =
                                HtmlCompat.fromHtml(it.question, HtmlCompat.FROM_HTML_MODE_LEGACY)
                                    .toString()
                            val correctAnswerDecoded = HtmlCompat.fromHtml(
                                it.correctAnswer,
                                HtmlCompat.FROM_HTML_MODE_LEGACY
                            ).toString()
                            val incorrectAnswersDecoded = it.incorrectAnswers.map { answer ->
                                HtmlCompat.fromHtml(answer, HtmlCompat.FROM_HTML_MODE_LEGACY)
                                    .toString()
                            }
                            Question(questionDecoded, correctAnswerDecoded, incorrectAnswersDecoded)
                        } ?: emptyList()
                        onSuccess(questions)
                    } else {
                        onError(Exception("Failed to fetch questions"))
                    }
                }

                override fun onFailure(
                    call: Call<ApiResponse<QuestionResponse>>,
                    t: Throwable
                ) {
                    onError(t)
                }
            })
    }
}