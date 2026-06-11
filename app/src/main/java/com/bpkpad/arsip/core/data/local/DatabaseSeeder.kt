package com.bpkpad.arsip.core.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bpkpad.arsip.core.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Provider

class DatabaseSeeder(
    private val databaseProvider: Provider<AppDatabase>
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // Seeding data using a Coroutine when database is created for the first time
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialData()
        }
    }

    private suspend fun seedInitialData() {
        val db = databaseProvider.get()
        val tempDao = db.tempDocumentDao()
        val userDao = db.userDao()
        val locationDao = db.storageLocationDao()
        val archiveDao = db.archiveDocumentDao()
        val storingDao = db.storingDao()

        // 1. Seed Users
        val adminId = UUID.randomUUID().toString()
        userDao.insert(
            UserEntity(
                id = adminId,
                name = "Admin BPKPAD",
                username = "admin",
                role = "ADMIN",
                instance = "BPKPAD",
                password = "admin123"
            )
        )

        userDao.insert(
            UserEntity(
                id = UUID.randomUUID().toString(),
                name = "Arsiparis BPKPAD",
                username = "arsiparis",
                role = "ARSIPARIS",
                instance = "BPKPAD",
                password = "arsiparis123"
            )
        )

        userDao.insert(
            UserEntity(
                id = UUID.randomUUID().toString(),
                name = "Kepala Bidang BPKPAD",
                username = "kabid",
                role = "KABID",
                instance = "BPKPAD",
                password = "kabid123"
            )
        )

        // 2. Seed Storage Location
        val locationId = UUID.randomUUID().toString()
        locationDao.insert(
            StorageLocationEntity(
                id = locationId,
                room = "Ruang Arsip Lt.1",
                shelves = "Rak A",
                number = "01"
            )
        )

        // 3. Seed Archived Document (Simulating already synced data)
        val archiveId = UUID.randomUUID().toString()
        archiveDao.insert(
            ArchiveDocumentEntity(
                id = archiveId,
                type = "SURAT",
                title = "Laporan Keuangan Tahunan 2022",
                year = 2022,
                condition = "BAIK",
                instance = "BPKPAD",
                metadata = "{\"letter_number\":\"LK/2022/01\",\"subject\":\"Laporan Keuangan\",\"sender\":\"Bidang Keuangan\"}",
                timestampUserId = adminId
            )
        )

        // 4. Seed Storing (Linking Archive to Location)
        storingDao.insert(
            StoringEntity(
                id = UUID.randomUUID().toString(),
                idStorageLocation = locationId,
                idArchiveDocuments = archiveId,
                idUser = adminId
            )
        )
        
        // 5. Seed Temp Documents (Local Drafts)
        val dummyDocuments = listOf(
            TempDocumentEntity(
                id = UUID.randomUUID().toString(),
                type = "SURAT",
                title = "Surat Keputusan Pengangkatan 2023",
                year = 2023,
                condition = "BAIK",
                instance = "BPKPAD",
                metadata = "{\"letter_number\":\"001/SK/2023\",\"subject\":\"Pengangkatan Pegawai\",\"sender\":\"Kepala BPKPAD\"}",
                coverLocalPath = null,
                status = "LOCAL_ONLY"
            ),
            TempDocumentEntity(
                id = UUID.randomUUID().toString(),
                type = "PERDA",
                title = "Peraturan Daerah No 5 Tahun 2022",
                year = 2022,
                condition = "BAIK",
                instance = "BPKPAD",
                metadata = "{\"regulation_number\":\"5/2022\",\"subject\":\"Pengelolaan Keuangan Daerah\"}",
                coverLocalPath = null,
                status = "LOCAL_ONLY"
            ),
            TempDocumentEntity(
                id = UUID.randomUUID().toString(),
                type = "PERBUP",
                title = "Peraturan Bupati No 12 Tahun 2024",
                year = 2024,
                condition = "BAIK",
                instance = "BPKPAD",
                metadata = "{\"regulation_number\":\"12/2024\",\"subject\":\"Tata Naskah Dinas\"}",
                coverLocalPath = null,
                status = "LOCAL_ONLY"
            )
        )
        
        dummyDocuments.forEach { tempDao.insert(it) }
    }
}