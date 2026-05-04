package com.aap.worldflags.screens.score

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aap.worldflags.R
import com.aap.worldflags.data.AnswerRecord
import com.aap.worldflags.ui.theme.WorldFlagsTheme
import com.aap.worldflags.widgets.FlagImage

@Composable
fun ShowDetailedScore(
    goBack: () -> Unit,
    onStartNewGame: () -> Unit,
    onTitleChange: (String, Boolean) -> Unit,
    viewModel: ShowDetailedScoreViewModel = hiltViewModel()
) {
    val scoreState by viewModel.uiState.collectAsState()
    onTitleChange(stringResource(id = R.string.detailed_score_title), true)
    scoreState?.run {
        ShowDetailedScore(this, goBack, onStartNewGame)
    } ?: run {
        viewModel.fetchScore()
    }
}


@Composable
private fun ShowDetailedScore(
    answers: List<AnswerRecord>,
    goBack: () -> Unit,
    onStartNewGame: () -> Unit,
) {
    val separatorColor = if (isSystemInDarkTheme()) {
            Color.LightGray
        } else {
            Color.DarkGray
        }

    Scaffold(bottomBar = {bottomButtons(goBack, onStartNewGame)}) {
        LazyColumn(modifier = Modifier.padding(paddingValues = it)) {
            this.itemsIndexed(answers, key = { index, item -> item.flagDrawable }) { index, item ->
                RenderRow(index, item, index == answers.lastIndex)
                if (index == answers.lastIndex) {
                    HorizontalDivider(Modifier.padding(top = 8.dp, bottom = 8.dp))
                } else {
                    Spacer(modifier = Modifier
                        .height(4.dp)
                        .fillMaxWidth())
                    Spacer(modifier = Modifier
                        .height(1.dp)
                        .fillMaxWidth()
                        .background(separatorColor))
                    Spacer(modifier = Modifier
                        .height(4.dp)
                        .fillMaxWidth())
                    //

                }
            }
        }
    }

}

@Composable
fun bottomButtons(goBack: () -> Unit,
                  onStartNewGame: () -> Unit,) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(start = 8.dp, end = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween) {
        ElevatedButton(onClick = goBack) {
            Text(text = stringResource(id = R.string.show_home), textAlign = TextAlign.Center)
        }
        ElevatedButton(onClick = onStartNewGame) {
            Text(text = stringResource(id = R.string.start_new_game), textAlign = TextAlign.Center)
        }

    }

}



@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RenderRow(index: Int, answerRecord: AnswerRecord, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        FlagImage(modifier = Modifier
            .width(64.dp)
            .height(64.dp), drawable = answerRecord.flagDrawable)

        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            val textColor = MaterialTheme.colorScheme.secondary
            Text(
                modifier = Modifier.basicMarquee(),
                text = answerRecord.correctAnswer,
                color = textColor,
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            val ans = if (answerRecord.isCorrect) {
                getAnswerString(stringResource(id = R.string.correct_answer), stringResource(id = R.string.correct_tick), Color.Green)
            } else {
                val prefix = stringResource(id = R.string.your_answer)
                getWrongAnswerString(prefix, answerRecord.yourAnswer, stringResource(id = R.string.wrong_tick),  Color.Red)
            }

            val inlineContent: Map<String, InlineTextContent> = getInlineContent(answerRecord)

            Text(
                modifier = Modifier.basicMarquee(),
                text = ans,
                color = textColor,
                maxLines = 3,
                inlineContent = inlineContent
            )
            Spacer(modifier = Modifier.width(4.dp))
                //Text(tick, color = color)
        }


    }

}
private const val CONTENT_ID_FLAG_IMAGE = "flagImage"

private fun getInlineContent(answerRecord: AnswerRecord)  = if (answerRecord.isCorrect) {
    emptyMap()
} else {
    mapOf(CONTENT_ID_FLAG_IMAGE to InlineTextContent(
        Placeholder(
            width = 40.sp,
            height = 40.sp,
            placeholderVerticalAlign = PlaceholderVerticalAlign.TextTop
        )
    ) {
        answerRecord.yourAnswerDrawable?.let {
            Image(painterResource(id = it), contentDescription = "flag image")
        }
    })
}

private fun getWrongAnswerString(answerPrefix: String, answer:String, iconString: String, color: Color): AnnotatedString {
    return buildAnnotatedString {
        append(answerPrefix)
        append("  ")
        appendInlineContent(CONTENT_ID_FLAG_IMAGE, "flag")
        append("  ")
        append(answer)
        pushStyle(SpanStyle(color = color))
        append("  ")
        append(iconString)
        toAnnotatedString()
    }
}

private fun getAnswerString(answer: String, iconString: String, color: Color): AnnotatedString = buildAnnotatedString {
    append(answer)
    pushStyle(SpanStyle(color = color))
    append(" ")
    append(iconString)
    toAnnotatedString()
}

@Preview("default")
@Preview("dark theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview("large font", fontScale = 2f)
@Composable
private fun ListPreview() {
    WorldFlagsTheme {
        ShowDetailedScore(
            listOf(
                AnswerRecord(
                    flagDrawable = R.drawable.be,
                    questionCountryCode = "Something",
                    correctAnswer = "Something",
                    yourAnswer = "Something",
                    yourAnswerDrawable = R.drawable.`in`
                ),
                AnswerRecord(
                    flagDrawable = R.drawable.de,
                    questionCountryCode = "Something",
                    correctAnswer = "Something2",
                    yourAnswer = "Something2",
                    yourAnswerDrawable = null
                ),
                AnswerRecord(
                    flagDrawable = R.drawable.us,
                    questionCountryCode = "Something",
                    correctAnswer = "Something1",
                    yourAnswer = "Something1",
                    yourAnswerDrawable = null
                ),
            ), {}, {})
    }
}
@Preview("default")
@Preview("dark theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview("large font", fontScale = 2f)
@Composable
private fun CardPreview() {
    WorldFlagsTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            RenderRow(0, AnswerRecord(
                flagDrawable = R.drawable.ad,
                questionCountryCode = "us",
                correctAnswer = "Very looong answer",
                yourAnswer = "Very long long answer",
                yourAnswerDrawable = R.drawable.es
            ), false)
        }
    }
}
