package com.bpkpad.arsip.data.remote

import com.bpkpad.arsip.data.remote.dto.ArchiveDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ArchiveApiService {

    @GET("archives")
    suspend fun getArchives(): Response<List<ArchiveDto>>

    @GET("archives/{id}")
    suspend fun getArchiveById(@Path("id") id: String): Response<ArchiveDto>

    @POST("archives")
    suspend fun saveArchive(@Body document: ArchiveDto): Response<Unit>

    @POST("parse-archive")
    suspend fun parseArchiveFromText(@Body rawText: Map<String, String>): Response<ArchiveDto>
}
