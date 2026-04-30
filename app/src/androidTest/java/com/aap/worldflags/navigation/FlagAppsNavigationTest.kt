package com.aap.worldflags.navigation

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import com.aap.worldflags.MainActivity
import com.aap.worldflags.R
import com.aap.worldflags.test.TestActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class FlagAppsNavigationTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<TestActivity>()

    @Test
    fun testNavigationHome() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            NavContent(navController, onTitleChange = { _, _ -> }, modifier = Modifier)
        }
        val applicationContext = composeTestRule.activity.applicationContext
        val newGameString = applicationContext.getString(R.string.start_new_game_home)
        composeTestRule.onNodeWithText(newGameString).assertIsDisplayed()
        val pastScoresString = applicationContext.getString(R.string.past_scores_home)
        composeTestRule.onNodeWithText(pastScoresString).assertIsDisplayed()
    }
}