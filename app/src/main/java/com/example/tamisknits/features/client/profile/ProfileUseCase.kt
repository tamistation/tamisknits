package com.example.tamisknits.features.client.profile

import com.example.tamisknits.features.client.data.preferences.ClientPreferences
import com.example.tamisknits.models.ClientOrder
import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.SavedAddress
import com.example.tamisknits.repository.ClientAccountRepository
import com.example.tamisknits.repository.ClientAddressRepository
import com.example.tamisknits.repository.ClientOrderRepository
import com.example.tamisknits.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProfileUseCase @Inject constructor(
    private val accountRepository: ClientAccountRepository,
    private val addressRepository: ClientAddressRepository,
    private val orderRepository: ClientOrderRepository,
    private val repository: UserRepository,
    private val preferences: ClientPreferences
) {
    fun getOrders(clientId: String): Flow<ClientResult<List<ClientOrder>>> =
        orderRepository.getClientOrders(clientId)

    fun getAvatarId(clientId: String): Flow<String> = preferences.avatarId(clientId)

    suspend fun setAvatarId(clientId: String, avatarId: String) =
        preferences.setAvatarId(clientId, avatarId)

    // Only fields the client owns are written; userType and status stay as the admin set them.
    fun updateProfile(
        clientId: String,
        name: String,
        phone: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) = repository.updateUser(
        clientId,
        mapOf("name" to name, "phone" to phone),
        onSuccess,
        onFailure
    )

    fun signOut() = accountRepository.signOut()

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) = accountRepository.changePassword(currentPassword, newPassword, onSuccess, onFailure)

    fun deleteAccount(
        currentPassword: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) = accountRepository.deleteAccount(currentPassword, onSuccess, onFailure)

    suspend fun clearLocalData(clientId: String) = preferences.clear(clientId)

    fun getAddresses(clientId: String): Flow<ClientResult<List<SavedAddress>>> =
        addressRepository.getAddresses(clientId)

    fun saveAddress(
        clientId: String,
        address: SavedAddress,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) = addressRepository.saveAddress(clientId, address, onSuccess, onFailure)

    fun setDefaultAddress(
        clientId: String,
        addressId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) = addressRepository.setDefault(clientId, addressId, onSuccess, onFailure)

    fun deleteAddress(
        clientId: String,
        addressId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) = addressRepository.deleteAddress(clientId, addressId, onSuccess, onFailure)

    fun isValidPhone(phone: String): Boolean {
        val digits = phone.count { it.isDigit() }
        return digits in 7..15 && phone.all { it.isDigit() || it in "+-() " }
    }
}
