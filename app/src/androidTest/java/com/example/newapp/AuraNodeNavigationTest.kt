package com.example.newapp

import android.content.pm.ActivityInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.newapp.ui.AuraNodeTestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuraNodeNavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun menuOpensCoreSections() {
        composeTestRule.onNodeWithTag(AuraNodeTestTags.MENU_SCREEN).assertIsDisplayed()
        composeTestRule.onNodeWithTag(AuraNodeTestTags.MENU_OPEN_SETTINGS).performClick()
        composeTestRule.onNodeWithTag(AuraNodeTestTags.SETTINGS_SCREEN).assertIsDisplayed()
        composeTestRule.activity.runOnUiThread { composeTestRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(AuraNodeTestTags.MENU_IMPORT_MATERIAL).performClick()
        composeTestRule.onNodeWithTag(AuraNodeTestTags.MATERIALS_SCREEN).assertIsDisplayed()
    }

    @Test
    fun classicRunReachesResultAndRetryWorks() {
        composeTestRule.onNodeWithText("Начать").performClick()
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
    fun atlasSurvivesRotation() {
        composeTestRule.onNodeWithTag(AuraNodeTestTags.MENU_OPEN_ATLAS).performClick()
        composeTestRule.onNodeWithTag(AuraNodeTestTags.ATLAS_SCREEN).assertIsDisplayed()

        composeTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(AuraNodeTestTags.ATLAS_SCREEN).assertIsDisplayed()

        composeTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
}
