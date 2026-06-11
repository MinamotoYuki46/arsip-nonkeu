package com.bpkpad.arsip.domain.usecase

import com.bpkpad.arsip.domain.model.ArchiveDocument
import com.bpkpad.arsip.domain.repository.ArchiveRepository
import com.bpkpad.arsip.utils.ResultState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetArchiveByIdUseCase @Inject constructor(
    private val repository: ArchiveRepository
) {
    operator fun invoke(id: String): Flow<ResultState<ArchiveDocument>> {
        return repository.getArchiveById(id)
    }
}
