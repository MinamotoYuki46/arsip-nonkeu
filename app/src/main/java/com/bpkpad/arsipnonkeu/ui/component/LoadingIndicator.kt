package com.bpkpad.arsipnonkeu.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Reusable loading indicator with the brand's green color.
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF0D631B)
) {
    CircularProgressIndicator(
        modifier = modifier,
        color = color
    )
}

/**
 * Full screen loading indicator centered on the screen.
 */
@Composable
fun FullScreenLoading(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF0D631B)
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicator(color = color)
    }
}
