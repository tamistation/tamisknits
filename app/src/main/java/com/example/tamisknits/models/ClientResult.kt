package com.example.tamisknits.models

sealed interface ClientResult<out T> {
    data object Loading : ClientResult<Nothing>
    data class Success<T>(val data: T, val isFromCache: Boolean = false) : ClientResult<T>
    data class Error(val message: String) : ClientResult<Nothing>
}

val <T> ClientResult<T>.dataOrNull: T?
    get() = (this as? ClientResult.Success)?.data

val ClientResult<*>.isFromCache: Boolean
    get() = (this as? ClientResult.Success)?.isFromCache == true
