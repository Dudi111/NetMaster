package com.smartnet.analyzer.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartnet.analyzer.common.theme.colorAccent
import com.smartnet.analyzer.common.theme.transparent

/**
 * MyProgressBar: This method contains view of progress bar
 */
@Composable
fun MyProgressBar(isVisible: MutableState<Boolean>, barText: String = "") {
    val interactionSource = remember { MutableInteractionSource() }
    AnimatedVisibility(
        visible = isVisible.value
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(color = transparent)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(50.dp)
                    .height(50.dp),
                color = colorAccent,
                strokeWidth = Dp(value = 4F)
            )

            Text(
                text = barText,
                fontSize = 10.sp
            )
        }
    }
}