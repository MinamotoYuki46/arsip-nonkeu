package com.bpkpad.arsip.presentation.scan

import com.bpkpad.arsip.domain.model.ArchiveDocument

data class ScanUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val parsedDocument: ArchiveDocument? = null
)
