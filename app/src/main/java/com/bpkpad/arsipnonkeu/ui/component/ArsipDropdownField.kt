package com.bpkpad.arsipnonkeu.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LocalPoppinsFont = FontFamily.Default

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ArsipDropdownField(
    label: String,
    value: T?,
    options: List<T>,
    optionLabel: (T) -> String,
    onValueChange: (T?) -> Unit,
    modifier: Modifier = Modifier,
    allowNull: Boolean = false,
    nullLabel: String = "Tidak diketahui",
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = {
            if (enabled) expanded = !expanded
        },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value?.let { optionLabel(it) } ?: nullLabel,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = {
                Text(
                    text = label,
                    color = Color.Black
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            textStyle = LocalTextStyle.current.copy(
                color = Color.Black,
                fontFamily = LocalPoppinsFont,
                fontSize = 14.sp
            ),
            colors = arsipTextFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = {
                expanded = false
            },
            containerColor = Color.White,
        ) {
            if (allowNull) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = nullLabel,
                            color = Color.Black,
                            fontFamily = LocalPoppinsFont
                        )
                    },
                    onClick = {
                        onValueChange(null)
                        expanded = false
                    }
                )
            }

            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = optionLabel(option),
                            color = Color.Black,
                            fontFamily = LocalPoppinsFont
                        )
                    },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
