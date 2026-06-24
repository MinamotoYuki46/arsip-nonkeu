package com.bpkpad.arsipnonkeu.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bpkpad.arsipnonkeu.data.local.database.AppDatabase
import com.bpkpad.arsipnonkeu.data.local.entity.ArchiveClassificationEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ArchiveClassificationDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ArchiveClassificationDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = db.archiveClassificationDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetAllClassifications() = runBlocking {
        val classifications = listOf(
            ArchiveClassificationEntity("000", "UMUM", null, 1, true),
            ArchiveClassificationEntity("000.1", "KETATAUSAHAAN", "000", 2, true)
        )
        dao.insertClassifications(classifications)
        
        val retrieved = dao.getAllClassifications().first()
        assertEquals(2, retrieved.size)
        assertEquals("UMUM", retrieved.find { it.code == "000" }?.name)
    }

    @Test
    fun clearClassifications() = runBlocking {
        val classifications = listOf(
            ArchiveClassificationEntity("000", "UMUM", null, 1, true)
        )
        dao.insertClassifications(classifications)
        dao.clearClassifications()
        
        val retrieved = dao.getAllClassifications().first()
        assertEquals(0, retrieved.size)
    }
}
