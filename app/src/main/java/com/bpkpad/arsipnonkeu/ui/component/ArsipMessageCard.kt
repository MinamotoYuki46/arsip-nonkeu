package com.bpkpad.arsipnonkeu.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LocalPoppinsFont = FontFamily.Default

enum class MessageVariant {
    SUCCESS, WARNING, DANGER
}

@Composable
fun ArsipMessageCard(
    message: String,
    variant: MessageVariant = MessageVariant.DANGER,
    onDismiss: (() -> Unit)? = null,
    onConfirm: (() -> Unit)? = null,
    confirmText: String? = null,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, borderColor, textColor) = when (variant) {
        MessageVariant.SUCCESS -> Triple(Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFF1B5E20))
        MessageVariant.WARNING -> Triple(Color(0xFFFFF7ED), Color(0xFFFED7AA), Color(0xFF9A3412))
        MessageVariant.DANGER -> Triple(Color(0xFFFEE2E2), Color(0xFFFECACA), Color(0xFF991B1B))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = LocalPoppinsFont,
                color = textColor,
                modifier = Modifier.weight(1f)
            )

            if (onDismiss != null) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Tutup",
                    tint = textColor,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(onClick = onDismiss)
                )
            }
        }

        if (onConfirm != null && confirmText != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = confirmText,
                    modifier = Modifier
                        .clickable(onClick = onConfirm)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = LocalPoppinsFont,
                    color = textColor
                )
            }
        }
    }
}
