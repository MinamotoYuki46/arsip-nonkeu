package com.bpkpad.arsipnonkeu.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bpkpad.arsipnonkeu.data.local.database.AppDatabase
import com.bpkpad.arsipnonkeu.data.local.entity.UserProfileEntity
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
class UserProfileDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: UserProfileDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = db.userProfileDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetUserProfile() = runBlocking {
        val profile = UserProfileEntity(
            id = "user-1",
            username = "admin",
            name = "Administrator",
            role = "ARSIPARIS",
            createdAt = "2024-01-01",
            updatedAt = "2024-01-01"
        )
        dao.insertUserProfile(profile)
        
        val retrieved = dao.getUserProfile().first()
        assertEquals(profile.name, retrieved?.name)
        assertEquals(profile.role, retrieved?.role)
    }

    @Test
    fun clearUserProfile() = runBlocking {
        val profile = UserProfileEntity("u1", "n1", "n1", "r1", "c1", "u1")
        dao.insertUserProfile(profile)
        dao.clearUserProfile()
        
        val retrieved = dao.getUserProfile().first()
        assertNull(retrieved)
    }
}
