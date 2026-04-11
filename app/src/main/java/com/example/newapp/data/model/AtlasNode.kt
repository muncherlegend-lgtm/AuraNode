package com.example.newapp.data.model

data class AtlasNode(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val highlightFact: String,
    val rewardTitle: String,
    val factCategory: FactCategory,
    val xFraction: Float,
    val yFraction: Float,
    val labelXFraction: Float = xFraction,
    val labelYFraction: Float = yFraction,
    val connections: List<String> = emptyList()
)
