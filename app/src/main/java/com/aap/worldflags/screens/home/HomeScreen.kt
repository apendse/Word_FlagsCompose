package com.aap.worldflags.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aap.worldflags.R
import com.aap.worldflags.ui.theme.WorldFlagsTheme
import kotlin.random.Random

@Composable
fun HomeScreen(
    onStartNewGame: () -> Unit,
    onShowPastScore: () -> Unit,
    onShowWinningStreaks: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        BackgroundFlagGrid(viewModel, Modifier.fillMaxSize())
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RenderButtons(
                onStartNewGame =
                    onStartNewGame,
                onShowPastScore = onShowPastScore,
                onShowWinningStreaks = onShowWinningStreaks,
            )
        }
    }
}

@Composable
fun RenderButtons(
    onStartNewGame: () -> Unit,
    onShowPastScore: () -> Unit,
    onShowWinningStreaks: () -> Unit
) {
    val fontSize = 24.sp
    ElevatedButton(onClick = onStartNewGame) {
        Text(
            text = stringResource(id = R.string.start_new_game_home),
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        )
    }
    Spacer(Modifier.height(16.dp))
    ElevatedButton(onClick = onShowPastScore) {
        Text(
            text = stringResource(id = R.string.past_scores_home),
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        )
    }

    Spacer(Modifier.height(16.dp))
    ElevatedButton(onClick = onShowWinningStreaks) {
        Text(
            text = stringResource(id = R.string.winning_streaks),
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RenderFlag(drawable: Int) {
    Spacer(Modifier.width(8.dp))
    Image(
        modifier = Modifier.fillMaxSize(),
        alpha = 0.3f,
        painter = painterResource(id = drawable), contentDescription = ""
    )
    Spacer(Modifier.width(8.dp))
}

suspend fun scrollHorizontalList(lazyListState: LazyListState) {
    if (Random.nextBoolean() && lazyListState.canScrollForward) {
        val step = if (Random.nextBoolean()) 1 else 2
        lazyListState.animateScrollToItem(lazyListState.firstVisibleItemIndex + step)
    }
}

@Composable
fun BackgroundFlagGrid(viewModel: HomeViewModel, modifier: Modifier) {
    // Show six rows of random flags. Each of these lists will scroll randomly.
    val listState1 = rememberLazyListState()
    val listState2 = rememberLazyListState()
    val listState3 = rememberLazyListState()
    val listState4 = rememberLazyListState()
    val listState5 = rememberLazyListState()
    val listState6 = rememberLazyListState()
// Remember a CoroutineScope to be able to launch
    val coroutineScope = rememberCoroutineScope()
    val list = viewModel.uiState.collectAsState()

    val offset = viewModel.scrollPos.collectAsState()
    LaunchedEffect(offset.value) {
        scrollHorizontalList(listState5)
        scrollHorizontalList(listState1)
        scrollHorizontalList(listState2)
        scrollHorizontalList(listState3)
        scrollHorizontalList(listState4)
        scrollHorizontalList(listState6)
    }

    val startOffsets = viewModel.startOffsets.collectAsState()

    //var offset = 0
    val listSize = list.value.size
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        LazyRow(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            state = listState1,
        ) {
            items(HORIZONTAL_ROW_COUNT) {
                RenderFlag(list.value[((startOffsets.value[0] + it) % listSize) % listSize])
            }
        }
        LazyRow(
            Modifier
                .fillMaxWidth()
                .height(48.dp),
            state = listState2,
        ) {
            items(HORIZONTAL_ROW_COUNT) {
                RenderFlag(list.value[(startOffsets.value[1] + it) % listSize])
            }

        }
        LazyRow(
            Modifier
                .fillMaxWidth()
                .height(48.dp),
            state = listState3,
        ) {
            items(HORIZONTAL_ROW_COUNT) {
                RenderFlag(list.value[(startOffsets.value[2] + it) % listSize])
            }

        }
        LazyRow(
            Modifier
                .fillMaxWidth()
                .height(48.dp),
            state = listState4,
        ) {
            items(HORIZONTAL_ROW_COUNT) {
                RenderFlag(list.value[(startOffsets.value[3] + it) % listSize])
            }

        }
        LazyRow(
            Modifier
                .fillMaxWidth()
                .height(48.dp),
            state = listState5,
        ) {
            items(HORIZONTAL_ROW_COUNT) {
                RenderFlag(list.value[(startOffsets.value[4] + it) % listSize])
            }
        }

        LazyRow(
            Modifier
                .fillMaxWidth()
                .height(48.dp),
            state = listState6,
        ) {
            items(HORIZONTAL_ROW_COUNT) {
                RenderFlag(list.value[(startOffsets.value[4] + it) % listSize])
            }
        }


    }
}

@Preview
@Composable
fun PreviewHome() {
    WorldFlagsTheme {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RenderButtons({}, {}, {})
        }
    }

}