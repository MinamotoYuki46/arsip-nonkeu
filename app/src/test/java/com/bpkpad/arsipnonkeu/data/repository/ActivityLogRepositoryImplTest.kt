package com.bpkpad.arsipnonkeu.data.repository

import com.bpkpad.arsipnonkeu.data.remote.datasource.ActivityLogRemoteDataSource
import com.bpkpad.arsipnonkeu.data.remote.datasource.AuthRemoteDataSource
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.*

class ActivityLogRepositoryImplTest : BehaviorSpec({

    val remoteDataSource = mockk<ActivityLogRemoteDataSource>()
    val authRemoteDataSource = mockk<AuthRemoteDataSource>()

    given("ActivityLogRepositoryImpl") {
        val repository = ActivityLogRepositoryImpl(remoteDataSource, authRemoteDataSource)

        `when`("createActivityLog is called") {
            every { authRemoteDataSource.getCurrentUserId() } returns "user-123"
            coEvery { remoteDataSource.createActivityLog(any()) } just Runs
            
            repository.createActivityLog("ACTION", "archive", "id-1", "metadata")
            
            then("it should get current user id and call remote data source") {
                verify { authRemoteDataSource.getCurrentUserId() }
                coVerify { remoteDataSource.createActivityLog(any()) }
            }
        }
    }
})
