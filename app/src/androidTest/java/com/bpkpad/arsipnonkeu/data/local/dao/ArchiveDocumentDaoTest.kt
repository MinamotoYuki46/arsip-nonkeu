package com.bpkpad.arsipnonkeu.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bpkpad.arsipnonkeu.data.local.database.AppDatabase
import com.bpkpad.arsipnonkeu.data.local.entity.ArchiveDocumentEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ArchiveDocumentDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ArchiveDocumentDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = db.archiveDocumentDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetArchiveDocument() = runBlocking {
        val document = ArchiveDocumentEntity(
            id = "test-id",
            documentType = "SURAT",
            title = "Test Archive",
            year = 2025,
            physicalForm = "SHEET",
            status = "AVAILABLE"
        )
        dao.insertArchiveDocument(document)
        
        val retrieved = dao.getArchiveDocumentById("test-id")
        assertEquals(document.title, retrieved?.title)
        assertEquals(document.year, retrieved?.year)
    }

    @Test
    fun deleteArchiveDocument() = runBlocking {
        val document = ArchiveDocumentEntity(
            id = "delete-id",
            documentType = "SURAT",
            title = "To Be Deleted",
            year = 2025,
            physicalForm = "SHEET",
            status = "AVAILABLE"
        )
        dao.insertArchiveDocument(document)
        dao.deleteArchiveDocument(document)
        
        val retrieved = dao.getArchiveDocumentById("delete-id")
        assertNull(retrieved)
    }

    @Test
    fun getArchiveDocumentsByYear() = runBlocking {
        val doc1 = ArchiveDocumentEntity(id = "1", title = "D1", year = 2025, documentType = "S", physicalForm = "S", status = "A")
        val doc2 = ArchiveDocumentEntity(id = "2", title = "D2", year = 2025, documentType = "S", physicalForm = "S", status = "A")
        val doc3 = ArchiveDocumentEntity(id = "3", title = "D3", year = 2024, documentType = "S", physicalForm = "S", status = "A")
        
        dao.insertArchiveDocuments(listOf(doc1, doc2, doc3))
        
        val docs2025 = dao.getArchiveDocumentsByYear(2025).first()
        assertEquals(2, docs2025.size)
    }

    @Test
    fun clearArchiveDocumentsByYear() = runBlocking {
        val doc1 = ArchiveDocumentEntity(id = "1", title = "D1", year = 2025, documentType = "S", physicalForm = "S", status = "A")
        dao.insertArchiveDocument(doc1)
        
        dao.clearArchiveDocumentsByYear(2025)
        
        val docs2025 = dao.getArchiveDocumentsByYear(2025).first()
        assertEquals(0, docs2025.size)
    }
}
