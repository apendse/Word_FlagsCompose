package com.aap.worldflags.screens.game

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import com.aap.widget.wavyimage.WavyImage
import com.aap.worldflags.data.Game
import com.aap.worldflags.data.QuestionOptions
import com.aap.worldflags.ui.theme.WorldFlagsTheme
import com.aap.worldflags.ui.theme.questionFlagBackground
import com.aap.worldflags.widgets.RadioWithDrawnCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


private const val PAGE_TRANSITION_DURATION = 400

@Composable
fun PlayGame(
    onTitleChange: (string: String, showBack: Boolean) -> Unit,
    onShowScore: () -> Unit,
    viewModel: PlayGameViewModel = hiltViewModel()
) {
    val gameUiState by viewModel.uiState.collectAsState()
    ShowCurrentScore(viewModel, onTitleChange)

    val currentUiState = gameUiState
    when (currentUiState) {
        is GameUiState.Empty -> {
            ShowSpinner()
            viewModel.startANewGame()
        }

        is GameUiState.GameUiStateWithData -> {
            ShowGame(
                game = currentUiState.game,
                viewModel = viewModel
            )
        }

        is GameUiState.GameComplete -> {
            onShowScore()
        }

    }
}

@Composable
fun ShowCurrentScore(
    viewModel: PlayGameViewModel,
    onTitleChange: (string: String, showBack: Boolean) -> Unit,
) {
    val score by viewModel.scoreUiState.collectAsState()
    if (!score.ignore) {
        onTitleChange(score.title, false)
    }
}

@Composable
fun ShowSpinner() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

private const val DAMPING_RATIO = 0.9f
private const val STIFFNESS = Spring.StiffnessVeryLow

private fun moveToNextPage(pagerState: PagerState, newPage: Int, coroutineScope: CoroutineScope) {
    coroutineScope.launch {
        pagerState.animateScrollToPage(
            newPage,
            animationSpec = spring(dampingRatio = DAMPING_RATIO, stiffness = STIFFNESS)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun ShowGame(
    game: Game,
    viewModel: PlayGameViewModel
) {

    val useViewAnimator = true
    if (useViewAnimator) {
        AnimatedContent(
            targetState = game.currentQuestion,
            transitionSpec = {
                when {
                    initialState == targetState -> EnterTransition.None togetherWith ExitTransition.None
                    else -> slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(PAGE_TRANSITION_DURATION)
                    ) togetherWith slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(PAGE_TRANSITION_DURATION)
                    )
                }
            },
            label = "Animated Content"
        ) { pageNumber ->
            val questionData = game.questions[pageNumber]
            ShowSingleQuestion(
                questionData, viewModel
            )
        }

    } else {
        val coroutineScope = rememberCoroutineScope()
        val pagerState = rememberPagerState(pageCount = {
            game.questions.size
        })

        if (pagerState.currentPage != game.currentQuestion) {
            moveToNextPage(pagerState, game.currentQuestion, coroutineScope)
        }

        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            beyondViewportPageCount = 1
        ) { pageNumber ->
            with(pagerState) {
                if (pageNumber != currentPage && !isScrollInProgress) {
                    return@HorizontalPager
                }
            }
            val questionData = game.questions[pageNumber]
            ShowSingleQuestion(
                questionData, viewModel
            )
        }
    }
}

@Composable
fun ShowSingleQuestion(questionOptions: QuestionOptions, viewModel: PlayGameViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        WavyImage(
            drawable = questionOptions.flagImage,
            modifier = Modifier
                .background(color = questionFlagBackground)
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
        )

        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
                .padding(top = 8.dp, bottom = 4.dp, start = 8.dp, end = 8.dp)
                .selectableGroup()
        ) {

            val (b1, b2, b3, b4) = createRefs()
            val startGuideline = createGuidelineFromStart(0.5f)
            val topGuideline = createGuidelineFromTop(0.5f)
            var offsetVar = 0
            offsetVar.let { offset ->
                RadioButtonWithText(
                    modifier = Modifier.constrainAs(b1) {
                        top.linkTo(parent.top, 8.dp)
                        start.linkTo(parent.start)
                        end.linkTo(startGuideline)
                        width = Dimension.fillToConstraints
                    },
                    questionOptions.answers[offset].countryName,

                    selected = questionOptions.answers[offset].isSelected,
                    onClick = {
                        viewModel.playSoundOnly(offset)
                    },
                    onSelectionComplete = {
                        viewModel.onAnswerClickCompleted(offset)
                    },
                    isAnswerCorrect = if (questionOptions.answers[offset].isSelected) questionOptions.answers[offset].isCorrect else null
                )
            }
            offsetVar += 1
            offsetVar.let { offset ->
                RadioButtonWithText(
                    modifier = Modifier.constrainAs(b2) {
                        top.linkTo(parent.top, 8.dp)
                        start.linkTo(startGuideline)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    },
                    questionOptions.answers[offset].countryName,
                    selected = questionOptions.answers[offset].isSelected,

                    onClick = {
                        viewModel.playSoundOnly(offset)
                    },
                    onSelectionComplete = {
                        viewModel.onAnswerClickCompleted(offset)
                    },
                    isAnswerCorrect = if (questionOptions.answers[offset].isSelected) questionOptions.answers[offset].isCorrect else null
                )
            }
            offsetVar += 1
            offsetVar.let { offset ->
                RadioButtonWithText(
                    modifier = Modifier.constrainAs(b3) {
                        start.linkTo(parent.start)
                        end.linkTo(startGuideline)
                        top.linkTo(topGuideline)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                    },
                    questionOptions.answers[offset].countryName,
                    selected = questionOptions.answers[offset].isSelected,
                    onClick = {
                        viewModel.playSoundOnly(offset)
                    },
                    onSelectionComplete = {
                        viewModel.onAnswerClickCompleted(offset)
                    },
                    isAnswerCorrect = if (questionOptions.answers[offset].isSelected) questionOptions.answers[offset].isCorrect else null
                )
            }
            offsetVar += 1
            offsetVar.let { offset ->
                RadioButtonWithText(
                    modifier = Modifier.constrainAs(b4) {
                        start.linkTo(startGuideline)
                        end.linkTo(parent.end)
                        top.linkTo(topGuideline)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                    },
                    questionOptions.answers[offset].countryName,
                    selected = questionOptions.answers[offset].isSelected,
                    onClick = {
                        viewModel.playSoundOnly(offset)
                    },
                    onSelectionComplete = {
                        viewModel.onAnswerClickCompleted(offset)
                    },
                    isAnswerCorrect = if (questionOptions.answers[offset].isSelected) questionOptions.answers[offset].isCorrect else null
                )
            }
        }

    }
}

@Composable
fun RadioButtonWithText(
    modifier: Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    onSelectionComplete: () -> Unit,
    isAnswerCorrect: Boolean? = null
) {
    val color = when (isAnswerCorrect) {
        true -> Color(0xFF2E7D32) // Success Green
        false -> MaterialTheme.colorScheme.error
        null -> MaterialTheme.colorScheme.primary
    }

    var selectedState by remember { mutableStateOf(selected) }
    val onClickHandler = {
        selectedState = !selectedState
        onClick()
        onSelectionComplete()
    }
    Row(modifier.padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        RadioWithDrawnCallback(
            selected = selectedState,
            onClick = onClickHandler,
            color = color,
            onSelectionCompleted = {}
        )
        Text(
            modifier = Modifier.clickable(onClick = onClickHandler),
            text = text,
            fontSize = 16.sp,
            maxLines = 3,
            color = color
        )
    }
}

@Preview("default", showBackground = true)
@Preview("dark theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview("large font", fontScale = 3f, showBackground = true)
@Composable
private fun CardPreview() {
    WorldFlagsTheme {
        RadioButtonWithText(
            Modifier.background(MaterialTheme.colorScheme.primary),
            "Trinidad and Tobago",
            false,
            {},
            {}
        )
    }
}
