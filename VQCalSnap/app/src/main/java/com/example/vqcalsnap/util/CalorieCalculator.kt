package com.example.vqcalsnap.util

import com.example.vqcalsnap.domain.model.UserProfile

object CalorieCalculator {

    fun calculateDailyTarget(profile: UserProfile): Int {
        // Công thức Mifflin-St Jeor
        val bmr = if (profile.gender == "Nam") {
            10 * profile.weight + 6.25 * profile.height - 5 * profile.age + 5
        } else {
            10 * profile.weight + 6.25 * profile.height - 5 * profile.age - 161
        }

        // Hệ số vận động (mặc định ít vận động)
        val tdee = bmr * 1.2

        return when (profile.goal) {
            "Giảm cân"      -> (tdee - 500).toInt()
            "Tăng cân"      -> (tdee + 500).toInt()
            else            -> tdee.toInt()
        }
    }

    fun getStartOfDay(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}