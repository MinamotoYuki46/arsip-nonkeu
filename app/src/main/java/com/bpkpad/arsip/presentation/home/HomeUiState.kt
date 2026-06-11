package com.bpkpad.arsip.presentation.home

import com.bpkpad.arsip.domain.model.ArchiveDocument

data class HomeUiState(
    val isLoading: Boolean = false,
    val archives: List<ArchiveDocument> = emptyList(),
    val error: String? = null,
    val exportFile: java.io.File? = null
)
