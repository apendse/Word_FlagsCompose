package com.aap.worldflags.screens.score

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import com.aap.worldflags.R
import com.aap.worldflags.data.SingleGameSummarizedData
import com.aap.worldflags.ui.theme.WorldFlagsTheme
import com.aap.worldflags.ui.theme.correctColor

@Composable
fun ShowScore(
    goBack: () -> Unit,
    onStartNewGame: () -> Unit,
    onShowDetailedScore: () -> Unit,
    viewModel: ShowScoreViewModel = hiltViewModel()
) {
    val scoreState by viewModel.uiState.collectAsState()
    scoreState?.run {
        DisplayScore(goBack, onStartNewGame, onShowDetailedScore, this)
    } ?: run {
        viewModel.fetchScore()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayScore(
    goBack: () -> Unit,
    onStartNewGame: () -> Unit,
    onShowDetailedScore: () -> Unit,

    singleGameSummarizedData: SingleGameSummarizedData
) {
    BasicAlertDialog(
        //modifier = Modifier.padding(8.dp),
        onDismissRequest = goBack, properties = DialogProperties(dismissOnClickOutside = true)
    ) {
        CompactScore(onStartNewGame, onShowDetailedScore, goBack, singleGameSummarizedData)
    }

}

@Composable
fun CompactScore(
    onStartNewGame: () -> Unit,
    onShowDetailedScore: () -> Unit,
    onShowHome: () -> Unit,
    singleGameSummarizedData: SingleGameSummarizedData
) {
    Surface {
        Card(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .padding(8.dp)
                .height(200.dp)
                .fillMaxWidth()

        ) {
            //Spacer(modifier = Modifier.weight(0.5f))
            val scoreStringMap = getScoreStrings(
                LocalContext.current,
                singleGameSummarizedData.correct,
                singleGameSummarizedData.wrong,
                singleGameSummarizedData.totalQuestions
            )

            val totalStrings = scoreStringMap.getOrDefault(TOTAL_LABEL, listOf("", "", ""))
            val correctStrings = scoreStringMap.getOrDefault(CORRECT_LABEL, listOf("", "", ""))
            val wrongStrings = scoreStringMap.getOrDefault(WRONG_LABEL, listOf("", "", ""))

            ConstraintLayout(modifier = Modifier
                .fillMaxWidth()
                .weight(0.8f)) {
                val (
                    title,
                    totalLabel,
                    totalSeparator,
                    totalValue,
                    correctLabel,
                    correctSeparator,
                    correctValue,
                    correctTick,
                    wrongLabel,
                    wrongSeparator,
                    wrongValue,
                    wrongTick,
                ) = createRefs()

                //val barrier = createEndBarrier(totalLabel, correctLabel, wrongLabel) // Barrier after the labels
                val verticalChain = createVerticalChain(
                    title,
                    totalSeparator,
                    correctSeparator,
                    wrongSeparator,
                    chainStyle = ChainStyle.Spread
                )
                constrain(verticalChain) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
                Column(modifier = Modifier
                    .constrainAs(title) {
                        top.linkTo(parent.top)
                        centerHorizontallyTo(parent)
                    }
                    .fillMaxWidth()) {
                    Text(
                        stringResource(R.string.your_score),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 4.dp,
                                start = 0.dp,
                                end = 0.dp,
                                bottom = 0.dp,
                            ),
                        thickness = 1.dp,
                        color = Color.DarkGray,
                    )


                }


                //total
                Text(
                    text = stringResource(R.string.score_text_separator),
                    modifier = Modifier.constrainAs(totalSeparator) {
                        top.linkTo(title.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                )
                Text(
                    text = totalStrings[0],
                    modifier = Modifier
                        .constrainAs(totalLabel) {
                            baseline.linkTo(totalSeparator.baseline)
                            end.linkTo(totalSeparator.start)
                        }
                        .padding(end = 8.dp)
                )
                Text(
                    text = totalStrings[1],
                    modifier = Modifier
                        .constrainAs(totalValue) {
                            baseline.linkTo(totalSeparator.baseline)
                            start.linkTo(totalSeparator.end)
                        }
                        .padding(start = 8.dp)
                )
                //correct
                Text(
                    text = stringResource(R.string.score_text_separator),
                    modifier = Modifier.constrainAs(correctSeparator) {
                        top.linkTo(totalSeparator.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                )
                Text(
                    text = correctStrings[0],
                    modifier = Modifier
                        .constrainAs(correctLabel) {
                            baseline.linkTo(correctSeparator.baseline)
                            end.linkTo(correctSeparator.start)
                        }
                        .padding(end = 8.dp)
                )
                Text(
                    text = correctStrings[1],
                    modifier = Modifier
                        .constrainAs(correctValue) {
                            baseline.linkTo(correctSeparator.baseline)
                            start.linkTo(correctSeparator.end)
                        }
                        .padding(start = 8.dp)
                )
                Text(
                    text = correctStrings[2],
                    color = correctColor,
                    modifier = Modifier
                        .constrainAs(correctTick) {
                            baseline.linkTo(correctValue.baseline)
                            start.linkTo(correctValue.end)
                        }
                        .padding(start = 8.dp)
                )

                //wrong
                Text(
                    text = stringResource(R.string.score_text_separator),
                    modifier = Modifier.constrainAs(wrongSeparator) {
                        top.linkTo(correctSeparator.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                )
                Text(
                    text = wrongStrings[0],
                    modifier = Modifier
                        .constrainAs(wrongLabel) {
                            baseline.linkTo(wrongSeparator.baseline)
                            end.linkTo(wrongSeparator.start)
                        }
                        .padding(end = 8.dp)
                )
                Text(
                    text = wrongStrings[1],
                    modifier = Modifier
                        .constrainAs(wrongValue) {
                            baseline.linkTo(wrongSeparator.baseline)
                            start.linkTo(wrongSeparator.end)
                        }
                        .padding(start = 8.dp)
                )
                Text(
                    text = wrongStrings[2],
                    fontSize = 8.sp,
                    modifier = Modifier
                        .constrainAs(wrongTick) {
                            top.linkTo(wrongValue.top)
                            start.linkTo(wrongValue.end)
                        }
                        .padding(start = 8.dp)
                )

            }

//            LazyColumn(modifier = Modifier.padding(4.dp)) {
//                val modifierText1 = Modifier.width(160.dp).align(Alignment.End)
//                val modifierText2 = Modifier.padding(start = 8.dp)
//                val modifierText3 = Modifier.padding(start = 4.dp)
//                item {
//                    Row {
//                        val strings = scoreStringMap.getOrDefault(TOTAL_LABEL, listOf("", "",""))
//                        Text(modifier = modifierText1, textAlign = TextAlign.End, text = strings.get(0), )
//                        Text(modifier = modifierText2, text = strings.get(1))
//                        Text(modifier = modifierText3, text = strings.get(2))
//                    }
//                }
//
//                item {
//                    Row() {
//                        val strings = scoreStringMap.getOrDefault(CORRECT_LABEL, listOf("", "", ""))
//                        Text(modifier = modifierText1, textAlign = TextAlign.End, text = strings.get(0))
//                        Text(modifier = modifierText2, text = strings.get(1))
//                        Text(modifier = modifierText3, text = strings.get(2))
//                    }
//                }
//                item {
//                    Row() {
//                        val strings = scoreStringMap.getOrDefault(WRONG_LABEL, listOf("", "", ""))
//                        Text(modifier = modifierText1, textAlign = TextAlign.End, text = strings.get(0))
//                        Text(modifier = modifierText2, text = strings.get(1))
//                        Text(modifier = modifierText3, text = strings.get(2))
//                    }
//
//                }
//            }

//            Text(
//                modifier = leftPadding,
//                text = scoreStringMap.getOrDefault(TOTAL_LABEL, "")
//            )
//            Text(
//                modifier = leftPadding,
//                text = scoreStringMap.getOrDefault(CORRECT_LABEL, "")
//            )
//            Text(
//                modifier = leftPadding,
//                text = scoreStringMap.getOrDefault(WRONG_LABEL, "")
//            )
            //Spacer(modifier = Modifier.weight(0.5f))
            Row(
                modifier = Modifier
                    .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ElevatedButton(onClick = onShowDetailedScore) {
                    Text(
                        text = stringResource(id = R.string.show_details),
                        textAlign = TextAlign.Center
                    )
                }
                ElevatedButton(onClick = onStartNewGame) {
                    Text(
                        text = stringResource(id = R.string.start_new_game),
                        textAlign = TextAlign.Center
                    )
                }
//                ElevatedButton(onClick = onShowHome) {
//                    Text(
//                        text = stringResource(id = R.string.show_home),
//                        textAlign = TextAlign.Center
//                    )
//                }

            }
        }
    }
}

private const val TOTAL_LABEL = "total"
private const val CORRECT_LABEL = "correct"
private const val WRONG_LABEL = "wrong"

private fun getScoreStrings(
    context: Context,
    correct: Int,
    wrong: Int,
    total: Int
): Map<String, List<String>> {
    val totalPrefix = context.getString(R.string.total_prefix)
    val correctPrefix = context.getString(R.string.correct_prefix)
    val wrongPrefix = context.getString(R.string.wrong_prefix)
    //prepend white spaces to make the prefixes of equal length
    return mapOf(
        TOTAL_LABEL to listOf(totalPrefix, context.getString(R.string.total_template, total), ""),
        CORRECT_LABEL to listOf(
            correctPrefix,
            context.getString(R.string.score_template, correct),
            context.getString(R.string.correct_tick)
        ),
        WRONG_LABEL to listOf(
            wrongPrefix,
            context.getString(R.string.score_template, wrong),
            context.getString(R.string.wrong_tick)
        )
    )
}


@PreviewLightDark
@Preview("dark theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview("large font", fontScale = 2f)
@Composable
private fun CardPreview() {
    WorldFlagsTheme {
        CompactScore({}, {}, {}, SingleGameSummarizedData(20, 15, 5, true,0L))
    }
}

@Preview("default")
@Preview("dark theme", uiMode = Configuration.ORIENTATION_LANDSCAPE)
@Preview("large font", fontScale = 2f)
@Composable
private fun CardPreviewLandscape() {
    WorldFlagsTheme {
        CompactScore({}, {}, {}, SingleGameSummarizedData(20, 15, 5, true, 0L))
    }
}



