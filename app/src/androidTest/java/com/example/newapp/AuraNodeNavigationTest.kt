package com.example.newapp

import android.content.pm.ActivityInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.pressBack
import com.example.newapp.data.model.Difficulty
import com.example.newapp.ui.AuraNodeTestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuraNodeNavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun classicCadetRunReachesResultAndRetryWorks() {
        resetDemoState()

        composeTestRule.onNodeWithTag(AuraNodeTestTags.difficultyTag(Difficulty.CADET)).performClick()
        composeTestRule.onNodeWithTag(AuraNodeTestTags.QUIZ_SCREEN).assertIsDisplayed()

        repeat(5) {
            composeTestRule.onNodeWithTag(AuraNodeTestTags.answerOptionTag(0)).performClick()
            Thread.sleep(1900)
        }

        composeTestRule.onNodeWithTag(AuraNodeTestTags.RESULT_SCREEN).assertIsDisplayed()
        composeTestRule.onNodeWithTag(AuraNodeTestTags.RESULT_RETRY).performClick()
        composeTestRule.onNodeWithTag(AuraNodeTestTags.QUIZ_SCREEN).assertIsDisplayed()
    }

    @Test
    fun themeSelectionAndAtlasNavigationSurviveRotation() {
        resetDemoState()

        composeTestRule.onNodeWithTag(AuraNodeTestTags.themeTag("steppe_gold")).performClick()
        composeTestRule.onNodeWithTag(AuraNodeTestTags.themeTag("steppe_gold")).assertIsSelected()

        composeTestRule.onNodeWithTag(AuraNodeTestTags.MENU_OPEN_ATLAS).performClick()
        composeTestRule.onNodeWithTag(AuraNodeTestTags.ATLAS_SCREEN).assertIsDisplayed()

        composeTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(AuraNodeTestTags.ATLAS_SCREEN).assertIsDisplayed()
        composeTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    @Test
    fun settingsSheetExposesDemoResetControl() {
        completeOnboardingIfNeeded()
        composeTestRule.onNodeWithTag(AuraNodeTestTags.MENU_OPEN_SETTINGS).performClick()
        composeTestRule.onNodeWithTag(AuraNodeTestTags.MENU_RESET_PROGRESS).assertIsDisplayed()
        pressBack()
    }

    @Test
    fun onboardingAndImportCtaAreVisible() {
        completeOnboardingIfNeeded()
        composeTestRule.onNodeWithTag(AuraNodeTestTags.MENU_IMPORT_MATERIAL).assertIsDisplayed()
        composeTestRule.onNodeWithTag(AuraNodeTestTags.packTag("official_altai_expedition")).assertIsDisplayed()
    }

    private fun resetDemoState() {
        completeOnboardingIfNeeded()
        composeTestRule.onNodeWithTag(AuraNodeTestTags.MENU_OPEN_SETTINGS).performClick()
        composeTestRule.onNodeWithTag(AuraNodeTestTags.MENU_RESET_PROGRESS).performClick()
        pressBack()
        composeTestRule.waitForIdle()
    }

    private fun completeOnboardingIfNeeded() {
        composeTestRule.waitForIdle()
        val onboardingNodes = composeTestRule.onAllNodes(hasTestTag(AuraNodeTestTags.MENU_ONBOARDING))
        if (onboardingNodes.fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onNodeWithTag(AuraNodeTestTags.MENU_ONBOARDING_CONTINUE).performClick()
            composeTestRule.waitForIdle()
        }
    }
}
