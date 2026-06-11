package com.bpkpad.arsip.di

import com.bpkpad.arsip.data.repository.ArchiveRepositoryImpl
import com.bpkpad.arsip.data.repository.FileRepositoryImpl
import com.bpkpad.arsip.domain.repository.ArchiveRepository
import com.bpkpad.arsip.domain.repository.FileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindArchiveRepository(
        archiveRepositoryImpl: ArchiveRepositoryImpl
    ): ArchiveRepository

    @Binds
    @Singleton
    abstract fun bindFileRepository(
        fileRepositoryImpl: FileRepositoryImpl
    ): FileRepository
}
