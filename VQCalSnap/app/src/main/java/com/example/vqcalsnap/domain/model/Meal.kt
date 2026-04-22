package com.example.vqcalsnap.domain.model

data class Meal(
    val id: Int = 0,
    val name: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val portionSize: Float,
    val portionUnit: String = "g",
    val timestamp: Long = System.currentTimeMillis()
)