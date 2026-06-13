package com.bpkpad.arsipnonkeu.ui.screen.staging

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bpkpad.arsipnonkeu.data.local.StagingDraftLocalDataSource
import com.bpkpad.arsipnonkeu.data.repository.StagingDraftRepository
import com.bpkpad.arsipnonkeu.di.ArchiveModule

class StagingViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val localDataSource = StagingDraftLocalDataSource(context)
        val repository = StagingDraftRepository(localDataSource)
        val archiveRepository = ArchiveModule.archiveRepositoryInstance

        return StagingViewModel(repository, archiveRepository) as T
    }
}