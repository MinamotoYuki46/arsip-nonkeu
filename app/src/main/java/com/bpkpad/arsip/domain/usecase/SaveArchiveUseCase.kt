package com.bpkpad.arsip.domain.usecase

import com.bpkpad.arsip.domain.model.ArchiveDocument
import com.bpkpad.arsip.domain.repository.ArchiveRepository
import com.bpkpad.arsip.utils.ResultState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SaveArchiveUseCase @Inject constructor(
    private val archiveRepository: ArchiveRepository
) {
    operator fun invoke(document: ArchiveDocument): Flow<ResultState<Unit>> {
        return archiveRepository.saveArchive(document)
    }
}
