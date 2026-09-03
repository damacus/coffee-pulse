package com.damacus.coffeepulse.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.damacus.coffeepulse.MainActivity
import org.junit.Rule
import org.junit.Test

class CoffeePulseAppTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun brewScreenShowsPrimaryTimerControls() {
        composeRule.onNodeWithText("Coffee Pulse").assertIsDisplayed()
        composeRule.onNodeWithText("Start Brew").assertIsDisplayed()
        composeRule.onNodeWithText("Coffee").assertIsDisplayed()
        composeRule.onNodeWithText("Water").assertIsDisplayed()
    }

    @Test
    fun historyTabShowsEmptyState() {
        composeRule.onNodeWithText("History").assertIsDisplayed()
        composeRule.onNodeWithText("History").performClick()
        composeRule.onNodeWithText("No brews saved yet").assertIsDisplayed()
    }
}
