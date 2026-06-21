package com.bpkpad.arsipnonkeu.utils

import io.ktor.client.plugins.HttpRequestTimeoutException
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

object ErrorHandler {
    fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is UnknownHostException, is ConnectException -> {
                "Tidak dapat terhubung ke server. Silakan periksa koneksi internet Anda."
            }
            is HttpRequestTimeoutException, is TimeoutException -> {
                "Koneksi ke server terputus (timeout). Silakan coba lagi."
            }
            else -> {
                throwable.message ?: "Terjadi kesalahan yang tidak terduga."
            }
        }
    }
}
