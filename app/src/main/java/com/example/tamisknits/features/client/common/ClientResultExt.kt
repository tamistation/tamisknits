package com.example.tamisknits.features.client.common

import com.example.tamisknits.models.ClientResult

fun <T, R> ClientResult<T>.mapData(transform: (T) -> R): ClientResult<R> = when (this) {
    is ClientResult.Success -> ClientResult.Success(transform(data), isFromCache)
    is ClientResult.Error -> ClientResult.Error(message)
    ClientResult.Loading -> ClientResult.Loading
}

fun <A, B, R> combineResults(
    first: ClientResult<A>,
    second: ClientResult<B>,
    transform: (A, B) -> R
): ClientResult<R> = when {
    first is ClientResult.Error -> ClientResult.Error(first.message)
    second is ClientResult.Error -> ClientResult.Error(second.message)
    first is ClientResult.Success && second is ClientResult.Success -> ClientResult.Success(
        transform(first.data, second.data),
        first.isFromCache || second.isFromCache
    )

    else -> ClientResult.Loading
}
