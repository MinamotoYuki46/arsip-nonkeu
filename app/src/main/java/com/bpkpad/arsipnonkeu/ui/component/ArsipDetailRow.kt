package com.bpkpad.arsipnonkeu.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LocalPoppinsFont = FontFamily.Default

@Composable
fun ArsipDetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 4
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = LocalPoppinsFont,
            color = Color(0xFF707A6C),
            letterSpacing = 0.48.sp
        )

        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = LocalPoppinsFont,
            color = Color(0xFF071E27),
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}
