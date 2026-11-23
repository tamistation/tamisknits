package com.example.tamisknits.features.client.common

import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.User
import com.example.tamisknits.repository.ClientAccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ClientUserUseCase @Inject constructor(
    private val accountRepository: ClientAccountRepository
) {
    val currentUid: String
        get() = accountRepository.currentUid.orEmpty()

    val currentEmail: String
        get() = accountRepository.currentEmail

    fun getProfile(): Flow<ClientResult<User?>> = accountRepository.getProfile(currentUid)
}
