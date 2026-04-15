package com.example.demo.dto

data class LoginResponse(
    val message: String,
    val userId: String,
    val name: String,
    val email: String
)
