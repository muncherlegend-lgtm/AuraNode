package com.example.newapp.ui.copy

import com.example.newapp.data.model.Difficulty
import com.example.newapp.data.model.QuizMode

const val APP_NAME = "Маршрут Алтая"

fun Difficulty.uiLabel(): String = when (this) {
    Difficulty.CADET -> "Кадет"
    Difficulty.ENGINEER -> "Инженер"
    Difficulty.COSMONAUT -> "Космонавт"
}

fun Difficulty.shortDescription(): String = when (this) {
    Difficulty.CADET -> "Базовые ориентиры по региону и городу."
    Difficulty.ENGINEER -> "Больше исторических, научных и географических связей."
    Difficulty.COSMONAUT -> "Редкие детали и сложные вопросы без поблажек."
}

fun QuizMode.uiLabel(): String = when (this) {
    QuizMode.CLASSIC -> "Основной"
    QuizMode.SPRINT -> "Быстрый"
    QuizMode.LEGEND -> "Углубленный"
}

fun QuizMode.longDescription(): String = when (this) {
    QuizMode.CLASSIC -> "Спокойный конкурсный формат с ровным темпом и полной серией вопросов."
    QuizMode.SPRINT -> "Короткий забег с более жёстким таймером и бонусом за скорость."
    QuizMode.LEGEND -> "Удлинённый проход с усиленным весом вопросов и акцентом на сложные темы."
}

fun QuizMode.modeHighlights(): List<String> = when (this) {
    QuizMode.CLASSIC -> listOf("Полный набор", "Ровный счёт", "Лучший для показа")
    QuizMode.SPRINT -> listOf("Меньше вопросов", "Короткий таймер", "Бонус за скорость")
    QuizMode.LEGEND -> listOf("Больше вопросов", "Сложный отбор", "Открывается позже")
}
