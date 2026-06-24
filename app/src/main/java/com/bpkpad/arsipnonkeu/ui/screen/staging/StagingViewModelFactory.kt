package com.bpkpad.arsipnonkeu.ui.screen.staging

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bpkpad.arsipnonkeu.di.ArchiveModule

class StagingViewModelFactory(
    private val context: Context,
    private val year: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val stagingRepository = ArchiveModule.stagingRepositoryInstance
        val archiveRepository = ArchiveModule.archiveRepositoryInstance

        return StagingViewModel(year, stagingRepository, archiveRepository) as T
    }
}
