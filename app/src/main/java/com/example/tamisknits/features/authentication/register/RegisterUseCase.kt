package com.example.tamisknits.features.authentication.register


import com.example.tamisknits.models.User
import com.example.tamisknits.models.UserType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
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
        val userTypeName: String, // "client" or "delivery"
    )

    suspend fun execute(params: Params): User {
        // 1. Look up the UserType doc matching the picked role
        val userTypeQuery = firestore.collection("UserType")
            .whereEqualTo("type", params.userTypeName)
            .limit(1)
            .get()
            .await()

        val userTypeDoc = userTypeQuery.documents.firstOrNull()
            ?: throw IllegalStateException("Could not find role \"${params.userTypeName}\". Please try again.")

        val usertypeId = userTypeDoc.id

        // 2. Create the Firebase Auth account
        val authResult = auth.createUserWithEmailAndPassword(params.email, params.password).await()
        val uid = authResult.user?.uid
            ?: throw IllegalStateException("Registration failed. Please try again.")

        val hashedPassword = hashPassword(params.password)
        // 3. Create the matching Firestore profile doc, keyed by the same uid
        val userDoc = mapOf(
            "uid" to uid,
            "name" to params.name,
            "email" to params.email,
            "phone" to params.phone,
            "usertypeId" to usertypeId,
            "passwordHash" to hashedPassword
        )
        firestore.collection("User").document(uid).set(userDoc).await()

        // 4. Return the full User with resolved userType attached, matching LoginUseCase's shape
        val userType = userTypeDoc.toObject(UserType::class.java) ?: UserType()
        return User(
            uid = uid,
            name = params.name,
            email = params.email,
            phone = params.phone,
            userType = userType,
        )
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}