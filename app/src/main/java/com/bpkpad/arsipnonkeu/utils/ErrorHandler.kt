package com.bpkpad.arsipnonkeu.utils

import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

object ErrorHandler {
    fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is UnknownHostException, is ConnectException -> {
                "Tidak dapat terhubung ke server. Silakan periksa koneksi internet Anda."
            }
            is HttpRequestTimeoutException, is TimeoutException, is SocketTimeoutException -> {
                "Koneksi ke server terputus (timeout). Silakan coba lagi."
            }
            is ResponseException -> {
                when (throwable.response.status.value) {
                    401 -> "Sesi telah berakhir atau kredensial salah. Silakan login kembali."
                    403 -> "Anda tidak memiliki akses untuk melakukan aksi ini."
                    404 -> "Data tidak ditemukan di server."
                    500, 502, 503, 504 -> "Server sedang mengalami gangguan. Silakan coba beberapa saat lagi."
                    else -> "Kesalahan server (${throwable.response.status.value})."
                }
            }
            else -> {
                val message = throwable.message ?: "Terjadi kesalahan yang tidak terduga."
                if (message.contains("Unable to resolve host", ignoreCase = true)) {
                    "Tidak ada koneksi internet."
                } else {
                    message
                }
            }
        }
    }
}
