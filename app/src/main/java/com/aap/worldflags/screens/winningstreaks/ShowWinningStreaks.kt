package com.aap.worldflags.screens.winningstreaks

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aap.worldflags.R
import com.aap.worldflags.room.table.ScoreTableRow
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.hours

@Composable
fun ShowWinningStreaks(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onStartNewGame: () -> Unit,
    onTitleChange: (String, Boolean) -> Unit,
    viewModel: ShowWinningStreaksViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    onTitleChange(stringResource(id = R.string.detailed_score_title), true)
    when(uiState) {
        is WinningStreakUiState.NoWinningStreak -> {
            EmptyWinningStreakMessage(modifier)
        }
        is WinningStreakUiState.WinningStreak -> {
            ShowWinningStreaksAsList(modifier = modifier, rows = (uiState as WinningStreakUiState.WinningStreak).list,
                onBack = onBack, onStartNewGame = onStartNewGame)
        }
        else -> {
            // NOOP
        }
    }

}

@Composable
private fun EmptyWinningStreakMessage(modifier: Modifier) {
    Box(modifier = modifier
        .fillMaxSize()
        .padding(16.dp),
        contentAlignment = Alignment.Center) {
        Text(stringResource(id = R.string.empty_winning_streaks), fontSize = 20.sp, maxLines = 3)
    }
}

@Composable
fun ShowWinningStreaksAsList(
    modifier: Modifier = Modifier,
    rows: List<ScoreTableRow>,
    onBack: () -> Unit,
    onStartNewGame: () -> Unit,
) {

    Column(modifier = modifier.padding(16.dp).fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(rows) { row ->
                RenderRow(Modifier.fillMaxWidth(), row)
            }
        }

    }
}

@Composable
fun RenderRow(modifier: Modifier = Modifier, row: ScoreTableRow) {
    ElevatedCard(
        modifier = modifier
            .padding(vertical = 6.dp)
            .fillMaxWidth()
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = "${row.correct} ${stringResource(R.string.correct)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32) // Success Green
                )
            },
            supportingContent = {
                Text(
                    text = "${row.wrong} ${stringResource(R.string.wrong)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            },
            trailingContent = {
                Text(
                    text = getDateString(row.playTime),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        )
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

