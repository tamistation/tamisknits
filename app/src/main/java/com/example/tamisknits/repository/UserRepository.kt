package com.example.tamisknits.repository

import com.example.tamisknits.models.Permissions
import com.example.tamisknits.models.User
import com.example.tamisknits.models.UserType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


class UserRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {


    fun addPermission(
        permission: Permissions,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val data = hashMapOf(
            "name" to permission.name,
            "description" to permission.description
        )
        firestore.collection("Permissions").add(data)
            .addOnSuccessListener { doc -> onSuccess(doc.id) }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add permission") }
    }


    fun getPermissions(): Flow<List<Permissions>> = callbackFlow {
        val listener = firestore.collection("Permissions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList()); return@addSnapshotListener
                }
                val list = snapshot?.documents?.map { doc ->
                    Permissions(
                        permissionId = doc.id,
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: ""
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }


    fun getPermissionsByIds(
        ids: List<String>,
        onSuccess: (List<Permissions>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (ids.isEmpty()) {
            onSuccess(emptyList())
            return
        }
        firestore.collection("Permissions")
            .whereIn(com.google.firebase.firestore.FieldPath.documentId(), ids)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.map { doc ->
                    Permissions(
                        permissionId = doc.id,
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: ""
                    )
                }
                onSuccess(list)
            }
            .addOnFailureListener { onFailure(it.message ?: "Failed to get permissions") }
    }

    // delete permission
    fun deletePermission(permissionId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("Permissions").document(permissionId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to delete permission") }
    }


    fun addUserType(
        userType: UserType,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val data = hashMapOf(
            "type" to userType.type,
            "permissionIds" to userType.permissionIds
        )
        firestore.collection("UserType").add(data)
            .addOnSuccessListener { doc -> onSuccess(doc.id) }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add user type") }
    }

    // get user type
    fun getUserType(
        usertypeId: String,
        onSuccess: (UserType) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (usertypeId.isEmpty()) {
            onSuccess(UserType())
            return
        }
        firestore.collection("UserType").document(usertypeId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    onSuccess(
                        UserType(
                            usertypeId = doc.id,
                            type = doc.getString("type") ?: "",
                            permissionIds = (doc.get("permissionIds") as? List<String>)
                                ?: emptyList()
                        )
                    )
                } else {
                    onFailure("User type not found")
                }
            }
            .addOnFailureListener { onFailure(it.message ?: "Failed to get user type") }
    }


    fun updateUserType(
        usertypeId: String,
        newType: String,
        newPermissionIds: List<String>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("UserType").document(usertypeId)
            .update(mapOf("type" to newType, "permissionIds" to newPermissionIds))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to update user type") }
    }

    // delete user type
    fun deleteUserType(usertypeId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("UserType").document(usertypeId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to delete user type") }
    }


    // get current logged in user
    fun getCurrentUser(): Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                trySend(null)
            } else {
                firestore.collection("User")
                    .document(firebaseUser.uid)
                    .addSnapshotListener { document, error ->
                        if (error != null) {
                            trySend(null); return@addSnapshotListener
                        }

                        if (document != null && document.exists()) {
                            val usertypeId = document.getString("usertypeId") ?: ""
                            getUserType(
                                usertypeId,
                                onSuccess = { userType ->
                                    val user = User(
                                        uid = document.id,
                                        name = document.getString("name") ?: "",
                                        email = document.getString("email") ?: "",
                                        phone = document.getString("phone") ?: "",
                                        userType = userType,
                                        status = document.getString("status") ?: "active"
                                    )
                                    trySend(user)
                                },
                                onFailure = {
                                    // still return the user even if usertype lookup fails,
                                    // just with an empty UserType
                                    trySend(
                                        User(
                                            uid = document.id,
                                            name = document.getString("name") ?: "",
                                            email = document.getString("email") ?: "",
                                            phone = document.getString("phone") ?: "",
                                            userType = UserType()
                                        )
                                    )
                                }
                            )
                        } else {
                            trySend(null)
                        }
                    }
            }
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose { auth.removeAuthStateListener(authStateListener) }
    }


    fun addUser(
        user: User,
        usertypeId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val data = hashMapOf(
            "uid" to user.uid,
            "name" to user.name,
            "email" to user.email,
            "phone" to user.phone,
            "usertypeId" to usertypeId,
            "createdAt" to FieldValue.serverTimestamp(),
            "status" to user.status,
        )
        firestore.collection("User").document(user.uid)
            .set(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add user") }
    }


    fun getUser(uid: String, onSuccess: (User) -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("User").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val usertypeId = doc.getString("usertypeId") ?: ""
                    getUserType(
                        usertypeId,
                        onSuccess = { userType ->
                            onSuccess(
                                User(
                                    uid = doc.id,
                                    name = doc.getString("name") ?: "",
                                    email = doc.getString("email") ?: "",
                                    phone = doc.getString("phone") ?: "",
                                    userType = userType,
                                    status = doc.getString("status") ?: "active"
                                )
                            )
                        },
                        onFailure = { onFailure(it) }
                    )
                } else {
                    onFailure("User not found")
                }
            }
            .addOnFailureListener { onFailure(it.message ?: "Failed to get user") }
    }


    fun getUsersByType(
        usertypeId: String,
        onSuccess: (List<User>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        getUserType(usertypeId, onSuccess = { userType ->
            firestore.collection("User")
                .whereEqualTo("usertypeId", usertypeId)
                .get()
                .addOnSuccessListener { snapshot ->
                    val list = snapshot.documents.map { doc ->
                        User(
                            uid = doc.id,
                            name = doc.getString("name") ?: "",
                            email = doc.getString("email") ?: "",
                            phone = doc.getString("phone") ?: "",
                            userType = userType,
                            status = doc.getString("status") ?: "active"
                        )
                    }
                    onSuccess(list)
                }
                .addOnFailureListener { onFailure(it.message ?: "Failed to get users") }
        }, onFailure = { onFailure(it) })
    }


    fun updateUser(
        uid: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("User").document(uid)
            .update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to update user") }
    }

    // delete user
    fun deleteUser(uid: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("User").document(uid)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to delete user") }
    }
}
