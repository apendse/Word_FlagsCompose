package com.aap.worldflags.screens.pastscores

import android.content.res.Configuration
import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aap.worldflags.R
import com.aap.worldflags.data.SingleGameSummarizedData
import com.aap.worldflags.ui.theme.WorldFlagsTheme
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.hours

@Composable
fun PastScoresScreen(viewModel: PastScoresViewModel = hiltViewModel()) {
    val state = viewModel.scoreUiState.collectAsStateWithLifecycle()
    ShowScore(state.value)
}


@Composable
private fun ShowScore(list: List<SingleGameSummarizedData>) {
    if (list.isEmpty()) {
        EmptyListMessage()
    } else {
        val totalWeight = .20f
        val correctWeight = .20f
        val wrongWeight = .20f
        val dateWeight = 0.40f
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 4.dp)
        ) {
            item {
                Row(Modifier.background(Color.DarkGray)) {
                    TableCell(text = stringResource(R.string.total), weight = totalWeight,  color = Color.White)
                    TableCell(text = stringResource(R.string.correct), weight = correctWeight,  color = Color.White)
                    TableCell(text = stringResource(R.string.wrong), weight = wrongWeight,  color = Color.White)
                    TableCell(text = stringResource(R.string.date), weight = dateWeight,  color = Color.White)
                }
            }
            this.itemsIndexed(items = list) { index, item ->
                Row {
                    TableCell(text = "${item.totalQuestions}", weight = totalWeight,)
                    TableCell(text = "${item.correct}", weight = correctWeight, color = Color.Black)
                    TableCell(text = "${item.wrong}", weight = wrongWeight, color = Color.Red)
                    TableCell(text = getDateString(item.time), weight = dateWeight, color = Color.DarkGray, fontSize = 14.sp)
                }
            }
        }
    }
}

private fun getDateString(time: Long): String {
    val now = System.currentTimeMillis()
    val format = if ( now.minus(time) < 24.hours.inWholeMilliseconds) {
        "h:mm a"
    } else {
        "yy/MM/dd h:mm"
    }

    return SimpleDateFormat(format, Locale.getDefault()).format(Date(time))
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    color: Color = Color.Black,
    fontSize: TextUnit = 16.sp
) {
    Text(
        text = text,
        fontSize = fontSize,
        color = color,
        modifier = Modifier
            .padding(start = 4.dp)
            .weight(weight)
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewPastRow() {
    val list = listOf(
        SingleGameSummarizedData(
        totalQuestions = 20,
        correct = 19,
        wrong = 1,
        time = System.currentTimeMillis() - 48.hours.inWholeMilliseconds),
        SingleGameSummarizedData(
            totalQuestions = 20,
            correct = 15,
            wrong = 5,
            time = System.currentTimeMillis() - 3.hours.inWholeMilliseconds)

    )
    WorldFlagsTheme {
        ShowScore(list)
    }
}
@Composable
private fun EmptyListMessage() {
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
        contentAlignment = Alignment.TopStart) {
        Text(stringResource(id = R.string.empty_past_scores), fontSize = 20.sp, maxLines = 3)
    }
}
@Composable
private fun RenderRow(index: Int, item: SingleGameSummarizedData) {
    val backgroundColor = if (index % 2 == 0) Color.White else Color.LightGray
    Column() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(all = 12.dp)
        ) {
            Text(
                text = "Total ${item.totalQuestions} correct: ${item.correct} wrong: ${item.wrong}",
                fontSize = 16.sp,
                color = Color.Black
            )

        }
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.End)
                .background(backgroundColor),
            text = "${item.time}",
            textAlign = TextAlign.End,
            fontSize = 11.sp
        )
    }
}
@Preview("default")
@Preview("dark theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview("large font", fontScale = 2f)
@Composable
fun PreviewEmpty() {
    WorldFlagsTheme {
        EmptyListMessage()
    }
}

@Preview("default")
@Preview("dark theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview("large font", fontScale = 2f)
@Composable
fun PreviewRow() {
    WorldFlagsTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            val s = SimpleDateFormat("yy/MM/dd h:mm", Locale.getDefault())
            val item = SingleGameSummarizedData(30, 16, 15, System.currentTimeMillis())
            RenderRow(0, item)
            RenderRow(1, item)
        }
    }
}