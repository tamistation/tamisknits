package com.example.tamisknits.features.client.common

import com.example.tamisknits.features.client.data.repository.ClientAccountRepository
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.User
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ClientUserUseCase @Inject constructor(
    private val accountRepository: ClientAccountRepository
) {
    val currentUid: String
        get() = accountRepository.currentUid.orEmpty()

    val currentEmail: String
        get() = accountRepository.currentEmail

    fun getProfile(): Flow<ClientResult<User?>> = accountRepository.getProfile(currentUid)
}
