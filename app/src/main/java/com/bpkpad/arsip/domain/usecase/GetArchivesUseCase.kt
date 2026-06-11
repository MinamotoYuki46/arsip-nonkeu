package com.bpkpad.arsip.domain.usecase

import com.bpkpad.arsip.domain.model.ArchiveDocument
import com.bpkpad.arsip.domain.repository.ArchiveRepository
import com.bpkpad.arsip.utils.ResultState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetArchivesUseCase @Inject constructor(
    private val archiveRepository: ArchiveRepository
) {
    operator fun invoke(): Flow<ResultState<List<ArchiveDocument>>> {
        return archiveRepository.getArchives()
    }
}
