package com.example.myapplication.api

import com.google.gson.annotations.SerializedName

data class QuestionResponse(
    val question: String,
    @SerializedName("correct_answer")
    val correctAnswer: String,
    @SerializedName("incorrect_answers")
    val incorrectAnswers: List<String>
)
