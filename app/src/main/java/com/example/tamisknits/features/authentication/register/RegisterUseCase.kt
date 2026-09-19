package com.example.tamisknits.features.authentication.register

import com.example.tamisknits.models.User
import com.example.tamisknits.models.UserType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) {
    class Params(
        val name: String,
        val email: String,
        val phone: String,
        val password: String,
    )

    companion object {
        private const val CLIENT_USER_TYPE_ID = "AiFmhFP1CUBDE56GD7ta"
    }

    suspend fun execute(params: Params): User {
        // 1. Create the Firebase Auth account
        val authResult = auth.createUserWithEmailAndPassword(
            params.email,
            params.password
        ).await()

        val uid = authResult.user?.uid
            ?: throw IllegalStateException("Registration failed. Please try again.")

        // 2. Create the matching Firestore profile doc, keyed by the same uid
        val userDoc = mapOf(
            "uid" to uid,
            "name" to params.name,
            "email" to params.email,
            "phone" to params.phone,
            "usertypeId" to CLIENT_USER_TYPE_ID,
            "status" to "active"
        )

        firestore.collection("User")
            .document(uid)
            .set(userDoc)
            .await()

        // 3. Get the client UserType
        val userTypeDoc = firestore.collection("UserType")
            .document(CLIENT_USER_TYPE_ID)
            .get()
            .await()

        val userType = userTypeDoc.toObject(UserType::class.java)
            ?: UserType(
                usertypeId = CLIENT_USER_TYPE_ID,
                type = "client"
            )

        // 4. Return the full User with resolved userType attached
        return User(
            uid = uid,
            name = params.name,
            email = params.email,
            phone = params.phone,
            userType = userType,
            status = "active"
        )
    }
}