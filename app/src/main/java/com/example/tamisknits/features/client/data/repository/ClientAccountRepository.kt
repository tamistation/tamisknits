package com.example.tamisknits.features.client.data.repository

import com.example.tamisknits.models.ClientResult
import com.example.tamisknits.models.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ClientAccountRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    val currentUid: String?
        get() = auth.currentUser?.uid

    val currentEmail: String
        get() = auth.currentUser?.email.orEmpty()

    fun signOut() = auth.signOut()

    // FirebaseRepository.getCurrentUser() opens a nested listener per collection that is never removed,
    // so the client screens read the profile through this single, properly closed listener.
    fun getProfile(uid: String): Flow<ClientResult<User?>> = callbackFlow {
        trySend(ClientResult.Loading)
        if (uid.isBlank()) {
            trySend(ClientResult.Success(null))
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("User").document(uid)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    trySend(ClientResult.Error(error.message ?: "Couldn't load your profile"))
                    return@addSnapshotListener
                }
                val user = document?.takeIf { it.exists() }?.let { doc ->
                    User(
                        uid = doc.id,
                        name = doc.getString("name") ?: "",
                        email = doc.getString("email") ?: "",
                        phone = doc.getString("phone") ?: "",
                        status = doc.getString("status") ?: "active"
                    )
                }
                trySend(ClientResult.Success(user, document?.metadata?.isFromCache == true))
            }
        awaitClose { listener.remove() }
    }

    // Firebase rejects password changes and account deletion unless the user signed in recently,
    // so both flows re-authenticate with the current password first.
    private fun reauthenticate(
        currentPassword: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = auth.currentUser
        val email = user?.email
        if (user == null || email.isNullOrBlank()) {
            onFailure("You're not signed in")
            return
        }
        user.reauthenticate(EmailAuthProvider.getCredential(email, currentPassword))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { error ->
                onFailure(
                    if (error is FirebaseAuthInvalidCredentialsException) "Your current password is incorrect"
                    else error.message ?: "Couldn't verify your password"
                )
            }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        reauthenticate(currentPassword, onSuccess = {
            auth.currentUser?.updatePassword(newPassword)
                ?.addOnSuccessListener { onSuccess() }
                ?.addOnFailureListener { error ->
                    onFailure(
                        if (error is FirebaseAuthWeakPasswordException) "Please choose a stronger password"
                        else error.message ?: "Failed to change password"
                    )
                }
        }, onFailure = onFailure)
    }

    // Orders are kept for the shop's records; everything personal to the client is removed.
    fun deleteAccount(
        currentPassword: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        reauthenticate(currentPassword, onSuccess = {
            val uid =
                auth.currentUser?.uid ?: return@reauthenticate onFailure("You're not signed in")
            val userRef = firestore.collection("User").document(uid)

            val queries: List<Task<QuerySnapshot>> = listOf(
                firestore.collection("Cart").whereEqualTo("clientId", uid).get(),
                firestore.collection("Favorites").whereEqualTo("clientId", uid).get(),
                firestore.collection("Reviews").whereEqualTo("clientId", uid).get(),
                userRef.collection("addresses").get()
            )

            Tasks.whenAllSuccess<QuerySnapshot>(queries)
                .addOnSuccessListener { snapshots ->
                    val batch = firestore.batch()
                    snapshots.flatMap { it.documents }.forEach { batch.delete(it.reference) }
                    batch.delete(userRef)
                    batch.commit()
                        .addOnSuccessListener {
                            auth.currentUser?.delete()
                                ?.addOnSuccessListener { onSuccess() }
                                ?.addOnFailureListener {
                                    onFailure(
                                        it.message ?: "Failed to delete account"
                                    )
                                }
                        }
                        .addOnFailureListener {
                            onFailure(
                                it.message ?: "Failed to delete account data"
                            )
                        }
                }
                .addOnFailureListener { onFailure(it.message ?: "Failed to delete account data") }
        }, onFailure = onFailure)
    }
}
