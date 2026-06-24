package com.bpkpad.arsipnonkeu.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LocalPoppinsFont = FontFamily.Default

@Composable
fun ArsipSearchInput(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = keyword,
        onValueChange = onKeywordChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholder,
                fontSize = 14.sp,
                fontFamily = LocalPoppinsFont,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.Black
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color(0xFF707A6C)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(9999.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color(0xFF0D631B),
            unfocusedBorderColor = Color(0xFFBFCABA)
        )
    )
}
