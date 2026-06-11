package com.bpkpad.arsip.domain.repository

import com.bpkpad.arsip.utils.ResultState
import kotlinx.coroutines.flow.Flow

interface FileRepository {
    fun uploadImage(imageBytes: ByteArray): Flow<ResultState<String>>
    fun extractTextFromImage(imageBytes: ByteArray): Flow<ResultState<String>>
}
