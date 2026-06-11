package com.bpkpad.arsip.core.di

import com.bpkpad.arsip.core.data.repository.AuthRepositoryImpl
import com.bpkpad.arsip.core.data.repository.StagingRepositoryImpl
import com.bpkpad.arsip.core.domain.repository.AuthRepository
import com.bpkpad.arsip.core.domain.repository.StagingRepository
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
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindStagingRepository(
        stagingRepositoryImpl: StagingRepositoryImpl
    ): StagingRepository
}