package com.bpkpad.arsipnonkeu.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LocalPoppinsFont = FontFamily.Default

enum class BadgeVariant {
    SUCCESS, WARNING, DANGER, INFO, NEUTRAL
}

@Composable
fun ArsipBadge(
    text: String,
    variant: BadgeVariant = BadgeVariant.SUCCESS,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (variant) {
        BadgeVariant.SUCCESS -> Color(0xFFE8F5E9)
        BadgeVariant.WARNING -> Color(0xFFFFF3CD)
        BadgeVariant.DANGER -> Color(0xFFFEE2E2)
        BadgeVariant.INFO -> Color(0xFFE6F6FF)
        BadgeVariant.NEUTRAL -> Color(0xFFF3F4F6)
    }

    val textColor = when (variant) {
        BadgeVariant.SUCCESS -> Color(0xFF1B5E20)
        BadgeVariant.WARNING -> Color(0xFF92400E)
        BadgeVariant.DANGER -> Color(0xFF991B1B)
        BadgeVariant.INFO -> Color(0xFF0D631B)
        BadgeVariant.NEUTRAL -> Color(0xFF374151)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = LocalPoppinsFont,
            color = textColor
        )
    }
}
