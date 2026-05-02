package com.aap.worldflags.widgets

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AssistChipDefaults.IconSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

private val RadioButtonPadding = 2.dp
private val iconSize = 20.dp
private val RadioAnimationDuration = 100


/**
 * Radio button that notifies that its animation to render the selection is complete
 */
@Composable
fun RadioWithDrawnCallback(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    onSelectionCompleted: () -> Unit
) {
    val dotRadius =
        animateDpAsState(
            targetValue = if (selected) 12.dp / 2 else 0.dp,
            animationSpec = tween(durationMillis = RadioAnimationDuration),
            finishedListener = {op -> onSelectionCompleted()}
        )

    val selectableModifier =
        Modifier.selectable(
            selected = selected,
            onClick = onClick,
            enabled = true,
            role = Role.RadioButton,
            interactionSource =  remember { MutableInteractionSource() },
            indication = null
                //ripple(true, 20.dp,)
        )
    val drawColor = color
    Canvas(modifier = modifier
        .then(
            Modifier.minimumInteractiveComponentSize()
        )
        .then(selectableModifier)
        .wrapContentSize(Alignment.Center)
        .padding(RadioButtonPadding)
        .requiredSize(iconSize)
    ) {
        val strokeWidth = 2.dp.toPx()
        drawCircle(
            drawColor,
            radius = (IconSize / 2).toPx() - strokeWidth / 2,
            style = Stroke(strokeWidth)
        )
        if (dotRadius.value > 0.dp) {
            drawCircle(drawColor, dotRadius.value.toPx() - strokeWidth / 2, style = Fill)
        }
    }
}