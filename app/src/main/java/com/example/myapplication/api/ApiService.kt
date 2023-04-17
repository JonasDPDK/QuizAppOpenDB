package com.example.myapplication.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api_category.php")
    fun getCategories(): Call<ApiResponse<CategoryResponse>>

    @GET("api.php?type=multiple")
    fun getQuestions(
        @Query("amount") amount: Int,
        @Query("category") category: Int,
        @Query("difficulty") difficulty: String
    ): Call<ApiResponse<QuestionResponse>>
}