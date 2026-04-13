package com.example.newapp.navigation

sealed class AuraNodeDestination(val route: String) {
    data object Menu : AuraNodeDestination("menu")
    data object Atlas : AuraNodeDestination("atlas")
    data object Settings : AuraNodeDestination("settings")
    data object Themes : AuraNodeDestination("themes")
    data object Materials : AuraNodeDestination("materials")
    data object Quiz : AuraNodeDestination("quiz")
    data object Result : AuraNodeDestination("result")
}
