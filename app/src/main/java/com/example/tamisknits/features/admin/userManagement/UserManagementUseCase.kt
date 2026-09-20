package com.example.tamisknits.features.admin.userManagement

import com.example.tamisknits.models.User
import com.example.tamisknits.models.UserTypeIds
import com.example.tamisknits.repository.FirebaseRepository
import javax.inject.Inject

class UserManagementUseCase @Inject constructor(
    private val repository: FirebaseRepository
) {

    companion object {
        private const val CLIENT_USER_TYPE_ID = UserTypeIds.CLIENT
        private const val DELIVERY_USER_TYPE_ID = UserTypeIds.DELIVERY
    }

    fun getClients(
        onSuccess: (List<User>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        repository.getUsersByType(
            usertypeId = CLIENT_USER_TYPE_ID,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun getDeliveryUsers(
        onSuccess: (List<User>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        repository.getUsersByType(
            usertypeId = DELIVERY_USER_TYPE_ID,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }
}