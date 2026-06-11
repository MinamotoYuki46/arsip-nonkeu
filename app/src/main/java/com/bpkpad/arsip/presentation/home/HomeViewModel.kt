package com.bpkpad.arsip.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpkpad.arsip.domain.usecase.ExcelExportUseCase
import com.bpkpad.arsip.domain.usecase.ExcelImportUseCase
import com.bpkpad.arsip.domain.usecase.GetArchivesUseCase
import com.bpkpad.arsip.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import android.content.Context
import android.net.Uri
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getArchivesUseCase: GetArchivesUseCase,
    private val excelExportUseCase: ExcelExportUseCase,
    private val excelImportUseCase: ExcelImportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        getArchives()
    }

    fun getArchives() {
        viewModelScope.launch {
            getArchivesUseCase().collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is ResultState.Success -> {
                        _uiState.update { it.copy(isLoading = false, archives = result.data) }
                    }
                    is ResultState.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            }
        }
    }

    fun importFromExcel(context: Context, uri: Uri) {
        viewModelScope.launch {
            excelImportUseCase(context, uri).collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is ResultState.Success -> {
                        // Success count: result.data
                        _uiState.update { it.copy(isLoading = false) }
                        // Navigate to staging after import
                    }
                    is ResultState.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            }
        }
    }

    fun exportToExcel(filesDir: File) {
        val documents = _uiState.value.archives
        if (documents.isEmpty()) return

        val filePath = "${filesDir.absolutePath}/archives_${System.currentTimeMillis()}.xlsx"
        viewModelScope.launch {
            excelExportUseCase(documents, filePath).collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is ResultState.Success -> {
                        _uiState.update { it.copy(isLoading = false, exportFile = result.data) }
                    }
                    is ResultState.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            }
        }
    }

    fun clearExportFile() {
        _uiState.update { it.copy(exportFile = null) }
    }
}
