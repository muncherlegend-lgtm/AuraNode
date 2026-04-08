package com.example.newapp.data.model

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val ruleType: AchievementRuleType,
    val threshold: Int = 1
)
