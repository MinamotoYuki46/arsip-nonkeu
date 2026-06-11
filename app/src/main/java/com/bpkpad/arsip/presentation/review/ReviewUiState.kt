package com.bpkpad.arsip.presentation.review

import com.bpkpad.arsip.domain.model.ArchiveDocument

data class ReviewUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val document: ArchiveDocument? = null
)
