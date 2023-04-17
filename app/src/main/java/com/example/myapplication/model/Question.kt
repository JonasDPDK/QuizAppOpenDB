package com.example.myapplication.model

data class Question(
    val question: String,
    val correctAnswer: String,
    val incorrectAnswers: List<String>
)
