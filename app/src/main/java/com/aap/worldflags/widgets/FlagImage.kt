package com.aap.worldflags.widgets

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aap.worldflags.R
import com.aap.worldflags.ui.theme.WorldFlagsTheme

@Composable
fun FlagImage(modifier: Modifier, drawable: Int) {
    val painter = painterResource(id = drawable)
    val size = painter.intrinsicSize
    //val aspect = size.width / size.height
    val separatorColor = if (isSystemInDarkTheme()) {
        Color.DarkGray
    } else {
        Color.LightGray
    }
    Box(modifier.background(separatorColor).padding(4.dp), contentAlignment = Alignment.Center) {
        Image(
            modifier = Modifier
                .fillMaxSize(),

            alignment = Alignment.Center,
            painter = painter,
            contentDescription = stringResource(id = R.string.image_desc)
        )
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
            FlagImage(Modifier, R.drawable.np)
        }
    }
}
