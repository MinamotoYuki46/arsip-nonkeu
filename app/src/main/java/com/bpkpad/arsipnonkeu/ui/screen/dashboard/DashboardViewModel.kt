package com.bpkpad.arsipnonkeu.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpkpad.arsipnonkeu.di.ArchiveModule
import com.bpkpad.arsipnonkeu.domain.model.ArchiveYearSummary
import com.bpkpad.arsipnonkeu.domain.usecase.GetArchiveYearSummariesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class ArchiveYearDashboardUiState(
    val isLoading: Boolean = false,
    val years: List<ArchiveYearSummary> = emptyList(),
    val errorMessage: String? = null
)

class DashboardViewModel(
    private val getArchiveYearSummariesUseCase: GetArchiveYearSummariesUseCase =
        ArchiveModule.getArchiveYearSummariesUseCase
) : ViewModel(){
    private val _uiState = MutableStateFlow(ArchiveYearDashboardUiState())
    val uiState: StateFlow<ArchiveYearDashboardUiState> = _uiState.asStateFlow()

    fun loadYears() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val years = getArchiveYearSummariesUseCase()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    years = years
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Gagal memuat data tahun arsip"
                )
            }
        }
    }
}