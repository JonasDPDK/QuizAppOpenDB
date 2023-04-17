package com.example.myapplication.model

data class CategoryCount(
    val categoryId: Int,
    val totalQuestionCount: Int,
    val totalEasyQuestionCount: Int,
    val totalMediumQuestionCount: Int,
    val totalHardQuestionCount: Int
    )
