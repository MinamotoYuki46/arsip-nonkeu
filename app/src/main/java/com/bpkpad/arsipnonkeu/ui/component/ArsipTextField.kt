package com.bpkpad.arsipnonkeu.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LocalPoppinsFont = FontFamily.Default

@Composable
fun arsipTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    disabledTextColor = Color.Black,

    focusedLabelColor = Color.Black,
    unfocusedLabelColor = Color.Black,
    disabledLabelColor = Color.Black,

    focusedPlaceholderColor = Color.Black,
    unfocusedPlaceholderColor = Color.Black,
    disabledPlaceholderColor = Color.Black,

    focusedBorderColor = Color.Black,
    unfocusedBorderColor = Color.Black,
    disabledBorderColor = Color.Black,

    cursorColor = Color.Black,

    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent
)

@Composable
fun ArsipTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(text = label, color = if (isError) MaterialTheme.colorScheme.error else Color.Black) },
        placeholder = placeholder?.let { { Text(text = it, color = Color.Black) } },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        isError = isError,
        readOnly = readOnly,
        enabled = enabled,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else minLines,
        shape = RoundedCornerShape(16.dp),
        colors = arsipTextFieldColors(),
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        textStyle = LocalTextStyle.current.copy(
            color = Color.Black,
            fontFamily = LocalPoppinsFont,
            fontSize = 14.sp
        )
    )
}
