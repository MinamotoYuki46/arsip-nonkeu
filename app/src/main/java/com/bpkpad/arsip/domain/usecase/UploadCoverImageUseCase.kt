package com.bpkpad.arsip.domain.usecase

import com.bpkpad.arsip.domain.repository.FileRepository
import com.bpkpad.arsip.utils.ResultState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UploadCoverImageUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    operator fun invoke(imageBytes: ByteArray): Flow<ResultState<String>> {
        return fileRepository.uploadImage(imageBytes)
    }
}
