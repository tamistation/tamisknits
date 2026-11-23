package com.example.tamisknits.features.account.password

import com.example.tamisknits.repository.ClientAccountRepository
import javax.inject.Inject

// Shared by every signed-in role (client, delivery). The repository re-authenticates with the
// current password first, so it works for any email/password FirebaseAuth user.
class ChangePasswordUseCase @Inject constructor(
    private val accountRepository: ClientAccountRepository
) {
    fun changePassword(
        currentPassword: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) = accountRepository.changePassword(currentPassword, newPassword, onSuccess, onFailure)
}
