package com.example.vqcalsnap.domain.model

data class UserProfile(
    val name: String = "",
    val age: Int = 22,
    val gender: String = "",
    val weight: Float = 60f,
    val height: Float = 165f,
    val goal: String = "Duy trì cân nặng",
    val dailyCalorieTarget: Int = 2000
)