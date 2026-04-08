package com.example.newapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object AuraNodeSpacing {
    val xSmall = 8.dp
    val small = 12.dp
    val medium = 16.dp
    val large = 24.dp
    val xLarge = 32.dp
}

object AuraNodeCorners {
    val pill = RoundedCornerShape(100)
    val chip = RoundedCornerShape(20.dp)
    val card = RoundedCornerShape(28.dp)
    val hero = RoundedCornerShape(36.dp)
}

val AuraNodeShapes = Shapes(
    small = RoundedCornerShape(18.dp),
    medium = RoundedCornerShape(28.dp),
    large = RoundedCornerShape(36.dp)
)
