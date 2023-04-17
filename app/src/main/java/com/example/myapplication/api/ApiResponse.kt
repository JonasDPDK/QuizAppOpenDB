package com.example.myapplication.api

data class ApiResponse<T>(
    val responseCode: Int,
    val results: List<T>
)
