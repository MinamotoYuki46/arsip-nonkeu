package com.bpkpad.arsipnonkeu

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bpkpad.arsipnonkeu.di.ArchiveModule
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test untuk memverifikasi koneksi ke Supabase dan ketersediaan data.
 * Pastikan perangkat/emulator terhubung ke internet.
 */
@RunWith(AndroidJUnit4::class)
class SupabaseConnectionTest {

    @Test
    fun testSupabaseConnectionAndData() = runBlocking {
        // Gunakan instance repository yang sudah ada di ArchiveModule
        val repository = ArchiveModule.archiveRepositoryInstance
        
        println("DEBUG: Memulai pengetesan koneksi Supabase...")
        
        try {
            val summaries = repository.getArchiveYearSummaries()
            
            println("DEBUG: Berhasil terhubung ke Supabase!")
            println("DEBUG: Jumlah ringkasan tahun ditemukan: ${summaries.size}")
            
            summaries.forEach { 
                println("DEBUG: Tahun ${it.year}: ${it.documentCount} dokumen")
            }
            
            // Verifikasi bahwa data tidak kosong jika memang sudah di-seed
            assertTrue("Data arsip kosong di hasil ringkasan! Pastikan seed SQL sudah dijalankan di database dan field 'year' serta 'deleted_at' sudah benar.", summaries.isNotEmpty())
            
        } catch (e: Exception) {
            println("DEBUG: Terjadi kesalahan saat mengambil data: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun testDirectPostgrestQuery() = runBlocking {
        // Ambil client secara refleksi untuk tes langsung (karena private di ArchiveModule)
        val supabase = ArchiveModule.javaClass.getDeclaredField("supabaseClient").let {
            it.isAccessible = true
            it.get(ArchiveModule) as io.github.jan.supabase.SupabaseClient
        }
        
        println("DEBUG: Menjalankan query langsung ke tabel archive_documents...")
        
        val response = supabase.postgrest["archive_documents"]
            .select(columns = Columns.raw("id, title, year")) {
                limit(1)
            }
        
        // Decode ke map untuk melihat isinya tanpa butuh DTO lengkap
        val data = response.decodeList<Map<String, String>>()
        
        println("DEBUG: Hasil query langsung: $data")
        
        assertNotNull("Response data null", data)
        assertTrue("Tabel archive_documents kosong!", data.isNotEmpty())
    }
}
