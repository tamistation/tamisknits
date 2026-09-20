package com.example.tamisknits.features.admin.userManagement.details

import com.example.tamisknits.models.User
import com.example.tamisknits.repository.FirebaseRepository
import javax.inject.Inject

class UserDetailsUseCase @Inject constructor(
    private val repository: FirebaseRepository
) {

    fun getUser(
        uid: String,
        onSuccess: (User) -> Unit,
        onFailure: (String) -> Unit
    ) {
        repository.getUser(
            uid = uid,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun updateUser(
        uid: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        repository.updateUser(
            uid = uid,
            updates = updates,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun deleteUser(
        uid: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        repository.deleteUser(
            uid = uid,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }
}