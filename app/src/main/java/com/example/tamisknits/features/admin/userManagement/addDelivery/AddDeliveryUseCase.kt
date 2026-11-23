package com.example.tamisknits.features.admin.userManagement.addDelivery

import android.content.Context
import com.example.tamisknits.models.UserTypeIds
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom
import javax.inject.Inject

// Creates a driver's Auth account without Cloud Functions. createUserWithEmailAndPassword signs the
// new user in on whichever FirebaseAuth runs it, so it runs on a secondary FirebaseApp; the admin
// stays signed in on the default app, whose Firestore (admin-authenticated) writes the User doc.
class AddDeliveryUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore
) {
    class Params(
        val name: String,
        val email: String,
        val phone: String
    )

    class Result(
        val uid: String,
        val email: String,
        val temporaryPassword: String
    )

    suspend fun execute(params: Params): Result {
        val email = params.email.trim()
        val password = generateTemporaryPassword()
        val secondaryAuth = FirebaseAuth.getInstance(secondaryApp())

        try {
            val authResult = try {
                secondaryAuth.createUserWithEmailAndPassword(email, password).await()
            } catch (e: FirebaseAuthUserCollisionException) {
                throw IllegalStateException("An account with this email already exists.")
            } catch (e: FirebaseAuthWeakPasswordException) {
                throw IllegalStateException("The generated password was rejected. Please try again.")
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                throw IllegalStateException("Please enter a valid email address.")
            }
            val newUser = authResult.user
                ?: throw IllegalStateException("Couldn't create the account. Please try again.")

            val data = hashMapOf(
                "uid" to newUser.uid,
                "name" to params.name.trim(),
                "email" to email,
                "phone" to params.phone.trim(),
                "usertypeId" to UserTypeIds.DELIVERY,
                "status" to "active",
                "createdAt" to FieldValue.serverTimestamp()
            )
            try {
                firestore.collection("User").document(newUser.uid).set(data).await()
            } catch (e: Exception) {
                // Without a User doc the account can't be used or managed; remove it so the
                // admin can retry with the same email. It's still signed in on secondaryAuth.
                runCatching { newUser.delete().await() }
                throw IllegalStateException(e.message ?: "Couldn't save the driver profile.")
            }

            return Result(uid = newUser.uid, email = email, temporaryPassword = password)
        } finally {
            secondaryAuth.signOut()
        }
    }

    private fun secondaryApp(): FirebaseApp =
        FirebaseApp.getApps(context).firstOrNull { it.name == SECONDARY_APP_NAME }
            ?: FirebaseApp.initializeApp(context, FirebaseApp.getInstance().options, SECONDARY_APP_NAME)

    // No look-alike characters (0/O, 1/l/I) so the admin can read it out to the driver.
    private fun generateTemporaryPassword(): String {
        val random = SecureRandom()
        return (1..PASSWORD_LENGTH)
            .map { PASSWORD_CHARS[random.nextInt(PASSWORD_CHARS.length)] }
            .joinToString("")
    }

    private companion object {
        const val SECONDARY_APP_NAME = "driverCreator"
        const val PASSWORD_LENGTH = 10
        const val PASSWORD_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789"
    }
}
