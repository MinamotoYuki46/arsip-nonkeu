package com.bpkpad.arsipnonkeu.di

import com.bpkpad.arsipnonkeu.BuildConfig
import com.bpkpad.arsipnonkeu.data.repository.ArchiveClassificationRepositoryImpl
import com.bpkpad.arsipnonkeu.data.repository.ArchiveRepositoryImpl
import com.bpkpad.arsipnonkeu.data.repository.StagingRepositoryImpl
import com.bpkpad.arsipnonkeu.domain.repository.ArchiveClassificationRepository
import com.bpkpad.arsipnonkeu.domain.repository.ArchiveRepository
import com.bpkpad.arsipnonkeu.domain.repository.StagingRepository
import com.bpkpad.arsipnonkeu.domain.usecase.GetArchiveClassificationsUseCase
import com.bpkpad.arsipnonkeu.domain.usecase.GetArchiveDocumentDetailUseCase
import com.bpkpad.arsipnonkeu.domain.usecase.GetArchiveDocumentListItemsUseCase
import com.bpkpad.arsipnonkeu.domain.usecase.GetArchiveYearSummariesUseCase
import io.github.jan.supabase.SupabaseClient
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
            install(Functions)
        }
    }

    private val archiveRepository: ArchiveRepository = ArchiveRepositoryImpl(supabaseClient)
    private val stagingRepository: StagingRepository = StagingRepositoryImpl(supabaseClient)
    private val archiveClassificationRepository: ArchiveClassificationRepository = 
        ArchiveClassificationRepositoryImpl(supabaseClient)

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
