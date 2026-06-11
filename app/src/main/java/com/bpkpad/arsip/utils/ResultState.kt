package com.bpkpad.arsip.utils

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class ResultState<out T> {
    data class Success<out T>(val data: T) : ResultState<T>()
    data class Error(val message: String, val exception: Throwable? = null) : ResultState<Nothing>()
    object Loading : ResultState<Nothing>()
}
