package com.bpkpad.arsipnonkeu.di

import com.bpkpad.arsipnonkeu.BuildConfig
import com.bpkpad.arsipnonkeu.data.local.database.AppDatabase
import com.bpkpad.arsipnonkeu.data.local.datasource.ArchiveLocalDataSource
import com.bpkpad.arsipnonkeu.data.local.datasource.ProfileLocalDataSource
import com.bpkpad.arsipnonkeu.data.remote.datasource.ActivityLogRemoteDataSource
import com.bpkpad.arsipnonkeu.data.remote.datasource.AuthRemoteDataSource
import com.bpkpad.arsipnonkeu.data.remote.datasource.ProfileRemoteDataSource
import com.bpkpad.arsipnonkeu.data.repository.ActivityLogRepositoryImpl
import com.bpkpad.arsipnonkeu.data.repository.ArchiveClassificationRepositoryImpl
import com.bpkpad.arsipnonkeu.data.repository.ArchiveRepositoryImpl
import com.bpkpad.arsipnonkeu.data.repository.AuthRepositoryImpl
import com.bpkpad.arsipnonkeu.data.repository.ProfileRepositoryImpl
import com.bpkpad.arsipnonkeu.data.repository.StagingRepositoryImpl
import com.bpkpad.arsipnonkeu.domain.repository.ActivityLogRepository
import com.bpkpad.arsipnonkeu.domain.repository.ArchiveClassificationRepository
import com.bpkpad.arsipnonkeu.domain.repository.ArchiveRepository
import com.bpkpad.arsipnonkeu.domain.repository.AuthRepository
import com.bpkpad.arsipnonkeu.domain.repository.ProfileRepository
import com.bpkpad.arsipnonkeu.domain.repository.StagingRepository
import com.bpkpad.arsipnonkeu.domain.usecase.GetArchiveClassificationsUseCase
import com.bpkpad.arsipnonkeu.domain.usecase.GetArchiveDocumentDetailUseCase
import com.bpkpad.arsipnonkeu.domain.usecase.GetArchiveDocumentListItemsUseCase
import com.bpkpad.arsipnonkeu.domain.usecase.GetArchiveYearSummariesUseCase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest

object ArchiveModule {
    private lateinit var database: AppDatabase

    fun initialize(appDatabase: AppDatabase) {
        database = appDatabase
    }

    private val supabaseClient: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Postgrest)
            install(Auth)
            install(Functions)
        }
    }

    private val archiveLocalDataSource: ArchiveLocalDataSource by lazy {
        ArchiveLocalDataSource(database.archiveDocumentDao())
    }

    private val profileLocalDataSource: ProfileLocalDataSource by lazy {
        ProfileLocalDataSource(database.userProfileDao())
    }

    val archiveRepository: ArchiveRepository by lazy {
        ArchiveRepositoryImpl(supabaseClient, archiveLocalDataSource)
    }

    val stagingRepository: StagingRepository by lazy {
        StagingRepositoryImpl(supabaseClient)
    }

    val archiveClassificationRepository: ArchiveClassificationRepository by lazy {
        ArchiveClassificationRepositoryImpl(supabaseClient)
    }

    private val authRemoteDataSource by lazy { AuthRemoteDataSource(supabaseClient) }
    private val profileRemoteDataSource by lazy { ProfileRemoteDataSource(supabaseClient) }
    private val activityLogRemoteDataSource by lazy { ActivityLogRemoteDataSource(supabaseClient) }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authRemoteDataSource, profileLocalDataSource)
    }

    val profileRepository: ProfileRepository by lazy {
        ProfileRepositoryImpl(profileRemoteDataSource, profileLocalDataSource)
    }

    val activityLogRepository: ActivityLogRepository by lazy {
        ActivityLogRepositoryImpl(activityLogRemoteDataSource, authRemoteDataSource)
    }

    val getArchiveYearSummariesUseCase by lazy {
        GetArchiveYearSummariesUseCase(archiveRepository)
    }

    val getArchiveDocumentListItemsUseCase by lazy {
        GetArchiveDocumentListItemsUseCase(archiveRepository)
    }

    val getArchiveDocumentDetailUseCase by lazy {
        GetArchiveDocumentDetailUseCase(archiveRepository)
    }

    val archiveRepositoryInstance: ArchiveRepository get() = archiveRepository
    val stagingRepositoryInstance: StagingRepository get() = stagingRepository

    val getArchiveClassificationsUseCase by lazy {
        GetArchiveClassificationsUseCase(archiveClassificationRepository)
    }
}
