package com.aap.worldflags.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import com.aap.worldflags.R
import com.aap.worldflags.navigation.NavDestinations.DETAILED_SCORE
import com.aap.worldflags.navigation.NavDestinations.HOME
import com.aap.worldflags.navigation.NavDestinations.PAST_SCORES
import com.aap.worldflags.navigation.NavDestinations.PLAY_GUESS_COUNTRY_FROM_FLAG_GAME
import com.aap.worldflags.navigation.NavDestinations.SCORE_SUMMARY
import com.aap.worldflags.navigation.NavDestinations.WINNING_STREAKS
import com.aap.worldflags.screens.game.PlayGame
import com.aap.worldflags.screens.home.HomeScreen
import com.aap.worldflags.screens.pastscores.PastScoresScreen
import com.aap.worldflags.screens.score.ShowDetailedScore
import com.aap.worldflags.screens.score.ShowScore
import com.aap.worldflags.screens.winningstreaks.ShowWinningStreaks
import com.aap.worldflags.ui.theme.WorldFlagsTheme

/**
 * The top level Composable that is used to show the app nav host
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlagsApp() {
    val defaultTitle = stringResource(id = R.string.app_title)
    var scaffoldTitle by rememberSaveable { mutableStateOf(defaultTitle) }
    val navController = rememberNavController()
//    var showBack by rememberSaveable {
//        mutableStateOf(false)
//    }

    var showBack  by rememberSaveable { mutableStateOf(false) }

    WorldFlagsTheme {
        Scaffold(topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(scaffoldTitle)
                },
                navigationIcon = { if (showBack) { IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = LocalContext.current.getString(R.string.back)
                    )
                }} }

            )
        }) {
            NavContent(
                navController,
                onTitleChange = { newTitle: String,
                                  showBackButton: Boolean ->
                                    scaffoldTitle = newTitle.ifEmpty { defaultTitle }
                                    showBack = showBackButton
                                },
                modifier = Modifier.padding(it),
            )
        }

    }
}


@Composable
fun NavContent(navController: NavHostController, onTitleChange: (str: String, showBack: Boolean) -> Unit, modifier: Modifier) {

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = HOME
    ) {

        val onShowScoreLambda = { navController.navigate(SCORE_SUMMARY) }
        val onStartNewGameLambda = {
            navController.navigate(PLAY_GUESS_COUNTRY_FROM_FLAG_GAME)
        }
        val onShowPastScoreLambda = {
            navController.navigate(PAST_SCORES)
        }
        val onShowWinningStreaks = {
            navController.navigate(WINNING_STREAKS)
        }
        composable(HOME) {
            onTitleChange("", false)
            HomeScreen(
                onStartNewGame = onStartNewGameLambda,
                onShowPastScore = onShowPastScoreLambda,
                onShowWinningStreaks = onShowWinningStreaks)
        }

        composable(PLAY_GUESS_COUNTRY_FROM_FLAG_GAME) {
            onTitleChange("", false)
            PlayGame(
                onTitleChange = onTitleChange,
                onShowScore = onShowScoreLambda
            )
        }

        composable(WINNING_STREAKS) {
            onTitleChange(stringResource(R.string.winning_streaks), true)
            ShowWinningStreaks(
                onBack = {
                    navController.navigate(HOME) {
                        popUpTo(HOME) {
                            this.inclusive = true
                        }
                    }
                },
                onStartNewGame = {
                    navController.navigate(PLAY_GUESS_COUNTRY_FROM_FLAG_GAME) {
                        popUpTo(HOME) {
                            inclusive = true
                        }
                    }
                },

                onTitleChange = onTitleChange,
            )
        }
        composable(DETAILED_SCORE) {
            ShowDetailedScore(
                goBack = {
                    navController.navigate(HOME) {
                        popUpTo(DETAILED_SCORE) {
                            this.inclusive = true
                        }
                    }
                         },
                onStartNewGame = {
                    navController.navigate(PLAY_GUESS_COUNTRY_FROM_FLAG_GAME) {
                        popUpTo(HOME) {
                            inclusive = true
                        }
                    }
                },

                onTitleChange = onTitleChange,
            )
        }

        composable(PAST_SCORES) {
            onTitleChange(stringResource(id = R.string.past_scores), true)
            PastScoresScreen()
        }

        dialog(SCORE_SUMMARY) {
            onTitleChange("", false)
            ShowScore(
                goBack = {
                    navController.navigate(HOME) {
                        popUpTo(HOME) {
                            this.inclusive = true
                        }
                    }
                },
                onStartNewGame = {
                    navController.navigate(PLAY_GUESS_COUNTRY_FROM_FLAG_GAME) {
                        popUpTo(PLAY_GUESS_COUNTRY_FROM_FLAG_GAME) {
                            inclusive = true
                        }
                    }
                },
                onShowDetailedScore = {
                    navController.navigate(DETAILED_SCORE) {
                        popUpTo(HOME) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }

}

/*
private fun dumpStack() {
    val stack = Thread.currentThread().stackTrace
//    stack.forEach {
//        Log.d("YYYY", "${it.className} : ${it.methodName} (${it.lineNumber})");
//    }
}
*/