package com.bpkpad.arsipnonkeu.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.UnfoldMore
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bpkpad.arsipnonkeu.domain.model.ArchiveClassification
import androidx.compose.material3.OutlinedTextFieldDefaults

@Composable
fun ArchiveClassificationField(
    selectedCode: String?,
    selectedLabel: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isRequired: Boolean = false,
    onClick: () -> Unit
) {
    val displayValue = when {
        !selectedLabel.isNullOrBlank() -> selectedLabel
        !selectedCode.isNullOrBlank() -> selectedCode
        else -> ""
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                enabled = enabled,
                onClick = onClick
            )
    ) {
        OutlinedTextField(
            value = displayValue,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            enabled = false, // Set to false to allow the parent Box to handle clicks
            readOnly = true,
            label = {
                Text(
                    text = if (isRequired) {
                        "Kode Klasifikasi Arsip *"
                    } else {
                        "Kode Klasifikasi Arsip"
                    }
                )
            },
            placeholder = {
                Text("Pilih kode klasifikasi arsip")
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Category,
                    contentDescription = null
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Rounded.UnfoldMore,
                    contentDescription = null
                )
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContainerColor = Color.Transparent
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveClassificationSelectorSheet(
    visible: Boolean,
    classifications: List<ArchiveClassification>,
    selectedCode: String?,
    keyword: String,
    isLoading: Boolean,
    onKeywordChange: (String) -> Unit,
    onSelect: (ArchiveClassification) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp
        )
    ) {
        ArchiveClassificationSelectorContent(
            classifications = classifications,
            selectedCode = selectedCode,
            keyword = keyword,
            isLoading = isLoading,
            onKeywordChange = onKeywordChange,
            onSelect = onSelect,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun ArchiveClassificationSelectorContent(
    classifications: List<ArchiveClassification>,
    selectedCode: String?,
    keyword: String,
    isLoading: Boolean,
    onKeywordChange: (String) -> Unit,
    onSelect: (ArchiveClassification) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 20.dp,
                end = 20.dp,
                bottom = 24.dp
            )
    ) {
        SheetHeader(
            onDismiss = onDismiss
        )

        OutlinedTextField(
            value = keyword,
            onValueChange = onKeywordChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (keyword.isNotBlank()) {
                    IconButton(
                        onClick = {
                            onKeywordChange("")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Hapus pencarian"
                        )
                    }
                }
            },
            singleLine = true,
            label = {
                Text("Cari kode atau nama klasifikasi")
            },
            placeholder = {
                Text("Contoh: 100.3.3, hukum, perjalanan")
            }
        )

        QuickCodeChips(
            onKeywordChange = onKeywordChange
        )

        if (isLoading) {
            LoadingContent()
        } else if (classifications.isEmpty()) {
            EmptyContent(
                keyword = keyword
            )
        } else {
            ClassificationList(
                classifications = classifications,
                selectedCode = selectedCode,
                onSelect = onSelect
            )
        }
    }
}

@Composable
private fun SheetHeader(
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 4.dp,
                bottom = 16.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Pilih Kode Klasifikasi",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Gunakan kode klasifikasi arsip non-keuangan.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(
            onClick = onDismiss
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Tutup"
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickCodeChips(
    onKeywordChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Kategori cepat",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickChip(
                text = "000 Umum",
                query = "000",
                onKeywordChange = onKeywordChange
            )

            QuickChip(
                text = "100 Pemerintahan",
                query = "100",
                onKeywordChange = onKeywordChange
            )

            QuickChip(
                text = "200 Politik",
                query = "200",
                onKeywordChange = onKeywordChange
            )

            QuickChip(
                text = "300 Keamanan dan Ketertiban",
                query = "300",
                onKeywordChange = onKeywordChange
            )

            QuickChip(
                text = "400 Kesra",
                query = "400",
                onKeywordChange = onKeywordChange
            )

            QuickChip(
                text = "500 Perekonomian",
                query = "500",
                onKeywordChange = onKeywordChange
            )

            QuickChip(
                text = "600 Pekerjaan Umum dan Ketenagaan",
                query = "600",
                onKeywordChange = onKeywordChange
            )

            QuickChip(
                text = "700 Pengawasan",
                query = "700",
                onKeywordChange = onKeywordChange
            )

            QuickChip(
                text = "800 Kepegawaian",
                query = "800",
                onKeywordChange = onKeywordChange
            )
        }
    }
}

@Composable
private fun QuickChip(
    text: String,
    query: String,
    onKeywordChange: (String) -> Unit
) {
    AssistChip(
        onClick = {
            onKeywordChange(query)
        },
        label = {
            Text(text)
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 220.dp)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()

        Spacer(modifier = Modifier.size(12.dp))

        Text(
            text = "Memuat kode klasifikasi...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyContent(
    keyword: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 220.dp)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Kode tidak ditemukan",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = if (keyword.isBlank()) {
                "Belum ada kode klasifikasi yang dapat ditampilkan."
            } else {
                "Tidak ada kode atau nama klasifikasi yang cocok dengan \"$keyword\"."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ClassificationList(
    classifications: List<ArchiveClassification>,
    selectedCode: String?,
    onSelect: (ArchiveClassification) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 460.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(
                items = classifications,
                key = { classification ->
                    classification.code
                }
            ) { classification ->
                ClassificationListItem(
                    classification = classification,
                    selected = classification.code.equals(
                        other = selectedCode,
                        ignoreCase = true
                    ),
                    onClick = {
                        onSelect(classification)
                    }
                )

                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun ClassificationListItem(
    classification: ArchiveClassification,
    selected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)
    } else {
        Color.Transparent
    }

    val badgeColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val badgeTextColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = containerColor,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 12.dp
                ),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier.width(74.dp),
                contentAlignment = Alignment.TopStart
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = badgeColor
                ) {
                    Text(
                        modifier = Modifier.padding(
                            horizontal = 9.dp,
                            vertical = 6.dp
                        ),
                        text = classification.code,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = badgeTextColor,
                        maxLines = 1
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${classification.code} - ${classification.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Medium
                    },
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                classification.parentCode?.let { parentCode ->
                    Text(
                        text = "Induk: $parentCode",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun ArchiveClassificationSelectorContentPreview() {
    MaterialTheme {
        ArchiveClassificationSelectorContent(
            classifications = listOf(
                ArchiveClassification(
                    code = "000",
                    name = "UMUM",
                    parentCode = null,
                    level = 1
                ),
                ArchiveClassification(
                    code = "000.1",
                    name = "KETATAUSAHAAN DAN KERUMAHTANGGAAN",
                    parentCode = "000",
                    level = 2
                ),
                ArchiveClassification(
                    code = "000.1.2",
                    name = "Perjalanan Dinas Dalam Negeri",
                    parentCode = "000.1",
                    level = 3
                ),
                ArchiveClassification(
                    code = "100.3.3",
                    name = "Keputusan/Ketetapan Pimpinan Pemerintah",
                    parentCode = "100.3",
                    level = 3
                )
            ),
            selectedCode = "000.1.2",
            keyword = "",
            isLoading = false,
            onKeywordChange = {},
            onSelect = {},
            onDismiss = {}
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 390
)
@Composable
private fun ArchiveClassificationFieldPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            ArchiveClassificationField(
                selectedCode = "100.3.3",
                selectedLabel = "100.3.3 - Keputusan/Ketetapan Pimpinan Pemerintah",
                onClick = {}
            )
        }
    }
}