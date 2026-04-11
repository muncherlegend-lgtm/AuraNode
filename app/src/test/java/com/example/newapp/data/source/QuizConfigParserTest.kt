package com.example.newapp.data.source

import com.example.newapp.data.model.AnswerMode
import com.example.newapp.data.model.QuizMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizConfigParserTest {

    @Test
    fun parseQuestionsSkipsMalformedEntries() {
        val parsed = QuizConfigParser.parseQuestions(
            """
            {
              "questions": [
                {
                  "id": 1,
                  "text": "Valid",
                  "options": ["A", "B", "C", "D"],
                  "correctAnswerIndex": 2,
                  "difficulty": "CADET",
                  "explanation": "ok"
                },
                {
                  "id": 2,
                  "text": "Broken",
                  "options": ["A", "B", "C"],
                  "correctAnswerIndex": 0,
                  "difficulty": "CADET",
                  "explanation": "bad"
                }
              ]
            }
            """.trimIndent()
        )

        assertEquals(1, parsed.size)
        assertEquals("Valid", parsed.single().text)
    }

    @Test
    fun parseThemePresetsSkipsInvalidPalette() {
        val parsed = QuizConfigParser.parseThemePresets(
            """
            {
              "themes": [
                {
                  "id": "valid",
                  "title": "Valid",
                  "description": "ok",
                  "isDark": false,
                  "backgroundStyle": "WAVES",
                  "palette": {
                    "primary": "#112233",
                    "onPrimary": "#FFFFFF",
                    "secondary": "#223344",
                    "onSecondary": "#FFFFFF",
                    "tertiary": "#334455",
                    "onTertiary": "#FFFFFF",
                    "background": "#F5F5F5",
                    "onBackground": "#111111",
                    "surface": "#FFFFFF",
                    "onSurface": "#111111",
                    "surfaceVariant": "#EEEEEE",
                    "onSurfaceVariant": "#222222",
                    "primaryContainer": "#CCDDEE",
                    "onPrimaryContainer": "#112233",
                    "secondaryContainer": "#DDEEFF",
                    "onSecondaryContainer": "#223344",
                    "tertiaryContainer": "#EEF1F5",
                    "onTertiaryContainer": "#334455",
                    "outline": "#556677"
                  }
                },
                {
                  "id": "invalid",
                  "title": "Invalid",
                  "description": "bad",
                  "isDark": false,
                  "backgroundStyle": "WAVES",
                  "palette": {
                    "primary": "oops",
                    "onPrimary": "#FFFFFF",
                    "secondary": "#223344",
                    "onSecondary": "#FFFFFF",
                    "tertiary": "#334455",
                    "onTertiary": "#FFFFFF",
                    "background": "#F5F5F5",
                    "onBackground": "#111111",
                    "surface": "#FFFFFF",
                    "onSurface": "#111111",
                    "surfaceVariant": "#EEEEEE",
                    "onSurfaceVariant": "#222222",
                    "primaryContainer": "#CCDDEE",
                    "onPrimaryContainer": "#112233",
                    "secondaryContainer": "#DDEEFF",
                    "onSecondaryContainer": "#223344",
                    "tertiaryContainer": "#EEF1F5",
                    "onTertiaryContainer": "#334455",
                    "outline": "#556677"
                  }
                }
              ]
            }
            """.trimIndent()
        )

        assertEquals(1, parsed.size)
        assertEquals("valid", parsed.single().id)
    }

    @Test
    fun parseQuizSettingsSanitizesUnsafeValuesAndJuryPreset() {
        val settings = QuizConfigParser.parseQuizSettings(
            """
            {
              "timerSeconds": 0,
              "autoAdvanceDelayMs": 100,
              "showTimer": false,
              "allowOptionSelection": false,
              "allowFreeTextAnswers": true,
              "compactUi": true,
              "motionEnabled": true,
              "hapticsEnabled": true,
              "soundEnabled": false,
              "juryModeEnabled": true,
              "demoResetOnLaunch": true,
              "shuffleQuestions": false,
              "shuffleOptions": false,
              "questionsPerDifficulty": 0,
              "defaultThemeId": "",
              "defaultMode": "SPRINT",
              "answerMode": "EXPLORER_TEXT"
            }
            """.trimIndent()
        )

        assertEquals(5, settings.timerSeconds)
        assertEquals(800L, settings.autoAdvanceDelayMs)
        assertEquals(1, settings.questionsPerDifficulty)
        assertTrue(settings.juryModeEnabled)
        assertTrue(settings.demoResetOnLaunch)
        assertTrue(settings.showTimer)
        assertEquals(QuizMode.CLASSIC, settings.defaultMode)
        assertEquals(AnswerMode.CLASSIC_OPTIONS, settings.answerMode)
        assertTrue(settings.allowOptionSelection)
        assertFalse(settings.allowFreeTextAnswers)
    }

    @Test
    fun parseAtlasNodesClampsCoordinates() {
        val parsed = QuizConfigParser.parseAtlasNodes(
            """
            {
              "nodes": [
                {
                  "id": "barnaul",
                  "title": "Барнаул",
                  "subtitle": "Столица",
                  "description": "Desc",
                  "highlightFact": "Fact",
                  "rewardTitle": "Reward",
                  "factCategory": "HISTORY",
                  "xFraction": 1.5,
                  "yFraction": -2,
                  "labelXFraction": 3.2,
                  "labelYFraction": -5,
                  "connections": []
                }
              ]
            }
            """.trimIndent()
        )

        assertEquals(0.95f, parsed.single().xFraction)
        assertEquals(0.08f, parsed.single().yFraction)
        assertEquals(0.95f, parsed.single().labelXFraction)
        assertEquals(0.05f, parsed.single().labelYFraction)
    }

    @Test
    fun parseAtlasNodesUsesDerivedLabelPositionWhenMissing() {
        val parsed = QuizConfigParser.parseAtlasNodes(
            """
            {
              "nodes": [
                {
                  "id": "biysk",
                  "title": "Biysk",
                  "subtitle": "Science",
                  "description": "Desc",
                  "highlightFact": "Fact",
                  "rewardTitle": "Reward",
                  "factCategory": "SCIENCE",
                  "xFraction": 0.4,
                  "yFraction": 0.6,
                  "connections": []
                }
              ]
            }
            """.trimIndent()
        )

        assertEquals(0.4f, parsed.single().labelXFraction)
        assertEquals(0.51f, parsed.single().labelYFraction)
    }
}
