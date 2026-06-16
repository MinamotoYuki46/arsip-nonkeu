package com.bpkpad.arsipnonkeu.di

import com.bpkpad.arsipnonkeu.BuildConfig
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

    private val archiveRepository: ArchiveRepository = ArchiveRepositoryImpl(supabaseClient)
    private val stagingRepository: StagingRepository = StagingRepositoryImpl(supabaseClient)
    private val archiveClassificationRepository: ArchiveClassificationRepository = 
        ArchiveClassificationRepositoryImpl(supabaseClient)

    private val authRemoteDataSource = AuthRemoteDataSource(supabaseClient)
    private val profileRemoteDataSource = ProfileRemoteDataSource(supabaseClient)
    private val activityLogRemoteDataSource = ActivityLogRemoteDataSource(supabaseClient)

    val authRepository: AuthRepository = AuthRepositoryImpl(authRemoteDataSource)
    val profileRepository: ProfileRepository = ProfileRepositoryImpl(profileRemoteDataSource)
    val activityLogRepository: ActivityLogRepository = ActivityLogRepositoryImpl(activityLogRemoteDataSource, authRemoteDataSource)

    val getArchiveYearSummariesUseCase =
        GetArchiveYearSummariesUseCase(archiveRepository)

    val getArchiveDocumentListItemsUseCase =
        GetArchiveDocumentListItemsUseCase(archiveRepository)

    val getArchiveDocumentDetailUseCase =
        GetArchiveDocumentDetailUseCase(archiveRepository)

    val archiveRepositoryInstance: ArchiveRepository = archiveRepository
    val stagingRepositoryInstance: StagingRepository = stagingRepository

    val getArchiveClassificationsUseCase =
        GetArchiveClassificationsUseCase(archiveClassificationRepository)
}
