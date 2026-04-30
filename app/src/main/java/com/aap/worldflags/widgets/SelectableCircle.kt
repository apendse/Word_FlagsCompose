package com.aap.worldflags.widgets

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aap.worldflags.ui.theme.WorldFlagsTheme

private val CircleButtonDotSize = 12.dp
private val CirclePadding = 2.dp
private val CircleStrokeWidth = 2.dp

val MinSize = 20.0.dp
private val CircleAnimationDuration = 100

@Composable
fun SelectableCircleWidget(selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val dotRadius =
        animateDpAsState(
            targetValue = if (selected) CircleButtonDotSize / 2 else 0.dp,
            animationSpec = tween(durationMillis = CircleAnimationDuration)
        )
    val selectableModifier = Modifier.selectable(selected = selected,
        onClick = onClick,
        role = Role.RadioButton,
    )
    val primaryColor = MaterialTheme.colorScheme.primary
    Canvas(
        modifier
            .then(Modifier.minimumInteractiveComponentSize())
            .then(selectableModifier)
            .wrapContentSize(Alignment.Center)
            .padding(CirclePadding)
            .requiredSize(MinSize)
    ) {
        val strokeWidth = CircleStrokeWidth.toPx()

        drawCircle(
            primaryColor,
            radius = (MinSize / 2).toPx() - strokeWidth / 2,
            style = Stroke(strokeWidth)
        )
        if (dotRadius.value > 0.dp) {
            drawCircle(primaryColor, dotRadius.value.toPx() - strokeWidth / 2, style = Fill)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectableCircleWidgetPreview() {
    WorldFlagsTheme {
        Column(modifier = Modifier.width(50.dp).height(50.dp),
            verticalArrangement = Arrangement.SpaceBetween) {
            SelectableCircleWidget(
                selected = true,
                onClick = {},
                modifier = Modifier.width(22.dp).height(22.dp)
            )

            SelectableCircleWidget(
                selected = false,
                onClick = {},
                modifier = Modifier.width(22.dp).height(22.dp)
            )

        }

    }

}