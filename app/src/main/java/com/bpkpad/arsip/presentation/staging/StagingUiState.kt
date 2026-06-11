package com.bpkpad.arsip.presentation.staging

import com.bpkpad.arsip.core.domain.model.StagingDocument

data class StagingUiState(
    val documents: List<StagingDocument> = emptyList(),
    val boxId: String = "",
    val locationId: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
