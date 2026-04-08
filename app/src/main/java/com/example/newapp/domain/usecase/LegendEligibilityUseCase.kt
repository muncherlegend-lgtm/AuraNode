package com.example.newapp.domain.usecase

import com.example.newapp.data.model.Difficulty
import com.example.newapp.data.model.MedalTier
import com.example.newapp.data.model.PlayerProgress
import com.example.newapp.data.model.QuizMode
import javax.inject.Inject

class LegendEligibilityUseCase @Inject constructor() {

    operator fun invoke(progress: PlayerProgress, difficulty: Difficulty): Boolean {
        return progress.bestRuns.any { summary ->
            summary.difficulty == difficulty &&
                summary.mode != QuizMode.LEGEND &&
                summary.medalTier >= MedalTier.SILVER
        }
    }
}
