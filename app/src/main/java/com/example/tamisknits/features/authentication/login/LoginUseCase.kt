package com.example.tamisknits.features.authentication.login

import com.example.tamisknits.models.User
import com.example.tamisknits.models.UserType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) {
    class Params(
        val email: String,
        val password: String,
    )

    suspend fun execute(params: Params): User {
        // 1. Authenticate with Firebase Auth
        val authResult = auth.signInWithEmailAndPassword(params.email, params.password).await()
        val uid = authResult.user?.uid
            ?: throw IllegalStateException("Login failed. Please try again.")

        // 2. Fetch the user's profile doc from Firestore
        val userSnapshot = firestore.collection("User").document(uid).get().await()

        val storedHash = userSnapshot.getString("passwordHash")
        val currentHash = hashPassword(params.password)

        if (storedHash != null) {
            // New User Check: Verify the hash matches
            if (storedHash != currentHash) {
                auth.signOut() // Force logout if hash mismatch
                throw IllegalStateException("Security verification failed.")
            }
        } else {
            // OLD USER MIGRATION:
            // They don't have a hash yet. Let's add it now that they've logged in successfully.
            firestore.collection("User").document(uid)
                .update("passwordHash", currentHash)
                .await()
        }

        val baseUser = userSnapshot.toObject(User::class.java)
            ?: throw IllegalStateException("No profile found for this account.")

        // 3. Resolve usertypeId → the actual UserType doc (role, permissions)
        val usertypeId = userSnapshot.getString("usertypeId").orEmpty()
        val userType = if (usertypeId.isNotBlank()) {
            firestore.collection("UserType").document(usertypeId).get().await()
                .toObject(UserType::class.java)
                ?: UserType()
        } else {
            UserType()
        }

        // 4. Combine into the final User with its resolved role attached
        return baseUser.copy(userType = userType)
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}